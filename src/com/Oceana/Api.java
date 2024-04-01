package com.Oceana;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import com.General.AppConstants;
import com.General.LoadApplicationProperties;
import com.avaya.sce.runtime.tracking.TraceInfo;
import com.avaya.sce.runtimecommon.ITraceInfo;
import com.avaya.sce.runtimecommon.SCESession;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flow.DataNode;
import com.flow.Set;

public class Api {

	String className = "Api";

	@SuppressWarnings("unchecked")
	private boolean getCustomerID(SCESession mySession) {

		String method = "getCustomerID";
		

		ApiConnection api = new ApiConnection();
		DataNode dataNode = new DataNode();
		HashMap<String,Object> responseValues = null;

		boolean responseStatus = false;

		String CLID = mySession.getVariableField("session", "ani").getStringValue();
		String GetCustomerIDURL = LoadApplicationProperties.getProperty("GetCustomerIDUrl", mySession);
		String sanitizedJson = "";
		String CustomerID = "";
		String StartDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
		String EndDate = "";

		int connectionTimeout = Integer.parseInt(LoadApplicationProperties.getProperty("GetCustomerIDConnnectionTimeOut", mySession));
		int readTimeout = Integer.parseInt(LoadApplicationProperties.getProperty("GetCustomerIDReadTimeout", mySession));
		int responseCode = 0;

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"Final getCustomerID URL :"+ GetCustomerIDURL+CLID, mySession);

