package org.sagebionetworks.authutil;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.UserDAO;

public class CrowdUserDAOTest {
	
	private static final String TEST_USER = "demouser@sagebase.org";

	private boolean isIntegrationTest() {
		String integrationTestEndpoint = System.getProperty("INTEGRATION_TEST_ENDPOINT");
		return (integrationTestEndpoint!=null && integrationTestEndpoint.length()>0);
	}

	private UserDAO userDAO;
	
	@Before
	public void setUp() throws Exception {
		if (!isIntegrationTest()) return;
		CrowdAuthUtil.acceptAllCertificates();
		userDAO = new CrowdUserDAO();
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testGetUser() throws Exception {
		if (!isIntegrationTest()) return;
		org.sagebionetworks.repo.model.User user = userDAO.getUser(TEST_USER);
		assertEquals(TEST_USER, user.getUserId());
		assertNotNull(user.getCreationDate());
	}
	
	@Test 
	public void testUpdate() throws Exception {
		if (!isIntegrationTest()) return;
		org.sagebionetworks.repo.model.User user = userDAO.getUser(TEST_USER);
		userDAO.update(user);
		user = userDAO.getUser(TEST_USER);
		Map<String, Collection<String>> attributes = new HashMap<String, Collection<String>>();
		attributes.put("foo", Arrays.asList(new String[] {"bar", "XYZ"}));
		CrowdAuthUtil.setUserAttributes(TEST_USER, attributes);
	}

	@Test 
	public void testGetUserGroupNames() throws Exception {
		if (!isIntegrationTest()) return;
		Collection<String> groups = userDAO.getUserGroupNames(TEST_USER);
		assertTrue(groups.contains("platform"));
	}
}
