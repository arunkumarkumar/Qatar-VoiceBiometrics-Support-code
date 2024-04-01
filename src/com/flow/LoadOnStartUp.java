package com.flow;

import java.io.*;
//import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.DBOperation.DBConnection;
import com.General.AppConstants;
import com.General.LoadApplicationProperties;
import com.avaya.sce.runtime.tracking.TraceInfo;
import com.avaya.sce.runtimecommon.ITraceInfo;
import com.avaya.sce.runtimecommon.SCESession;

public class LoadOnStartUp {
	
	@SuppressWarnings("unchecked")
	public void setInitialValues(SCESession mySession) {

		String className = "LoadOnStartUp";
		String method = "setInitialValues";

		Variables variables = new Variables();
		CheckInitialTableStatus initValues = new CheckInitialTableStatus();
		DBConnection db = new DBConnection();
		
		String CLID = "NA";
		String DNIS = "NA";
		String appServerIP = "NA";
		String VC_UCID = "NA";
		String CallStartTime = "NA";
		String MPPSessionId = "NA";
		String path = "NA";
		String ExternalProperties = "NA";
		
		File fileLocation = null;

		HashMap<String,String> App_Prop = new HashMap<String,String>();
		LinkedHashMap<String,String> CallHistory = new LinkedHashMap<String,String>();
		LinkedHashMap<String,Object> TransactionHistory = new LinkedHashMap<String,Object>();
		HashMap<String,String> customerDetails = new HashMap<String,String>();

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"**********************", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"*****Call Started*****", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"**********************", mySession);
		
		try {

			//Load Values from Application Properties
			path = mySession.getVariableField("PropertyFileLocation").getStringValue();

			fileLocation = new File(new File(path).getCanonicalPath());

			if(fileLocation.exists()){

				LoadApplicationProperties load = new LoadApplicationProperties();
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Property File Path\t|\t"+path, mySession);
				load.IsPropModified(path, mySession);

			} else{

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_WARN, className +"\t|\t"+ method +"\t|\t"+ "Requested File "+fileLocation+" is not found", mySession);

			}

			boolean isPropertyFileModified = mySession.getVariableField("IsPropertyFileModified").getBooleanValue();
			App_Prop = (HashMap<String, String>) mySession.getVariableField("HashMap", "PropertyValue").getObjectValue();
			ExternalProperties = App_Prop.get("ExternalPropertyLocation");
			fileLocation = new File(new File(ExternalProperties).getCanonicalPath());
			
