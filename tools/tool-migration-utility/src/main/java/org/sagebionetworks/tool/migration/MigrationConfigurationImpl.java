package org.sagebionetworks.tool.migration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides configuration information 
 * 
 * TODO we can probably wrap this around an instance of TemplatedConfigurationImpl for richer config loading options and support for encrypted passwords
 * 
 * @author John
 *
 */
public class MigrationConfigurationImpl implements Configuration {
	
	static private Log log = LogFactory.getLog(MigrationConfigurationImpl.class);
	private final String CONFIGUATION_TEMPLATE_PROPERTIES = "configuation-template.properties";
	
	/**
	 * The the passed configuration file and add the properties to the system properties.
	 * @param path
	 * @throws IOException
	 */
	public void loadConfigurationFile(String path) throws IOException{
		log.debug("Using configuation file: "+path);
		File file = new File(path);
		if(!file.exists()){
			throw new IllegalArgumentException("The configuartion file: "+path+" does not exist");
		}
		FileInputStream fis = null;
		try{
			fis = new FileInputStream(file);
			loadConfiguration(fis);
		}finally{
			fis.close();
		}
	}

	public void loadConfiguration(InputStream inputStream) throws IOException{
		Properties props = new Properties();
		props.load(inputStream);
		// Add all of these properties to the system properties.
		System.getProperties().putAll(props);
		validateConfigurationProperties();
	}
	
	/**
	 * Validate the properties against the command line.
	 * @throws IOException
	 */
	public void validateConfigurationProperties() throws IOException{
		// Load the template from the classpath
		InputStream in = RepositoryMigrationDriver.class.getClassLoader().getResourceAsStream(CONFIGUATION_TEMPLATE_PROPERTIES);
		if(in == null) throw new IllegalArgumentException("Cannot find: "+CONFIGUATION_TEMPLATE_PROPERTIES+" on the classpath");
		Properties template = new Properties();
		try{
			template.load(in);
		}finally{
			in.close();
		}
		// Compare the passed properties to the template
		Iterator<String> keyIt = template.stringPropertyNames().iterator();
		Properties props = System.getProperties();
		log.debug("Loading the following properties: ");
		while(keyIt.hasNext()){
			String key = keyIt.next();
			Object value = props.getProperty(key);
			if(value == null){
				throw new IllegalArgumentException("Cannot find property: "+props.getProperty(key));
			}
			if(key.indexOf("password")> 1){
				// Do not print passwords
				log.debug(key+"="+createObfuscatedPassword((String) value));
			}else{
				log.debug(key+"="+value);
			}
		}
	}
	
	/**
	 * Used to print Obfuscated Password
	 * @param password
	 * @return
	 */
	private String createObfuscatedPassword(String password){
		StringBuilder builder = new StringBuilder();
		for(int i=0; i<password.length(); i++){
			builder.append("*");
		}
		return builder.toString();
	}
	
	@Override
	public SynapseConnectionInfo getSourceConnectionInfo(){
		return new SynapseConnectionInfo(
					System.getProperty("org.sagebionetworks.source.authentication.endpoint"),
					System.getProperty("org.sagebionetworks.source.repository.endpoint"),
					System.getProperty("org.sagebionetworks.source.admin.username"),
					System.getProperty("org.sagebionetworks.source.admin.password"),
					System.getProperty("org.sagebionetworks.stack.iam.id"),
					System.getProperty("org.sagebionetworks.stack.iam.key"),
					System.getProperty("org.sagebionetworks.shared.s3.backup.bucket"),
					System.getProperty("org.sagebionetworks.crowd.endpoint"),
					System.getProperty("org.sagebionetworks.crowdApplicationKey")

				);
	}
	
	@Override
	public SynapseConnectionInfo getDestinationConnectionInfo(){
		return new SynapseConnectionInfo(
					System.getProperty("org.sagebionetworks.destination.authentication.endpoint"),
					System.getProperty("org.sagebionetworks.destination.repository.endpoint"),
					System.getProperty("org.sagebionetworks.destination.admin.username"),
					System.getProperty("org.sagebionetworks.destination.admin.password"),
					System.getProperty("org.sagebionetworks.stack.iam.id"),
					System.getProperty("org.sagebionetworks.stack.iam.key"),
					System.getProperty("org.sagebionetworks.shared.s3.backup.bucket"),
					System.getProperty("org.sagebionetworks.crowd.endpoint"),
					System.getProperty("org.sagebionetworks.crowdApplicationKey")
				);
	}
	
	@Override
	public int getMaximumNumberThreads() {
		return Integer.parseInt(System.getProperty("org.sagebionetworks.max.threads"));
	}
	
	@Override
	public int getMaximumBatchSize(){
		return Integer.parseInt(System.getProperty("org.sagebionetworks.batch.size"));
	}
	
	@Override
	public long getWorkerTimeoutMs(){
		return Long.parseLong(System.getProperty("org.sagebionetworks.worker.thread.timout.ms"));
	}

	
}
