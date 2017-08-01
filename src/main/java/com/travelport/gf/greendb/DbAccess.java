package com.travelport.gf.greendb;

import java.sql.* ;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.travelport.gf.util.ConfigBuilder;

import argo.jdom.*;
import argo.saj.InvalidSyntaxException;

/**
 * This class demonstrates how information from VCAP_SERVICES can be used to connect to a bound service instance.
 * The only public method is getDataFromTable - it queries data from the table based on name provided and returns formatted data.
 * 
 * Intention is to prove out connectivity using existing project - this quick and dirty code should NOT be reused.
 * 
 * Actual implementation should be much easier and elegant with JTA or Spring framework.
 * 
 * @author E029140
 *
 */

public class DbAccess {
	
	private static final String NULL_STRING = "" ;
	private static final String FIELD_SEPARATOR = "," ;
	private static final String LINE_END = System.lineSeparator() ;
	private static final String DB_SVC_NAME = "testdb-connection" ;
	
	private static final String DATA_START = "<table>" ;
	private static final String DATA_END = "</table>" ;
	private static final String HEADER_START = "<th>" ;
	private static final String HEADER_END = "</th>" ;
	private static final String ROW_START = "<tr>" ;
	private static final String ROW_END = "</tr>" ;
	private static final String VAL_START = "<td>" ;
	private static final String VAL_END = "</td>" ;

	private String hostname = NULL_STRING ;
	private String dbname = NULL_STRING;
	private String user = NULL_STRING;
	private String password = NULL_STRING;
	private String port = NULL_STRING ;
	
	
	private Connection createConnectionFromStackatoEnv(String vcap_services){
		Connection connection = null ;
		Map<String, String> dbMap = ConfigBuilder.getVcapMap(vcap_services, DB_SVC_NAME) ;
		if(dbMap==null || dbMap.size() < 5){
			System.out.println("Unable to get db credentials from "+ DB_SVC_NAME);
			return null ;
		}
		
			    dbname = dbMap.get("database");
			    hostname = dbMap.get("host");
			    user = dbMap.get("user");
			    password = dbMap.get("pass");
			    port = dbMap.get("port");

			    String dbUrl = "jdbc:mysql://" + hostname + ":" + port + "/" + dbname;
			    System.out.println("dbURL :: " + dbUrl);

			    try {
					Class.forName("com.mysql.jdbc.Driver");	
					connection = DriverManager.getConnection(dbUrl, user, password);
				} catch (ClassNotFoundException cfe) {
					System.out.println("There were errors reistering db driver " + cfe.getMessage());
					cfe.printStackTrace();
				} catch (SQLException sqle) {
					System.out.println("Tehere were errors in cerating connection " + sqle.getMessage());
				}

	return connection ;
	}
	

	
	private void closeConnection(Connection con){
		try{
			if(con != null){
				con.close();
			}
		
		}catch(SQLException sqle){
			System.out.println("Exception in closing connection " + sqle.getMessage());
		}
	con = null ;
	}
	
	private void closeStatement(Statement stmt){
		try{
			if(stmt != null){
				stmt.close();
			}
			
		}catch(SQLException sqle){
			System.out.println("Exception in closing statement " + sqle.getMessage());
		}
	stmt = null ;
	}
	
	private void closeResultset(ResultSet rs){
		try{
			if(rs != null){
				rs.close();
			}
			
		}catch(SQLException sqle){
			System.out.println("Exception in closing resultset " + sqle.getMessage());
		}
	rs = null ;
	}
	
	/**
	 *  This method parses the VCAP_SERVICES string to find out the db credentials required to connect to a bound service.
	 *  It then tries to fetch first 10 records from a table, based on the table name provided.
	 *  Finally, it returns the formatted resultset.
	 *  Intention is to prove out connectivity using existing project - this quick and dirty code should NOT be reused.
	 * Actual implementation should be much easier and elegant with JTA or Spring framework.
	 * @param vcap_services - The VCAP_SERVICES string (JSON format), usually from environment variable
	 * @param tableName - Name of a table in the provisioned database
	 * @return
	 */
	
	public String getDataFromTable(String vcap_services, String tableName){

		Connection con = createConnectionFromStackatoEnv(vcap_services) ;
		if(con == null){
			return "Failed to create connection... exiting..." ;
		}
		StringBuilder output = new StringBuilder() ;
		Statement statement = null ;
		ResultSet rs = null ;
		try{
			statement = con.createStatement() ;
			rs = statement.executeQuery("Select * from " + tableName + " LIMIT 10") ;
			ResultSetMetaData metadata = rs.getMetaData() ;
			int colCount = metadata.getColumnCount() ;
			output.append(DATA_START) ;
			output.append(ROW_START);
			for(int i=1; i<= colCount; i++){
			output.append(VAL_START);
			output.append(metadata.getColumnLabel(i))	;
			output.append(VAL_END);
			}
			output.append(ROW_END);
			output.append(LINE_END) ;
			while (rs.next()){
			output.append(ROW_START) ;
				for(int i=1; i<= colCount; i++){
				output.append(VAL_START) ;
				output.append(rs.getString(i)) ;
				output.append(VAL_END) ;
				}
			output.append(ROW_END) ;
			output.append(LINE_END) ;
			}
		output.append(DATA_END) ;
		closeResultset(rs);	
		closeStatement(statement);
		closeConnection(con);
		}catch(SQLException sqle){
		output.append(sqle.getMessage()) ;
		}
		finally{
		      //finally block used to close resources
			closeResultset(rs);	
			closeStatement(statement);
			closeConnection(con);
		   }//end finally
	
	return output.toString() ;	
	}
	
	private void testConnection(){
		String vcap_services = System.getenv("VCAP_SERVICES") ;
		System.out.println("VCAP_SERVICES :: " + vcap_services);
		Connection con = createConnectionFromStackatoEnv(vcap_services);
		if (con !=null){
			System.out.println("Connected");
			closeConnection(con);
		}
	}
	

	
	public static void main (String args[]){
		//System.setProperty("VCAP_SERVICES", "{"user-provided":[{"name":"testdb-connection","label":"user-provided","tags":[],"credentials":{"database":"perfreporting","host":"shlgnrhds003.tvlport.net","pass":"uaZVC8HQC5vtKfUEBemy","port":"3306","user":"perfreporting"},"syslog_drain_url":""}]})
		String vcap = System.getenv("VCAP_SERVICES") ;
		System.out.println("VCAP = " + vcap);
		new DbAccess().testConnection();
		
	}



}


