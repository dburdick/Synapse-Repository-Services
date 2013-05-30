package org.sagebionetworks.evaluation.workers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.asynchronous.workers.sqs.MessageUtils;
import org.sagebionetworks.evaluation.dao.EvaluationDAO;
import org.sagebionetworks.evaluation.dao.ParticipantDAO;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.evaluation.model.Participant;
import org.sagebionetworks.repo.model.SchemaCache;
import org.sagebionetworks.repo.model.UserGroup;
import org.sagebionetworks.repo.model.UserGroupDAO;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserProfileDAO;
import org.sagebionetworks.repo.model.message.ChangeMessage;
import org.sagebionetworks.repo.model.message.ChangeType;
import org.sagebionetworks.repo.model.message.ObjectType;
import org.sagebionetworks.repo.web.NotFoundException;
import org.sagebionetworks.schema.ObjectSchema;

import com.amazonaws.services.sqs.model.Message;
import com.ecwid.mailchimp.MailChimpClient;
import com.ecwid.mailchimp.MailChimpException;
import com.ecwid.mailchimp.MailChimpObject;
import com.ecwid.mailchimp.method.list.ListBatchSubscribeMethod;
import com.ecwid.mailchimp.method.list.ListMembersMethod;
import com.ecwid.mailchimp.method.list.ListMembersResult;
import com.ecwid.mailchimp.method.list.MemberStatus;
import com.ecwid.mailchimp.method.list.ShortMemberInfo;

/**
 * The worker that processes messages for Evaluation asynchronous jobs.
 * 
 * @author dburdick
 *
 */
public class EvaluationQueueWorker implements Callable<List<Message>> {
	
	static private Log log = LogFactory.getLog(EvaluationQueueWorker.class);
	static private final String MAILCHIMP_APIKEY = StackConfiguration.getMailChimpAPIKey();
	static private enum CurrentChallenges { HPN, TOXICOGENETICS, WHOLECELL };
	
	
	List<Message> messages;
	EvaluationDAO evaluationDAO;
	ParticipantDAO participantDAO;
	UserGroupDAO userGroupDAO;
	UserProfileDAO userProfileDAO;
	MailChimpClient mailChimpClient;
	Map<CurrentChallenges, String> challengeToMailChimpId;
	
	public EvaluationQueueWorker(List<Message> messages,
			EvaluationDAO evaluationDAO, 
			ParticipantDAO participantDAO,
			UserGroupDAO userGroupDAO,
			UserProfileDAO userProfileDAO,
			MailChimpClient mailChimpClient) {
		if(messages == null) throw new IllegalArgumentException("Messages cannot be null");
		if(evaluationDAO == null) throw new IllegalArgumentException("evaluationDAO cannot be null");
		if(participantDAO == null) throw new IllegalArgumentException("participantDAO cannot be null");
		if(userGroupDAO == null) throw new IllegalArgumentException("userGroupDAO cannot be null");
		if(userProfileDAO == null) throw new IllegalArgumentException("userProfileDAO cannot be null");
		if(mailChimpClient == null) throw new IllegalArgumentException("mailChimpClient cannot be null");
		
		this.messages = messages;
		this.evaluationDAO = evaluationDAO;
		this.participantDAO = participantDAO;
		this.userGroupDAO = userGroupDAO;
		this.userProfileDAO = userProfileDAO;
		this.mailChimpClient = mailChimpClient;
		
		challengeToMailChimpId = new HashMap<EvaluationQueueWorker.CurrentChallenges, String>();
		challengeToMailChimpId.put(CurrentChallenges.HPN, "78979af628");
		challengeToMailChimpId.put(CurrentChallenges.TOXICOGENETICS, "ca2a921c6f");
		challengeToMailChimpId.put(CurrentChallenges.WHOLECELL, "5a2d90e13e");
	}

	@Override
	public List<Message> call() throws Exception {
		List<Message> processedMessages = new LinkedList<Message>();
		// process each message
		for(Message message: messages){
			// Extract the ChangeMessage
			ChangeMessage change = MessageUtils.extractMessageBody(message);
			// We only care about entity messages here
			if(ObjectType.EVALUATION == change.getObjectType()){
				try{				
					Evaluation evaluation = evaluationDAO.get(change.getObjectId());
					if(ChangeType.CREATE == change.getChangeType() || ChangeType.UPDATE == change.getChangeType()){
						// create an email list for this evaluation and add any existing members
						addUsersToEmailList(evaluation);									
					}else if(ChangeType.DELETE == change.getChangeType()){
						deleteEmailList(evaluation);
					}else{
						throw new IllegalArgumentException("Unknown change type: "+change.getChangeType());
					}
					// This message was processed.
					processedMessages.add(message);
				}catch(NotFoundException e){
					log.info("NotFound: "+e.getMessage()+". The message will be returend as processed and removed from the queue");
					// If an evaluation does not exist anymore then we want the message to be deleted from the queue
					processedMessages.add(message);
				}catch (Throwable e){
					// Something went wrong and we did not process the message.
					log.error("Failed to process message", e);
				}
			}else{
				// Non-entity messages must be returned so they can be removed from the queue.
				processedMessages.add(message);
			}
		}
		return processedMessages;
	}


