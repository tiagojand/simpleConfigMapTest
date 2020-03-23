package tests;


import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.Consume;
import org.apache.camel.Endpoint;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.PropertyInject;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringRunner;
import org.apache.camel.test.spring.CamelSpringTestSupport;
import org.apache.camel.test.spring.CamelTestContextBootstrapper;
import org.jgroups.annotations.Property;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import pt.cgi.CamelRoute;

//@RunWith(SpringJUnit4ClassRunner.class)
//@BootstrapWith(SpringBootTestContextBootstrapper.class)
//@ContextConfiguration("classpath:spring/camel-context.xml")
public class TestPublishedMessage extends CamelSpringTestSupport{

	
	@Produce
	ProducerTemplate template;
	
	  @EndpointInject(uri = "mock:noWork")
	  private MockEndpoint noWork;
	
	@Override
	protected AbstractApplicationContext createApplicationContext() {
	return new ClassPathXmlApplicationContext("spring/camel-context.xml");
	// or get the ApplicationContext in Java
	}
	
	@Override
	public Properties useOverridePropertiesWithPropertiesComponent() {
		 Properties properties = new Properties();
		  try
		  {
		    properties.load(new FileInputStream(
		      "./src/main/resources/application.properties"));
		  }
		  catch (IOException e)
		  {
		    fail(e.getMessage());
		  }
	
		return properties;
	}
	
	
	@Before
	public void setupRoute() throws Exception {
		  context.addRoutes(new CamelRoute());
			
	}

	public void simulateEntryEvaluateExit() throws Exception {
	
		  context.getRouteDefinitions().get(0)
        .adviceWith(context, new AdviceWithRouteBuilder() {

            @Override
            public void configure() throws Exception {
          	  replaceFromWith("direct:testEntry");
          	  weaveById("amqURL").replace().to("mock:amq");
               //this.weaveById("azureAdress").replace().setBody(constant("a"));
          	interceptSendToEndpoint("mock:amq").skipSendToOriginalEndpoint().setBody(constant("ok")).to("mock:noWork");
            }
        });
	} 
	
	
	
	 @After
	  public void after() throws Exception {
	    context.stop();
	  }
	
	@DirtiesContext
	@Test
	public void evaluatePublishMessage() throws Exception {
		simulateEntryEvaluateExit();
		noWork.expectedMessageCount(1);
		template.sendBody("direct:testEntry","funciona");
		assertMockEndpointsSatisfied();
	}


}
