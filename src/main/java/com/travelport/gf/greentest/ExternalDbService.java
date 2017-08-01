package com.travelport.gf.greentest;

import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.travelport.gf.greendb.DbAccess;

@Path("/db")
public class ExternalDbService {
	
	@GET
	@Path("/{param}")
	@Produces(MediaType.TEXT_HTML)
	public Response getMsg(@PathParam("param") String msg) {
		StringBuilder output = new StringBuilder("<html><body><h1>Green DB Service welcomes you : </h1>") ;
		output.append("<p>Trying to get data for ") ;
		output.append(msg);
		
		output.append("</p>") ;
		DbAccess dbAccess = new DbAccess() ;
		String tabOutput = dbAccess.getDataFromTable(System.getenv("VCAP_SERVICES"), msg) ;
		
		output.append(tabOutput) ;
		output.append("</body></html>") ;
		System.out.println("GreenEcho log test : sending response from db...");
		return Response.status(200).entity(output.toString()).build();
	}
	
	private String getRequestId(){
		return UUID.randomUUID().toString() ;
	}
	
	/**
	 * This is to demonstrate the env variables - do NOT use in actual application
	 * @param msg - a user provided string
	 * @return - String echoing back msg along with env variables
	 */
	private String echoEnv(String msg){
		StringBuilder output = new StringBuilder("Green DB Service welcomes you : ") ;
		output.append(msg);
		output.append(System.lineSeparator()) ;
		output.append("STACKATO_SERVICES :: ") ;
		output.append(System.lineSeparator()) ;
		output.append(System.getenv("STACKATO_SERVICES"));
		output.append("VCAP_SERVICES :: ") ;
		output.append(System.lineSeparator()) ;
		output.append(System.getenv("VCAP_SERVICES"));
		
		output.append("DATABASE_URL :: ") ;
		output.append(System.lineSeparator()) ;
		output.append(System.getenv("DATABASE_URL"));
		return output.toString() ;
	}

}
