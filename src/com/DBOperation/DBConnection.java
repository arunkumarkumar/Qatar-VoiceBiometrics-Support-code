package com.DBOperation;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.dbcp2.BasicDataSource;
import org.json.simple.JSONObject;

import com.General.AppConstants;
import com.General.LoadApplicationProperties;
import com.avaya.sce.runtime.tracking.TraceInfo;
import com.avaya.sce.runtimecommon.ITraceInfo;
import com.avaya.sce.runtimecommon.SCESession;

public class DBConnection {

	public static BasicDataSource dataSource;
	public static BasicDataSource failoverDataSource;
	String Primary_DB_IP = "";
	String FailOver_DB_IP = "";
	String DB_User = "";
	String DB_Password = "";
	String DB_Name = "";
	int MinIdle = 0;
	int MaxIdle = 0;
	int DB_TimeOut = 0;
	int MaxOpenPreparedStatements = 0;
	int defaultQueryTimeout = 10;
	int maxWaitMillis = 10;
	boolean removeAbandonedBorrow = false;
	int removeAbandonedTimeout = 60; 
	Connection connection = null;

	private static BasicDataSource getDataSource(String DB_IP, String DB_User, String DB_Password, String DB_Name, int MinIdle, int MaxIdle, int DB_TimeOut, int MaxOpenPreparedStatements, int defaultQueryTimeout, int maxWaitMillis,boolean removeAbandonedBorrow,int removeAbandonedTimeout,SCESession mySession){

		BasicDataSource ds = new BasicDataSource();

		boolean isWinAuth = Boolean.parseBoolean(LoadApplicationProperties.getProperty("IsWinAuth", mySession));

		String className = "DBConnection";
		String method = "getDataSource";

		try {

			ds.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

			if(isWinAuth) {
				ds.setUrl("jdbc:sqlserver://"+DB_IP+";databaseName="+DB_Name+";integratedSecurity=true;loginTimeout="+DB_TimeOut);
			} else {
				ds.setUrl("jdbc:sqlserver://"+DB_IP+";user="+DB_User+";password="+DB_Password+";databasename="+DB_Name+";loginTimeout="+DB_TimeOut);
			}

			ds.setDefaultQueryTimeout(defaultQueryTimeout);
			ds.setMinIdle(MinIdle);
			ds.setMaxIdle(MaxIdle);
			ds.setMaxOpenPreparedStatements(MaxOpenPreparedStatements);

			if(maxWaitMillis!=0) {
				ds.setMaxWaitMillis(maxWaitMillis*1000);
			}
			if(removeAbandonedBorrow) {
				ds.setRemoveAbandonedOnBorrow(removeAbandonedBorrow);
				ds.setRemoveAbandonedTimeout(removeAbandonedTimeout);
			}

		} catch(Exception e){

			mySession.getVariableField("ApplicationVariable", "DbException").setValue(true);

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

		}
		return ds;
	}