			if(fileLocation.exists()){

				LoadApplicationProperties load = new LoadApplicationProperties();
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Property File Path\t|\t"+fileLocation, mySession);
				load.IsPropModified(ExternalProperties, mySession);

			} else{

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_WARN, className +"\t|\t"+ method +"\t|\t"+ "Requested File "+fileLocation+" is not found", mySession);

			}

			//LoadVariables
			variables.initializeDynamicVariables(mySession);

			//Create DB URL
			if(isPropertyFileModified) {

				DBConnection.dataSource = null;
				DBConnection.failoverDataSource = null;
				db.CreateURL(mySession);

			} else {

				db.CreateURL(mySession);

			}
			
		} catch(Exception e) {
			
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
			
		}
		

		String phoneNumber = LoadApplicationProperties.getProperty("TestMobileNumber", mySession);
		if(!phoneNumber.equalsIgnoreCase("NA")) {
			mySession.getVariableField("session", "ani").setValue(phoneNumber);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"CLI is taken from Property file | "+phoneNumber, mySession);
		}
		
		//CLID
		CLID = mySession.getVariableField("session", "ani").getStringValue();
		
		//IDENT_TYPE
		mySession.getVariableField("ident_type").setValue("NA");
		
		//DNIS
		DNIS = mySession.getVariableField("session", "dnis").getStringValue();
				
		//GenerateUCID
		VC_UCID = mySession.getVariableField("session", "ucid").getStringValue();
		if (VC_UCID.equalsIgnoreCase("undefined") || VC_UCID.isEmpty()) {
			VC_UCID = "20000"+new SimpleDateFormat("yyyyMMddkkmmss").format(new Date());
		}

		//StartTime
		CallStartTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
			
		//MPPSessionID
		MPPSessionId = mySession.getVariableField("session","sessionid").getStringValue();
			
		//GetAppServerIP
		try {
			appServerIP = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
			
		}

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Session Values are loaded", mySession);
		
		App_Prop = (HashMap<String, String>) mySession.getVariableField("HashMap", "PropertyValue").getObjectValue();
		//mySession.setProperty("App_Prop", App_Prop);
		//SetAudioLocation
		mySession.getVariableField("DynamicPrompt","PromptLocation").setValue(App_Prop.get("AudioLocation"));
		mySession.getVariableField("DynamicPrompt","PromptLanguage").setValue("AR");
		
		//setDefaultValues
		mySession.getVariableField("ApplicationVariable", "IsTransHistoryInserted").setValue(AppConstants.F);
		mySession.getVariableField("ApplicationVariable", "IsCallHistoryInserted").setValue(AppConstants.F);
		mySession.getVariableField("ApplicationVariable", "BargeIn").setValue(AppConstants.T);
		//changes
		mySession.getVariableField("ApplicationVariable", "Timeout").setValue("8");
		//mySession.getVariableField("ApplicationVariable", "Timeout").setValue(App_Prop.get("Timeout"));
		
		mySession.getVariableField("timeout").setValue(App_Prop.get("FetchTimeout"));
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"timeout obtained from property as  "+mySession.getVariableField("timeout").getStringValue(), mySession);
		
		mySession.getVariableField("ApplicationVariable", "NoInput").setValue("1");
		mySession.getVariableField("ApplicationVariable", "NoMatch").setValue("1");
		mySession.getVariableField("ApplicationVariable", "internalRetryCount").setValue(0);
		mySession.getVariableField("ApplicationVariable", "retryCount").setValue(3);
		
		//Initialize CustomerDetail Values
		customerDetails.put("Channel","VOICE");
		customerDetails.put("CustomerName","");
		customerDetails.put("CustomerNumber",CLID);
		customerDetails.put("CallReason","");
		customerDetails.put("CustomerLanguage","");
		customerDetails.put("PNR","");
		customerDetails.put("BookingChannel","");
		customerDetails.put("BookingClass","");
		customerDetails.put("FareFamily","");
		customerDetails.put("TicketStatus","");
		customerDetails.put("FFPNumber","");
		customerDetails.put("IATANumber","");
		customerDetails.put("StaffID","");
		customerDetails.put("CustomerTier","");
		customerDetails.put("IsIdentifiedCustomer","N");
		customerDetails.put("IsVerifiedCustomer","N");
		customerDetails.put("CustomerStatus","");
		customerDetails.put("IsBlacklisted","NO");
		customerDetails.put("Dnis","");
		customerDetails.put("CountryName","");
		customerDetails.put("IsPreferredLanguage","NO");
		customerDetails.put("ServiceType","");
		customerDetails.put("DisruptionStatus","N");
		customerDetails.put("TravelWithin48Hours","N");
		customerDetails.put("BusinessUnit","");
		customerDetails.put("CustomerCallCount","");
		
		try {
			mySession.getVariableField("HashMap","customerDetails").setValue(customerDetails);
		} catch (Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ Arrays.toString(e.getStackTrace()), mySession);

		}
		//Initialize DB Values
		CallHistory.put("VC_UCID", VC_UCID);
		CallHistory.put("VC_FLOW", App_Prop.get("FlowName"));
		CallHistory.put("VC_MARKET", "NA");
		CallHistory.put("VC_LINE_TYPE", "");
		CallHistory.put("VC_CLI_NO", CLID);
		CallHistory.put("VC_DNIS", DNIS);
		CallHistory.put("VC_LANGUAGE", "NA");
		CallHistory.put("VC_TIME_ZONE", "NA");
		CallHistory.put("DT_START_DATE", CallStartTime);
		CallHistory.put("DT_END_DATE", "NA");
		CallHistory.put("DT_ORIGIN_START_DATE", "NA");
		CallHistory.put("DT_ORIGIN_END_DATE", "NA");
		CallHistory.put("NU_CALL_DURATION", "0");
		CallHistory.put("VC_GDPR_STATUS", App_Prop.get("DefaultGDPRValue"));
		CallHistory.put("VC_SURVEY_CONSENT", App_Prop.get("DefaultSurveyConsent"));
		CallHistory.put("VC_MENU_ID", "1000|");
		CallHistory.put("VC_MENU_DESCRIPTION", "START|");
		CallHistory.put("VC_CALLER_SEGMENT", "NA");
		CallHistory.put("VC_EXIT_LOCATION", "NA");
		CallHistory.put("VC_CALL_END_TYPE", "DISCONNECT");
		CallHistory.put("VC_TRANSFER_VDN", "NA");
		CallHistory.put("VC_UUI", "NA");
		CallHistory.put("VC_WORK_REQUEST_ID", "");
		CallHistory.put("VC_SESSION_MPP_ID", MPPSessionId);
		CallHistory.put("VC_APP_SRVR_IP", appServerIP);
		CallHistory.put("VC_CALL_STATUS", "SUCCESS");
		CallHistory.put("VC_SERVICE_TYPE", "NA");
		CallHistory.put("VC_CUSTOMER_TIER", "NA");
		CallHistory.put("VC_DID", "NA");
		CallHistory.put("VC_FAIR_FAMILY", "NA");
		CallHistory.put("VC_OCDS_BUSINESS_UNIT", "NA");
		
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);
		
		//set Transaction History
		TransactionHistory.put("VC_UCID", VC_UCID);
		TransactionHistory.put("VC_CLI_NO", CLID);
		TransactionHistory.put("DT_START_DATE", "");
		TransactionHistory.put("DT_END_DATE", "");
		TransactionHistory.put("VC_FUNCTION_NAME", "");
		TransactionHistory.put("VC_HOST_URL", "");
		TransactionHistory.put("VC_HOST_REQUEST", "");
		TransactionHistory.put("VC_HOST_RESPONSE", "");
		TransactionHistory.put("VC_TRANS_STATUS", "");
		TransactionHistory.put("VC_APP_SRVR_IP", appServerIP);

		mySession.getVariableField("HashMap", "TransactionHistory").setValue(TransactionHistory);
		
		//Set Menu History Details
		LinkedHashMap<String,Object> MenuHistory = new LinkedHashMap<String,Object>();
		mySession.getVariableField("ApplicationVariable", "MenuHistory").setValue(MenuHistory);
		mySession.getVariableField("ApplicationVariable", "MenuHistorySequence").setValue(0);
		
		//SettingDefaultValues
		mySession.getVariableField("ApplicationVariable", "isMenuKeyAdded").setValue(AppConstants.False);
		mySession.getVariableField("IsOceanaSuccess").setValue(AppConstants.False);
		mySession.getVariableField("agentAvailability").setValue(0);
		mySession.getVariableField("checkEnglishAgent24").setValue(AppConstants.F);
		mySession.getVariableField("PreferredLanguage").setValue("");
		mySession.getVariableField("isEURouting").setValue(AppConstants.F);
		mySession.getVariableField("ApplicationVariable", "apiNoInput").setValue(1);
		mySession.getVariableField("ApplicationVariable", "apiNoMatch").setValue(1);
		mySession.getVariableField("isMultilingual").setValue(AppConstants.F);
		mySession.getVariableField("IsOOHPrompt").setValue(AppConstants.False);
		
		List<String> prompt = new ArrayList<String>();
		mySession.getVariableField("OOHTiming").setValue(prompt);
		
		initValues.getInitialTableStatus(mySession);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);
		
	}

}