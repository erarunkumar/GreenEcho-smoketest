package com.travelport.gf.greentest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.jms.DeliveryMode;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

// import com.ibm.mq.jms.JMSC;
import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import com.travelport.common.technical.logging.enterpriseloggingrecord_v1.EnterpriseLoggingRecord;
import com.travelport.common.technical.logging.enterpriseloggingrecord_v1.LogMessage;
import com.travelport.common.technical.logging.enterpriseloggingrecord_v1.ServiceInfo;
import com.travelport.gf.util.ConfigBuilder;
import com.travelport.soa.lib.common.technical.logging.client.EnterpriseLoggingLogManager;
import com.travelport.soa.lib.common.technical.logging.client.EnterpriseLoggingLogger;
import com.travelport.soa.lib.common.technical.logging.client.EnterpriseLoggingLogger.Format;

//import org.apache.log4j.Logger;
//import org.apache.log4j.BasicConfigurator;

@Path("/echo")
public class GreenEchoService {
	
	private static final EnterpriseLoggingLogger logger = EnterpriseLoggingLogManager.getEnterpriseLoggingLogger(GreenEchoService.class);
	private static final String MQ_SVC_NAME = "greenfield-mq" ;
	
	@GET
	@Path("/hello/{param : (.+)?}")
	public Response getMsg(@PathParam("param") String msg) {
		String output = "Green Echo Service welcomes you : " + msg ;
		System.out.println("Logging via System.Out " + output);
		return Response.status(200).entity(output).build();
	}
	
	@GET
	@Path("/ExternalCallDirect/{param : (.+)?}")
	public Response callInternetNoProxy(@PathParam("param") String urlIn) throws Exception {
		String rspStr = "";
		URL url = new URL(urlIn);
		rspStr = urlToString(url, null);
		return Response.status(200).entity(rspStr).build();
	}
	
	@GET
	@Path("/ExternalCallSTPProxy/{param : (.+)?}")
	public Response callInternetProxy(@PathParam("param") String urlIn) throws Exception {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("dv-outbound-proxy.tvlport.com", 30375));
		String rspStr = "";
		URL url = new URL(urlIn);
		rspStr = urlToString(url, proxy);
		return Response.status(200).entity(rspStr).build();
	}
	
	@GET
	@Path("/InternalCall/{param : (.+)?}")
	public Response callInternalCallNoUPS(@PathParam("param") String urlIn) throws Exception {
		String rspStr = "";
		URL url = new URL(urlIn);
		rspStr = urlToString(url, null);
		return Response.status(200).entity(rspStr).build();
	}
	
	@GET
	@Path("/mqsend/{param : (.+)?}")
	public Response MQMessageService(@PathParam("param") String msg) throws Exception {
		String vcap_services = System.getenv("VCAP_SERVICES") ;
		MQQueueConnectionFactory cf = new MQQueueConnectionFactory();
		String HOSTNAME = "host";
		String CHANNEL = "channel";
		String PORT = "port";
		String QUEUE_MANAGER = "queuemanager";
		String QUEUE = "queue";
		StringBuffer sb = new StringBuffer();
		long timetolive = 120000;
		
		Map<String, String> wmqConnector = ConfigBuilder.getVcapMap(vcap_services, MQ_SVC_NAME) ;
		try {
			cf.setHostName(wmqConnector.get(HOSTNAME));
			cf.setPort(Integer.parseInt(wmqConnector.get(PORT)));
			cf.setChannel(wmqConnector.get(CHANNEL));
			cf.setQueueManager(wmqConnector.get(QUEUE_MANAGER));
			cf.setTransportType(WMQConstants.WMQ_CM_CLIENT);
            
			QueueConnection connection = cf.createQueueConnection();
			connection.start();
			QueueSession queueSession = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			TextMessage textMessage = queueSession.createTextMessage(msg);
            textMessage.setJMSType("mcd://xmlns");
            textMessage.setJMSExpiration(timetolive);
            textMessage.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
            QueueSender queueSender = queueSession.createSender(queueSession.createQueue(wmqConnector.get(QUEUE)));
            queueSender.setTimeToLive(timetolive);
            queueSender.send(textMessage);
            sb.append("Message id "+ textMessage.getJMSMessageID()+ "\r\n");
		    queueSender.close();
		    queueSession.close();
		    connection.close();
		} catch (Exception e){
			e.printStackTrace(System.out);
		} finally{
    	
		} 
		sb.append("Message send: "+ msg + "\r\n"); 
		return Response.status(200).entity(sb.toString()).build();
	}	
	
	private String urlToString(URL url, Proxy proxy)  {
		URLConnection connection;
	    StringBuffer sb = new StringBuffer();
	    
	    try{
			if(proxy != null) {
				sb.append("Proxy Has Been Set \r\n URL Being Called: " + url.toExternalForm() + "\r\n" + "Result: \r\n ");
				connection = url.openConnection(proxy);
				connection.setUseCaches(false);
			} else {
				sb.append("Proxy Has NOT Been Set \r\n URL Being Called: " + url.toExternalForm() + "\r\n" + "Result: \r\n ");
				connection = url.openConnection();
				connection.setUseCaches(false);
			}	      

			InputStream result = connection.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(result));
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();
	    } catch (Exception e){
	    	e.printStackTrace(System.out);
	    	sb.append("Failed Calling: " + url.toExternalForm() + " : " + e.getMessage());
	    } finally{
	    	
	    }
	    String resp = sb.toString().replaceAll("\\<.*?>","");
//	    System.out.println(resp);
	    logger.log(sendLog(resp),Format.JSON);
	    return resp;
	}	
		
	private EnterpriseLoggingRecord sendLog(String logMessage){
		GregorianCalendar c = new GregorianCalendar();
		XMLGregorianCalendar xc = null;
		try {
			xc = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		} catch (DatatypeConfigurationException e) {
			System.err.println("Error ~ " + e.getMessage());
		}
		EnterpriseLoggingRecord rec = new EnterpriseLoggingRecord();
		ServiceInfo si = new ServiceInfo();
		si.setComponent("GreenEchoService");
		si.setDomain("CloudFoundry");
		si.setEventTimeStamp(xc);
		si.setFunction("Echo");
		si.setInstanceId("instanceId");
		si.setLineNumber(Integer.parseInt("101010"));
		si.setServiceId("GreenEchoService");
		si.setServiceIpAddress("192.168.12.1");
		si.setServiceVersion("1.0");
		si.setPayloadVersion("1.0");
		LogMessage lm = new LogMessage();
		lm.getContent().add(logMessage);
		rec.setServiceInfo(si);
		rec.setLogMessage(lm);
		
		return rec;

	}
		
	
}