	public void CreateURL(SCESession mySession) {

		String className = "DBConnection";
		String method = "CreateURL";


		AES aes = new AES();

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Creating DB URL", mySession);

		String ssap = aes.decrypt(LoadApplicationProperties.getProperty("DBPassword", mySession), mySession);
		Primary_DB_IP = LoadApplicationProperties.getProperty("PrimaryDBIp", mySession);
		FailOver_DB_IP = LoadApplicationProperties.getProperty("FailoverDBIp", mySession);
		DB_User = LoadApplicationProperties.getProperty("DBUserName", mySession);
		DB_Password = ssap.equalsIgnoreCase("NA")?DB_User="NA":ssap;
		DB_Name = LoadApplicationProperties.getProperty("DBName", mySession);
		MinIdle = Integer.parseInt(LoadApplicationProperties.getProperty("DB_MinIdle", mySession).equalsIgnoreCase("NA")?"5":LoadApplicationProperties.getProperty("DB_MinIdle", mySession));
		MaxIdle = Integer.parseInt(LoadApplicationProperties.getProperty("DB_MaxIdle", mySession).equalsIgnoreCase("NA")?"10":LoadApplicationProperties.getProperty("DB_MaxIdle", mySession));
		DB_TimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("DB_TimeOut", mySession).equalsIgnoreCase("NA")?"5":LoadApplicationProperties.getProperty("DB_TimeOut", mySession));
		MaxOpenPreparedStatements = Integer.parseInt(LoadApplicationProperties.getProperty("DB_MaxOpenPreparedStatements", mySession).equalsIgnoreCase("NA")?"100":LoadApplicationProperties.getProperty("DB_MaxOpenPreparedStatements", mySession));
		defaultQueryTimeout = Integer.parseInt(LoadApplicationProperties.getProperty("DB_DefaultQueryTimeout", mySession).equalsIgnoreCase("NA")?"10":LoadApplicationProperties.getProperty("DB_DefaultQueryTimeout", mySession));
		maxWaitMillis = Integer.parseInt(LoadApplicationProperties.getProperty("DB_MaxWaitMillis", mySession).equalsIgnoreCase("NA")?"10":LoadApplicationProperties.getProperty("DB_MaxWaitMillis", mySession));
		removeAbandonedBorrow = Boolean.parseBoolean(LoadApplicationProperties.getProperty("DB_RemoveAbandonedBorrow", mySession).equalsIgnoreCase("NA")?"false":LoadApplicationProperties.getProperty("DB_RemoveAbandonedBorrow", mySession));
		removeAbandonedTimeout = Integer.parseInt(LoadApplicationProperties.getProperty("DB_RemoveAbandonedTimeout", mySession).equalsIgnoreCase("NA")?"60":LoadApplicationProperties.getProperty("DB_RemoveAbandonedTimeout", mySession));

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Primary_DB_IP | "+Primary_DB_IP, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"MinIdle | "+MinIdle, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"MaxIdle | "+MaxIdle, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"DB_TimeOut | "+DB_TimeOut, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"MaxOpenPreparedStatements | "+MaxOpenPreparedStatements, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"defaultQueryTimeout | "+defaultQueryTimeout, mySession);

