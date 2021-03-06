package org.sagebionetworks.repo.manager;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sagebionetworks.repo.model.AuthorizationConstants;
import org.sagebionetworks.repo.model.AuthorizationConstants.DEFAULT_GROUPS;
import org.sagebionetworks.repo.model.jdo.KeyFactory;
import org.sagebionetworks.repo.model.query.BasicQuery;
import org.sagebionetworks.repo.model.query.Comparator;
import org.sagebionetworks.repo.model.query.CompoundId;
import org.sagebionetworks.repo.model.query.Expression;
import org.sagebionetworks.repo.model.DatastoreException;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.NodeConstants;
import org.sagebionetworks.repo.model.NodeDAO;
import org.sagebionetworks.repo.model.NodeQueryDao;
import org.sagebionetworks.repo.model.NodeQueryResults;
import org.sagebionetworks.repo.model.UserGroup;
import org.sagebionetworks.repo.model.UserGroupDAO;
import org.sagebionetworks.repo.model.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:manager-test-context.xml" })
public class UserManagerImplTest {
	
	@Autowired
	UserManager userManager;
	
	@Autowired
	UserGroupDAO userGroupDAO;
	
	@Autowired
	NodeDAO nodeDao;
	
	@Autowired
	NodeQueryDao nodeQueryDao;
	
	private static final String TEST_USER = "test-user";
	
	private List<String> groupsToDelete = null;
	
	
	@Before
	public void setUp() throws Exception {
		groupsToDelete = new ArrayList<String>();
		userManager.setUserDAO(new TestUserDAO());
		userManager.deletePrincipal(TEST_USER);
	}

	@After
	public void tearDown() throws Exception {
		if(groupsToDelete != null && userGroupDAO != null){
			for(String groupId: groupsToDelete){
				UserGroup ug = userGroupDAO.get(groupId);
				userManager.deletePrincipal(ug.getName());
			}
		}
	}
	
	@Test
	public void testPLFM1399() throws Exception {
		
		// verify that our test user belongs to the 'public' group
		UserInfo userInfo = userManager.getUserInfo(TEST_USER);
		UserGroup publicPrincipal = null;
		for (UserGroup g : userInfo.getGroups()) {
			if (g.getName().equals(DEFAULT_GROUPS.PUBLIC.name())) publicPrincipal = g;
		}
		assertNotNull(publicPrincipal);
		String origPrincipalId = publicPrincipal.getId();
		
		// now we duplicate the action of the migrator:  delete the group and recreate (with another ID)
		
		// we need to delete all objects (except root), else foreign key constraints fail upon deleting the group
		String rootId = nodeDao.getNodeIdForPath(NodeConstants.ROOT_FOLDER_PATH);
		EntityType[] entityTypes = new EntityType[]{EntityType.folder, EntityType.project};
		UserInfo adminInfo = userManager.getUserInfo(TestUserDAO.ADMIN_USER_NAME);
		for (EntityType entityType : entityTypes) {
			BasicQuery queryForNode = new BasicQuery();
			queryForNode.setFrom(entityType.name());
			queryForNode.addExpression(new Expression(new CompoundId("JDONODE", "parentId"), 
				org.sagebionetworks.repo.model.query.Comparator.EQUALS, rootId));
			NodeQueryResults queryResults = nodeQueryDao.executeQuery(queryForNode, adminInfo);
			List<String> resultIds = queryResults.getResultIds();
			for (String resultId : resultIds) {
				System.out.println(resultId);
				nodeDao.delete(resultId);
			}
		}
		
		// *** NOTE:  To make this work we delete via the UserManager (which clears the user info cache) rather than the userGroupDAO
		assertTrue(userManager.deletePrincipal(publicPrincipal.getName()));
		publicPrincipal = new UserGroup();
		publicPrincipal.setIsIndividual(false);
		publicPrincipal.setName(DEFAULT_GROUPS.PUBLIC.name());
		String newId = userGroupDAO.create(publicPrincipal);
		assertFalse(newId.equals(origPrincipalId));
		
		// now get the user info again
		userInfo = userManager.getUserInfo(TEST_USER);
		publicPrincipal = null;
		for (UserGroup g : userInfo.getGroups()) {
			if (g.getName().equals(DEFAULT_GROUPS.PUBLIC.name())) publicPrincipal = g;
		}
		assertNotNull(publicPrincipal);
		
		// if the info is cached, then the ID of the retrieved group will be the old one (different from the new one)
		// and the following will fail
		assertEquals(newId, publicPrincipal.getId());
	}
	
