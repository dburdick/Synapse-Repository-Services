package org.sagebionetworks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sagebionetworks.client.Synapse;
import org.sagebionetworks.client.exceptions.SynapseServiceException;
import org.sagebionetworks.client.exceptions.SynapseUserException;
import org.sagebionetworks.repo.model.Preview;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;

/**
 * Validate that we can load data as expected from the repository services.
 */
public class IT101LoadMigratedData {
	
	private static Synapse synapse = null;

	/**
	 * @throws Exception
	 * 
	 */
	@BeforeClass
	public static void beforeClass() throws Exception {

		synapse = new Synapse();
		synapse.setAuthEndpoint(StackConfiguration
				.getAuthenticationServicePrivateEndpoint());
		synapse.setRepositoryEndpoint(StackConfiguration
				.getRepositoryServiceEndpoint());
		synapse.login(StackConfiguration.getIntegrationTestUserOneName(),
				StackConfiguration.getIntegrationTestUserOnePassword());
		
	}
	
	/**
	 * Read an input stream into a string.
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	private static String readToString(InputStream in) throws IOException {
		try {
			BufferedInputStream bufferd = new BufferedInputStream(in);
			byte[] buffer = new byte[1024];
			StringBuilder builder = new StringBuilder();
			int index = -1;
			while ((index = bufferd.read(buffer, 0, buffer.length)) > 0) {
				builder.append(new String(buffer, 0, index, "UTF-8"));
			}
			return builder.toString();
		} finally {
			in.close();
		}
	}
	
	/**
	 * This is a test for PLFM-775.
	 * @throws JSONObjectAdapterException 
	 * @throws SynapseServiceException 
	 * @throws SynapseUserException 
	 * @throws JSONException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 * 
	 */
	@Test
	public void testLoadPreview() throws Exception {
		// Load preview 149
		Preview preview = synapse.getEntity("113327", Preview.class);
		assertNotNull(preview);
		assertNotNull(preview.getPreviewString());
		assertNotNull(preview.getHeaders());
		assertEquals(50, preview.getHeaders().size());
		assertNotNull(preview.getRows());
		assertEquals(5, preview.getRows().size());
	}

}
