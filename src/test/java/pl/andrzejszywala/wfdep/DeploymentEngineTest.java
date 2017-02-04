package pl.andrzejszywala.wfdep;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class DeploymentEngineTest {

	@Test
	public void testDeploy() throws IOException {
		String result = new DeploymentEngine("http://localhost:9990", "src/test/resources/hellojee.war", "admin", "admin").deploy();
		assertEquals("{\"outcome\" : \"success\"}", result);
	}
}