		try {
			
			boolean cliFlag = dataNode.isNumericClid(CLID, mySession);
			
			if(cliFlag) {

			String URL = GetCustomerIDURL+CLID;

			responseValues = api.httpGetResponse(method, URL, connectionTimeout, readTimeout, mySession);

			EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

			if(responseValues!=null) {

				responseCode = (int) responseValues.get(method+"Code");
				sanitizedJson = responseValues.get(method+"Response").toString();

				if (responseCode == 200) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"GetCustomer ID Response : "+ sanitizedJson, mySession);
					JSONObject responseObject = new JSONObject(sanitizedJson);

					if(responseObject.has("customerId")) {
						CustomerID = responseObject.get("customerId").toString();
					}

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"Customer ID is : "+ CustomerID, mySession);
					mySession.getVariableField("CustomerID").setValue(CustomerID);
					responseStatus=true;

				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR," Exception on getting Customer ID : "+ responseCode, mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR," Setting ANI value as Customer ID : ", mySession);
					mySession.getVariableField("CustomerID").setValue(CLID.replace("+", "").replace(" ", "").replace("-", ""));

				}

			} else {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, " Response is not received " , mySession);
			}

			try {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"InsertingTransactionHistory", mySession);

				LinkedHashMap<String,Object> TransactionHistory = new LinkedHashMap<String,Object>();

				TransactionHistory = (LinkedHashMap<String, Object>) mySession.getVariableField("HashMap", "TransactionHistory").getObjectValue();

				if(EndDate.equalsIgnoreCase("")) {

					EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

				}

				TransactionHistory.put("DT_START_DATE", StartDate);
				TransactionHistory.put("DT_END_DATE", EndDate);
				TransactionHistory.put("VC_FUNCTION_NAME", "GET_CUSTOMER_ID");
				TransactionHistory.put("VC_HOST_URL", GetCustomerIDURL);
				TransactionHistory.put("VC_HOST_REQUEST", CLID);
				TransactionHistory.put("VC_HOST_RESPONSE", sanitizedJson);
				TransactionHistory.put("VC_TRANS_STATUS", responseCode);

				mySession.getVariableField("HashMap", "TransactionHistory").setValue(TransactionHistory);

				dataNode.setTransactionHistory(mySession);

			} catch(Exception e) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

			}
		} else {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"GetCustomerID API not performed since CLI is Non-Numeric", mySession);
		}

		} catch (Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR," Exception on getting Customer ID "+e.getMessage(), mySession);
			mySession.getVariableField("CustomerID").setValue(CLID.replace("+", "").replace(" ", "").replace("-", ""));

		}

		return responseStatus;

	}

	@SuppressWarnings("unchecked")
	private boolean checkEWT(SCESession mySession) {

		String method = "checkEWT";
		

		boolean responseStatus = false;

		DataNode dataNode = new DataNode();
		Set set = new Set();
		HashMap<String,Object> responseValues = null;

		String Request = "";
		String sanitizedJson = "";
		String StartDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
		String EndDate = "";
		String URL = "";
		int responseCode = 0;
		String Location = "";
		String EWT="";

		try {

			mySession.getVariableField("IsOceanaSuccess").setValue(true);

			HashMap<String, String> DnisHashMap =  (HashMap<String, String>) mySession.getVariableField("ApplicationVariable", "DBAppValues").getObjectValue();
			String marketId = DnisHashMap.get("NU_MARKET_ID");
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, " checkEWT | marketId : " + marketId , mySession);

			try {

				String LocationOne = set.Location(mySession);
				String isEuRouting = mySession.getVariableField("isEURouting").getStringValue();

				if(isEuRouting.equalsIgnoreCase(AppConstants.T)) {
					Location = LocationOne+"_ONLY";
				} else {
					Location = LocationOne;
				}

				try {

					//InitialteVariable
					ApiConnection api = new ApiConnection();

					URL = LoadApplicationProperties.getProperty("OceanaUrl", mySession)+LoadApplicationProperties.getProperty("EWT", mySession);
					int connectionTimeout = Integer.parseInt(LoadApplicationProperties.getProperty("checkEWTConnnectionTimeOut", mySession));
					int readTimeout = Integer.parseInt(LoadApplicationProperties.getProperty("checkEWTReadTimeout", mySession));

					HashMap<String,String> CallHistory = new HashMap<String,String>();

					CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

					String Lang = CallHistory.get("VC_LANGUAGE");
					String EWTID = CallHistory.get("VC_UCID");
					String Service = mySession.getVariableField("Service").getStringValue();
					HashMap<String,String> customerDetails = (HashMap<String, String>) mySession.getVariableField("HashMap", "customerDetails").getObjectValue();
					String CustTier = customerDetails.get("MemberTier")!=null ? customerDetails.get("MemberTier").toString():"NA";
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "cust tier obtained from cust is "+CustTier, mySession);
					if("NA".equalsIgnoreCase(CustTier)|| "".equalsIgnoreCase(CustTier)) {
						CustTier = mySession.getVariableField("CustTier").getStringValue();
					}else {
						mySession.getVariableField("CustTier").setValue(customerDetails.get("MemberTier"));
						
					}
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "member tier set is "+mySession.getVariableField("CustTier").getStringValue(), mySession);

					HashMap<String, Object> jsonResposeMap = null;

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "Language is "+Lang, mySession);

					List<Object> languageList = (List<Object>) mySession.getVariableField("ApplicationVariable", "LanguageHashMap").getObjectValue();
					HashMap<String,String> languageMap = null;

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

					JsonRequest Json = new JsonRequest();
					Request = Json.EWTRequest(Lang, Service, Location,CustTier,EWTID,mySession);

					responseValues = api.httpPostResponse(method, URL, connectionTimeout, readTimeout, Request, mySession);

					EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

					if(responseValues!=null) {

						responseCode = (int) responseValues.get(method+"Code");
						sanitizedJson = responseValues.get(method+"Response").toString();

						if(responseCode == 200){

							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, " EWT | RESPONSE : " + sanitizedJson , mySession);

							jsonResposeMap = new ObjectMapper().readValue(sanitizedJson, HashMap.class);

							String count1 = "0";

							try {
								count1 = (String) ((HashMap<String, Object>)((HashMap<String, Object>)((HashMap<String, Object>) jsonResposeMap.get("serviceMetricsResponseMap")).get("1")).get("metrics")).get("ResourceStaffedCount");
								EWT = (String) ((HashMap<String, Object>)((HashMap<String, Object>)((HashMap<String, Object>) jsonResposeMap.get("serviceMetricsResponseMap")).get("1")).get("metrics")).get("EWT");
								mySession.getVariableField("ApiEwt").setValue(EWT);
							} catch(Exception e) {
								count1 = "0";
								TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, "Error While Getting  ResourceStaffedCount : "+Arrays.toString(e.getStackTrace()), mySession);
							}

							Integer staffCount1 = Integer.parseInt(count1);

							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG, "CalculateEWT - staffCount | " +  staffCount1, mySession);

							if(staffCount1>0) {
								mySession.getVariableField("agentAvailability").setValue(1);
							} else {
								Location = LocationOne;
							}

							responseStatus=true;

						} else {

							mySession.getVariableField("agentAvailability").setValue(1);
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, "Error in CalculateEWT : "+ responseCode, mySession);

						}

					} else {
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, " Response is not received " , mySession);
					}

					try {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"InsertingTransactionHistory", mySession);

						LinkedHashMap<String,Object> TransactionHistory = new LinkedHashMap<String,Object>();

						TransactionHistory = (LinkedHashMap<String, Object>) mySession.getVariableField("HashMap", "TransactionHistory").getObjectValue();

						if(EndDate.equalsIgnoreCase("")) {

							EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

						}

						TransactionHistory.put("DT_START_DATE", StartDate);
						TransactionHistory.put("DT_END_DATE", EndDate);
						TransactionHistory.put("VC_FUNCTION_NAME", "CHECK_EWT");
						TransactionHistory.put("VC_HOST_URL", URL);
						TransactionHistory.put("VC_HOST_REQUEST", Request);
						TransactionHistory.put("VC_HOST_RESPONSE", sanitizedJson);
						TransactionHistory.put("VC_TRANS_STATUS", responseCode);

						mySession.getVariableField("HashMap", "TransactionHistory").setValue(TransactionHistory);

						dataNode.setTransactionHistory(mySession);

					} catch(Exception e) {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

					}

				} catch (Exception e) {

					mySession.getVariableField("agentAvailability").setValue(1);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR," Exception on check EWT "+e.getMessage(), mySession);

				}

			} catch(Exception e) {

				mySession.getVariableField("agentAvailability").setValue(1);
				mySession.getVariableField("IsOceanaSuccess").setValue(false);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR," Exception on creating context "+e.getMessage(), mySession);

			}

			mySession.getVariableField("Location").setValue(Location);

		} catch(Exception e) {

			mySession.getVariableField("agentAvailability").setValue(1);
			mySession.getVariableField("IsOceanaSuccess").setValue(false);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

		}

		return responseStatus;

	}

	private boolean checkLocation(SCESession mySession) {

		boolean responseStatus = false;

		try {

			Set set = new Set();
			mySession.getVariableField("IsOceanaSuccess").setValue(true);

			String LocationOne = set.Location(mySession);

			mySession.getVariableField("agentAvailability").setValue(1);

			mySession.getVariableField("Location").setValue(LocationOne);

		} catch(Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR," Exception on creating context "+e.getMessage(), mySession);

		}

		return responseStatus;

	}

	@SuppressWarnings("unchecked")
	private boolean createOcds(SCESession mySession) {

		String method = "createOcds";
		

		ApiConnection api = new ApiConnection();
		DataNode dataNode = new DataNode();
		HashMap<String,Object> responseValues = null;

		boolean responseStatus = false;

		String URL = LoadApplicationProperties.getProperty("OceanaUrl", mySession)+LoadApplicationProperties.getProperty("createocds", mySession);
		int connectionTimeout = Integer.parseInt(LoadApplicationProperties.getProperty("OcdsConnnectionTimeOut", mySession));
		int readTimeout = Integer.parseInt(LoadApplicationProperties.getProperty("OcdsReadTimeout", mySession));
		int responseCode = 0;

		HashMap<String,String> CallHistory = new HashMap<String,String>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		//Service Attributes
		String Request = "";
		String sanitizedJson = "";
		String ContextId = "NA";
		String StartDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
		String EndDate = "";
		//String conectbackContextId = mySession.getVariableField("contextid").getStringValue();
		JsonRequest Json = new JsonRequest();
		if("true".equalsIgnoreCase(mySession.getVariableField("connectback").getStringValue())) {
			//HashMap<String,String> customerDetails = (HashMap<String, String>) mySession.getVariableField("HashMap", "customerDetails").getObjectValue();
			
			method = "updateOcds";
			URL = LoadApplicationProperties.getProperty("OceanaUrl", mySession) + LoadApplicationProperties.getProperty("updatecontext", mySession)
			+"/"+mySession.getProperty("contextId").toString();
			connectionTimeout = Integer.parseInt(LoadApplicationProperties.getProperty("updateOcdsConnnectionTimeOut", mySession));
			readTimeout = Integer.parseInt(LoadApplicationProperties.getProperty("updateOcdsReadTimeout", mySession));
			Request = Json.updateOCDS(mySession);
			responseCode = 0;

		}else {
			Request = Json.CreateOCDS(mySession);
		}
		

		boolean ocdsStatus = mySession.getVariableField("IsOceanaSuccess").getBooleanValue();

		if(ocdsStatus) {

			try {

				if("true".equalsIgnoreCase(mySession.getVariableField("connectback").getStringValue())) {
					responseValues = api.httpPutResponse(method, URL, connectionTimeout, readTimeout, Request, mySession);
				}else{
					responseValues = api.httpPostResponse(method, URL, connectionTimeout, readTimeout, Request, mySession);
				}
				

				EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
				
				if(responseValues!=null) {
					
					responseCode = (int) responseValues.get(method+"Code");
					sanitizedJson = responseValues.get(method+"Response").toString();

					if(responseCode == 200){

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, " OCDS | RESPONSE : " + sanitizedJson , mySession);
						if("true".equalsIgnoreCase(mySession.getVariableField("connectback").getStringValue())) {
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, " OCDS | RESPONSE FOR UPDATE OCDS : " + sanitizedJson , mySession);
							ContextId = mySession.getProperty("contextId").toString();
							String UUI = ContextId + ",VO,T";
							mySession.getVariableField("UUI").setValue(UUI);
							CallHistory.put("VC_UUI", UUI);
							CallHistory.put("VC_WORK_REQUEST_ID", ContextId);
						}else {
						JSONObject responseObject = new JSONObject(sanitizedJson);
						if(responseObject.has("data")){

							JSONObject data = new JSONObject(responseObject.get("data").toString());
							if(data.has("contextId")){
								
								ContextId = data.get("contextId").toString();

							}
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "ContextId is : "+ ContextId, mySession);

						}
						String UUI = ContextId + ",VO,N";
						

						mySession.getVariableField("UUI").setValue(UUI);
						CallHistory.put("VC_UUI", UUI);
						CallHistory.put("VC_WORK_REQUEST_ID", ContextId);
						}
						responseStatus=true;
						mySession.getVariableField("IsOceanaSuccess").setValue(true);

					} else {
						mySession.getVariableField("IsOceanaSuccess").setValue(false);
						CallHistory.put("VC_CALL_STATUS", "FAILURE");
						if("true".equalsIgnoreCase(mySession.getVariableField("connectback").getStringValue())) {
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, "Error in UpdateOCDS : "+ responseCode, mySession);
						}else {
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, "Error in CreateOCDS : "+ responseCode, mySession);	
						}
					}

				} else {
					mySession.getVariableField("IsOceanaSuccess").setValue(false);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, " Response is not received " , mySession);
				}

				try {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"InsertingTransactionHistory", mySession);

					LinkedHashMap<String,Object> TransactionHistory = new LinkedHashMap<String,Object>();

					TransactionHistory = (LinkedHashMap<String, Object>) mySession.getVariableField("HashMap", "TransactionHistory").getObjectValue();

					if(EndDate.equalsIgnoreCase("")) {

						EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

					}

					TransactionHistory.put("DT_START_DATE", StartDate);
					TransactionHistory.put("DT_END_DATE", EndDate);
					if("true".equalsIgnoreCase(mySession.getVariableField("connectback").getStringValue())) {
						TransactionHistory.put("VC_FUNCTION_NAME", "UPDATE_OCDS");
					}else {
						TransactionHistory.put("VC_FUNCTION_NAME", "CREATE_OCDS");	
					}
					
					TransactionHistory.put("VC_HOST_URL", URL);
					TransactionHistory.put("VC_HOST_REQUEST", Request);
					TransactionHistory.put("VC_HOST_RESPONSE", sanitizedJson);
					TransactionHistory.put("VC_TRANS_STATUS", responseCode);

					mySession.getVariableField("HashMap", "TransactionHistory").setValue(TransactionHistory);

					dataNode.setTransactionHistory(mySession);

				} catch(Exception e) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

				}

			} catch (Exception e) {
				mySession.getVariableField("IsOceanaSuccess").setValue(false);
				CallHistory.put("VC_CALL_STATUS", "FAILURE");
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR," Exception on creating context "+e.getMessage(), mySession);
			}


		}

		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		return responseStatus;

	}
	
	@SuppressWarnings("unchecked")
	private boolean updateOcds(SCESession mySession) {

		String method = "updateOcds";
		

		ApiConnection api = new ApiConnection();
		DataNode dataNode = new DataNode();
		HashMap<String,Object> responseValues = null;

		boolean responseStatus = false;

		String URL = LoadApplicationProperties.getProperty("updateOcdsUrl", mySession);
		int connectionTimeout = Integer.parseInt(LoadApplicationProperties.getProperty("updateOcdsConnnectionTimeOut", mySession));
		int readTimeout = Integer.parseInt(LoadApplicationProperties.getProperty("updateOcdsReadTimeout", mySession));
		int responseCode = 0;

		HashMap<String,String> CallHistory = new HashMap<String,String>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		//Service Attributes
		String Request = "";
		String sanitizedJson = "";
		String ContextId = "NA";
		String StartDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
		String EndDate = "";

		JsonRequest Json = new JsonRequest();
		Request = Json.updateOCDS(mySession);

		boolean ocdsStatus = mySession.getVariableField("IsOceanaSuccess").getBooleanValue();

		if(ocdsStatus) {

			try {

				responseValues = api.httpPostResponse(method, URL, connectionTimeout, readTimeout, Request, mySession);

				EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
				
				if(responseValues!=null) {
					
					responseCode = (int) responseValues.get(method+"Code");
					sanitizedJson = responseValues.get(method+"Response").toString();

					if(responseCode == 200){

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, " OCDS | RESPONSE : " + sanitizedJson , mySession);
						JSONObject responseObject = new JSONObject(sanitizedJson);
						if(responseObject.has("data")){

							JSONObject data = new JSONObject(responseObject.get("data").toString());
							if(data.has("contextId")){
								
								ContextId = data.get("contextId").toString();

							}
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "ContextId is : "+ ContextId, mySession);

						}
						String UUI = ContextId + ",VO,N";

						mySession.getVariableField("UUI").setValue(UUI);
						CallHistory.put("VC_UUI", UUI);
						CallHistory.put("VC_WORK_REQUEST_ID", ContextId);
						responseStatus=true;
						mySession.getVariableField("IsOceanaSuccess").setValue(true);

					} else {
						mySession.getVariableField("IsOceanaSuccess").setValue(false);
						CallHistory.put("VC_CALL_STATUS", "FAILURE");
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, "Error in UpdateOCDS : "+ responseCode, mySession);
					}

				} else {
					mySession.getVariableField("IsOceanaSuccess").setValue(false);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, " Response is not received " , mySession);
				}

				try {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"InsertingTransactionHistory", mySession);

					LinkedHashMap<String,Object> TransactionHistory = new LinkedHashMap<String,Object>();

					TransactionHistory = (LinkedHashMap<String, Object>) mySession.getVariableField("HashMap", "TransactionHistory").getObjectValue();

					if(EndDate.equalsIgnoreCase("")) {

						EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

					}

					TransactionHistory.put("DT_START_DATE", StartDate);
					TransactionHistory.put("DT_END_DATE", EndDate);
					TransactionHistory.put("VC_FUNCTION_NAME", "CREATE_OCDS");
					TransactionHistory.put("VC_HOST_URL", URL);
					TransactionHistory.put("VC_HOST_REQUEST", Request);
					TransactionHistory.put("VC_HOST_RESPONSE", sanitizedJson);
					TransactionHistory.put("VC_TRANS_STATUS", responseCode);

					mySession.getVariableField("HashMap", "TransactionHistory").setValue(TransactionHistory);

					dataNode.setTransactionHistory(mySession);

				} catch(Exception e) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

				}

			} catch (Exception e) {
				mySession.getVariableField("IsOceanaSuccess").setValue(false);
				CallHistory.put("VC_CALL_STATUS", "FAILURE");
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR," Exception on creating context "+e.getMessage(), mySession);
			}


		}

		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		return responseStatus;

	}


	@SuppressWarnings("unchecked")
	private boolean updateCustomerJourney(SCESession mySession) {

		String method = "updateCustomerJourney";
		

		boolean responseStatus = false;

		//InitialteVariable
		ApiConnection api = new ApiConnection();
		DataNode dataNode = new DataNode();
		HashMap<String,Object> responseValues = null;

		String URL = LoadApplicationProperties.getProperty("updateCustomerJourneyUrl", mySession);
		String StartDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
		String EndDate = "";

		int responseCode = 0;
		int connectionTimeout = Integer.parseInt(LoadApplicationProperties.getProperty("updateCustomerJourneyConnnectionTimeOut", mySession));
		int readTimeout = Integer.parseInt(LoadApplicationProperties.getProperty("updateCustomerJourneyReadTimeout", mySession));

		HashMap<String,String> CallHistory = new HashMap<String,String>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String Request = "";
		String Response = "";
		String sanitizedJson = "";
		String UCID = CallHistory.get("VC_UCID");
		String CLID = mySession.getVariableField("session", "ani").getStringValue();
		String CustomerID = mySession.getVariableField("customerId").getStringValue();
		String Lang = CallHistory.get("VC_LANGUAGE");
		String Service = mySession.getVariableField("Service").getStringValue();
		String CustTier = mySession.getVariableField("CustTier").getStringValue();

		try {
			JsonRequest Json = new JsonRequest();

			Request = Json.UpdateCustomerJourney(UCID, CustomerID, Service, Lang, CustTier, CLID,mySession);

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, "Create Customer Journey Request : " + Request, mySession);

			responseValues = api.httpPostResponse(method, URL, connectionTimeout, readTimeout, Request, mySession);

			EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

			if(responseValues!=null) {

				responseCode = (int) responseValues.get(method+"Code");
				sanitizedJson = responseValues.get(method+"Response").toString();

				if(responseCode == 200){

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, " CustomerJourney | RESPONSE : " + sanitizedJson , mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, "CustomerJourney Web-service Success", mySession);

				} else {
					mySession.getVariableField("Error").setValue(true);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, "Error in CustomerJourney : "+ responseCode, mySession);
				}
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG, "CustomerJourney - Status : " + responseCode, mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG, "CustomerJourney - ResponseBody : " + Response, mySession);

			} else {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, " Response is not received " , mySession);
			}

			try {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"InsertingTransactionHistory", mySession);

				LinkedHashMap<String,Object> TransactionHistory = new LinkedHashMap<String,Object>();

				TransactionHistory = (LinkedHashMap<String, Object>) mySession.getVariableField("HashMap", "TransactionHistory").getObjectValue();

				TransactionHistory = (LinkedHashMap<String, Object>) mySession.getVariableField("HashMap", "TransactionHistory").getObjectValue();

				if(EndDate.equalsIgnoreCase("")) {

					EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

				}

				TransactionHistory.put("DT_START_DATE", StartDate);
				TransactionHistory.put("DT_END_DATE", EndDate);
				TransactionHistory.put("VC_FUNCTION_NAME", "UPDATE_JOURNEY");
				TransactionHistory.put("VC_HOST_URL", URL);
				TransactionHistory.put("VC_HOST_REQUEST", Request);
				TransactionHistory.put("VC_HOST_RESPONSE", sanitizedJson);
				TransactionHistory.put("VC_TRANS_STATUS", responseCode);

				mySession.getVariableField("HashMap", "TransactionHistory").setValue(TransactionHistory);

				dataNode.setTransactionHistory(mySession);

			} catch(Exception e) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

			}



		} catch (Exception e) {

			mySession.getVariableField("Error").setValue(true);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR," Exception on creating context "+e.getMessage(), mySession);

		}

		return responseStatus;

	}

	public void callGetCustomerId(SCESession mySession) {

		String flag = "false";
		String MethodName = "callGetCustomerId";

		try {
			flag = String.valueOf(getCustomerID(mySession));
		} catch(Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | "+MethodName+" | "+Arrays.toString(e.getStackTrace()), mySession);
		}
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+MethodName+" | "+flag, mySession);

		setApiFlag(mySession,flag);
	}

	public void callCheckEWT(SCESession mySession) {

		String flag = "false";
		String MethodName = "callCheckEWT";

		try {

			flag = String.valueOf(checkEWT(mySession));


		} catch(Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | "+MethodName+" | "+Arrays.toString(e.getStackTrace()), mySession);
		}
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+MethodName+" | "+flag, mySession);

		setApiFlag(mySession,flag);

	}

	public void setLocation(SCESession mySession) {

		String flag = "false";
		String MethodName = "setLocation";

		try {

			flag = String.valueOf(checkLocation(mySession));


		} catch(Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | "+MethodName+" | "+Arrays.toString(e.getStackTrace()), mySession);
		}
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+MethodName+" | "+flag, mySession);

		setApiFlag(mySession,flag);

	}

	public void callCreateOCDS(SCESession mySession) {

		String flag = "false";
		String MethodName = "callCreateOCDS";

		try {

			flag = String.valueOf(createOcds(mySession));

		} catch(Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | "+MethodName+" | "+Arrays.toString(e.getStackTrace()), mySession);
		}
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+MethodName+" | "+flag, mySession);

		setApiFlag(mySession,flag);

	}
	
	public void callupdateOCDS(SCESession mySession) {

		String flag = "false";
		String MethodName = "callupdateOCDS";

		try {

			flag = String.valueOf(updateOcds(mySession));

		} catch(Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | "+MethodName+" | "+Arrays.toString(e.getStackTrace()), mySession);
		}
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+MethodName+" | "+flag, mySession);

		setApiFlag(mySession,flag);

	}

	public void callUpdateCustomerJourney(SCESession mySession) {

		String flag = "false";
		String MethodName = "callUpdateCustomerJourney";

		try {

			flag = String.valueOf(updateCustomerJourney(mySession));

		} catch(Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | "+MethodName+" | "+Arrays.toString(e.getStackTrace()), mySession);
		}
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+MethodName+" | "+flag, mySession);

		setApiFlag(mySession,flag);

	}

	@SuppressWarnings("unchecked")
	private void setApiFlag(SCESession mySession,String inputFlag) {

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className, mySession);

		try {

			HashMap<String,Object> flow= new HashMap<String,Object>();
			HashMap<String,Object> flowvalues = new HashMap<String,Object>();

			HashMap<String,String> CallHistory = new HashMap<String,String>();

			CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

			String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

			String Flag = "";
			String NextNode = "";
			String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

			flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

			if(flow.containsKey(NodeDescription)) {

				flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

				if(flowvalues.get("IsEnabled").toString().equalsIgnoreCase("true")) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | NodeDescription "+NodeDescription, mySession);

					if(inputFlag.equalsIgnoreCase("true")) {

						Flag = "true";

					} else {
						Flag = "false";
					}

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | Status | "+Flag, mySession);
					NodeDescription = flowvalues.get(Flag).toString().trim();

					if(NodeDescription.equalsIgnoreCase("Disconnect")) {

						NextNode = NodeDescription;

					} else {

						if(flow.containsKey(NodeDescription)) {
							flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
							NextNode = flowvalues.get("Type").toString();
						} else {

							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | Next NodeDescription "+NodeDescription+" is Missing", mySession);
							mySession.getVariableField("Error").setValue(true);

						}

					}
				}

			} else {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | NodeDescription "+NodeDescription+" is Missing", mySession);
				mySession.getVariableField("Error").setValue(true);

			}

			MenuDescription += Flag.toUpperCase()+"|";

			CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
			mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | NextNode | "+NextNode, mySession);

			mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
			mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		} catch(Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | setApiFlag | "+Arrays.toString(e.getStackTrace()), mySession);
		}

	}
	
	
	@SuppressWarnings({ "unused", "unchecked" })
	public String getContextData(String input ,SCESession mySession) {

		String CLID=mySession.getVariableField("session", "ani").getStringValue();
		String method = "getContextData";
		

		ApiConnection api = new ApiConnection();
		DataNode dataNode = new DataNode();
		HashMap<String,Object> responseValues = null;

		boolean responseStatus = false;

		String contextID = input;
		String GetContextDataURL = LoadApplicationProperties.getProperty("OceanaUrl", mySession)+
				LoadApplicationProperties.getProperty("GetContextData", mySession);
		String sanitizedJson = "";
		String ContextData = "";
		String StartDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
		String EndDate = "";

		int connectionTimeout = Integer.parseInt(LoadApplicationProperties.getProperty("GetContextDataConnnectionTimeOut", mySession));
		int readTimeout = Integer.parseInt(LoadApplicationProperties.getProperty("GetContextDataReadTimeout", mySession));
		int responseCode = 0;

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"Final GetContextDataURL URL :"+ GetContextDataURL+contextID, mySession);

		try {
			
			String URL = GetContextDataURL+contextID;

			responseValues = api.httpGetResponse(method, URL, connectionTimeout, readTimeout, mySession);

			EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

			if(responseValues!=null) {

				responseCode = (int) responseValues.get(method+"Code");
				sanitizedJson = responseValues.get(method+"Response").toString();

				if (responseCode == 200) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"GetContextData ID Response : "+ sanitizedJson, mySession);
					JSONObject responseObject = new JSONObject(sanitizedJson);

					if(responseObject.has("schema")) {
						ContextData = responseObject.toString();
					}

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"ContextData is : "+ ContextData, mySession);
					
					//new changes
					JSONParser parser = new JSONParser();
					org.json.simple.JSONObject jsonNode = null;
			            // Parse the JSON data into a JSONObject
			        	jsonNode = (org.json.simple.JSONObject) parser.parse(ContextData);

					if(jsonNode!=null) {
						org.json.simple.JSONObject customerObject = (org.json.simple.JSONObject) jsonNode.get("data");
					HashMap<String, String> CallHistory = new HashMap<String, String>();
					CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();
					CallHistory.put("VC_CLI_NO",customerObject.get("cli").toString());
					CLID = customerObject.get("cli").toString();
					}
					//jsonparseJsonResponse(customerObject, "cli",mySession)
					responseStatus=true;

				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR," Exception on getting ContextData ID : "+ responseCode, mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR," Setting ANI value as ContextData ID : ", mySession);
				}

			} else {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, " Response is not received " , mySession);
			}

			try {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"InsertingTransactionHistory", mySession);

				LinkedHashMap<String,Object> TransactionHistory = new LinkedHashMap<String,Object>();

				TransactionHistory = (LinkedHashMap<String, Object>) mySession.getVariableField("HashMap", "TransactionHistory").getObjectValue();

				if(EndDate.equalsIgnoreCase("")) {

					EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

				}

				TransactionHistory.put("VC_CLI_NO", CLID);
				TransactionHistory.put("DT_START_DATE", StartDate);
				TransactionHistory.put("DT_END_DATE", EndDate);
				TransactionHistory.put("VC_FUNCTION_NAME", "GET_CONTEXT_DATA");
				TransactionHistory.put("VC_HOST_URL", GetContextDataURL);
				TransactionHistory.put("VC_HOST_REQUEST", contextID);
				TransactionHistory.put("VC_HOST_RESPONSE", sanitizedJson);
				TransactionHistory.put("VC_TRANS_STATUS", responseCode);

				mySession.getVariableField("HashMap", "TransactionHistory").setValue(TransactionHistory);

				dataNode.setTransactionHistory(mySession);

			} catch(Exception e) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

			}
		

		} catch (Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR," Exception on getting Customer ID "+e.getMessage(), mySession);
			mySession.getVariableField("CustomerID").setValue(contextID.replace("+", "").replace(" ", "").replace("-", ""));

		}
		
		
		
		return ContextData;

	}

}
