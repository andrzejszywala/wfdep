package pl.andrzejszywala.wfdep;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

public class DeploymentEngine {
	private static final Logger logger = Logger.getLogger(DeploymentEngine.class.getName());
	private String host;
	private String path;
	private String user;
	private String password;
	private String file;

	public DeploymentEngine(String host, String path, String user, String password) {
		super();
		this.host = host;
		this.path = path;
		this.file = new File(this.path).getName();
		this.user = user;
		this.password = password;
	}
	
	public String deploy() throws IOException  {
		undeploy();
		remove();
		return enable(add());
	}
	
	private String enable(JsonValue hash)  {
		String result = null;
		logger.info("Starting");
		Response post = newClient()
				.target(host + "/management")
				.request()
			    .post(json(createObjectBuilder()
			    		.add("operation", "add")
			    		.add("enabled", "true")
			    		.add("content", createArrayBuilder()
			    							.add(createObjectBuilder()
			    									.add("hash", createObjectBuilder().add("BYTES_VALUE", hash))))
			    		.add("address", createArrayBuilder()
			    				            .add(createObjectBuilder().add("deployment", file))).build()));
		result = post.readEntity(String.class);
		logger.info(result);
		return result;
	}

	private  void undeploy() {
		logger.info("Undeploying");
		Response post = newClient()
			.target(host + "/management")
			.request()
		    .post(json(createObjectBuilder()
		    		.add("operation", "undeploy")
		    		.add("address", createArrayBuilder()
		    				.add(createObjectBuilder().add("deployment", file))).build()));
		logger.info(post.readEntity(String.class));
	}
	
	private  void remove() {
		logger.info("Removeing");
		Response post = newClient()
			.target(host + "/management")
			.request()
		    .post(json(createObjectBuilder()
		    		.add("operation", "remove")
		    		.add("address", createArrayBuilder()
		    				.add(createObjectBuilder().add("deployment", file))).build()));
		logger.info(post.readEntity(String.class));
	}
	
	private JsonValue add() throws IOException  {
		logger.info("Adding");
		final FileDataBodyPart filePart = new FileDataBodyPart("file", new File(path));
		 
		try (MultiPart multiPart = new FormDataMultiPart()) {
			multiPart.bodyPart(filePart);
			Response post = newClient()
				.target(host + "/management/add-content")
				.request(APPLICATION_JSON_TYPE)
			    .post(Entity.entity(multiPart, multiPart.getMediaType()));
			String entity = post.readEntity(String.class);
			logger.info(entity);
			JsonReader reader = Json.createReader(new StringReader(entity));
			JsonObject jo = (JsonObject) reader.read();
			logger.info("" + jo);
			return ((JsonObject)jo.get("result")).get("BYTES_VALUE");
		}
	}

	public Client newClient()  {		
		ClientConfig clientConfig = new ClientConfig();
		clientConfig.register(HttpAuthenticationFeature.digest(user, password.getBytes()));
		clientConfig.register(MultiPartFeature.class);
		return ClientBuilder.newBuilder().withConfig(clientConfig).build();
	}
}

