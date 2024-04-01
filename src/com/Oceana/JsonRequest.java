package com.Oceana;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.DBOperation.DBConnection;
import com.General.ApiCallFromExtJar;
import com.General.AppConstants;
import com.avaya.sce.runtime.tracking.TraceInfo;
import com.avaya.sce.runtimecommon.ITraceInfo;
import com.avaya.sce.runtimecommon.SCESession;
import com.flow.DataNode;

public class JsonRequest {

	String className = "JsonRequest";
	@SuppressWarnings("unchecked")
	public String EWTRequest(String Lang, String Service, String LocationOne, String CustomerTier,String EWTID,SCESession mySession){

		String Request = "";
		
		HashMap<String,String> customerDetails = new HashMap<String,String>();
		customerDetails = (HashMap<String, String>) mySession.getVariableField("HashMap", "customerDetails").getObjectValue();
		
		
		if(mySession.getVariableField("connectback").getBooleanValue()){
			
			if(!customerDetails.get(AppConstants.VB_enrollment_status).toString().contains("Enroll Failed on"))
			{
				Service = mySession.getVariableField("servicemap","servicetype").getStringValue();		
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "service obtained from conxtet "+Service, mySession);
			}else {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "service obtained from xml", mySession);
			}
		}
		Request = "{\r\n" + 
				"	\"id\" : \""+EWTID+"\",\r\n" + 
				"	\"serviceMap\": {\r\n" + 
				"	\"1\": {\r\n" + 
				"		\"attributes\": {\r\n" + 
				"				\"Channel\": [\"Voice\"],\r\n" + 
				"				\"Language\": [\""+Lang+"\"],\r\n" + 
				"				\"ServiceType\": [\""+Service+"\"],\r\n" + 
				"				\"Location\": [\""+LocationOne+"\"],\r\n" + 
				"				\"CustomerTier\": [\""+CustomerTier+"\"]\r\n" + 
				"			},\r\n" + 
				"		\"priority\" : 5\r\n" + 
				"		}\r\n" + 
				"	}\r\n" + 
				"}";
		

		return Request;

	}

	public String EWTRequestTwo(String Lang,String Service,String LocationOne, String LocationTwo, String CustomerTier,String EWTID){

		String Request = "";

		if(LocationTwo.equalsIgnoreCase("NA")) {

			Request = "{\r\n" + 
					"	\"id\" : \""+EWTID+"\",\r\n" + 
					"	\"serviceMap\": {\r\n" + 
					"	\"1\": {\r\n" + 
					"		\"attributes\": {\r\n" + 
					"				\"Channel\": [\"Voice\"],\r\n" + 
					"				\"Language\": [\""+Lang+"\"],\r\n" + 
					"				\"ServiceType\": [\""+Service+"\"],\r\n" + 
					"				\"Location\": [\""+LocationOne+"\"],\r\n" + 
					"				\"CustomerTier\": [\""+CustomerTier+"\"]\r\n" + 
					"			},\r\n" + 
					"		\"priority\" : 5\r\n" + 
					"		}\r\n" + 
					"	}\r\n" + 
					"}";

		} else {

			Request = "{\r\n" + 
					"	\"id\" : \""+EWTID+"\",\r\n" + 
					"	\"serviceMap\": {\r\n" + 
					"	\"1\": {\r\n" + 
					"		\"attributes\": {\r\n" + 
					"				\"Channel\": [\"Voice\"],\r\n" + 
					"				\"Language\": [\""+Lang+"\"],\r\n" + 
					"				\"ServiceType\": [\""+Service+"\"],\r\n" + 
					"				\"Location\": [\""+LocationOne+"\"],\r\n" + 
					"				\"CustomerTier\": [\""+CustomerTier+"\"]\r\n" + 
					"			},\r\n" + 
					"		\"priority\" : 5\r\n" + 
					"		},\r\n" + 
					"	\"2\": {\r\n" + 
					"		\"attributes\": {\r\n" + 
					"				\"Channel\": [\"Voice\"],\r\n" + 
					"				\"Language\": [\""+Lang+"\"],\r\n" + 
					"				\"ServiceType\": [\""+Service+"\"],\r\n" + 
					"				\"Location\": [\""+LocationTwo+"\"],\r\n" + 
					"				\"CustomerTier\": [\""+CustomerTier+"\"]\r\n" + 
					"			},\r\n" + 
					"		\"priority\" : 5\r\n" + 
					"		}\r\n" + 
					"	}\r\n" + 
					"}";

		}

		return Request;

	}

	@SuppressWarnings({ "unchecked"})
	public String CreateOCDS(SCESession mySession) {

		String Request = "NA";

		String method = "CreateOCDS";

		String  cli = mySession.getVariableField("session","ani").getStringValue();
		
		HashMap<String,String> CallHistory = new HashMap<String,String>();
		DataNode dn = new DataNode();
		ApiCallFromExtJar apiCall = new ApiCallFromExtJar();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		try {

			HashMap<String,String> requestHashMap= null;
			HashMap<String,String> customerDetails = new HashMap<String,String>();
			customerDetails = (HashMap<String, String>) mySession.getVariableField("HashMap", "customerDetails").getObjectValue();
			boolean DBException = mySession.getVariableField("ApplicationVariable", "DbException").getBooleanValue();
			String EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "customerDetails in \t|\t CreateOCDS"+customerDetails.toString(), mySession);
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.HOUR, -24);

			Date oneHourBack = cal.getTime();
			String startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(oneHourBack);
			String Last24HrsCount = "0";
			Last24HrsCount = mySession.getVariableField("ocdsCallCount").getStringValue();

			try {
				String CLI_NO = mySession.getVariableField("session","ani").getStringValue();
				boolean cliFlag = dn.isNumericClid(CLI_NO, mySession);

				if(cliFlag) {
					apiCall.checkCountryCode(CLI_NO, mySession);

					requestHashMap = (HashMap<String, String>) mySession.getVariableField("MobileNumber").getObjectValue();

					String FinalCLINo = requestHashMap.get("PhoneNumber");

					if(!DBException) {

						DBConnection db = new DBConnection();
						List<HashMap<Object, Object>> DBResult = null;
						LinkedHashMap<String, Object> input = new LinkedHashMap<String, Object>();

						input.put("VC_CLI_NO", FinalCLINo);
						input.put("DT_START_DATE", startDate);
						input.put("DT_END_DATE", EndDate);

						DBResult = db.getResultSet("SP_GET_CALL_COUNT", input, mySession);

						Last24HrsCount = Integer.toString((Integer)DBResult.get(0).get("COUNT"));
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "JsonRequest\t|\tCreateOCDS"+DBResult.toString(), mySession);

					} else {

						Last24HrsCount = "0";
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "JsonRequest\t|\tCreateOCDS"+"DataBase is Un available", mySession);

					}

				} else {

					Last24HrsCount = "0";
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Since CLI is Non-Numeric setting 0 by Default to Call Count", mySession);

				}

			} catch(Exception e) {

				Last24HrsCount = "0";
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, "JsonRequest\t|\tCreateOCDS"+ Arrays.toString(e.getStackTrace()), mySession);

			}

			String Lang = customerDetails.get("CustomerLanguage").trim();
			
			String custId = mySession.getVariableField("customerId").getStringValue();
			String Service = mySession.getVariableField("Service").getStringValue();
			String CustomerTier = mySession.getVariableField("CustTier").getStringValue();
			String Location = mySession.getVariableField("Location").getStringValue();

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "JsonRequest\t|\tCreateOCDS"+"Language is "+Lang, mySession);

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "JsonRequest\t|\tCreateOCDS"+"customerDetails obtained"
					+ " is "+customerDetails.toString(), mySession);
			List<Object> languageList = (List<Object>) mySession.getVariableField("ApplicationVariable", "LanguageHashMap").getObjectValue();
			HashMap<String,String> languageMap = null;

			for(int i=0;i<languageList.size();i++) {

				languageMap = (HashMap<String, String>) languageList.get(i);
				if(languageMap.containsKey("VC_LANGUAGE_NAME")&&languageMap.containsKey("VC_LANGUAGE_2")) {

					if(Lang.equalsIgnoreCase(languageMap.get("VC_LANGUAGE_NAME"))) {

						Lang = languageMap.get("VC_LANGUAGE_2");
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "JsonRequest\t|\tCreateOCDS"+ "Language Received as "+Lang, mySession);

						break;
					}

				}

			}

			Request = "{"+
					"\"schema\":{"+
					"\"Locale\":\"en_us\","+
					"\"ResourceMap\":null,"+
					"\"DataCenter\":\"AWS\","+
					"\"ServiceMap\":{"+
					"\"1\":{"+
					"\"minProficiency\":null,"+
					"\"resourceCount\":null,"+
					"\"rank\":null,"+
					"		\"attributes\": {\r\n" + 
					"				\"Channel\": [\"Voice\"],\r\n" + 
					"				\"Language\": [\""+Lang+"\"],\r\n" + 
					"				\"ServiceType\": [\""+Service+"\"],\r\n" + 
					"				\"Location\": [\""+Location+"\"],\r\n" + 
					"				\"CustomerTier\": [\""+CustomerTier+"\"]\r\n" + 
					"			},\r\n" + 
					"\"priority\":5,"+
					"\"maxProficiency\":null"+   
					"}},"+
					"\"TransferServiceMap\":null,"+
					"\"CollectedDigits\":null,"+
					"\"Strategy\":\"Most Idle\","+
					"\"CustomerId\":\"" +custId+"\""+
					"},"+
					"\"groupId\":\""+custId+"\","+
					"\"contextId\":\"\","+
					"\"persistToEDM\":\"true\","+
					"\"data\":{"+
					"\"Channel\":\""+customerDetails.get("Channel")+"\","+
					"\"AppLocation\":\""+Location+"\","+
					"\"CustomerName\":\""+customerDetails.get("CustomerName")+"\","+
					"\"CustomerNumber\":\""+customerDetails.get("CustomerNumber")+"\","+
					"\"CallEndReason\":\""+customerDetails.get("CallReason")+"\","+
					"\"CustomerLanguage\":\""+Lang+"\","+
					"\"PNR\":\""+customerDetails.get("PNR")+"\","+
					"\"BookingChannel\":\""+customerDetails.get("BookingChannel")+"\","+
					"\"BookingClass\":\""+customerDetails.get("BookingClass")+"\","+
					"\"FareFamily\":\""+customerDetails.get("FareFamily")+"\","+
					"\"TicketStatus\":\""+customerDetails.get("TicketStatus")+"\","+
					"\"FFPNumber\":\""+customerDetails.get("FFPNumber")+"\","+
					"\"IATANumber\":\""+customerDetails.get("IATANumber")+"\","+
					"\"StaffID\":\""+customerDetails.get("StaffID")+"\","+
					"\"CustomerTier\":\""+CustomerTier+"\","+
					"\"IsIdentifiedCustomer\":\""+customerDetails.get("IsIdentifiedCustomer")+"\","+
					"\"IsVerifiedCustomer\":\""+customerDetails.get("IsVerifiedCustomer")+"\","+
					"\"CustomerStatus\":\""+customerDetails.get("CustomerStatus")+"\","+
					"\"IsBlacklisted\":\""+customerDetails.get("IsBlacklisted")+"\","+
					"\"Dnis\":\""+customerDetails.get("Dnis")+"\","+
					"\"CountryName\":\""+customerDetails.get("CountryName")+"\","+
					"\"IsPreferredLanguage\":\""+customerDetails.get("IsPreferredLanguage")+"\","+
					"\"ServiceType\":\""+Service+"\","+
					"\"DisruptionStatus\":\""+customerDetails.get("DisruptionStatus")+"\","+
					"\"TravelWithin48Hours\":\""+customerDetails.get("TravelWithin48Hours")+"\","+
					"\"BusinessUnit\":\""+customerDetails.get("BusinessUnit")+"\","+
					"\"CustomerCallCount\":\""+Last24HrsCount+"\","+
					
					//VB details to be stored in context store data block
					"\"VB_Status\":\""+customerDetails.get(AppConstants.VB_Status)+"\","+
					"\"VB_Status_Date\":\""+customerDetails.get(AppConstants.VB_Status_date)+"\","+
					"\"VB_Enrollment_Failed\":\""+customerDetails.get(AppConstants.VB_Enrollment_Failed)+"\","+
					"\"Verified_via_vb\":\""+customerDetails.get(AppConstants.Verified_via_vb)+"\","+
					"\"De_enrolled_caller_from_Vb\":\""+customerDetails.get(AppConstants.De_enrolled_caller_from_Vb)+"\","+
					"\"VB_enrollment_status\":\""+customerDetails.get(AppConstants.VB_enrollment_status)+"\","+
					"\"VB_enrollment_status_date\":\""+customerDetails.get(AppConstants.VB_enrollment_status_date)+"\","+
					"\"VB_verification_status\":\""+customerDetails.get(AppConstants.VB_verification_status)+"\","+
					"\"VB_verification_status_date\":\""+customerDetails.get(AppConstants.VB_verification_status_date)+"\","+
					"\"configSetName\":\""+customerDetails.get(AppConstants.configSetName)+"\","+
					"\"voiceprintTag\":\""+customerDetails.get(AppConstants.voiceprintTag)+"\","+
					"\"CountryCode\":\""+customerDetails.get(AppConstants.CountryCode)+"\","+
					"\"CustomerID\":\""+customerDetails.get(AppConstants.CustomerID)+"\","+
					"\"DoB\":\""+customerDetails.get("DoB")+"\","+
					"\"Email\":\""+customerDetails.get(AppConstants.Email)+"\","+
					
					"\"ELG\":\""+customerDetails.get("ELG")+"\","+
					
					"\"cli\":\""+cli+"\","+
					"\"ident_type\":\""+mySession.getVariableField("ident_type").getStringValue()+"\","+
					
					"\"ucid\":\""+mySession.getVariableField("session", "ucid").getStringValue()+"\""+
					
					"}"+
					"}";

		} catch(Exception e) {

			mySession.getVariableField("IsOceanaSuccess").setValue(false);
			CallHistory.put("VC_CALL_STATUS", "FAILURE");
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, "JsonRequest\t|\tCreateOCDS"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, "JsonRequest\t|\tCreateOCDS"+ Arrays.toString(e.getStackTrace()), mySession);

		}
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);
		return Request;

	}

	public String UpdateCustomerJourney(String UCID, String CustomerID, String Service, String Language, String CustTier, String CLID,SCESession mySession) {

		String Request = "NA";
		//English instead of  english or ENGLISH
		Language = Language.substring(0, 1).toUpperCase() + Language.substring(1).toLowerCase(); 
		String Location = mySession.getVariableField("Location").getStringValue();
		Request = "{"+
				"\"elementId\": \""+UCID+"\","+
				"\"customerId\": \""+CustomerID+"\","+
				"\"type\": \"SelfService\","+
				"\"topicName\": \""+Service+"\","+
				"\"status\": \"TransfertoAgent\","+
				"\"detail\": {"+
				"\"CustomerID\": \""+CustomerID+"\","+
				"\"ServiceType\": \""+Service+"\","+
				"\"Language\": \""+Language+"\","+
				"\"CustomerTier\": \""+CustTier+"\","+
				"\"Location\":\""+Location+"\","+
				"\"CallerNumber\": \""+CLID+"\""+
				"},"+
				"\"threadId\": \"threadId\""+
				"}";

		return Request;

	}
	
	@SuppressWarnings({ "unchecked"})
	public String updateOCDS(SCESession mySession) {

		String Request = "NA";

		String method = "updateOCDS";
		String  cli = mySession.getVariableField("session","ani").getStringValue();

		HashMap<String,String> CallHistory = new HashMap<String,String>();
		DataNode dn = new DataNode();
		ApiCallFromExtJar apiCall = new ApiCallFromExtJar();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		try {

			HashMap<String,String> requestHashMap= null;
			HashMap<String,String> customerDetails = new HashMap<String,String>();
			customerDetails = (HashMap<String, String>) mySession.getVariableField("HashMap", "customerDetails").getObjectValue();
			boolean DBException = mySession.getVariableField("ApplicationVariable", "DbException").getBooleanValue();
			String EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "customerDetails in \t|\t updateOCDS"+customerDetails.toString(), mySession);
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.HOUR, -24);

			Date oneHourBack = cal.getTime();
			String startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(oneHourBack);
			String Last24HrsCount = "0";
			Last24HrsCount = mySession.getVariableField("ocdsCallCount").getStringValue();

			try {
				String CLI_NO = mySession.getVariableField("session","ani").getStringValue();
				boolean cliFlag = dn.isNumericClid(CLI_NO, mySession);

				if(cliFlag) {
					apiCall.checkCountryCode(CLI_NO, mySession);

					requestHashMap = (HashMap<String, String>) mySession.getVariableField("MobileNumber").getObjectValue();

					String FinalCLINo = requestHashMap.get("PhoneNumber");

					if(!DBException) {

						DBConnection db = new DBConnection();
						List<HashMap<Object, Object>> DBResult = null;
						LinkedHashMap<String, Object> input = new LinkedHashMap<String, Object>();

						input.put("VC_CLI_NO", FinalCLINo);
						input.put("DT_START_DATE", startDate);
						input.put("DT_END_DATE", EndDate);

						DBResult = db.getResultSet("SP_GET_CALL_COUNT", input, mySession);

						Last24HrsCount = Integer.toString((Integer)DBResult.get(0).get("COUNT"));
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "JsonRequest\t|\tupdateOCDS"+DBResult.toString(), mySession);

					} else {

						Last24HrsCount = "0";
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "JsonRequest\t|\tupdateOCDS"+"DataBase is Un available", mySession);

					}

				} else {

					Last24HrsCount = "0";
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Since CLI is Non-Numeric setting 0 by Default to Call Count", mySession);

				}

			} catch(Exception e) {

				Last24HrsCount = "0";
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, "JsonRequest\t|\tupdateOCDS"+ Arrays.toString(e.getStackTrace()), mySession);

			}

			String Lang = customerDetails.get("CustomerLanguage").trim();
			
			//String custId = mySession.getVariableField("customerId").getStringValue();
			String Service = mySession.getVariableField("Service").getStringValue();
			String CustomerTier = mySession.getVariableField("CustTier").getStringValue();
			String Location = mySession.getVariableField("Location").getStringValue();

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "JsonRequest\t|\tupdateOCDS"+"Language is "+Lang, mySession);

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "JsonRequest\t|\tupdateOCDS"+"customerDetails obtained"
					+ " is "+customerDetails.toString(), mySession);
			List<Object> languageList = (List<Object>) mySession.getVariableField("ApplicationVariable", "LanguageHashMap").getObjectValue();
			HashMap<String,String> languageMap = null;

			for(int i=0;i<languageList.size();i++) {

				languageMap = (HashMap<String, String>) languageList.get(i);
				if(languageMap.containsKey("VC_LANGUAGE_NAME")&&languageMap.containsKey("VC_LANGUAGE_2")) {

					if(Lang.equalsIgnoreCase(languageMap.get("VC_LANGUAGE_NAME"))) {

						Lang = languageMap.get("VC_LANGUAGE_2");
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "JsonRequest\t|\tupdateOCDS"+ "Language Received as "+Lang, mySession);

						break;
					}

				}

			}

			if("".equalsIgnoreCase(Lang.trim()) || "NA".equalsIgnoreCase(Lang)|| Lang==null) {
				Lang = CallHistory.get("VC_LANGUAGE");
				if("".equalsIgnoreCase(Lang.trim()) || "NA".equalsIgnoreCase(Lang)|| Lang==null) {
					Lang = "English";
				}
				for(int i=0;i<languageList.size();i++) {

					languageMap = (HashMap<String, String>) languageList.get(i);
					if(languageMap.containsKey("VC_LANGUAGE_NAME")&&languageMap.containsKey("VC_LANGUAGE_2")) {

						if(Lang.equalsIgnoreCase(languageMap.get("VC_LANGUAGE_NAME"))) {

							Lang = languageMap.get("VC_LANGUAGE_2");
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "Language Received as "+Lang, mySession);

							break;
						}

					}

				}
			}
			
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "enrollment status is "+customerDetails.get(AppConstants.VB_enrollment_status).toString(), mySession);
			if(!customerDetails.get(AppConstants.VB_enrollment_status).toString().contains("Enroll Failed on"))
			{
				Service = mySession.getVariableField("servicemap","servicetype").getStringValue();
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "service obtained from context data for update ocds ", mySession);
			}else {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "service obtained from xml for update ocds ", mySession);
			}
			
				Request = "{"+
						
					"\"ServiceMap\":{"+
					"\"1\":{"+
					"\"minProficiency\":null,"+
					"\"resourceCount\":null,"+
					"\"rank\":null,"+
					"		\"attributes\": {\r\n" + 
					"				\"Channel\": [\"Voice\"],\r\n" + 
					"				\"Language\": [\""+Lang+"\"],\r\n" + 
					"				\"ServiceType\": [\""+Service+"\"],\r\n" + 
					"				\"Location\": [\""+Location+"\"],\r\n" + 
					"				\"CustomerTier\": [\""+CustomerTier+"\"]\r\n" + 
					"			},\r\n" + 
					"\"priority\":5,"+
					"\"maxProficiency\":null"+   
					"}},"+
					
					"\"data\":{"+
					"\"Channel\":\""+customerDetails.get("Channel")+"\","+
					"\"AppLocation\":\""+Location+"\","+
					"\"CustomerName\":\""+customerDetails.get("CustomerName")+"\","+
					"\"CustomerNumber\":\""+customerDetails.get("CustomerNumber")+"\","+
					"\"CallEndReason\":\""+customerDetails.get("CallReason")+"\","+
					"\"CustomerLanguage\":\""+Lang+"\","+
					"\"PNR\":\""+customerDetails.get("PNR")+"\","+
					"\"BookingChannel\":\""+customerDetails.get("BookingChannel")+"\","+
					"\"BookingClass\":\""+customerDetails.get("BookingClass")+"\","+
					"\"FareFamily\":\""+customerDetails.get("FareFamily")+"\","+
					"\"TicketStatus\":\""+customerDetails.get("TicketStatus")+"\","+
					"\"FFPNumber\":\""+customerDetails.get("FFPNumber")+"\","+
					"\"IATANumber\":\""+customerDetails.get("IATANumber")+"\","+
					"\"StaffID\":\""+customerDetails.get("StaffID")+"\","+
					"\"CustomerTier\":\""+CustomerTier+"\","+
					"\"IsIdentifiedCustomer\":\""+customerDetails.get("IsIdentifiedCustomer")+"\","+
					"\"IsVerifiedCustomer\":\""+customerDetails.get("IsVerifiedCustomer")+"\","+
					"\"CustomerStatus\":\""+customerDetails.get("CustomerStatus")+"\","+
					"\"IsBlacklisted\":\""+customerDetails.get("IsBlacklisted")+"\","+
					"\"Dnis\":\""+customerDetails.get("Dnis")+"\","+
					"\"CountryName\":\""+customerDetails.get("CountryName")+"\","+
					"\"IsPreferredLanguage\":\""+customerDetails.get("IsPreferredLanguage")+"\","+
					"\"ServiceType\":\""+Service+"\","+
					"\"DisruptionStatus\":\""+customerDetails.get("DisruptionStatus")+"\","+
					"\"TravelWithin48Hours\":\""+customerDetails.get("TravelWithin48Hours")+"\","+
					"\"BusinessUnit\":\""+customerDetails.get("BusinessUnit")+"\","+
					"\"CustomerCallCount\":\""+Last24HrsCount+"\","+
					
					//VB details to be stored in context store data block
					"\"VB_Status\":\""+customerDetails.get(AppConstants.VB_Status)+"\","+
					"\"VB_Status_Date\":\""+customerDetails.get(AppConstants.VB_Status_date)+"\","+
					"\"VB_Enrollment_Failed\":\""+customerDetails.get(AppConstants.VB_Enrollment_Failed)+"\","+
					"\"Verified_via_vb\":\""+customerDetails.get(AppConstants.Verified_via_vb)+"\","+
					"\"De_enrolled_caller_from_Vb\":\""+customerDetails.get(AppConstants.De_enrolled_caller_from_Vb)+"\","+
					"\"VB_enrollment_status\":\""+customerDetails.get(AppConstants.VB_enrollment_status)+"\","+
					"\"VB_enrollment_status_date\":\""+customerDetails.get(AppConstants.VB_enrollment_status_date)+"\","+
					"\"VB_verification_status\":\""+customerDetails.get(AppConstants.VB_verification_status)+"\","+
					"\"VB_verification_status_date\":\""+customerDetails.get(AppConstants.VB_verification_status_date)+"\","+
					"\"configSetName\":\""+customerDetails.get(AppConstants.configSetName)+"\","+
					"\"voiceprintTag\":\""+customerDetails.get(AppConstants.voiceprintTag)+"\","+
					"\"CountryCode\":\""+customerDetails.get(AppConstants.CountryCode)+"\","+
					//"\"CustomerID\":\""+customerDetails.get(AppConstants.CustomerID)+"\","+
					
					"\"ELG\":\""+customerDetails.get("ELG")+"\","+
					
					"\"Email\":\""+customerDetails.get(AppConstants.Email)+"\","+
					"\"DoB\":\""+customerDetails.get("DoB")+"\","+
					"\"cli\":\""+cli+"\","+
					"\"ident_type\":\""+mySession.getVariableField("ident_type").getStringValue()+"\","+
					
					"\"ucid\":\""+mySession.getVariableField("session", "ucid").getStringValue()+"\""+
					
					"}"+
					"}";
		} catch(Exception e) {

			mySession.getVariableField("IsOceanaSuccess").setValue(false);
			CallHistory.put("VC_CALL_STATUS", "FAILURE");
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, "JsonRequest\t|\tupdateOCDS"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, "JsonRequest\t|\tupdateOCDS"+ Arrays.toString(e.getStackTrace()), mySession);

		}
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);
		return Request;

	}

}