	/*
	 * Private Methods
	 */

	/**
	 * Itempotent
	 * @param evaluation
	 * @throws NotFoundException 
	 * @throws MailChimpException 
	 * @throws IOException 
	 */
	private void addUsersToEmailList(Evaluation evaluation) throws NotFoundException, IOException, MailChimpException {		
		String listId = challengeToMailChimpId.get(identifyEvaluation(evaluation));
		if(listId == null) throw getNotFoundException(evaluation);
		
		Set<String> listEmails = getAllListEmails(listId);				
		
		// get all participants in the competition and batch update new ones into the MailChimp list
		long total = participantDAO.getCountByEvaluation(evaluation.getId());
		int offset = 0;
		int limit = 100;
		while(offset < total) {
			List<Participant> batch = participantDAO.getAllByEvaluation(evaluation.getId(), limit, offset);
			ListBatchSubscribeMethod subscribeRequest = new ListBatchSubscribeMethod();
			List<MailChimpObject> mcBatch = new ArrayList<MailChimpObject>();
			for(Participant participant : batch) {
				// get user's email and if not in email list already, add
				UserGroup userGroup = userGroupDAO.get(participant.getUserId());
				String participantEmail = userGroup.getName();
				if(participantEmail != null && !listEmails.contains(participantEmail)) {
					ObjectSchema schema = SchemaCache.getSchema(UserProfile.class);
					UserProfile userProfile = userProfileDAO.get(participant.getUserId(), schema);
					MailChimpObject obj = new MailChimpObject();
					obj.put("EMAIL", participantEmail);					
					obj.put("EMAIL_TYPE", "html");
					obj.put("FNAME", userProfile.getFirstName());
					obj.put("LNAME", userProfile.getLastName());
					mcBatch.add(obj);
				}
			}
			subscribeRequest.apikey = MAILCHIMP_APIKEY;
			subscribeRequest.id = listId;
			subscribeRequest.double_optin = false;
			subscribeRequest.update_existing = true;
			subscribeRequest.batch = mcBatch;
			
			try {
				mailChimpClient.execute(subscribeRequest);
			} catch (IOException e) {
				log.error("Error updating MailChimp list for evaluation: " + evaluation.getId(), e);
			} catch (MailChimpException e) {
				log.error("Error updating MailChimp list for evaluation: " + evaluation.getId(), e);
			}
			
			offset += limit;
		}
	}

	private Set<String> getAllListEmails(String listId) throws IOException, MailChimpException {
		Set<String> emails = new HashSet<String>();
				
		// get all subscribed & unsubscribed members of the list
		for(MemberStatus status : new MemberStatus[]{ MemberStatus.subscribed, MemberStatus.unsubscribed}) {
			int offset = 0;
			int limit = 100;
			boolean done = false;
			while(!done) {
				ListMembersMethod request = new ListMembersMethod();
				request.apikey = MAILCHIMP_APIKEY;
				request.id = listId;
				request.status = status;
				request.start = offset;
				request.limit = limit;
				
				ListMembersResult result = mailChimpClient.execute(request);
				for(ShortMemberInfo member : result.data) {
					if(member.email != null && !member.email.isEmpty())
						emails.add(member.email);
				}
				
				offset += limit;			
				if(result.total <= offset) done = true;
			}
		}
		return emails;
	}
	
	private CurrentChallenges identifyEvaluation(Evaluation evaluation) throws NotFoundException {
		if(evaluation.getName().contains("HPN")) {
			return CurrentChallenges.HPN;
		} else if(evaluation.getName().contains("Toxicogenetics")) {
			return CurrentChallenges.TOXICOGENETICS;
		} else if(evaluation.getName().contains("Whole-Cell")) {
			return CurrentChallenges.WHOLECELL;
		}
		throw getNotFoundException(evaluation);
	}

	private void deleteEmailList(Evaluation evaluation) {
		// do nothing for now
	}
	
	private NotFoundException getNotFoundException(Evaluation evaluation) {
		return new NotFoundException("Unknown mailing list for evaluation:" + evaluation.getId() + ", " + evaluation.getName());
	}

}
