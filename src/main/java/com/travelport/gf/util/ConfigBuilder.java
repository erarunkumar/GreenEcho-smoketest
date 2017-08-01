package com.travelport.gf.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;


import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import argo.jdom.JsonStringNode;
import argo.saj.InvalidSyntaxException;

public class ConfigBuilder {
	
	private static final String VCAP_USER_NODE = "user-provided" ; 
	private static final String VCAP_CREDENTIAL_NODE = "credentials" ; 
	private static final String VCAP_NAME_NODE = "name" ; 
	
	/**
	 * Constructs a map of env variables required for a particular user defined service by extracting them from VCAP_SERVICES
	 * @param vcap_services - The VCAP_SERVICES env variable
	 * @param serviceName - Name of the service for which k
	 * @return - A map of key value pairs for the service credentials
	 */
	public static Map<String, String> getVcapMap(String vcap_services, String serviceName) {
		if(vcap_services==null || serviceName == null){
			System.out.println("Expecting not-null arguments");
			return null ;
		}
		Map<String, String> vcapMap = null ;
		if (vcap_services != null && vcap_services.length() > 0) {
			    JsonRootNode root;
				try {
					root = new JdomParser().parse(vcap_services);
				} catch (InvalidSyntaxException e) {
					System.out.println("Error in parsing string: " + e.getMessage());
					return vcapMap ;
				}
			    JsonNode userProvided = root.getNode(VCAP_USER_NODE);
			    for (JsonNode serviceNode : userProvided.getElements()) {
			    	System.out.println("name = " + serviceNode.getStringValue(VCAP_NAME_NODE)) ;
			    	if(serviceName.equalsIgnoreCase(serviceNode.getStringValue(VCAP_NAME_NODE))){
			    		vcapMap = new HashMap<String, String>() ;
				    	JsonNode credentials = serviceNode.getNode(VCAP_CREDENTIAL_NODE);
					    Map<JsonStringNode, JsonNode> map = credentials.getFields() ; 
					    Set<Entry<JsonStringNode, JsonNode>> entryset  = map.entrySet();
					    for (Entry<JsonStringNode, JsonNode> entry : entryset) {
					    	vcapMap.put(entry.getKey().getText(), entry.getValue().getText()) ;
						} 
					break ;
			    	}

				}
 
			}			
	return vcapMap ;
	}
	
	/**
	 * Constructs a map of env variables required for all user provided services by extracting them from VCAP_SERVICES
	 * @param vcap_services - The VCAP_SERVICES env variable
	 * @return - A map of service name as key and a map of key value pairs for the service credentials
	 */
	public static Map<String, Map<String, String>> getVcapMapForAll(String vcap_services) {
		Map<String, String> vcapMap = null ;
		Map<String, Map<String, String>> masterMap = null ;
		if (vcap_services != null && vcap_services.length() > 0) {
			    JsonRootNode root;
				try {
					root = new JdomParser().parse(vcap_services);
				} catch (InvalidSyntaxException e) {
					System.out.println("Error in parsing string: " + e.getMessage());
					return null ;
				}
			    JsonNode userProvided = root.getNode(VCAP_USER_NODE);
			    if(userProvided == null){
			    	return null ;
			    }
			    masterMap = new HashMap<String, Map<String, String>>() ;
			    
			    for (JsonNode serviceNode : userProvided.getElements()) {
			    	System.out.println("name = " + serviceNode.getStringValue(VCAP_NAME_NODE)) ;
			    		
			    		vcapMap = new HashMap<String, String>() ;
				    	JsonNode credentials = serviceNode.getNode(VCAP_CREDENTIAL_NODE);
					    Map<JsonStringNode, JsonNode> map = credentials.getFields() ; 
					    Set<Entry<JsonStringNode, JsonNode>> entryset  = map.entrySet();
					    for (Entry<JsonStringNode, JsonNode> entry : entryset) {
					    	vcapMap.put(entry.getKey().getText(), entry.getValue().getText()) ;
						} 
					masterMap.put(serviceNode.getStringValue(VCAP_NAME_NODE), vcapMap) ;	    
				}
 
			}			
	return masterMap ;
	}
	
	public static void main (String args[]){
		String vcap = System.getenv("VCAP_SERVICES") ;

		System.out.println("VCAP = " + vcap);

		Map<String, Map<String, String>>	vmap = ConfigBuilder.getVcapMapForAll(vcap);
		System.out.println(vmap);
		
		System.out.println("Trying single service name ");
		System.out.println( ConfigBuilder.getVcapMap(vcap, "testdb-connection") );
	
	}

}