		try {

			if(DB_User.equalsIgnoreCase("NA")) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ "EXCEPTION IN DECRYPTING PASSWORD | "+DB_Password, mySession);
				mySession.getVariableField("ApplicationVariable", "DbException").setValue(true);

			} else {

				if (dataSource == null) {

					dataSource = DBConnection.getDataSource(Primary_DB_IP, DB_User, DB_Password, DB_Name, MinIdle, MaxIdle, DB_TimeOut, MaxOpenPreparedStatements, defaultQueryTimeout, maxWaitMillis, removeAbandonedBorrow, removeAbandonedTimeout, mySession);

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"getDataSource | PRIMARY URL_CREATED", mySession);

				}
				if (failoverDataSource == null && !FailOver_DB_IP.equalsIgnoreCase("NA")) {

					failoverDataSource = DBConnection.getDataSource(FailOver_DB_IP, DB_User, DB_Password, DB_Name, MinIdle, MaxIdle, DB_TimeOut, MaxOpenPreparedStatements, defaultQueryTimeout, maxWaitMillis, removeAbandonedBorrow, removeAbandonedTimeout, mySession);

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"getDataSource | SECONDARY URL_CREATED", mySession);

				}

				mySession.getVariableField("ApplicationVariable", "DbException").setValue(false);

			}

		} catch (Exception e){

			mySession.getVariableField("ApplicationVariable", "DbException").setValue(true);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

		}

	}

	boolean db_open (SCESession mySession) {

		String className = "DBConnection";
		String method = "db_open";


		try {

			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Creating SQL Connection", mySession);

			connection = dataSource.getConnection();

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"SQL Connection Created", mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG, className +"\t|\t"+ method +"\t|\t"+"Connection is Open", mySession);

			mySession.getVariableField("ApplicationVariable", "DbException").setValue(false);

			return true;

		} catch(SQLException e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

			if (failoverDataSource == null && !FailOver_DB_IP.equalsIgnoreCase("NA")) {

				try {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Creating Failover SQL Connection", mySession);

					connection = failoverDataSource.getConnection();

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Failover SQL Connection Created", mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG, className +"\t|\t"+ method +"\t|\t"+"Connection is Open", mySession);

					mySession.getVariableField("ApplicationVariable", "DbException").setValue(false);

					return true;

				} catch(Exception e1) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e1.getMessage()+AppConstants.EXCEPTION_2, mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ Arrays.toString(e.getStackTrace()), mySession);
					mySession.getVariableField("ApplicationVariable", "DbException").setValue(true);

				}

			} else {
				mySession.getVariableField("ApplicationVariable", "DbException").setValue(true);
			}

		} catch (Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ Arrays.toString(e.getStackTrace()), mySession);
			mySession.getVariableField("ApplicationVariable", "DbException").setValue(true);

		}

		return false;

	}

	boolean check_connection (SCESession mySession) {

		String className = "DBConnection";
		String method = "check_connection";


		boolean con = false;
		try {

			if ( connection == null || connection.isClosed() == true ) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Sql Connection is Null or Closed", mySession);
				con = db_open(mySession);

			} else {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Sql Connection is Already in Open", mySession);

			}

		} catch (SQLException e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

		} catch (Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

		}
		return con;

	}

	@SuppressWarnings("unchecked")
	public void getInsertValues(String spName,LinkedHashMap<String,Object> input, SCESession mySession){

		String className = "DBConnection";
		String method = "getInsertValues";

		LinkedHashMap<String,Object> inputUpperCase = new LinkedHashMap<>();
		CallableStatement statement = null;
		String propQueryTimeout = LoadApplicationProperties.getProperty("QueryTimeout", mySession);

		propQueryTimeout = propQueryTimeout.equalsIgnoreCase("NA")?"10":propQueryTimeout;
		int queryTimeout = Integer.parseInt(propQueryTimeout);

		if (!(input != null) && ((spName != null) && (spName.trim().length() > 0))) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"No Values to Insert in DB", mySession);

		} else {

			try {

				int columnCount = 1;
				String isCallHistoryInserted = "";
				String isTransHistoryInserted = AppConstants.F;
				String isVBHistoryInserted = AppConstants.F;

				check_connection(mySession);
				boolean DBException = mySession.getVariableField("ApplicationVariable", "DbException").getBooleanValue();
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG, className +"\t|\t"+ method +"\t|\t"+"spname : "+spName, mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG, className +"\t|\t"+ method +"\t|\t"+"input : "+input.toString(), mySession);
				
				if(DBException){

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ "Due to DB Exception Values are logged in Flat File", mySession);

					FlatFile ch = new FlatFile();
					if(spName.equalsIgnoreCase("SP_INSERT_CALL_HISTORY")) {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG, className +"\t|\t"+ method +"\t|\t"+"DataBase is Un available", mySession);

						isCallHistoryInserted = mySession.getVariableField("ApplicationVariable", "IsCallHistoryInserted").getStringValue();
						if(isCallHistoryInserted.equalsIgnoreCase(AppConstants.F)) {
							String InsertQuery = "INSERT INTO IVR_CALL_HISTORY ";
							ch.writeToFile(InsertQuery,"CallHistory", input, mySession);
							mySession.getVariableField("ApplicationVariable", "IsCallHistoryInserted").setValue(AppConstants.T);
						} else {
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"CALL HISTORY ALREADY INSERTED | SUCCESS", mySession);
						}
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ "DB Exception While inserting Call History | "+DBException , mySession);

					} else if(spName.equalsIgnoreCase("SP_INSERT_TRANSACTION_HISTORY")) {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG, className +"\t|\t"+ method +"\t|\t"+"DataBase is Un available", mySession);
						isTransHistoryInserted = mySession.getVariableField("ApplicationVariable", "IsTransHistoryInserted").getStringValue();

						if(isTransHistoryInserted.equalsIgnoreCase(AppConstants.F)) {
							String InsertQuery = "INSERT INTO IVR_TRANSACTION_HISTORY ";
							ch.writeToFile(InsertQuery,"TransactionHistory", input, mySession);
							mySession.getVariableField("ApplicationVariable", "IsTransHistoryInserted").setValue(AppConstants.T);
						} else {
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"TRANSACTION HISTORY ALREADY INSERTED | SUCCESS", mySession);
						}
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ "DB Exception While inserting Transaction History | "+DBException , mySession);

					}else if(spName.equalsIgnoreCase("SP_WIDGET_INSERT_IVR_VB_STATUS_HISTORY")) {

						
						for (Map.Entry<String, Object> entry : input.entrySet()) {
				            String key = entry.getKey();
				            Object value = entry.getValue();
				            inputUpperCase.put(key.toUpperCase(), value);
				            // Perform actions with key and value
				           //System.out.println("Key: " + key + ", Value: " + value);

				            // If you need to perform specific actions based on the type of value
				            if (value instanceof String) {
				                String stringValue = (String) value;
				                System.out.println("String value: " + stringValue);
				            } else {
				                System.out.println("Non-String value: " + value);
				            }
				        }
						
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG, className +"\t|\t"+ method +"\t|\tinputUpperCase passed as input to flatfile : "+inputUpperCase.toString(), mySession);
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG, className +"\t|\t"+ method +"\t|\t"+"DataBase is Un available", mySession);
						isVBHistoryInserted = mySession.getVariableField("isVBHistoryInserted").getStringValue();
						
						 
						if(isVBHistoryInserted.equalsIgnoreCase(AppConstants.F)) {
							String InsertQuery = "INSERT INTO IVR_VB_STATUS_HISTORY ";
							ch.writeToFile(InsertQuery,"VBHistory", inputUpperCase, mySession);
							mySession.getVariableField("isVBHistoryInserted").setValue(AppConstants.T);
						} else {
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"VB HISTORY ALREADY INSERTED | SUCCESS", mySession);
						}
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ "DB Exception While inserting VB History | "+DBException , mySession);

					} 
					else {
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG, className +"\t|\t"+ method +"\t|\t"+"Appart from Call History , Transaction History and VB History, Other don't have Flat file Operation", mySession);
					}

				} else {

					String query = "";
					if("SP_WIDGET_INSERT_IVR_VB_STATUS_HISTORY".equalsIgnoreCase(spName)) {
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG, className +"\t|\t"+ method +"\t|\t VB status procedure ",mySession);
						HashMap<String, Object> tmp = new HashMap<>();
						tmp.put("1", "1");
						query = prepareQueryParameters(spName, tmp, mySession);
					}else {
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG, className +"\t|\t"+ method +"\t|\t normal procedure ",mySession);
						query = prepareQueryParameters(spName, input, mySession);
					}
					
					if(query == null) {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Insert Query is Empty", mySession);

					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\tquery to be processed : "+query, mySession);

					}

					statement = connection.prepareCall(query);
					statement.setQueryTimeout(queryTimeout);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Query Timout |"+"\t"+queryTimeout, mySession);
					if("SP_WIDGET_INSERT_IVR_VB_STATUS_HISTORY".equalsIgnoreCase(spName)) {
						JSONObject request = new JSONObject();
						request.put("VC_UCID", input.get("VC_UCID").toString());
						request.put("VC_FFP_NUMBER", input.get("VC_FFP_NUMBER").toString());
						request.put("VC_WORK_REQUEST_ID", input.get("VC_WORK_REQUEST_ID").toString());
						request.put("VC_IDENTIFY_TYPE", input.get("VC_IDENTIFY_TYPE").toString());
						request.put("VC_SEGMENT", input.get("VC_SEGMENT").toString());
						request.put("VC_CHANNEL", input.get("VC_CHANNEL").toString());
						request.put("VC_STATUS", input.get("VC_STATUS").toString());
						request.put("DT_STATUS_UPDATE_DATE", input.get("DT_STATUS_UPDATE_DATE").toString());
						request.put("VC_DE_ENROLL_REASON", input.get("VC_DE_ENROLL_REASON").toString());
						request.put("VC_TRANSFERRED_BY_AGENT_ID", input.get("VC_TRANSFERRED_BY_AGENT_ID").toString());
						request.put("VC_TRANSFERRED_BY_AGENT_ROLE", input.get("VC_TRANSFERRED_BY_AGENT_ROLE").toString());
						request.put("VC_DE_ENROLLED_AGENT_ID", input.get("VC_DE_ENROLLED_AGENT_ID").toString());
						request.put("VC_DE_ENROLLED_AGENT_ROLE", input.get("VC_DE_ENROLLED_AGENT_ROLE").toString());
						request.put("VC_FUNCTION_NAME", input.get("VC_FUNCTION_NAME").toString());
						request.put("VC_VB_REASON", input.get("VC_VB_REASON").toString());
						
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Values to be inserted : "+request.toJSONString(), mySession);
						statement.setString(1,request.toJSONString());
						
					}
					else{
					for (Entry<String, Object> entry : input.entrySet()) {

						if(entry.getKey().toString().startsWith("C")) {
							statement.setString(columnCount++, entry.getValue().toString());
						} else if (entry.getKey().toString().startsWith("VC")) {
							statement.setString(columnCount++, entry.getValue().toString());
						} else if (entry.getKey().toString().startsWith("NU")) {
							statement.setInt(columnCount++, Integer.parseInt(entry.getValue().toString()));
						} else if (entry.getKey().toString().startsWith("DT")) {
							statement.setString(columnCount++, entry.getValue().toString());
						}

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+columnCount+"\t|\t"+entry.getValue().toString(), mySession);

					}
					}
					
					statement.execute();

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Values Inserted in to DataBase", mySession);

				}

			} catch (Exception e) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
				mySession.getVariableField("ApplicationVariable", "DbException").setValue(true);

			} finally {

				if(connection != null) {

					try {

						statement.close();
						connection.close();
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG, className +"\t|\t"+ method +"\t|\t"+"Connection is Closed", mySession);

					} catch (SQLException e) {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

					}

				}

			}

		}

	}

	public boolean insertMenuHistory(String MenuData, SCESession mySession) {

		String className = "DBConnection";
		String method = "getResultSet";


		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);

		boolean insertStatus = false;
		String propQueryTimeout = LoadApplicationProperties.getProperty("QueryTimeout", mySession);

		propQueryTimeout = propQueryTimeout.equalsIgnoreCase("NA")?"10":propQueryTimeout;
		int queryTimeout = Integer.parseInt(propQueryTimeout);
		CallableStatement prepStmt = null;

		try {

			if(check_connection(mySession)) {

				prepStmt = connection.prepareCall(MenuData);
				prepStmt.setQueryTimeout(queryTimeout);
				prepStmt.execute();
				insertStatus = true;

			} else {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ "Sql Connection is not Created", mySession);

			}

		} catch (Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
		} finally {

			try {

				prepStmt.close();

				if(connection != null) {
					connection.close();
				}

			} catch (SQLException e) {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
			}

		}

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"InsertStatus | "+insertStatus, mySession);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);

		return insertStatus;

	}

	public List<HashMap<Object, Object>> getResultSet(String spName, LinkedHashMap<String, Object> input,SCESession mySession){


		String className = "DBConnection";
		String method = "getResultSet";

		List<HashMap<Object, Object>> output = new ArrayList<HashMap<Object, Object>>();
		CallableStatement statement = null;
		String propQueryTimeout = LoadApplicationProperties.getProperty("QueryTimeout", mySession);

		propQueryTimeout = propQueryTimeout.equalsIgnoreCase("NA")?"10":propQueryTimeout;
		int queryTimeout = Integer.parseInt(propQueryTimeout);

		try {

			int columnCount = 1;
			check_connection(mySession);
			boolean DBException = mySession.getVariableField("ApplicationVariable", "DbException").getBooleanValue();

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"SP Name\t|\t"+spName, mySession);

			if(DBException){

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ "Due to DB Exception Can't able to fetch Value from DB", mySession);

			} else {

				String query = prepareQueryParameters(spName, input, mySession);

				if(query == null) {
					output=null;
				}

				statement = connection.prepareCall(query);
				statement.setQueryTimeout(queryTimeout);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Query Timout |"+"\t"+queryTimeout, mySession);

				if(input!=null){

					for (Entry<String, Object> entry : input.entrySet()) {

						if(entry.getKey().toString().startsWith("C")) {
							statement.setString(columnCount++, entry.getValue().toString());
						} else if (entry.getKey().toString().startsWith("VC")) {
							statement.setString(columnCount++, entry.getValue().toString());
						} else if (entry.getKey().toString().startsWith("NU")) {
							statement.setInt(columnCount++, Integer.parseInt(entry.getValue().toString()));
						} else if (entry.getKey().toString().startsWith("D")) {
							statement.setString(columnCount++, entry.getValue().toString());
						} else if (entry.getKey().toString().startsWith("T")) {
							statement.setString(columnCount++, entry.getValue().toString());
						}

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+columnCount+"\t|\t"+entry.getValue().toString(), mySession);

					}

				}
				else {
					
				if("SP_GET_COMMON_CONFIG_VALUES".equalsIgnoreCase(spName)) {
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+columnCount+"\t|\t procedure is getting common config values", mySession);
				}else{
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+columnCount+"\t|\t input is null and procedure is not common config values", mySession);
				}
				}

				try (ResultSet resultSet = statement.executeQuery();) {


					output = resultSetToList(resultSet, mySession);

				} catch (Exception e) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
					output=null;

				}

			}

		} catch (Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

		} finally {

			if(connection != null) {

				try {

					statement.close();
					connection.close();

				} catch (SQLException e) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
					output=null;

				}

			}

		}

		try {

			if (output.isEmpty()) { 

				output=null;

			}

		} catch(Exception e) {

			output=null;
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

		}

		return output;

	}

	private List<HashMap<Object, Object>> resultSetToList(ResultSet rs, SCESession mySession) throws SQLException {

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "DBConnection\t|\tresultSetToList\t|\tStarted", mySession);

		ResultSetMetaData metaData = rs.getMetaData();
		int count = metaData.getColumnCount();
		String[] columnName = new String[count];
		List<HashMap<Object,Object>> list = new ArrayList<>();

		while(rs.next()) {
			HashMap<Object,Object> map=new HashMap<>();
			for (int i = 1; i <= count; i++) {
				columnName[i-1] = metaData.getColumnLabel(i);
				map.put(columnName[i-1], rs.getObject(i));
			}
			list.add(map);
		}

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "DBConnection\t|\tresultSetToList\t|\tCompleted", mySession);

		return list;

	}

	private String prepareQueryParameters(String spName, HashMap<String, Object> inputMap, SCESession mySession) {



		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "DBConnection\t|\tprepareQueryParameters\t|tStarted", mySession);

		String queryBody = "";
		if(inputMap!=null){
			for(int inputLength = 0; inputLength<inputMap.size(); inputLength++) {
				queryBody = queryBody.concat(AppConstants.STATEMENTBODY);
			}
			spName = spName.concat(AppConstants.STATEMENTBEGIN.concat(queryBody.substring(0, queryBody.length() - 1)).concat(AppConstants.STATEMENTEND));
		}

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,  "DBConnection\t|\tprepareQueryParameters\t|tCompleted", mySession);

		return AppConstants.CALLABLESTATEMNT.replace("*", spName);

	}

	public List<HashMap<Object, Object>> getModifiedTable(LinkedHashMap<String, Object> input,SCESession mySession){
		String className = "DBConnection";
		String method = "getModifiedTable";

		List<HashMap<Object, Object>> output = new ArrayList<HashMap<Object, Object>>();
		PreparedStatement statement = null;
		String propQueryTimeout = LoadApplicationProperties.getProperty("QueryTimeout", mySession);

		propQueryTimeout = propQueryTimeout.equalsIgnoreCase("NA")?"10":propQueryTimeout;
		int queryTimeout = Integer.parseInt(propQueryTimeout);

		try {

			int columnCount = 1;
			check_connection(mySession);
			boolean DBException = mySession.getVariableField("ApplicationVariable", "DbException").getBooleanValue();

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"TableName Name\t|\t"+"IVR_MASTER", mySession);

			if(DBException){

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ "Due to DB Exception Can't able to fetch Value from DB", mySession);

			} else {

				String query = "SELECT VC_TABLES FROM IVR_MASTER WHERE VC_IS_MODIFIED = ? AND VC_APP_SRVR_IP = ?";

				if(query == null|| query==" ") {
					
					output=null;
					
				}
				
				statement = connection.prepareStatement(query);
				statement.setQueryTimeout(queryTimeout);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Query Timout |"+"\t"+queryTimeout, mySession);

				if(input!=null){

					statement.setString(1, "Y");
					statement.setString(2, input.get("VC_APP_SRVR_IP").toString());

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+columnCount+"\t|\t"+input.get("VC_APP_SRVR_IP").toString(), mySession);

				}

				try (ResultSet resultSet = statement.executeQuery();) {

					output = resultSetToList(resultSet, mySession);

				} catch (Exception e) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
					output=null;

				}

			}

		} catch (Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

		} finally {

			if(connection != null) {

				try {

					statement.close();
					connection.close();

				} catch (SQLException e) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
					output=null;

				}

			}

		}

		try {

			if (output.isEmpty()) { 

				output=null;

			}

		} catch(Exception e) {

			output=null;
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

		}

		return output;
	}

	public void InsertTransHistory(LinkedHashMap<String,Object> input, SCESession mySession){

		String className = "DBConnection";
		String method = "InsertTransHistory";

		String isTransHistoryInserted = AppConstants.F;


		PreparedStatement statement = null;
		String propQueryTimeout = LoadApplicationProperties.getProperty("QueryTimeout", mySession);

		propQueryTimeout = propQueryTimeout.equalsIgnoreCase("NA")?"10":propQueryTimeout;
		int queryTimeout = Integer.parseInt(propQueryTimeout);



		try {

			//int columnCount = 1;
			check_connection(mySession);
			boolean DBException = mySession.getVariableField("ApplicationVariable", "DbException").getBooleanValue();

			if(DBException){

				FlatFile ch = new FlatFile();

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG, className +"\t|\t"+ method +"\t|\t"+"DataBase is Un available", mySession);
				isTransHistoryInserted = mySession.getVariableField("ApplicationVariable", "IsTransHistoryInserted").getStringValue();

				if(isTransHistoryInserted.equalsIgnoreCase(AppConstants.F)) {
					String InsertQuery = "INSERT INTO IVR_TRANSACTION_HISTORY ";
					ch.writeToFile(InsertQuery,"TransactionHistory", input, mySession);
					mySession.getVariableField("ApplicationVariable", "IsTransHistoryInserted").setValue(AppConstants.T);
				} else {
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"TRANSACTION HISTORY ALREADY INSERTED | SUCCESS", mySession);
				}
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ "DB Exception While inserting Transaction History | "+DBException , mySession);



				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ "Due to DB Exception Insert Values are logged in Flat File", mySession);

			} else {

				String query = "INSERT INTO IVR_TRANSACTION_HISTORY (VC_UCID,VC_CLI_NO,DT_START_DATE,DT_END_DATE,VC_FUNCTION_NAME,"
						+ "VC_HOST_URL,VC_HOST_REQUEST,VC_HOST_RESPONSE,VC_TRANS_STATUS,VC_APP_SRVR_IP)VALUES (?,?,?,?,?,?,?,?,?,?)";


				if(query == null || query==" ") {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Insert Query is Empty", mySession);

				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"", mySession);

				}

				statement = connection.prepareStatement(query);
				statement.setQueryTimeout(queryTimeout);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Query Timout |"+"\t"+queryTimeout, mySession);

				statement.setString(1, input.get("VC_UCID").toString());
				statement.setString(2, input.get("VC_CLI_NO").toString());
				statement.setString(3, input.get("DT_START_DATE").toString());
				statement.setString(4, input.get("DT_END_DATE").toString());
				statement.setString(5, input.get("VC_FUNCTION_NAME").toString());
				statement.setString(6, input.get("VC_HOST_URL").toString());
				statement.setString(7, input.get("VC_HOST_REQUEST").toString());
				statement.setString(8, input.get("VC_HOST_RESPONSE").toString());
				statement.setString(9, input.get("VC_TRANS_STATUS").toString());
				statement.setString(10, input.get("VC_APP_SRVR_IP").toString());

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"VC_UCID"+"\t|\t"+input.get("VC_UCID").toString(), mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"VC_CLI_NO"+"\t|\t"+input.get("VC_CLI_NO").toString(), mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"DT_START_DATE"+"\t|\t"+input.get("DT_START_DATE").toString(), mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"DT_END_DATE"+"\t|\t"+input.get("DT_END_DATE").toString(), mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"VC_FUNCTION_NAME"+"\t|\t"+input.get("VC_FUNCTION_NAME").toString(), mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"VC_HOST_URL"+"\t|\t"+input.get("VC_HOST_URL").toString(), mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"VC_HOST_REQUEST"+"\t|\t"+input.get("VC_HOST_REQUEST").toString(), mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"VC_HOST_RESPONSE"+"\t|\t"+input.get("VC_HOST_RESPONSE").toString(), mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"VC_TRANS_STATUS"+"\t|\t"+input.get("VC_TRANS_STATUS").toString(), mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"VC_APP_SRVR_IP"+"\t|\t"+input.get("VC_APP_SRVR_IP").toString(), mySession);


				statement.execute();

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"INSERT TRANSACTION HISTORY | SUCCESS", mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Values Inserted in to DataBase", mySession);

			}

		} catch (Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
			mySession.getVariableField("ApplicationVariable", "DbException").setValue(true);

		} finally {

			if(connection != null) {

				try {

					statement.close();
					connection.close();
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG, className +"\t|\t"+ method +"\t|\t"+"Connection is Closed", mySession);

				} catch (SQLException e) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

				}

			}

		}



	}

	public void insertOcdsCallCount(LinkedHashMap<String,Object> input, SCESession mySession){
		String className = "DBConnection";
		String method = "insertOcdsCallCount";


		PreparedStatement statement = null;
		String propQueryTimeout = LoadApplicationProperties.getProperty("QueryTimeout", mySession);

		propQueryTimeout = propQueryTimeout.equalsIgnoreCase("NA")?"10":propQueryTimeout;
		int queryTimeout = Integer.parseInt(propQueryTimeout);



		try {

			//int columnCount = 1;
			check_connection(mySession);
			boolean DBException = mySession.getVariableField("ApplicationVariable", "DbException").getBooleanValue();

			if(DBException){				

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG, className +"\t|\t"+ method +"\t|\t"+"DataBase is Un available", mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ "DB Exception While inserting OCDS Call Count | "+DBException , mySession);

			} else {

				String query = "INSERT INTO IVR_CALL_COUNT VALUES (?,?,?,?,?)";


				if(query == null || query==" ") {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Insert Query is Empty", mySession);

				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"", mySession);

				}

				statement = connection.prepareStatement(query);
				statement.setQueryTimeout(queryTimeout);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Query Timout |"+"\t"+queryTimeout, mySession);

				statement.setString(1, input.get("VC_UCID").toString());
				statement.setString(2, input.get("VC_CLI_NO").toString());
				statement.setString(3, input.get("DT_START_DATE").toString());
				statement.setString(4, input.get("VC_MARKET").toString());
				statement.setString(5, input.get("VC_LINE_TYPE").toString());

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"VC_UCID"+"\t|\t"+input.get("VC_UCID").toString(), mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"VC_CLI_NO"+"\t|\t"+input.get("VC_CLI_NO").toString(), mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"DT_START_DATE"+"\t|\t"+input.get("DT_START_DATE").toString(), mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"VC_MARKET"+"\t|\t"+input.get("VC_MARKET").toString(), mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"VC_LINE_TYPE"+"\t|\t"+input.get("VC_LINE_TYPE").toString(), mySession);


				statement.execute();

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Values Inserted in to DataBase", mySession);

			}

		} catch (Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
			mySession.getVariableField("ApplicationVariable", "DbException").setValue(true);

		} finally {

			if(connection != null) {

				try {

					statement.close();
					connection.close();
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG, className +"\t|\t"+ method +"\t|\t"+"Connection is Closed", mySession);

				} catch (SQLException e) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

				}

			}

		}

	}

}