	@Test
	public void testFilterInvalidGroupNames(){
		ArrayList<String> list = new ArrayList<String>();
		list.add("badGroupName@someplace.com");
		list.add("GOOD_GROUP_NAME");
		Collection<String> results = UserManagerImpl.filterInvalidGroupNames(list);
		assertNotNull(results);
		assertEquals("The group name that is an email addresss should have been filter out", 1,results.size());
		assertEquals("GOOD_GROUP_NAME",results.iterator().next());
	}
	
	@Test
	public void testGetDefaultGroup() throws DatastoreException{
		// We should be able to get all default groups
		DEFAULT_GROUPS[] array = DEFAULT_GROUPS.values();
		for(DEFAULT_GROUPS group: array){
			UserGroup userGroup = userManager.getDefaultUserGroup(group);
			assertNotNull(userGroup);
		}
	}
	
	// invoke getUserInfo for Anonymous and check returned userInfo
	@Test
	public void testGetAnonymous() throws Exception {
		UserInfo ui = userManager.getUserInfo(AuthorizationConstants.ANONYMOUS_USER_ID);
		assertEquals(AuthorizationConstants.ANONYMOUS_USER_ID, ui.getUser().getUserId());
		assertNotNull(ui.getUser().getId());
		assertEquals(AuthorizationConstants.ANONYMOUS_USER_ID, ui.getIndividualGroup().getName());
		assertEquals(2, ui.getGroups().size());
		assertTrue(ui.getGroups().contains(ui.getIndividualGroup()));
		//assertEquals(ui.getIndividualGroup(), ui.getGroups().iterator().next());
		// They belong to the public group but not the authenticated user's group
		assertTrue(ui.getGroups().contains(userGroupDAO.findGroup(AuthorizationConstants.DEFAULT_GROUPS.PUBLIC.name(), false)));
		// Anonymous does not belong to the authenticated user's group.
		assertFalse(ui.getGroups().contains(userGroupDAO.findGroup(AuthorizationConstants.DEFAULT_GROUPS.AUTHENTICATED_USERS.name(), false)));
	}
	
	@Test
	public void testStandardUser() throws Exception {
		System.out.println("Groups in the system: "+userGroupDAO.getAll());
		// call for a user in the User system but not the Permissions system.  
		assertNull(userGroupDAO.findGroup(TEST_USER, true));
		// This will create the user if they do not exist
		UserInfo ui = userManager.getUserInfo(TEST_USER);
		assertNotNull(ui.getIndividualGroup());
		assertNotNull(ui.getIndividualGroup().getId());
		groupsToDelete.add(ui.getIndividualGroup().getId());
		// also delete the group
		UserGroup testGroup = userGroupDAO.findGroup(TestUserDAO.TEST_GROUP_NAME, false);
		assertNotNull(testGroup);
		groupsToDelete.add(testGroup.getId());
		//		check the returned userInfo
		//		verify the user gets created
		assertNotNull(userGroupDAO.findGroup(TEST_USER, true));
		//		should include Public and authenticated users' group.
		assertTrue(ui.getGroups().contains(userGroupDAO.findGroup(AuthorizationConstants.DEFAULT_GROUPS.PUBLIC.name(), false)));
		assertTrue(ui.getGroups().contains(userGroupDAO.findGroup(AuthorizationConstants.DEFAULT_GROUPS.AUTHENTICATED_USERS.name(), false)));
		// call for a user in a Group not in the Permissions system
		//		verify the group is created in the Permissions system
		assertTrue("Missing "+testGroup+"  Has "+ui.getGroups(), ui.getGroups().contains(testGroup));
	}
		
	@Test
	public void testGetAnonymousUserInfo() throws Exception {
		userManager.getUserInfo(AuthorizationConstants.ANONYMOUS_USER_ID);
	}

	@Test
	public void testIdempotency() throws Exception {
		userManager.getUserInfo(AuthorizationConstants.ANONYMOUS_USER_ID);
		userManager.getUserInfo(AuthorizationConstants.ANONYMOUS_USER_ID);
	}
	

}
