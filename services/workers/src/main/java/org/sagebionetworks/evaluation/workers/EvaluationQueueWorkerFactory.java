package org.sagebionetworks.evaluation.workers;

import java.util.List;
import java.util.concurrent.Callable;

import org.sagebionetworks.asynchronous.workers.sqs.MessageWorkerFactory;
import org.sagebionetworks.evaluation.dao.EvaluationDAO;
import org.sagebionetworks.evaluation.dao.ParticipantDAO;
import org.sagebionetworks.repo.model.UserGroupDAO;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserProfileDAO;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.services.sqs.model.Message;
import com.ecwid.mailchimp.MailChimpClient;

/**
 * The Evaluation message workers.
 * 
 * @author dburdick
 *
 */
public class EvaluationQueueWorkerFactory implements MessageWorkerFactory{
	
	@Autowired
	EvaluationDAO evaluationDAO;
	@Autowired
	ParticipantDAO participantDAO;
	@Autowired
	UserGroupDAO userGroupDAO;
	@Autowired
	UserProfileDAO userProfileDAO;
	@Autowired
	MailChimpClient mailChimpClient;
	
	
	@Override
	public Callable<List<Message>> createWorker(List<Message> messages) {		
		return new EvaluationQueueWorker(messages, evaluationDAO, participantDAO, userGroupDAO, userProfileDAO, mailChimpClient);
	}

}
