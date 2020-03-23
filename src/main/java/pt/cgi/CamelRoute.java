package pt.cgi;

import org.apache.camel.builder.RouteBuilder;

public class CamelRoute extends RouteBuilder {
	
	@Override
	public void configure() throws Exception {

		errorHandler(defaultErrorHandler().logExhaustedMessageHistory(false));
		
		onException(javax.jms.JMSException.class)
		.handled(true)
		.logStackTrace(false)
		.log("E0101- The azure Service Bus is Unreachable")
		.to("direct:errorHandler");

		onException(org.apache.activemq.artemis.api.core.ActiveMQNotConnectedException.class)
		.logStackTrace(false)
		.handled(true)
		.setBody(constant("E0102- Unable to Acess Red Hat AMQ"))
		.to("direct:errorHandler");

		from("amqp:{{AZURE_MULTICAST_ADDRESS}}").id("azureAdress").log("${body}")
		.to("jms:topic:{{AMQ_ADDRESS_NAME}}?errorHandlerLoggingLevel=OFF").id("amqURL");
		 
		from("direct:errorHandler").log("${body}").to("direct:mock").markRollbackOnly();
		
	}
	
}





