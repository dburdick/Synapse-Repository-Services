package org.sagebionetworks.repo.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.ACTAccessApproval;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessApproval;
import org.sagebionetworks.repo.model.AccessApprovalDAO;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirementDAO;
import org.sagebionetworks.repo.model.ConflictingUpdateException;
import org.sagebionetworks.repo.model.DatastoreException;
import org.sagebionetworks.repo.model.InvalidModelException;
import org.sagebionetworks.repo.model.QueryResults;
import org.sagebionetworks.repo.model.TermsOfUseAccessApproval;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.UnauthorizedException;
import org.sagebionetworks.repo.model.UserGroup;
import org.sagebionetworks.repo.model.UserInfo;
import org.sagebionetworks.repo.web.ForbiddenException;
import org.sagebionetworks.repo.web.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

public class AccessApprovalManagerImpl implements AccessApprovalManager {
	
	@Autowired
	private AccessRequirementDAO accessRequirementDAO;
	
	@Autowired
	private AuthorizationManager authorizationManager;
	
	@Autowired
	private AccessApprovalDAO accessApprovalDAO;
	
	// this is the type of access the user needs to have on the owner Entity in order to be
	// able to administer (create, update, delete) the AccessApproval of the Entity
	public static final ACCESS_TYPE ADMINISTER_ACCESS_APPROVAL_ACCESS_TYPE = ACCESS_TYPE.CHANGE_PERMISSIONS;
	
	// check an incoming object (i.e. during 'create' and 'update')
	private void validateAccessApproval(UserInfo userInfo, AccessApproval a) throws 
	InvalidModelException, UnauthorizedException, DatastoreException, NotFoundException {
		if (a.getAccessorId()==null ||
				a.getEntityType()==null || 
				a.getRequirementId()==null ) throw new InvalidModelException();
		
		if (!a.getEntityType().equals(a.getClass().getName())) throw new InvalidModelException("entity type differs from class");
		
		if (a instanceof TermsOfUseAccessApproval) {
			if (!userInfo.isAdmin() && !userInfo.getIndividualGroup().getId().equals(a.getAccessorId()))
				throw new UnauthorizedException("A user may not sign Terms of Use on another's behalf");
		}
		
		// make sure the approval matches the requirement
		AccessRequirement ar = accessRequirementDAO.get(a.getRequirementId().toString());
		if (((ar instanceof TermsOfUseAccessRequirement) && !(a instanceof TermsOfUseAccessApproval))
				|| ((ar instanceof ACTAccessRequirement) && !(a instanceof ACTAccessApproval))) {
			throw new InvalidModelException("Cannot apply an approval of type "+a.getEntityType()+" to an access requirement of type "+ar.getEntityType());
		}
		
	}

	public static void populateCreationFields(UserInfo userInfo, AccessApproval a) {
		Date now = new Date();
		a.setCreatedBy(userInfo.getIndividualGroup().getId());
		a.setCreatedOn(now);
		a.setModifiedBy(userInfo.getIndividualGroup().getId());
		a.setModifiedOn(now);
	}

	public static void populateModifiedFields(UserInfo userInfo, AccessApproval a) {
		Date now = new Date();
		a.setCreatedBy(null); // by setting to null we are telling the DAO to use the current values
		a.setCreatedOn(null);
		a.setModifiedBy(userInfo.getIndividualGroup().getId());
		a.setModifiedOn(now);
	}

	private List<String> getEntityIds(AccessApproval accessApproval) throws DatastoreException, NotFoundException {
		String requirementId = accessApproval.getRequirementId().toString();
		AccessRequirement accessRequirement = accessRequirementDAO.get(requirementId);
		return accessRequirement.getEntityIds();		
	}
	
	private void verifyAccess(UserInfo userInfo, AccessApproval accessApproval, ACCESS_TYPE accessType) throws DatastoreException, NotFoundException, ForbiddenException {
		List<String> entityIds = getEntityIds(accessApproval);
		List<String> lackAccess = new ArrayList<String>();
		for (String entityId : entityIds) {
			if (!authorizationManager.canAccess(userInfo, entityId, accessType)) lackAccess.add(entityId);
		}
		if (!lackAccess.isEmpty()) {
			throw new ForbiddenException("Based on your permission levels on these entities: "+lackAccess+
					", you are forbidden from accessing the requested resource.");
		}
	}

	@Override
	public <T extends AccessApproval> T createAccessApproval(UserInfo userInfo, T accessApproval) throws DatastoreException,
			InvalidModelException, UnauthorizedException, NotFoundException,ForbiddenException {
		validateAccessApproval(userInfo, accessApproval);

		if ((accessApproval instanceof ACTAccessApproval)) {
			verifyAccess(userInfo, accessApproval, ADMINISTER_ACCESS_APPROVAL_ACCESS_TYPE);
		}
		
		if ((accessApproval instanceof TermsOfUseAccessApproval)) {
			verifyAccess(userInfo, accessApproval, ACCESS_TYPE.READ);
		}
		
		populateCreationFields(userInfo, accessApproval);
		return accessApprovalDAO.create(accessApproval);
	}

	@Override
	public QueryResults<AccessApproval> getAccessApprovalsForEntity(
			UserInfo userInfo, String entityId) throws DatastoreException,
			NotFoundException, ForbiddenException {
		if (!authorizationManager.canAccess(userInfo, entityId, ACCESS_TYPE.READ)) {
			throw new ForbiddenException("You are not allowed to access the requested resource.");
		}
		List<AccessRequirement> ars = accessRequirementDAO.getForNode(entityId);
		List<String> arIds = new ArrayList<String>();
		for (AccessRequirement ar : ars) arIds.add(ar.getId().toString());
		List<String> principalIds = new ArrayList<String>();
		principalIds.add(userInfo.getIndividualGroup().getId());
		for (UserGroup ug : userInfo.getGroups()) principalIds.add(ug.getId());
		List<AccessApproval> aas = accessApprovalDAO.getForAccessRequirementsAndPrincipals(arIds, principalIds);
		QueryResults<AccessApproval> result = new QueryResults<AccessApproval>(aas, aas.size());
		return result;
	}

	@Override
	public <T extends AccessApproval> T  updateAccessApproval(UserInfo userInfo, T accessApproval) throws NotFoundException,
			DatastoreException, UnauthorizedException,
			ConflictingUpdateException, InvalidModelException, ForbiddenException {
		validateAccessApproval(userInfo, accessApproval);
		verifyAccess(userInfo, accessApproval, ADMINISTER_ACCESS_APPROVAL_ACCESS_TYPE);
		populateModifiedFields(userInfo, accessApproval);
		return accessApprovalDAO.update(accessApproval);
	}

	@Override
	public void deleteAccessApproval(UserInfo userInfo, String accessApprovalId)
			throws NotFoundException, DatastoreException, UnauthorizedException, ForbiddenException {
		AccessApproval accessApproval = accessApprovalDAO.get(accessApprovalId);
		verifyAccess(userInfo, accessApproval, ADMINISTER_ACCESS_APPROVAL_ACCESS_TYPE);
		accessApprovalDAO.delete(accessApproval.getId().toString());
	}

}
