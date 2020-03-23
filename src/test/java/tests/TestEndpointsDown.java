package tests;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.annotation.DirtiesContext;


import pt.cgi.CamelRoute;

public class TestEndpointsDown extends CamelSpringTestSupport {

	@Produce
	ProducerTemplate template;

	@EndpointInject(uri = "mock:noWork")
	private MockEndpoint noWork;


	
	@Override
	protected AbstractApplicationContext createApplicationContext() {
		return new ClassPathXmlApplicationContext("spring/camelContext-test.xml");
		// or get the ApplicationContext in Java
	}

	@Override
	public Properties useOverridePropertiesWithPropertiesComponent() {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("./src/main/resources/application-test.properties"));
		} catch (IOException e) {
			fail(e.getMessage());
		}

		return properties;
	}

	@Before
	public void setupRoute() throws Exception {
		context.addRoutes(new CamelRoute());

	}

	public void simulateAMQDown() throws Exception {

		context.getRouteDefinitions().get(0).adviceWith(context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				replaceFromWith("direct:testEntry");

			}
		});

		context.getRouteDefinitions().get(1).adviceWith(context, new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				interceptSendToEndpoint("direct:mock").skipSendToOriginalEndpoint().to("mock:noWork");

			}
		});

	}

	@After
	public void after() throws Exception {
		context.stop();
	}

	@DirtiesContext
	@Test
	public void evaluateAMQDown() throws Exception {
		simulateAMQDown();
		noWork.expectedMessageCount(1);
		template.sendBody("direct:testEntry", "funciona");
		assertMockEndpointsSatisfied();
	}

}
