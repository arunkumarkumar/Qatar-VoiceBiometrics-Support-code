package com.General;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TimeZone;

import com.HoursOfOperation.CheckBusinessHours;
//import com.RestApi.QatarWebRequestor;
import com.RestApi.WebServices;
import com.XML_Parsing.langauge;
import com.avaya.sce.runtime.tracking.TraceInfo;
import com.avaya.sce.runtimecommon.ITraceInfo;
import com.avaya.sce.runtimecommon.SCESession;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flow.DataNode;
import com.flow.Set;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;


@SuppressWarnings("unchecked")
public class ApiCallFromExtJar {

	String className = "ApiCallFromExtJar";

	String successCode = null;
	String errorCode = null;

	public void ICDUsingMobileNoCountryCode(SCESession mySession) {

		String method = "ICDUsingMobileNoCountryCode";
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ ********IVR API CALL START ********  | IVR METHOD NAME -> ICD_UsingMobileNoCountryCode  ", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR CALLING API FROM EXTERNAL JAR    | JAR_NAME        -> QatarWebRequestor  ", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR CALLING METHOD FROM EXTERNAL JAR | JAR METHOD_NAME- > getIdentifyCustomerResponse ", mySession);

		String phoneNumber = mySession.getVariableField("session", "ani").getStringValue();

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR INPUT |  phoneNumber | ->  "+phoneNumber, mySession);

		//QatarWebRequestor qwr = new QatarWebRequestor();
		DataNode dataNode = new DataNode();

		HashMap<String,String> requestHashMap= null;
		String requestBody= "";
		String responseBody = "";
		String responseCode = "";
		String StartDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
		String EndDate = "";
		String url = LoadApplicationProperties.getProperty("Qatar.webservices.endPoint.url", mySession);
		String endPointUlr = url+LoadApplicationProperties.getProperty("IdentifyCustomer", mySession);
		boolean tokenStatus = false;

		HashMap<String, Object> jsonResposeMap = null;

		mySession.getVariableField("QA_WebService","Success").setValue(false);
		mySession.getVariableField("QA_WebService","LinkDown").setValue(false);
		mySession.getVariableField("WS_IdentifyCustomerDetails","SuccessFlag").setValue(false);
		tokenStatus = mySession.getVariableField("TokenStatus").getBooleanValue();
		boolean cliFlag = dataNode.isNumericClid(phoneNumber, mySession);


		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ token status | ->  "+tokenStatus, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ cliflag status | ->  "+cliFlag, mySession);
		if(tokenStatus) {

			if(cliFlag) {

				try {

					mySession.getVariableField("WS_IdentifyCustomerDetails","RequestType").setValue("Mobile");
					successCode = LoadApplicationProperties.getProperty("ICD_SUCCESS_CODE", mySession);
					errorCode = LoadApplicationProperties.getProperty("ICD_ERROR_CODE", mySession);
					checkCountryCode(phoneNumber, mySession);
					requestHashMap = (HashMap<String, String>) mySession.getVariableField("MobileNumber").getObjectValue();
					requestBody = "{\"PhoneNumber\":\""+requestHashMap.get("PhoneNumber")+
							"\",\"CountryCode\":\""+requestHashMap.get("CountryCode")+"\"}";

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR SENDING REQUEST TO EXT JAR |  FRAMED JSON REQUEST IN IVR | ->  "+requestBody, mySession);
					responseBody = "";//qwr.getIdentifyCustomerResponse(requestBody,mySession);

					String requestType = mySession.getVariableField("WS_IdentifyCustomerDetails","RequestType").getStringValue();

					String isAuthentication=LoadApplicationProperties.getProperty("Qatar.webservices.isAuthentication", mySession);
					int connectionTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("Qatar.webservices.connectionTimeout", mySession));
					int readTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("Qatar.webservices.readTimeout", mySession));


					if(requestType.equalsIgnoreCase("Mobile")){

						connectionTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("getICconnectionTimeoutMobile", mySession));
						readTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("getICreadTimeoutMobile", mySession));

					} else if (requestType.equalsIgnoreCase("FFP")) {

						connectionTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("getICconnectionTimeoutFFP", mySession));
						readTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("getICreadTimeoutFFP", mySession));

					} else if (requestType.equalsIgnoreCase("IATA")) {

						connectionTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("getICconnectionTimeoutIATA", mySession));
						readTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("getICreadTimeoutIATA", mySession));

					}

					try {

						WebServices webServices= new WebServices();
						responseBody=webServices.Webservice(mySession,endPointUlr, "POST",requestBody, connectionTimeOut, readTimeOut,isAuthentication);
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+method+" | IdentityCustomer is called", mySession);

					} catch(Exception e) {
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+method+ " EXCEPTION: " +Arrays.toString(e.getStackTrace()), mySession);

					}

					EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR RECEIVED RESPONSE FROM EXT JAR |  RESPONSE JSON FROM JAR | ->  "+responseBody, mySession);

					if(responseBody!=null) {

						responseCode = "0";
						jsonResposeMap = new ObjectMapper().readValue(responseBody, HashMap.class);
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ JSON RESPONSE CONVERTED INTO HASHMAP | ->  "+jsonResposeMap, mySession);

						if(jsonResposeMap.containsKey("ResponseCode")) {

							responseCode = (String) jsonResposeMap.get("ResponseCode");

							if(responseCode.equalsIgnoreCase(successCode)) {

								TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ API SUCCESS $$$$", mySession);
								mySession.getVariableField("QA_WebService","Success").setValue(true);
//								mySession.getVariableField("ident_type").setValue("MOBILE");
								mySession.getVariableField("WS_IdentifyCustomerDetails","SuccessFlag").setValue(true);
								TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ PARSING START $$$$", mySession);
								mySession.getVariableField("WS_IdentifyCustomerDetails","MobileResponseMap").setValue(jsonResposeMap);
								jsonResposeMap = (HashMap<String, Object>) jsonResposeMap.get("Response");
								
								TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ Customer ID $$$$"+parseJsonResponse1(jsonResposeMap, "CustomerID", mySession)+"----", mySession);
								boolean status = setOcdsData(jsonResposeMap, mySession);
								String CustomerID = parseJsonResponse1(jsonResposeMap, "CustomerID", mySession);
								if(CustomerID != null && !CustomerID.equalsIgnoreCase("NA") && !CustomerID.equalsIgnoreCase("null") && !"".equalsIgnoreCase(CustomerID.trim()) && !CustomerID.isEmpty()) {
									mySession.getVariableField("ident_type").setValue("MOBILE");
								}else {
									mySession.getVariableField("ident_type").setValue("NA");
								}
								TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ ident_type is set as $$$$"+mySession.getVariableField("ident_type"), mySession);
								TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | Setting Data to OCDS | "+status, mySession);
								TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ PARSING END $$$$", mySession);

							} else if (responseCode.equalsIgnoreCase(errorCode)){

								TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING API FAILURE  DUE TO RESPONSE CODE | ->"+responseCode, mySession);
								mySession.getVariableField("QA_WebService","Success").setValue(false);


							}  else {

								mySession.getVariableField("QA_WebService","LinkDown").setValue(true);
								TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING LINK DOWN FLAG ENABLED DUE TO RESPONSE CODE | ->"+responseCode, mySession);

							}

							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ RESPONSE CODE | ->  "+responseCode, mySession);

						}

					} else {

						responseBody = "";
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ IVR RECEIVED NULL RESPONSE FROM EXTERNAL JAR", mySession);
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING LINK DOWN FLAG ENABLED DUE TO NULL RESPONSE", mySession);
						mySession.getVariableField("QA_WebService","LinkDown").setValue(true);

					}

					try {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "InsertingTransactionHistory", mySession);
						LinkedHashMap<String,Object> TransactionHistory = new LinkedHashMap<String,Object>();
						TransactionHistory = (LinkedHashMap<String, Object>) mySession.getVariableField("HashMap", "TransactionHistory").getObjectValue();

						if(EndDate.equalsIgnoreCase("")) {

							EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

						}

						TransactionHistory.put("DT_START_DATE", StartDate);
						TransactionHistory.put("DT_END_DATE", EndDate);
						TransactionHistory.put("VC_FUNCTION_NAME", "IDENTIFY_CUSTOMER_MOBILE");
						TransactionHistory.put("VC_HOST_URL", endPointUlr);
						TransactionHistory.put("VC_HOST_REQUEST", requestBody);
						TransactionHistory.put("VC_HOST_RESPONSE", responseBody);
						TransactionHistory.put("VC_TRANS_STATUS", responseCode);
						//mySession.getVariableField("HashMap", "TransactionHistory").setValue(TransactionHistory);
						dataNode.setTransactionHistory(mySession);

					} catch(Exception e) {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+"\t|\t"+method+"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

					}

				} catch(Exception e) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING API FAILURE DUE TO EXCEPTION IN IVR", mySession);
					mySession.getVariableField("QA_WebService","Success").setValue(false);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | ICD_UsingMobileNoCountryCode | "+Arrays.toString(e.getStackTrace()), mySession);

				}

			} else {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"ICDUsingMobileNumber API not performed since CLI is Non-Numeric", mySession);
			}

		} else {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | Token is not Generated", mySession);
		}

		setApiStatus(mySession);

	}

	public void ICDUsingFFPNumber(SCESession mySession) {

		String method = "ICDUsingFFPNumber";

		mySession.getVariableField("ident_type").setValue("NA");
		
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ ********IVR API CALL START ********  | IVR METHOD NAME -> ICDUsingFFPNumber  ", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR CALLING API FROM EXTERNAL JAR    | JAR_NAME        -> QatarWebRequestor  ", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR CALLING METHOD FROM EXTERNAL JAR | JAR METHOD_NAME- > getIdentifyCustomerResponse ", mySession);

		String FFPNumber = mySession.getVariableField("QA_WebService","FFPNumber").getStringValue();

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR INPUT |  FFPNumber | ->  "+FFPNumber, mySession);

		//QatarWebRequestor qwr = new QatarWebRequestor();
		DataNode dataNode = new DataNode();

		String requestBody= "";
		String responseBody = "";
		String responseCode = "";
		String StartDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
		String EndDate = "";
		String url = LoadApplicationProperties.getProperty("Qatar.webservices.endPoint.url", mySession);
		String endPointUlr = url+LoadApplicationProperties.getProperty("IdentifyCustomer", mySession);
		boolean tokenStatus = false;

		HashMap<String, Object> jsonResposeMap = null;

		mySession.getVariableField("QA_WebService","Success").setValue(false);
		mySession.getVariableField("QA_WebService","LinkDown").setValue(false);
		tokenStatus = mySession.getVariableField("TokenStatus").getBooleanValue();

		if(tokenStatus) {

			try {

				mySession.getVariableField("WS_IdentifyCustomerDetails","RequestType").setValue("FFP");

				successCode = LoadApplicationProperties.getProperty("ICD_SUCCESS_CODE", mySession);
				errorCode = LoadApplicationProperties.getProperty("ICD_ERROR_CODE", mySession);

				requestBody = "{\"FFPNumber\":\""+FFPNumber+"\"}";

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR SENDING REQUEST TO EXT JAR |  FRAMED JSON REQUEST IN IVR | ->  "+requestBody, mySession);

				responseBody = "";//qwr.getIdentifyCustomerResponse(requestBody,mySession);

				String requestType = mySession.getVariableField("WS_IdentifyCustomerDetails","RequestType").getStringValue();

				String isAuthentication=LoadApplicationProperties.getProperty("Qatar.webservices.isAuthentication", mySession);
				int connectionTimeOut = 10;
				int readTimeOut = 10;

				if(requestType.equalsIgnoreCase("Mobile")){

					connectionTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("getICconnectionTimeoutMobile", mySession));
					readTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("getICreadTimeoutMobile", mySession));

				} else if (requestType.equalsIgnoreCase("FFP")) {

					connectionTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("getICconnectionTimeoutFFP", mySession));
					readTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("getICreadTimeoutFFP", mySession));

				} else if (requestType.equalsIgnoreCase("IATA")) {

					connectionTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("getICconnectionTimeoutIATA", mySession));
					readTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("getICreadTimeoutIATA", mySession));

				}

				try {

					WebServices webServices= new WebServices();
					responseBody=webServices.Webservice(mySession,endPointUlr, "POST",requestBody, connectionTimeOut, readTimeOut,isAuthentication);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+method+" | IdentityCustomer is called", mySession);

				} catch(Exception e) {
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+method+ " EXCEPTION: " +Arrays.toString(e.getStackTrace()), mySession);

				}


				EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR RECEIVED RESPONSE FROM EXT JAR |  RESPONSE JSON FROM JAR | ->  "+responseBody, mySession);

				if(responseBody!=null) {

					responseCode = "1";

					jsonResposeMap = new ObjectMapper().readValue(responseBody, HashMap.class);

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ JSON RESPONSE CONVERTED INTO HASHMAP | ->  "+jsonResposeMap, mySession);

					if(jsonResposeMap.containsKey("ResponseCode")) {
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+"contains ResponseCode",mySession);
						responseCode = jsonResposeMap.get("ResponseCode").toString();
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+"ResponseCode is "+responseCode,mySession);
						if(responseCode.equalsIgnoreCase(successCode)) {
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ API SUCCESS $$$$", mySession);
							mySession.getVariableField("QA_WebService","Success").setValue(true);
							mySession.getVariableField("ident_type").setValue("FFP");

							mySession.getVariableField("WS_IdentifyCustomerDetails","SuccessFlag").setValue(true);

							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ PARSING START $$$$", mySession);

							mySession.getVariableField("WS_IdentifyCustomerDetails","FfpResponseMap").setValue(jsonResposeMap);

							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ PARSING END $$$$", mySession);

						} else if (responseCode.equalsIgnoreCase(errorCode)){

							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING API FAILURE  DUE TO RESPONSE CODE | ->"+responseCode, mySession);
							mySession.getVariableField("QA_WebService","Success").setValue(false);

						}  else {
							mySession.getVariableField("QA_WebService","LinkDown").setValue(true);
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING LINK DOWN FLAG ENABLED DUE TO RESPONSE CODE | ->"+responseCode, mySession);

						}
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ RESPONSE CODE | ->  "+responseCode, mySession);
					}

				} else {

					responseBody = "";
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ IVR RECEIVED NULL RESPONSE FROM EXTERNAL JAR", mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING LINK DOWN FLAG ENABLED DUE TO NULL RESPONSE", mySession);

					mySession.getVariableField("QA_WebService","LinkDown").setValue(true);
				}

				try {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "InsertingTransactionHistory", mySession);

					LinkedHashMap<String,Object> TransactionHistory = new LinkedHashMap<String,Object>();

					TransactionHistory = (LinkedHashMap<String, Object>) mySession.getVariableField("HashMap", "TransactionHistory").getObjectValue();

					if(EndDate.equalsIgnoreCase("")) {

						EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

					}

					TransactionHistory.put("DT_START_DATE", StartDate);
					TransactionHistory.put("DT_END_DATE", EndDate);
					TransactionHistory.put("VC_FUNCTION_NAME", "IDENTIFY_CUSTOMER_FFP");
					TransactionHistory.put("VC_HOST_URL", endPointUlr);
					TransactionHistory.put("VC_HOST_REQUEST", requestBody);
					TransactionHistory.put("VC_HOST_RESPONSE", responseBody);
					TransactionHistory.put("VC_TRANS_STATUS", responseCode);

					//mySession.getVariableField("HashMap", "TransactionHistory").setValue(TransactionHistory);

					dataNode.setTransactionHistory(mySession);

				} catch(Exception e) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+"\t|\t"+method+"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

				}

			} catch(Exception e) {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING API FAILURE DUE TO EXCEPTION IN IVR", mySession);
				mySession.getVariableField("QA_WebService","Success").setValue(false);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | ICD_UsingMobileNoCountryCode | "+Arrays.toString(e.getStackTrace()), mySession);

			}

		} else {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | Token is not Generated", mySession);
		}

		setApiStatus(mySession);

	}

	public void ICDUsingIATANumber(SCESession mySession) {

		String method = "ICDUsingIATANumber";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ ********IVR API CALL START ********  | IVR METHOD NAME -> ICDUsingIATANumber  ", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR CALLING API FROM EXTERNAL JAR    | JAR_NAME        -> QatarWebRequestor  ", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR CALLING METHOD FROM EXTERNAL JAR | JAR METHOD_NAME- > getIdentifyCustomerResponse ", mySession);

		String IATANumber = mySession.getVariableField("QA_WebService","IATANumber").getStringValue();
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR INPUT |  IATANumber | ->  "+IATANumber, mySession);

		//QatarWebRequestor qwr = new QatarWebRequestor();
		DataNode dataNode = new DataNode();

		String requestBody= "";
		String responseBody = "";
		String responseCode = "";
		String StartDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
		String EndDate = "";
		String url = LoadApplicationProperties.getProperty("Qatar.webservices.endPoint.url", mySession);
		String endPointUlr = url+LoadApplicationProperties.getProperty("IdentifyCustomer", mySession);
		boolean tokenStatus = false;

		HashMap<String, Object> jsonResposeMap = null;

		mySession.getVariableField("QA_WebService","Success").setValue(false);
		mySession.getVariableField("QA_WebService","LinkDown").setValue(false);
		tokenStatus = mySession.getVariableField("TokenStatus").getBooleanValue();

		String CustomerTier = "TRAVELAGENT";
		HashMap<String,String> customerDetails = new HashMap<String,String>();
		customerDetails = (HashMap<String, String>) mySession.getVariableField("HashMap", "customerDetails").getObjectValue();

		if(tokenStatus) {

			try {

				mySession.getVariableField("WS_IdentifyCustomerDetails","RequestType").setValue("IATA");

				successCode = LoadApplicationProperties.getProperty("ICD_SUCCESS_CODE", mySession);
				errorCode = LoadApplicationProperties.getProperty("ICD_ERROR_CODE", mySession);

				requestBody = "{\"IATARegNo\":\""+IATANumber+"\"}";

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR SENDING REQUEST TO EXT JAR |  FRAMED JSON REQUEST IN IVR | ->  "+requestBody, mySession);

				responseBody = "";//qwr.getIdentifyCustomerResponse(requestBody,mySession);


				String requestType = mySession.getVariableField("WS_IdentifyCustomerDetails","RequestType").getStringValue();

				String isAuthentication=LoadApplicationProperties.getProperty("Qatar.webservices.isAuthentication", mySession);
				int connectionTimeOut = 10;
				int readTimeOut = 10;

				if(requestType.equalsIgnoreCase("Mobile")){

					connectionTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("getICconnectionTimeoutMobile", mySession));
					readTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("getICreadTimeoutMobile", mySession));

				} else if (requestType.equalsIgnoreCase("FFP")) {

					connectionTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("getICconnectionTimeoutFFP", mySession));
					readTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("getICreadTimeoutFFP", mySession));

				} else if (requestType.equalsIgnoreCase("IATA")) {

					connectionTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("getICconnectionTimeoutIATA", mySession));
					readTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("getICreadTimeoutIATA", mySession));

				}

				try {

					WebServices webServices= new WebServices();
					responseBody=webServices.Webservice(mySession,endPointUlr, "POST",requestBody, connectionTimeOut, readTimeOut,isAuthentication);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+method+" | IdentityCustomer is called", mySession);

				} catch(Exception e) {
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+method+ " EXCEPTION: " +Arrays.toString(e.getStackTrace()), mySession);

				}

				EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR RECEIVED RESPONSE FROM EXT JAR |  RESPONSE JSON FROM JAR | ->  "+responseBody, mySession);


				if(responseBody!=null) {

					responseCode = "1";

					jsonResposeMap = new ObjectMapper().readValue(responseBody, HashMap.class);

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ JSON RESPONSE CONVERTED INTO HASHMAP | ->  "+jsonResposeMap, mySession);


					if(jsonResposeMap.containsKey("ResponseCode")) {

						responseCode = (String) jsonResposeMap.get("ResponseCode");

						if(responseCode.equalsIgnoreCase(successCode)) {
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ API SUCCESS $$$$", mySession);
							mySession.getVariableField("QA_WebService","Success").setValue(true);
							mySession.getVariableField("WS_IdentifyCustomerDetails","SuccessFlag").setValue(true);

							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ PARSING START $$$$", mySession);

							mySession.getVariableField("WS_IdentifyCustomerDetails","IataResponseMap").setValue(jsonResposeMap);

							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ PARSING END $$$$", mySession);

						} else if (responseCode.equalsIgnoreCase(errorCode)){

							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING API FAILURE  DUE TO RESPONSE CODE | ->"+responseCode, mySession);
							mySession.getVariableField("QA_WebService","Success").setValue(false);

						}  else {
							mySession.getVariableField("QA_WebService","LinkDown").setValue(true);
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING LINK DOWN FLAG ENABLED DUE TO RESPONSE CODE | ->"+responseCode, mySession);

						}
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ RESPONSE CODE | ->  "+responseCode, mySession);
					}

				} else {

					responseBody = "";
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ IVR RECEIVED NULL RESPONSE FROM EXTERNAL JAR", mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING LINK DOWN FLAG ENABLED DUE TO NULL RESPONSE", mySession);

					mySession.getVariableField("QA_WebService","LinkDown").setValue(true);

				}

				try {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "InsertingTransactionHistory", mySession);

					LinkedHashMap<String,Object> TransactionHistory = new LinkedHashMap<String,Object>();

					TransactionHistory = (LinkedHashMap<String, Object>) mySession.getVariableField("HashMap", "TransactionHistory").getObjectValue();

					if(EndDate.equalsIgnoreCase("")) {

						EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

					}

					TransactionHistory.put("DT_START_DATE", StartDate);
					TransactionHistory.put("DT_END_DATE", EndDate);
					TransactionHistory.put("VC_FUNCTION_NAME", "IDENTIFY_CUSTOMER_IATA");
					TransactionHistory.put("VC_HOST_URL", endPointUlr);
					TransactionHistory.put("VC_HOST_REQUEST", requestBody);
					TransactionHistory.put("VC_HOST_RESPONSE", responseBody);
					TransactionHistory.put("VC_TRANS_STATUS", responseCode);

					//mySession.getVariableField("HashMap", "TransactionHistory").setValue(TransactionHistory);

					dataNode.setTransactionHistory(mySession);

				} catch(Exception e) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+"\t|\t"+method+"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

				}

			} catch(Exception e) {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING API FAILURE DUE TO EXCEPTION IN IVR", mySession);
				mySession.getVariableField("QA_WebService","Success").setValue(false);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | ICD_UsingMobileNoCountryCode | "+Arrays.toString(e.getStackTrace()), mySession);

			}

			mySession.getVariableField("CustTier").setValue(CustomerTier);
			customerDetails.put("CustomerTier",CustomerTier);
			mySession.getVariableField("HashMap", "customerDetails").setValue(customerDetails);

		} else {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | Token is not Generated", mySession);
		}

		setApiStatus(mySession);

	}

	public void IdentifyStaff(SCESession mySession) {

		String method = "IdentifyStaff";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ ********IVR API CALL START ********  | IVR METHOD NAME -> IdentifyStaff  ", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR CALLING API FROM EXTERNAL JAR    | JAR_NAME        -> QatarWebRequestor  ", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR CALLING METHOD FROM EXTERNAL JAR | JAR METHOD_NAME- > getIdentifyCustomerResponse ", mySession);

		String StaffNumber = mySession.getVariableField("QA_WebService","StaffNumber").getStringValue();

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR INPUT |  StaffNumber | ->  "+StaffNumber, mySession);

		//QatarWebRequestor qwr = new QatarWebRequestor();
		DataNode dataNode = new DataNode();

		String requestBody= "";
		String responseBody = "";
		String responseCode = "";
		String StartDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
		String EndDate = "";
		String url = LoadApplicationProperties.getProperty("Qatar.webservices.endPoint.url", mySession);
		String endPointUlr = url+LoadApplicationProperties.getProperty("IdentifyStaff", mySession);
		boolean tokenStatus = false;

		HashMap<String, Object> jsonResposeMap = null;

		mySession.getVariableField("QA_WebService","Success").setValue(false);
		mySession.getVariableField("QA_WebService","LinkDown").setValue(false);
		mySession.getVariableField("WS_IdentifyStaff","SuccessFlag").setValue(false);
		tokenStatus = mySession.getVariableField("TokenStatus").getBooleanValue();

		if(tokenStatus) {
			try {

				successCode = LoadApplicationProperties.getProperty("IS_SUCCESS_CODE", mySession);
				errorCode = LoadApplicationProperties.getProperty("IS_ERROR_CODE", mySession);

				requestBody = "{\"StaffNumber\":\""+StaffNumber+"\"}";

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR SENDING REQUEST TO EXT JAR |  FRAMED JSON REQUEST IN IVR | ->  "+requestBody, mySession);

				responseBody = "";//qwr.getIdentifyStaffResponse(requestBody,mySession);


				String isAuthentication=LoadApplicationProperties.getProperty("Qatar.webservices.isAuthentication", mySession);
				int connectionTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("Qatar.webservices.connectionTimeout", mySession));
				int readTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("Qatar.webservices.readTimeout", mySession));

				try {

					WebServices webServices= new WebServices();
					responseBody=webServices.Webservice(mySession,endPointUlr, "POST",requestBody, connectionTimeOut, readTimeOut,isAuthentication);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+method+" | Identify Staff is called", mySession);


				} catch(Exception e) {
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+method+ " EXCEPTION: " +Arrays.toString(e.getStackTrace()), mySession);
				}

				EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR RECEIVED RESPONSE FROM EXT JAR |  RESPONSE JSON FROM JAR | ->  "+responseBody, mySession);


				if(responseBody!=null) {

					responseCode = "1";

					jsonResposeMap = new ObjectMapper().readValue(responseBody, HashMap.class);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ JSON RESPONSE CONVERTED INTO HASHMAP | ->  "+jsonResposeMap, mySession);


					if(jsonResposeMap.containsKey("ResponseCode")) {

						responseCode = (String) jsonResposeMap.get("ResponseCode");

						if(responseCode.equalsIgnoreCase(successCode)) {
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ API SUCCESS $$$$", mySession);
							mySession.getVariableField("QA_WebService","Success").setValue(true);
							mySession.getVariableField("WS_IdentifyStaff","SuccessFlag").setValue(true);

							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ PARSING START $$$$", mySession);

							mySession.getVariableField("WS_IdentifyStaff","responseMap").setValue(jsonResposeMap);


							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ PARSING END $$$$", mySession);

						}  else if (responseCode.equalsIgnoreCase(errorCode)){

							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING API FAILURE  DUE TO RESPONSE CODE | ->"+responseCode, mySession);
							mySession.getVariableField("QA_WebService","Success").setValue(false);

						}  else {
							mySession.getVariableField("QA_WebService","LinkDown").setValue(true);
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING LINK DOWN FLAG ENABLED DUE TO RESPONSE CODE | ->"+responseCode, mySession);

						}
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ RESPONSE CODE | ->  "+responseCode, mySession);
					}

				} else {

					responseBody = "";
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ IVR RECEIVED NULL RESPONSE FROM EXTERNAL JAR", mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING LINK DOWN FLAG ENABLED DUE TO NULL RESPONSE", mySession);

					mySession.getVariableField("QA_WebService","LinkDown").setValue(true);

				}

				try {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "InsertingTransactionHistory", mySession);

					LinkedHashMap<String,Object> TransactionHistory = new LinkedHashMap<String,Object>();

					TransactionHistory = (LinkedHashMap<String, Object>) mySession.getVariableField("HashMap", "TransactionHistory").getObjectValue();

					if(EndDate.equalsIgnoreCase("")) {

						EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

					}

					TransactionHistory.put("DT_START_DATE", StartDate);
					TransactionHistory.put("DT_END_DATE", EndDate);
					TransactionHistory.put("VC_FUNCTION_NAME", "IDENTIFY_STAFF");
					TransactionHistory.put("VC_HOST_URL", endPointUlr);
					TransactionHistory.put("VC_HOST_REQUEST", requestBody);
					TransactionHistory.put("VC_HOST_RESPONSE", responseBody);
					TransactionHistory.put("VC_TRANS_STATUS", responseCode);

					//mySession.getVariableField("HashMap", "TransactionHistory").setValue(TransactionHistory);

					dataNode.setTransactionHistory(mySession);

				} catch(Exception e) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+"\t|\t"+method+"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

				}

			} catch(Exception e) {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING API FAILURE DUE TO EXCEPTION IN IVR", mySession);
				mySession.getVariableField("QA_WebService","Success").setValue(false);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | ICD_UsingMobileNoCountryCode | "+Arrays.toString(e.getStackTrace()), mySession);

			}

		} else {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | Token is not Generated", mySession);
		}

		setApiStatus(mySession);

	}

	public void VerifyStaff(SCESession mySession) {

		String method = "VerifyStaff";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ ********IVR API CALL START ********  | IVR METHOD NAME -> VerifyStaff  ", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR CALLING API FROM EXTERNAL JAR    | JAR_NAME        -> QatarWebRequestor  ", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR CALLING METHOD FROM EXTERNAL JAR | JAR METHOD_NAME- > getIdentifyCustomerResponse ", mySession);

		String StaffNumber = mySession.getVariableField("QA_WebService","StaffNumber").getStringValue();
		String DateOfJoining = mySession.getVariableField("DynamicPC","value").getStringValue();

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR INPUT |  StaffNumber   |   ->  "+StaffNumber, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR INPUT |  DateOfJoining | ->  "+DateOfJoining, mySession);

		try {
			Date date = new SimpleDateFormat("ddMMyy").parse(DateOfJoining);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			DateOfJoining = sdf.format(date)+"T00:00:00";
		} catch (Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+"\t|\t"+method+"\t|\t"+Arrays.toString(e.getStackTrace()), mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+"\t|\t"+method+"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
		}

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ Final |  DateOfJoining | ->  "+DateOfJoining, mySession);

		//QatarWebRequestor qwr = new QatarWebRequestor();
		DataNode dataNode = new DataNode();

		String requestBody= "";
		String responseBody = "";
		String responseCode = "";
		String StartDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
		String EndDate = "";
		String url = LoadApplicationProperties.getProperty("Qatar.webservices.endPoint.url", mySession);
		String endPointUlr = url+LoadApplicationProperties.getProperty("VerifyStaff", mySession);
		boolean tokenStatus = false;

		HashMap<String, Object> jsonResposeMap = null;

		mySession.getVariableField("QA_WebService","Success").setValue(false);
		mySession.getVariableField("QA_WebService","LinkDown").setValue(false);
		mySession.getVariableField("WS_VerifyStaff","SuccessFlag").setValue(false);
		tokenStatus = mySession.getVariableField("TokenStatus").getBooleanValue();

		if(tokenStatus) {

			try {

				requestBody = "{\"StaffNumber\":\""+StaffNumber+"\",\"DOJ\":\""+DateOfJoining+"\"}";
				successCode = LoadApplicationProperties.getProperty("VS_SUCCESS_CODE", mySession);
				errorCode = LoadApplicationProperties.getProperty("VS_ERROR_CODE", mySession);

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR SENDING REQUEST TO EXT JAR |  FRAMED JSON REQUEST IN IVR | ->  "+requestBody, mySession);

				responseBody = "";//qwr.getVerifyStaffResponse(requestBody,mySession);


				String isAuthentication=LoadApplicationProperties.getProperty("Qatar.webservices.isAuthentication", mySession);
				int connectionTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("Qatar.webservices.connectionTimeout", mySession));
				int readTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("Qatar.webservices.readTimeout", mySession));


				try {

					WebServices webServices= new WebServices();
					responseBody=webServices.Webservice(mySession,endPointUlr, "POST",requestBody, connectionTimeOut, readTimeOut,isAuthentication);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+method+" | VerifyStaff is called ", mySession);

				} catch(Exception e) {
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+method+ " EXCEPTION: " +Arrays.toString(e.getStackTrace()), mySession);
				}

				EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR RECEIVED RESPONSE FROM EXT JAR |  RESPONSE JSON FROM JAR | ->  "+responseBody, mySession);

				if(responseBody!=null) {

					responseCode = "1";

					jsonResposeMap = new ObjectMapper().readValue(responseBody, HashMap.class);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ JSON RESPONSE CONVERTED INTO HASHMAP | ->  "+jsonResposeMap, mySession);


					if(jsonResposeMap.containsKey("ResponseCode")) {

						responseCode = (String) jsonResposeMap.get("ResponseCode");

						if(responseCode.equalsIgnoreCase(successCode)) {
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ API SUCCESS $$$$", mySession);
							mySession.getVariableField("QA_WebService","Success").setValue(true);
							mySession.getVariableField("WS_VerifyStaff","SuccessFlag").setValue(true);

							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ PARSING START $$$$", mySession);

							mySession.getVariableField("WS_VerifyStaff","responseMap").setValue(jsonResposeMap);

							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ PARSING END $$$$", mySession);

						} else if (responseCode.equalsIgnoreCase(errorCode)){

							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING API FAILURE  DUE TO RESPONSE CODE | ->"+responseCode, mySession);
							mySession.getVariableField("QA_WebService","Success").setValue(false);

						}  else {
							mySession.getVariableField("QA_WebService","LinkDown").setValue(true);
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING LINK DOWN FLAG ENABLED DUE TO RESPONSE CODE | ->"+responseCode, mySession);

						}
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ RESPONSE CODE | ->  "+responseCode, mySession);
					}

				} else {

					responseBody = "";
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ IVR RECEIVED NULL RESPONSE FROM EXTERNAL JAR", mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING LINK DOWN FLAG ENABLED DUE TO NULL RESPONSE", mySession);

					mySession.getVariableField("QA_WebService","LinkDown").setValue(true);

				}

				try {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "InsertingTransactionHistory", mySession);

					LinkedHashMap<String,Object> TransactionHistory = new LinkedHashMap<String,Object>();

					TransactionHistory = (LinkedHashMap<String, Object>) mySession.getVariableField("HashMap", "TransactionHistory").getObjectValue();

					if(EndDate.equalsIgnoreCase("")) {

						EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

					}

					TransactionHistory.put("DT_START_DATE", StartDate);
					TransactionHistory.put("DT_END_DATE", EndDate);
					TransactionHistory.put("VC_FUNCTION_NAME", "VERIFY_STAFF");
					TransactionHistory.put("VC_HOST_URL", endPointUlr);
					TransactionHistory.put("VC_HOST_REQUEST", requestBody);
					TransactionHistory.put("VC_HOST_RESPONSE", responseBody);
					TransactionHistory.put("VC_TRANS_STATUS", responseCode);

					//mySession.getVariableField("HashMap", "TransactionHistory").setValue(TransactionHistory);

					dataNode.setTransactionHistory(mySession);

				} catch(Exception e) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+"\t|\t"+method+"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

				}

			} catch(Exception e) {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING API FAILURE DUE TO EXCEPTION IN IVR", mySession);
				mySession.getVariableField("QA_WebService","Success").setValue(false);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | ICD_UsingMobileNoCountryCode | "+Arrays.toString(e.getStackTrace()), mySession);

			}

		} else {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | Token is not Generated", mySession);
		}

		setApiStatus(mySession);

	}

	public void UpdateLanguagePreference(SCESession mySession) {

		String method = "UpdateLanguagePreference";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ ********IVR API CALL START ********  | IVR METHOD NAME -> UpdateLanguagePreference  ", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR CALLING API FROM EXTERNAL JAR    | JAR_NAME        -> QatarWebRequestor  ", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR CALLING METHOD FROM EXTERNAL JAR | JAR METHOD_NAME- > getIdentifyCustomerResponse ", mySession);

		HashMap<String, Object> ICDresponseMap = null;

		ICDresponseMap = (HashMap<String, Object>) mySession.getVariableField("ICD","responseMap").getObjectValue();

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR INPUT FROM  |  ICDresponseMap   | ->  "+ICDresponseMap, mySession);

		DataNode dataNode = new DataNode();
		//QatarWebRequestor qwr = new QatarWebRequestor();

		HashMap<String,String> CallHistory = new HashMap<String,String>();

		String language = CallHistory.get("VC_LANGUAGE");

		try {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "Language is "+language, mySession);

			List<Object> languageList = (List<Object>) mySession.getVariableField("ApplicationVariable", "LanguageHashMap").getObjectValue();
			HashMap<String,String> languageMap = null;

			for(int i=0;i<languageList.size();i++) {

				languageMap = (HashMap<String, String>) languageList.get(i);
				if(languageMap.containsKey("VC_LANGUAGE_NAME")&&languageMap.containsKey("VC_LANGUAGE_1")) {

					if(language.equalsIgnoreCase(languageMap.get("VC_LANGUAGE_NAME"))) {

						language = languageMap.get("VC_LANGUAGE_1");
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "Language Received as "+language, mySession);

						break;
					}

				}

			}

		} catch(Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+"\t|\t"+method+"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

		}

		String CustomerID = (String) ICDresponseMap.get("CustomerID");
		String LanguageCode = language;
		String Sourcesystem = (String) ICDresponseMap.get("SourceSystem");

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR INPUT |  CustomerID   | ->  "+CustomerID, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR INPUT |  LanguageCode | ->  "+LanguageCode, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR INPUT |  Sourcesystem | ->  "+Sourcesystem, mySession);

		String requestBody= "";
		String responseBody = "";
		String responseCode = "";
		String StartDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
		String EndDate = "";
		String url = LoadApplicationProperties.getProperty("Qatar.webservices.endPoint.url", mySession);
		String endPointUlr= url+LoadApplicationProperties.getProperty("UpdateLanguagePreference", mySession);

		HashMap<String, Object> jsonResposeMap = null;

		mySession.getVariableField("QA_WebService","Success").setValue(false);
		mySession.getVariableField("QA_WebService","LinkDown").setValue(false);

		mySession.getVariableField("WS_UpdateLanguagePreference","SuccessFlag").setValue(false);

		try {

			requestBody = "{\"CustomerID\":\""+CustomerID+"\",\"LanguageCode\":\""+LanguageCode+"\",\"Sourcesystem\":\""+Sourcesystem+"\"}";

			successCode = LoadApplicationProperties.getProperty("ULP_SUCCESS_CODE", mySession);
			errorCode = LoadApplicationProperties.getProperty("ULP_ERROR_CODE", mySession);

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR SENDING REQUEST TO EXT JAR |  FRAMED JSON REQUEST IN IVR | ->  "+requestBody, mySession);

			responseBody = "";//qwr.getUpdateLanguageResponse(requestBody,mySession);

			String isAuthentication=LoadApplicationProperties.getProperty("Qatar.webservices.isAuthentication", mySession);
			int connectionTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("Qatar.webservices.connectionTimeout", mySession));
			int readTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("Qatar.webservices.readTimeout", mySession));


			try {

				WebServices webServices= new WebServices();
				responseBody=webServices.Webservice(mySession,endPointUlr, "POST",requestBody, connectionTimeOut, readTimeOut,isAuthentication);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+method+" | UpdateLanguagePreference is called", mySession);

			} catch(Exception e) {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+method+ " EXCEPTION: " +Arrays.toString(e.getStackTrace()), mySession);
			}


			EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR RECEIVED RESPONSE FROM EXT JAR |  RESPONSE JSON FROM JAR | ->  "+responseBody, mySession);

			if(responseBody!=null) {

				responseCode = "1";

				jsonResposeMap = new ObjectMapper().readValue(responseBody, HashMap.class);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ JSON RESPONSE CONVERTED INTO HASHMAP | ->  "+jsonResposeMap, mySession);

				if(jsonResposeMap.containsKey("ResponseCode")) {

					responseCode = (String) jsonResposeMap.get("ResponseCode");

					if(responseCode.equalsIgnoreCase(successCode)) {
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ API SUCCESS $$$$", mySession);
						mySession.getVariableField("QA_WebService","Success").setValue(true);
						mySession.getVariableField("WS_UpdateLanguagePreference","SuccessFlag").setValue(true);

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ PARSING START $$$$", mySession);

						mySession.getVariableField("WS_UpdateLanguagePreference","responseMap").setValue(jsonResposeMap);

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ PARSING END $$$$", mySession);

					}  else if (responseCode.equalsIgnoreCase(errorCode)){

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING API FAILURE  DUE TO RESPONSE CODE | ->"+responseCode, mySession);
						mySession.getVariableField("QA_WebService","Success").setValue(false);

					}  else {
						mySession.getVariableField("QA_WebService","LinkDown").setValue(true);
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING LINK DOWN FLAG ENABLED DUE TO RESPONSE CODE | ->"+responseCode, mySession);

					}
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ RESPONSE CODE | ->  "+responseCode, mySession);
				}

			} else {

				responseBody = "";
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ IVR RECEIVED NULL RESPONSE FROM EXTERNAL JAR", mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING LINK DOWN FLAG ENABLED DUE TO NULL RESPONSE", mySession);

				mySession.getVariableField("QA_WebService","LinkDown").setValue(true);

			}

			try {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "InsertingTransactionHistory", mySession);

				LinkedHashMap<String,Object> TransactionHistory = new LinkedHashMap<String,Object>();

				TransactionHistory = (LinkedHashMap<String, Object>) mySession.getVariableField("HashMap", "TransactionHistory").getObjectValue();

				if(EndDate.equalsIgnoreCase("")) {

					EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

				}

				TransactionHistory.put("DT_START_DATE", StartDate);
				TransactionHistory.put("DT_END_DATE", EndDate);
				TransactionHistory.put("VC_FUNCTION_NAME", "VERIFY_STAFF");
				TransactionHistory.put("VC_HOST_URL", endPointUlr);
				TransactionHistory.put("VC_HOST_REQUEST", requestBody);
				TransactionHistory.put("VC_HOST_RESPONSE", responseBody);
				TransactionHistory.put("VC_TRANS_STATUS", responseCode);

				//mySession.getVariableField("HashMap", "TransactionHistory").setValue(TransactionHistory);

				dataNode.setTransactionHistory(mySession);

			} catch(Exception e) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+"\t|\t"+method+"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

			}

		} catch(Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING API FAILURE DUE TO EXCEPTION IN IVR", mySession);
			mySession.getVariableField("QA_WebService","Success").setValue(false);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | ICD_UsingMobileNoCountryCode | "+Arrays.toString(e.getStackTrace()), mySession);

		}

		setApiStatus(mySession);

	}

	public void getCustomerDetail(SCESession mySession) {

		String method = "getCustomerDetail";


		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ ********IVR API CALL START ********  | IVR METHOD NAME -> GetCustomerDetail  ", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR CALLING API FROM EXTERNAL JAR    | JAR_NAME        -> QatarWebRequestor  ", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR CALLING METHOD FROM EXTERNAL JAR | JAR METHOD_NAME- > getCustomerDetailResponse ", mySession);

		//QatarWebRequestor qwr = new QatarWebRequestor();
		DataNode dataNode = new DataNode();

		String requestBody= "";
		String responseBody = "";
		String responseCode = "";
		String Channel = "";
		String OID = "";
		String BookingChannel = "";
		String BookingClass = "";
		String FareFamily = "";
		String TicketStatus = "";
		String StartDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
		String EndDate = "";
		String url = LoadApplicationProperties.getProperty("Qatar.webservices.endPoint.url", mySession);
		String endPointUlr = url+LoadApplicationProperties.getProperty("CustomerDetails", mySession);
		boolean tokenStatus = false;

		HashMap<String, Object> jsonResposeMap = null;
		HashMap<String, Object> IDCjsonResposeMap = null;
		HashMap<String, Object> PNRjsonResposeMap = null;

		HashMap<String,String> customerDetails = new HashMap<String,String>();
		customerDetails = (HashMap<String, String>) mySession.getVariableField("HashMap", "customerDetails").getObjectValue();

		mySession.getVariableField("QA_WebService","Success").setValue(false);
		mySession.getVariableField("QA_WebService","LinkDown").setValue(false);
		mySession.getVariableField("WS_GetCustomerDetail","SuccessFlag").setValue(false);
		tokenStatus = mySession.getVariableField("TokenStatus").getBooleanValue();

		if(tokenStatus) {

			try {

				String RequestType= mySession.getVariableField("WS_IdentifyCustomerDetails","RequestType").getStringValue();
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "RequestType | "+RequestType, mySession);
				if("FFP".equalsIgnoreCase(RequestType)) {
					IDCjsonResposeMap = (HashMap<String, Object>) mySession.getVariableField("WS_IdentifyCustomerDetails","FfpResponseMap").getObjectValue();
				}else if("Mobile".equalsIgnoreCase(RequestType)) {
					IDCjsonResposeMap = (HashMap<String, Object>) mySession.getVariableField("WS_IdentifyCustomerDetails","MobileResponseMap").getObjectValue();
				}
				else {
					IDCjsonResposeMap = (HashMap<String, Object>) mySession.getVariableField("WS_IdentifyCustomerDetails","MobileResponseMap").getObjectValue();
				}

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "IDCjsonResposeMap inside getcustomerdetails | "+IDCjsonResposeMap, mySession);
				PNRjsonResposeMap = (HashMap<String, Object>) mySession.getVariableField("WS_CheckPNRDetail","responseMap").getObjectValue();
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "PNRjsonResposeMap inside getcustomerdetails | "+PNRjsonResposeMap, mySession);
				String customerID = parseJsonResponse(IDCjsonResposeMap, "CustomerID", mySession);

				int SourceSystem=0;
				if(AppConstants.T.equalsIgnoreCase(mySession.getVariableField("connectback").getStringValue())){
					SourceSystem = customerDetails.get("SourceSystem")!=null ? Integer.parseInt(customerDetails.get("SourceSystem").toString()) : 0;
				}else {
					SourceSystem = (int) ((HashMap<String, Object>) IDCjsonResposeMap.get("Response")).get("SourceSystem");
				}

				//parseJsonResponse(IDCjsonResposeMap, "SourceSystem", mySession);

				PNRjsonResposeMap = (HashMap<String, Object>) PNRjsonResposeMap.get("Response");

				if(PNRjsonResposeMap!=null) {

					ArrayList<Object> PnrArray = (ArrayList<Object>) PNRjsonResposeMap.get("Pnrs");
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "PnrArray | "+PnrArray, mySession);
					if(PnrArray!=null) {
						if(PnrArray.size()>=1) {
							String PNR = PnrArray.get(0).toString().trim();
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "PNR | "+PNR, mySession);

							requestBody = "{\"CustomerID\":\""+customerID+"\",\"SourceSystem\":\""+SourceSystem+"\",\"PNR\":\""+PNR+"\"}";
							successCode = LoadApplicationProperties.getProperty("CD_SUCCESS_CODE", mySession);
							errorCode = LoadApplicationProperties.getProperty("CD_ERROR_CODE", mySession);

							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR SENDING REQUEST TO EXT JAR |  FRAMED JSON REQUEST IN IVR | ->  "+requestBody, mySession);

							responseBody = "";//qwr.getCustomerDetailsResponse(requestBody,mySession);


							String isAuthentication=LoadApplicationProperties.getProperty("Qatar.webservices.isAuthentication", mySession);
							int connectionTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("Qatar.webservices.connectionTimeout", mySession));
							int readTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("Qatar.webservices.readTimeout", mySession));


							try {

								WebServices webServices= new WebServices();
								responseBody=webServices.Webservice(mySession,endPointUlr, "POST",requestBody, connectionTimeOut, readTimeOut,isAuthentication);
								TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+method+" | CustomerDetails is called", mySession);

							} catch(Exception e) {
								TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+method+ " EXCEPTION: " +Arrays.toString(e.getStackTrace()), mySession);

							}


							EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR RECEIVED RESPONSE FROM EXT JAR |  RESPONSE JSON FROM JAR | ->  "+responseBody, mySession);

							if(responseBody!=null) {

								responseCode = "1";

								jsonResposeMap = new ObjectMapper().readValue(responseBody, HashMap.class);
								TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ JSON RESPONSE CONVERTED INTO HASHMAP | ->  "+jsonResposeMap, mySession);

								if(jsonResposeMap.containsKey("ResponseCode")) {

									responseCode = (String) jsonResposeMap.get("ResponseCode");

									if(responseCode.equalsIgnoreCase(successCode)) {

										TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ API SUCCESS $$$$", mySession);
										mySession.getVariableField("QA_WebService","Success").setValue(true);

										TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ PARSING START $$$$", mySession);

										mySession.getVariableField("WS_GetCustomerDetail","responseMap").setValue(jsonResposeMap);

										jsonResposeMap = (HashMap<String, Object>) jsonResposeMap.get("Response");

										boolean status = setOcdsDataUsingCustomerDetails(jsonResposeMap, mySession);

										TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | Setting Data to OCDS | "+status, mySession);

										ArrayList<Object> Bookingsarray = (ArrayList<Object>) jsonResposeMap.get("Bookings");

										if(Bookingsarray!=null&&!Bookingsarray.isEmpty()) {

											HashMap<String, Object> bookingHashMap = (HashMap<String, Object>) Bookingsarray.get(0);

											Channel = bookingHashMap.get("Channel")!=null?bookingHashMap.get("Channel").toString().trim():"";
											OID = bookingHashMap.get("CreatorOID")!=null?bookingHashMap.get("CreatorOID").toString().trim():"";

											ArrayList<Object> Segmentsarray = (ArrayList<Object>) bookingHashMap.get("Segments");

											if(Segmentsarray!=null&&!Segmentsarray.isEmpty()) {

												HashMap<String, Object> SegmentsHashMap = (HashMap<String, Object>) Segmentsarray.get(0);

												BookingClass =SegmentsHashMap.get("RBD")!=null?SegmentsHashMap.get("RBD").toString().trim():"";
												FareFamily = SegmentsHashMap.get("FareFamily")!=null?SegmentsHashMap.get("FareFamily").toString().trim():"";
												TicketStatus = SegmentsHashMap.get("Status")!=null?SegmentsHashMap.get("Status").toString().trim():"";

											}

										}

										if(Channel.isEmpty()) {
											if(OID.isEmpty()) {
												BookingChannel = "";
											} else {
												BookingChannel = "("+OID+")";
											}
										} else {
											if(OID.isEmpty()) {
												BookingChannel = Channel;
											} else {
												BookingChannel = Channel+"("+OID+")";
											}
										}

										TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "BookingChannel | "+BookingChannel, mySession);
										TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "BookingClass | "+BookingClass, mySession);
										TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "FareFamily | "+FareFamily, mySession);
										TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "TicketStatus | "+TicketStatus, mySession);

										TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ PARSING END $$$$", mySession);

									} else if (responseCode.equalsIgnoreCase(errorCode)){

										TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING API FAILURE  DUE TO RESPONSE CODE | ->"+responseCode, mySession);
										mySession.getVariableField("QA_WebService","Success").setValue(false);

									}  else {
										mySession.getVariableField("QA_WebService","LinkDown").setValue(true);
										TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING LINK DOWN FLAG ENABLED DUE TO RESPONSE CODE | ->"+responseCode, mySession);

									}
									TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ RESPONSE CODE | ->  "+responseCode, mySession);
								}

							} else {

								responseBody = "";
								TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ IVR RECEIVED NULL RESPONSE FROM EXTERNAL JAR", mySession);
								TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING LINK DOWN FLAG ENABLED DUE TO NULL RESPONSE", mySession);

								mySession.getVariableField("QA_WebService","LinkDown").setValue(true);

							}
						}
						else {
							responseBody = "";
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | PNRS Array is empty for the Request", mySession);
							mySession.getVariableField("QA_WebService","Success").setValue(false);

						}
					}
					else {

						responseBody = "";
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | PNRS Array is not Available for the Request", mySession);
						mySession.getVariableField("QA_WebService","Success").setValue(false);

					}
				} else {

					responseBody = "";
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | PNR is not Available for the Request", mySession);
					mySession.getVariableField("QA_WebService","Success").setValue(false);

				}

				//setCallHistory
				HashMap<String,String> CallHistory = new HashMap<String,String>();
				CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();
				CallHistory.put("VC_FAIR_FAMILY",""+FareFamily);
				mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

				//setOceanaData
				customerDetails.put("BookingChannel", ""+BookingChannel);
				customerDetails.put("BookingClass", BookingClass);
				customerDetails.put("FareFamily", ""+FareFamily);
				customerDetails.put("TicketStatus", TicketStatus);
				mySession.getVariableField("HashMap", "customerDetails").setValue(customerDetails);

				try {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "InsertingTransactionHistory", mySession);

					LinkedHashMap<String,Object> TransactionHistory = new LinkedHashMap<String,Object>();

					TransactionHistory = (LinkedHashMap<String, Object>) mySession.getVariableField("HashMap", "TransactionHistory").getObjectValue();

					if(EndDate.equalsIgnoreCase("")) {

						EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

					}

					TransactionHistory.put("DT_START_DATE", StartDate);
					TransactionHistory.put("DT_END_DATE", EndDate);
					TransactionHistory.put("VC_FUNCTION_NAME", "GET_CUSTOMER_DETAIL");
					TransactionHistory.put("VC_HOST_URL", endPointUlr);
					TransactionHistory.put("VC_HOST_REQUEST", requestBody);
					TransactionHistory.put("VC_HOST_RESPONSE", responseBody);
					TransactionHistory.put("VC_TRANS_STATUS", responseCode);

					//mySession.getVariableField("HashMap", "TransactionHistory").setValue(TransactionHistory);

					dataNode.setTransactionHistory(mySession);

				} catch(Exception e) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+"\t|\t"+method+"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

				}

			} catch(Exception e) {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING API FAILURE DUE TO EXCEPTION IN IVR", mySession);
				mySession.getVariableField("QA_WebService","Success").setValue(false);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | getCustomerDetail | "+Arrays.toString(e.getStackTrace()), mySession);

			}

		} else {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | Token is not Generated", mySession);
		}
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | setting api has started in getcustdetail", mySession);
		setApiStatus(mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | setting api is completed in getcustdetail", mySession);
	}

	public void checkPNRDetail(SCESession mySession) {

		String method = "checkPNRDetail";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ ********IVR API CALL START ********  | IVR METHOD NAME -> CheckPNRDetail  ", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR CALLING API FROM EXTERNAL JAR    | JAR_NAME        -> QatarWebRequestor  ", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR CALLING METHOD FROM EXTERNAL JAR | JAR METHOD_NAME- > checkPNRDetail ", mySession);

		HashMap<String,String> customerDetails = new HashMap<String,String>();
		customerDetails = (HashMap<String, String>) mySession.getVariableField("HashMap", "customerDetails").getObjectValue();

		//QatarWebRequestor qwr = new QatarWebRequestor();
		DataNode dataNode = new DataNode();

		String requestBody= "";
		String responseBody = "";
		String responseCode = "";
		String Pnrs = "";
		String StartDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
		String EndDate = "";
		String url = LoadApplicationProperties.getProperty("Qatar.webservices.endPoint.url", mySession);
		String endPointUlr = url+LoadApplicationProperties.getProperty("customerPnrInfo", mySession);
		String RequestType= mySession.getVariableField("WS_IdentifyCustomerDetails","RequestType").getStringValue();
		boolean tokenStatus = false;

		HashMap<String, Object> jsonResposeMap = null;

		mySession.getVariableField("QA_WebService","Success").setValue(false);
		mySession.getVariableField("QA_WebService","LinkDown").setValue(false);
		tokenStatus = mySession.getVariableField("TokenStatus").getBooleanValue();

		if(tokenStatus) {

			try {

				HashMap<String, Object> IdentifyCallerResponse = null;

				if(RequestType.equalsIgnoreCase("Mobile")) {

					HashMap<String,String> requestHashMap= null;
					requestHashMap = (HashMap<String, String>) mySession.getVariableField("MobileNumber").getObjectValue();

					String ICDSuccessFlag = mySession.getVariableField("WS_IdentifyCustomerDetails","SuccessFlag").getStringValue();

					if(ICDSuccessFlag.equalsIgnoreCase("true")) {

						IdentifyCallerResponse = (HashMap<String, Object>) mySession.getVariableField("WS_IdentifyCustomerDetails","MobileResponseMap").getObjectValue();
						String CustomerID = parseJsonResponse(IdentifyCallerResponse, "CustomerID", mySession);

						if(!CustomerID.equalsIgnoreCase("NA")) {

							if(requestHashMap!=null&&!requestHashMap.isEmpty()) {
								if(requestHashMap.containsKey("PhoneNumber")&&requestHashMap.containsKey("CountryCode")) {

									requestBody = "{\"PhoneNumber\":\""+requestHashMap.get("PhoneNumber")+
											"\",\"CountryCode\":\""+requestHashMap.get("CountryCode")+
											"\",\"CustomerId\":\""+CustomerID+"\"}";

								} else {
									TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | Mandatory Request Parameter is not found", mySession);
									TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | PhoneNumber | "+requestHashMap.containsKey("PhoneNumber"), mySession);
									TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | CountryCode | "+requestHashMap.containsKey("CountryCode"), mySession);
								}

							} else {
								TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | No Response found", mySession);
							}

						} else {

							if(requestHashMap!=null&&!requestHashMap.isEmpty()) {
								if(requestHashMap.containsKey("PhoneNumber")&&requestHashMap.containsKey("CountryCode")) {
									requestBody = "{\"PhoneNumber\":\""+requestHashMap.get("PhoneNumber")+
											"\",\"CountryCode\":\""+requestHashMap.get("CountryCode")+"\"}";
								} else {
									TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | Mandatory Request Parameter is not found", mySession);
									TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | PhoneNumber | "+requestHashMap.containsKey("PhoneNumber"), mySession);
									TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | CountryCode | "+requestHashMap.containsKey("CountryCode"), mySession);
								}

							} else {
								TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | No Response found", mySession);
							}

						}

					} else {

						if(requestHashMap!=null&&!requestHashMap.isEmpty()) {
							if(requestHashMap.containsKey("PhoneNumber")&&requestHashMap.containsKey("CountryCode")) {
								requestBody = "{\"PhoneNumber\":\""+requestHashMap.get("PhoneNumber")+
										"\",\"CountryCode\":\""+requestHashMap.get("CountryCode")+"\"}";
							} else {
								TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | Mandatory Request Parameter is not found", mySession);
								TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | PhoneNumber | "+requestHashMap.containsKey("PhoneNumber"), mySession);
								TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | CountryCode | "+requestHashMap.containsKey("CountryCode"), mySession);
							}

						} else {
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | No Response found", mySession);
						}

					}

				} else if (RequestType.equalsIgnoreCase("FFP")) {

					IdentifyCallerResponse = (HashMap<String, Object>) mySession.getVariableField("WS_IdentifyCustomerDetails","FfpResponseMap").getObjectValue();

					requestBody = "{\"FFPNumber\":\""+parseJsonResponse(IdentifyCallerResponse, "FFPNumber", mySession)+
							"\",\"CustomerId\":\""+parseJsonResponse(IdentifyCallerResponse, "CustomerID", mySession)+"\"}";

				} else {

					HashMap<String,String> requestHashMap= null;

					requestHashMap = (HashMap<String, String>) mySession.getVariableField("MobileNumber").getObjectValue();
					if(requestHashMap!=null&&!requestHashMap.isEmpty()) {
						if(requestHashMap.containsKey("PhoneNumber")&&requestHashMap.containsKey("CountryCode")) {
							requestBody = "{\"PhoneNumber\":\""+requestHashMap.get("PhoneNumber")+
									"\",\"CountryCode\":\""+requestHashMap.get("CountryCode")+"\"}";
						} else {
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | Mandatory Request Parameter is not found", mySession);
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | PhoneNumber | "+requestHashMap.containsKey("PhoneNumber"), mySession);
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | CountryCode | "+requestHashMap.containsKey("CountryCode"), mySession);
						}

					} else {
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | No Response found", mySession);
					}

				}

			} catch(Exception e) {

				requestBody = "";
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING API FAILURE DUE TO REQUEST IS NOT FRAMED", mySession);
				mySession.getVariableField("QA_WebService","Success").setValue(false);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | CheckPNRDetail | "+Arrays.toString(e.getStackTrace()), mySession);

			}

			if(requestBody.isEmpty()) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | Request is not framed", mySession);

			} else {

				try {

					successCode = LoadApplicationProperties.getProperty("PNR_SUCCESS_CODE", mySession);
					errorCode = LoadApplicationProperties.getProperty("PNR_ERROR_CODE", mySession);

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR SENDING REQUEST TO EXT JAR |  FRAMED JSON REQUEST IN IVR | ->  "+requestBody, mySession);

					responseBody = "";//qwr.getCustomerPnrDetailResponse(requestBody,mySession);

					String isAuthentication=LoadApplicationProperties.getProperty("Qatar.webservices.isAuthentication", mySession);
					int connectionTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("Qatar.webservices.connectionTimeout", mySession));
					int readTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("Qatar.webservices.readTimeout", mySession));

					try {

						WebServices webServices= new WebServices();
						responseBody=webServices.Webservice(mySession,endPointUlr, "POST",requestBody, connectionTimeOut, readTimeOut,isAuthentication);
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+method+" | CustomerPnrInfo is called", mySession);

					} catch(Exception e) {
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+method+ " EXCEPTION: " +Arrays.toString(e.getStackTrace()), mySession);
					}

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+method+" | CustomerPnrInfo is called and ended ", mySession);

					EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR RECEIVED RESPONSE FROM EXT JAR |  RESPONSE JSON FROM JAR | ->  "+responseBody, mySession);


					if(responseBody!=null) {

						responseCode = "0";

						jsonResposeMap = new ObjectMapper().readValue(responseBody, HashMap.class);

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ JSON RESPONSE CONVERTED INTO HASHMAP | ->  "+jsonResposeMap, mySession);

						if(jsonResposeMap.containsKey("ResponseCode")) {

							responseCode = (String) jsonResposeMap.get("ResponseCode");

							if(responseCode.equalsIgnoreCase(successCode)) {

								TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ API SUCCESS $$$$", mySession);

								mySession.getVariableField("QA_WebService","Success").setValue(true);
								mySession.getVariableField("WS_CheckPNRDetail","SuccessFlag").setValue(true);

								TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ PARSING START $$$$", mySession);

								mySession.getVariableField("WS_CheckPNRDetail","responseMap").setValue(jsonResposeMap);
								TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ PNR RESPONSE CONVERTED INTO HASHMAP | ->  "+mySession.getVariableField("WS_CheckPNRDetail","responseMap").toString(), mySession);

								jsonResposeMap = (HashMap<String, Object>) jsonResposeMap.get("Response");

								if(jsonResposeMap.get("Pnrs")!=null) {

									ArrayList<Object> PnrArray = (ArrayList<Object>) jsonResposeMap.get("Pnrs");

									for(int i=0;i<PnrArray.size();i++) {
										if(i==0) {
											Pnrs = PnrArray.get(i).toString();
										} else {
											Pnrs += ","+PnrArray.get(i).toString();
										}
									}

								} else {
									Pnrs = "";
									TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ RECEIVED PNR NULL FROM RESPONSE $$$$", mySession);
								}

								TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ PARSING END $$$$", mySession);

							} else if (responseCode.equalsIgnoreCase(errorCode)){

								TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING API FAILURE  DUE TO RESPONSE CODE | ->"+responseCode, mySession);
								mySession.getVariableField("QA_WebService","Success").setValue(false);


							}  else {
								mySession.getVariableField("QA_WebService","LinkDown").setValue(true);
								TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING LINK DOWN FLAG ENABLED DUE TO RESPONSE CODE | ->"+responseCode, mySession);

							}

							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ RESPONSE CODE | ->  "+responseCode, mySession);

						}

					} else {

						responseBody = "";
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ IVR RECEIVED NULL RESPONSE FROM EXTERNAL JAR", mySession);
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING LINK DOWN FLAG ENABLED DUE TO NULL RESPONSE", mySession);

						mySession.getVariableField("QA_WebService","LinkDown").setValue(true);

					}

					try {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "InsertingTransactionHistory", mySession);

						LinkedHashMap<String,Object> TransactionHistory = new LinkedHashMap<String,Object>();

						TransactionHistory = (LinkedHashMap<String, Object>) mySession.getVariableField("HashMap", "TransactionHistory").getObjectValue();

						if(EndDate.equalsIgnoreCase("")) {

							EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

						}

						TransactionHistory.put("DT_START_DATE", StartDate);
						TransactionHistory.put("DT_END_DATE", EndDate);
						TransactionHistory.put("VC_FUNCTION_NAME", "CUSTOMER_PNR_INFO");
						TransactionHistory.put("VC_HOST_URL", endPointUlr);
						TransactionHistory.put("VC_HOST_REQUEST", requestBody);
						TransactionHistory.put("VC_HOST_RESPONSE", responseBody);
						TransactionHistory.put("VC_TRANS_STATUS", responseCode);

						//mySession.getVariableField("HashMap", "TransactionHistory").setValue(TransactionHistory);

						dataNode.setTransactionHistory(mySession);

					} catch(Exception e) {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+"\t|\t"+method+"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

					}

				} catch(Exception e) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING API FAILURE DUE TO EXCEPTION IN IVR", mySession);
					mySession.getVariableField("QA_WebService","Success").setValue(false);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | CheckPNRDetail | "+Arrays.toString(e.getStackTrace()), mySession);

				}

				//setOceanaData
				customerDetails.put("PNR",Pnrs);
				mySession.getVariableField("HashMap", "customerDetails").setValue(customerDetails);

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ customerDetails MAP $$$$"+customerDetails, mySession);

			}

		} else {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | Token is not Generated", mySession);
		}

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | setting api has started in pnr", mySession);
		setApiStatus(mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | setting api is completed in pnr", mySession);

	}

	private void setApiStatus(SCESession mySession) {

		String Successflag = "false";

		try {

			Successflag = mySession.getVariableField("QA_WebService","Success").getStringValue();
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className, mySession);
			setApiFlag(mySession, Successflag);

		} catch(Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | setApiStatus | "+Arrays.toString(e.getStackTrace()), mySession);
		}

	}

	public void checkApiDown(SCESession mySession) {

		String LinkDownFlag = "true";

		try {

			LinkDownFlag = mySession.getVariableField("QA_WebService","LinkDown").getStringValue();
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className, mySession);
			setApiFlag(mySession, LinkDownFlag);

		} catch(Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | checkApiDown | "+Arrays.toString(e.getStackTrace()), mySession);
		}
	}

	public void setApiFlag(SCESession mySession,String inputFlag) {

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className, mySession);

		try {

			HashMap<String,Object> flow= new HashMap<String,Object>();
			HashMap<String,Object> flowvalues = new HashMap<String,Object>();

			HashMap<String,String> CallHistory = new HashMap<String,String>();

			CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

			String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

			String NextNode = "";
			String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

			flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

			if(flow.containsKey(NodeDescription)) {

				flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

				if(flowvalues.get("IsEnabled").toString().equalsIgnoreCase("true")) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | NodeDescription "+NodeDescription, mySession);

					if(inputFlag.isEmpty()||inputFlag.equalsIgnoreCase("NA")) {
						inputFlag = "false";
					}

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | Status | "+inputFlag, mySession);
					if(flowvalues.containsKey(inputFlag)) {
						NodeDescription = flowvalues.get(inputFlag).toString().trim();
					}

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

			MenuDescription += inputFlag.toUpperCase()+"|";

			CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
			mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | NextNode | "+NextNode, mySession);

			mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
			mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		} catch(Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | setApiFlag | "+Arrays.toString(e.getStackTrace()), mySession);

		}

	}

	public void isCustomerIdentified(SCESession mySession) {

		String ICDFlag = "false";

		try {
			ICDFlag = mySession.getVariableField("WS_IdentifyCustomerDetails","SuccessFlag").getStringValue();
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | isCustomerIdentified Method Returns | "+ICDFlag, mySession);

		} catch(Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | isCustomerIdentified | "+Arrays.toString(e.getStackTrace()), mySession);
		}

		setApiFlag(mySession, ICDFlag);
	}

	public void CheckPreferredLang(SCESession mySession) {

		String CPLFlag = "false";


		try {

			String ICDSuccessFlag = mySession.getVariableField("WS_IdentifyCustomerDetails","SuccessFlag").getStringValue();

			if(ICDSuccessFlag.equalsIgnoreCase("true")) {
				HashMap<String, Object> MobileResponseMap = (HashMap<String, Object>) mySession.getVariableField("WS_IdentifyCustomerDetails","MobileResponseMap").getObjectValue();
				String PreferredLanguage = parseJsonResponse(MobileResponseMap,"PreferredLanguage",mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | CheckPreferredLang | PreferredLanguage | "+PreferredLanguage, mySession);

				if(!PreferredLanguage.equalsIgnoreCase("NA")) {

					CPLFlag = "true";

					try {

						//DBConnection db = new DBConnection();
						//List<HashMap<Object, Object>> resultSet = null;
						HashMap<String,String> CallHistory = new HashMap<String,String>();
						//LinkedHashMap<String, Object> input = new LinkedHashMap<String, Object>();
						HashMap<String,String> customerDetails = new HashMap<String,String>();

						customerDetails = (HashMap<String, String>) mySession.getVariableField("HashMap", "customerDetails").getObjectValue();
						CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

						String Language = "";

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+ "CheckPreferredLang\t|\tLanguage is "+PreferredLanguage, mySession);

						List<Object> languageList = (List<Object>) mySession.getVariableField("ApplicationVariable", "LanguageHashMap").getObjectValue();
						HashMap<String,String> languageMap = null;

						for(int i=0;i<languageList.size();i++) {

							languageMap = (HashMap<String, String>) languageList.get(i);
							if(languageMap.containsKey("VC_LANGUAGE_NAME")&&languageMap.containsKey("VC_LANGUAGE_1")) {

								if(PreferredLanguage.equalsIgnoreCase(languageMap.get("VC_LANGUAGE_1"))) {

									Language = languageMap.get("VC_LANGUAGE_NAME");
									TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+ "CheckPreferredLang\t|\tLanguage Received as "+Language, mySession);

									break;
								}

							}

						}

						//input.put("VC_LANGUAGE", PreferredLanguage);

						//resultSet = db.getResultSet("SP_GET_LANGUAGE_NAME", input, mySession);
						//Language = (String) resultSet.get(0).get("VC_LANGUAGE_NAME");

						mySession.getVariableField("PreferredLanguage").setValue(PreferredLanguage);
						customerDetails.put("CustomerLanguage", Language);
						CallHistory.put("VC_LANGUAGE", Language);

						mySession.getVariableField("ApplicationVariable", "Language").setValue(Language);
						mySession.getVariableField("DynamicPrompt","PromptLanguage").setValue(Language);

						mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);
						mySession.getVariableField("HashMap", "customerDetails").setValue(customerDetails);

					} catch(Exception e) {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG, className+ "CheckPreferredLang\t|\tDataBase is Un available", mySession);

					}

				}
			} 

		} catch(Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | isCustomerIdentified | "+Arrays.toString(e.getStackTrace()), mySession);

		}

		setApiFlag(mySession, CPLFlag);

	}

	public String parseJsonResponse(HashMap<String, Object> map , String key,SCESession mySession) {

		String str = "NA";
		String value;

		map = (HashMap<String, Object>)  map.get("Response");

		if(map.containsKey(key)) {

			value = (String) map.get(key);

			if(value!=null && !value.isEmpty() && value.trim()!="null") {
				str = value.trim();
			}

		} else {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | parseJsonResponse | key not present | "+key, mySession);
		}

		return str;

	}

	public String parseJsonResponse1(HashMap<String, Object> map , String key,SCESession mySession) {

		String str = "";
		String value;

		if(map.containsKey(key)) {

			value = (String) map.get(key);

			if(value!=null && !value.isEmpty() && value.trim()!="null") {
				str = value.trim();
			}

		} else {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | parseJsonResponse | key not present"+key, mySession);
		}
		return str;
	}

	public void isProfileFound(SCESession mySession) {

		String ICDFlag = "false";

		try {

			ICDFlag = mySession.getVariableField("WS_IdentifyCustomerDetails","SuccessFlag").getStringValue();

			if(ICDFlag.equalsIgnoreCase("true")) {
				ICDFlag = "true";
			}

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | isProfileFound Method Returns | "+ICDFlag, mySession);

		} catch(Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | isProfileFound | "+Arrays.toString(e.getStackTrace()), mySession);
		}

		setApiFlag(mySession, ICDFlag);
	}

	public void isIdentifiedProfile(SCESession mySession) {

		String IdentifiedProfile = "UN_IDENTIFIED";

		try {

			String ICDSuccessFlag = mySession.getVariableField("WS_IdentifyCustomerDetails","SuccessFlag").getStringValue();

			if(ICDSuccessFlag.equalsIgnoreCase("true")) {
				HashMap<String, Object> MobileResponseMap = (HashMap<String, Object>) mySession.getVariableField("WS_IdentifyCustomerDetails","MobileResponseMap").getObjectValue();
				String MemberTier = parseJsonResponse(MobileResponseMap,"MemberTier",mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | checkMemberTier | MemberTier | "+MemberTier, mySession);

				if(!MemberTier.equalsIgnoreCase("NA")) {

					String GoldKey = LoadApplicationProperties.getProperty("GOLD_TIER", mySession);
					String PlatinumKey = LoadApplicationProperties.getProperty("PLATINUM_TIER", mySession);
					String SilverKey = LoadApplicationProperties.getProperty("SILVER_TIER", mySession);
					String BurgundyKey = LoadApplicationProperties.getProperty("BURGUNDY_TIER", mySession);
					String OneWorldKey = LoadApplicationProperties.getProperty("ONE_WORLD_TIER", mySession);

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | checkMemberTier | Gold tier GOLD_TIER name from ext props | "+GoldKey, mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | checkMemberTier | Platinum tier PLATINUM_TIER name from ext props | "+PlatinumKey, mySession);

					if(MemberTier.equalsIgnoreCase(GoldKey)) {
						IdentifiedProfile = "FFP";
					} else if(MemberTier.equalsIgnoreCase(PlatinumKey)) {
						IdentifiedProfile = "FFP";
					} else if(MemberTier.equalsIgnoreCase(SilverKey)) {
						IdentifiedProfile = "FFP";
					} else if(MemberTier.equalsIgnoreCase(BurgundyKey)) {
						IdentifiedProfile = "FFP";
					} else if(MemberTier.equalsIgnoreCase(OneWorldKey)) {
						IdentifiedProfile = "FFP";
					}

				}

			}

		} catch(Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | checkMemberTier | "+Arrays.toString(e.getStackTrace()), mySession);

		}

		setApiFlag(mySession, IdentifiedProfile);

	}

	public void isDisrupted(SCESession mySession) {

		String isDistrupted = "false";

		try {
			String ICDSuccessFlag = mySession.getVariableField("WS_IdentifyCustomerDetails","SuccessFlag").getStringValue();

			if(ICDSuccessFlag.equalsIgnoreCase("true")) {

				HashMap<String,String> customerDetails = new HashMap<String,String>();
				customerDetails = (HashMap<String, String>) mySession.getVariableField("HashMap", "customerDetails").getObjectValue();

				String DisruptionStatus = customerDetails.get("DisruptionStatus");
				String TravelWithin48Hours = customerDetails.get("TravelWithin48Hours");

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | isDistrupted | DisruptionStatus | "+DisruptionStatus, mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | isDistrupted | TravelWithin48Hours | "+TravelWithin48Hours, mySession);

				if(DisruptionStatus.equalsIgnoreCase("Y")&&TravelWithin48Hours.equalsIgnoreCase("Y")) {
					isDistrupted = "true";
				}

			} 

		} catch(Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | isDistrupted | "+Arrays.toString(e.getStackTrace()), mySession);

		}
		setApiFlag(mySession, isDistrupted);
	}

	public void isFFNoMatching(SCESession mySession) {

		String isFFPMatched = "false";
		String FFPNumberFromUser = "";
		HashMap<String, Object> MobileResponseMap = null;

		try {
			String ICDSuccessFlag = mySession.getVariableField("WS_IdentifyCustomerDetails","SuccessFlag").getStringValue();

			if(ICDSuccessFlag.equalsIgnoreCase("true")) {

				MobileResponseMap = (HashMap<String, Object>) mySession.getVariableField("WS_IdentifyCustomerDetails","FfpResponseMap").getObjectValue();
				String FFPNumber = parseJsonResponse(MobileResponseMap,"FFPNumber",mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | isFFNoMatching | FFPNumber | "+FFPNumber, mySession);

				FFPNumberFromUser = mySession.getVariableField("QA_WebService","FFPNumber").getStringValue();
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | isFFNoMatching | FFPNumberFromUser | "+FFPNumberFromUser, mySession);

				if(FFPNumber.equalsIgnoreCase(FFPNumberFromUser)) {
					isFFPMatched = "true";
				}

			}

			if(isFFPMatched.equalsIgnoreCase("true")) {

				MobileResponseMap = (HashMap<String, Object>) MobileResponseMap.get("Response");

				boolean status = setOcdsData(MobileResponseMap, mySession);

				mySession.getVariableField("ApplicationVariable", "NoInput").setValue("1");
				mySession.getVariableField("ApplicationVariable", "NoMatch").setValue("1");
				mySession.getVariableField("ApplicationVariable", "apiNoInput").setValue(1);
				mySession.getVariableField("ApplicationVariable", "apiNoMatch").setValue(1);

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | Setting Data to OCDS | "+status, mySession);

			}

		} catch(Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | isFFNoMatching | "+Arrays.toString(e.getStackTrace()), mySession);

		}
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | isFFNoMatching | isFFNoMatching | "+isFFPMatched, mySession);
		setApiFlag(mySession, isFFPMatched);



	}

	public void isIataMatching(SCESession mySession) {

		String isIataMatched = "false";
		String IATANumber = "";
		HashMap<String, Object> jsonResposeMap = null;
		String CustomerID = "";
		String CustomerTier = "TRAVELAGENT";
		HashMap<String,String> customerDetails = new HashMap<String,String>();
		customerDetails = (HashMap<String, String>) mySession.getVariableField("HashMap", "customerDetails").getObjectValue();

		try {

			jsonResposeMap = (HashMap<String, Object>) mySession.getVariableField("WS_IdentifyCustomerDetails","IataResponseMap").getObjectValue();
			String ICDSuccessFlag = mySession.getVariableField("WS_IdentifyCustomerDetails","SuccessFlag").getStringValue();

			if(ICDSuccessFlag.equalsIgnoreCase("true")) {

				CustomerID = parseJsonResponse(jsonResposeMap, "CustomerID", mySession);
				if(!CustomerID.equalsIgnoreCase("NA")) {

					IATANumber = mySession.getVariableField("QA_WebService","IATANumber").getStringValue();
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | isIataMatching | IATANumber | "+IATANumber, mySession);				
					isIataMatched = "true";

				}

			}

		} catch(Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | isIataNoMatching | "+Arrays.toString(e.getStackTrace()), mySession);

		}

		if(isIataMatched.equalsIgnoreCase("true")) {

			jsonResposeMap = (HashMap<String, Object>) jsonResposeMap.get("Response");

			boolean status = setOcdsData(jsonResposeMap, mySession);

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | Setting Data to OCDS | "+status, mySession);

			mySession.getVariableField("ApplicationVariable", "NoInput").setValue("1");
			mySession.getVariableField("ApplicationVariable", "NoMatch").setValue("1");
			mySession.getVariableField("ApplicationVariable", "apiNoInput").setValue(1);
			mySession.getVariableField("ApplicationVariable", "apiNoMatch").setValue(1);

			//SetCustomerDetails
			customerDetails.put("IsVerifiedCustomer", "Y");
			customerDetails.put("IATANumber", IATANumber);

		}

		mySession.getVariableField("CustTier").setValue(CustomerTier);
		customerDetails.put("CustomerTier",CustomerTier);
		mySession.getVariableField("HashMap", "customerDetails").setValue(customerDetails);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | isIataNoMatching | isIataNoMatching | "+isIataMatched, mySession);
		setApiFlag(mySession, isIataMatched);
	}

	public void isFFPPinMatched(SCESession mySession) {

		String isFFPPinMatched = "false";
		String DobFromUser = "";
		HashMap<String,String> DBAppValues = new HashMap<String,String>();
		String ResponseType = mySession.getVariableField("WS_IdentifyCustomerDetails","RequestType").getStringValue();

		try {

			HashMap<String, Object> ResponseMap = null;
			DBAppValues = (HashMap<String, String>) mySession.getVariableField("ApplicationVariable", "DBAppValues").getObjectValue();

			HashMap<String,String> customerDetails = new HashMap<String,String>();
			customerDetails = (HashMap<String, String>) mySession.getVariableField("HashMap", "customerDetails").getObjectValue();

			String Market = DBAppValues.get("VC_MARKET");

			String ICDSuccessFlag = mySession.getVariableField("WS_IdentifyCustomerDetails","SuccessFlag").getStringValue();

			if(ICDSuccessFlag.equalsIgnoreCase("true")) {

				if(ResponseType.equalsIgnoreCase("Mobile")) {
					ResponseMap = (HashMap<String, Object>) mySession.getVariableField("WS_IdentifyCustomerDetails","MobileResponseMap").getObjectValue();
				} else if (ResponseType.equalsIgnoreCase("FFP")) {
					ResponseMap = (HashMap<String, Object>) mySession.getVariableField("WS_IdentifyCustomerDetails","FfpResponseMap").getObjectValue();
				}

				String DobFromRes = parseJsonResponse(ResponseMap,"DoB",mySession);
				String MarketFromProperty = LoadApplicationProperties.getProperty("FFB_DOB_MARKET", mySession);

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | isFFPPinMatched | DobFromRes | "+DobFromRes, mySession);
				String[] arr = DobFromRes.split("-");

				if(MarketFromProperty.contains(Market)) {
					DobFromRes = arr[1]+arr[2].substring(0,2)+arr[0].substring(2,4);
				} else {
					DobFromRes = arr[2].substring(0,2)+arr[1]+arr[0].substring(2,4);
				}

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | isFFPPinMatched | Market | "+Market, mySession);

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | isFFPPinMatched | ConvertedDobFromRes | "+DobFromRes, mySession);

				DobFromUser = mySession.getVariableField("QA_WebService","DateOfBirth").getStringValue();
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | isFFPPinMatched | DobFromUser | "+DobFromUser, mySession);

				if(DobFromRes.equalsIgnoreCase(DobFromUser)) {
					isFFPPinMatched = "true";
					customerDetails.put("IsVerifiedCustomer", "Y");

					mySession.getVariableField("ApplicationVariable", "NoInput").setValue("1");
					mySession.getVariableField("ApplicationVariable", "NoMatch").setValue("1");
					mySession.getVariableField("ApplicationVariable", "apiNoInput").setValue(1);
					mySession.getVariableField("ApplicationVariable", "apiNoMatch").setValue(1);

				}
				mySession.getVariableField("HashMap", "customerDetails").setValue(customerDetails);

			} 

		} catch(Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | isFFPPinMatched | "+Arrays.toString(e.getStackTrace()), mySession);

		}
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | isFFPPinMatched | FFPPINMATCHED | "+DobFromUser, mySession);

		setApiFlag(mySession, isFFPPinMatched);




	}

	public void isStaffIDMatching(SCESession mySession) {

		String isStaffIDMatched = "false";
		String staffIDFromUser = "";
		HashMap<String, Object> StaffIDResponseMap = null;
		HashMap<String,String> customerDetails = new HashMap<String,String>();
		customerDetails = (HashMap<String, String>) mySession.getVariableField("HashMap", "customerDetails").getObjectValue();

		try {
			String StaffIDSuccessFlag = mySession.getVariableField("WS_IdentifyStaff","SuccessFlag").getStringValue();

			if(StaffIDSuccessFlag.equalsIgnoreCase("true")) {

				StaffIDResponseMap = (HashMap<String, Object>) mySession.getVariableField("WS_IdentifyStaff","responseMap").getObjectValue();
				String staffID = parseJsonResponse(StaffIDResponseMap,"StaffNumber",mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | isStaffIDMatching | Staff ID | "+staffID, mySession);

				staffIDFromUser = mySession.getVariableField("QA_WebService","StaffNumber").getStringValue().trim();
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | isStaffIDMatching | staffIDFromUser | "+staffIDFromUser, mySession);

				if(staffID.equalsIgnoreCase(staffIDFromUser)) {

					isStaffIDMatched = "true";
					mySession.getVariableField("ApplicationVariable", "NoInput").setValue("1");
					mySession.getVariableField("ApplicationVariable", "NoMatch").setValue("1");
					mySession.getVariableField("ApplicationVariable", "apiNoInput").setValue(1);
					mySession.getVariableField("ApplicationVariable", "apiNoMatch").setValue(1);

					//SetCustomerDetails
					customerDetails.put("StaffID", staffIDFromUser);
				}

			}

		} catch(Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | isStaffIDMatching | "+Arrays.toString(e.getStackTrace()), mySession);

		}
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | isStaffIDMatching | "+isStaffIDMatched, mySession);

		mySession.getVariableField("HashMap", "customerDetails").setValue(customerDetails);
		setApiFlag(mySession, isStaffIDMatched);
	}

	public void isStaffDOJMatched(SCESession mySession) {

		String isStaffDOJMatched = "false";

		try {

			String ICDSuccessFlag = mySession.getVariableField("WS_VerifyStaff","SuccessFlag").getStringValue();

			if(ICDSuccessFlag.equalsIgnoreCase("true")) {



				HashMap<String, Object> VerifyStaffResponseMap = (HashMap<String, Object>) mySession.getVariableField("WS_VerifyStaff","responseMap").getObjectValue();
				String isVerified = parseJsonResponse(VerifyStaffResponseMap,"IsValidStaff",mySession);

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | isStaffVerified | "+isVerified, mySession);

				if(isVerified.equalsIgnoreCase("Y")) {

					isStaffDOJMatched = "true";

					mySession.getVariableField("ApplicationVariable", "NoInput").setValue("1");
					mySession.getVariableField("ApplicationVariable", "NoMatch").setValue("1");
					mySession.getVariableField("ApplicationVariable", "apiNoInput").setValue(1);
					mySession.getVariableField("ApplicationVariable", "apiNoMatch").setValue(1);

				} else {
					String Error = parseJsonResponse(VerifyStaffResponseMap,"Error",mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | Error | "+Error, mySession);
				}

			} 

		} catch(Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | isStaffVerified | "+Arrays.toString(e.getStackTrace()), mySession);

		}

		setApiFlag(mySession, isStaffDOJMatched);

	}

	public void checkMemberTier(SCESession mySession) {

		String checkMemberTierFlag = "GREY";

		try {
			String ICDSuccessFlag = mySession.getVariableField("WS_IdentifyCustomerDetails","SuccessFlag").getStringValue();

			if(ICDSuccessFlag.equalsIgnoreCase("true")) {

				HashMap<String, Object> MobileResponseMap = new HashMap<String, Object>();
				//(HashMap<String, Object>) mySession.getVariableField("WS_IdentifyCustomerDetails","FfpResponseMap").getObjectValue();

				String Flowtype = mySession.getVariableField("WS_IdentifyCustomerDetails","RequestType").getStringValue();

				if(Flowtype.equalsIgnoreCase("Mobile")) {
					MobileResponseMap = (HashMap<String, Object>) mySession.getVariableField("WS_IdentifyCustomerDetails","MobileResponseMap").getObjectValue();
				} else if(Flowtype.equalsIgnoreCase("FFP")) {
					MobileResponseMap = (HashMap<String, Object>) mySession.getVariableField("WS_IdentifyCustomerDetails","FfpResponseMap").getObjectValue();
				}

				String MemberTier = parseJsonResponse(MobileResponseMap,"MemberTier",mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | checkMemberTier | MemberTier | "+MemberTier, mySession);

				if(!MemberTier.equalsIgnoreCase("NA")) {

					String GoldKey = LoadApplicationProperties.getProperty("GOLD_TIER", mySession);
					String PlatinumKey = LoadApplicationProperties.getProperty("PLATINUM_TIER", mySession);
					String SilverKey = LoadApplicationProperties.getProperty("SILVER_TIER", mySession);
					String BurgundyKey = LoadApplicationProperties.getProperty("BURGUNDY_TIER", mySession);
					String OneWorldKey = LoadApplicationProperties.getProperty("ONEWORLD_TIER", mySession);

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | checkMemberTier | Gold tier GOLD_TIER name from ext props | "+GoldKey, mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | checkMemberTier | Platinum tier PLATINUM_TIER name from ext props | "+PlatinumKey, mySession);

					if(MemberTier.equalsIgnoreCase(GoldKey)) {
						checkMemberTierFlag = "GOLD";
					} else if(MemberTier.equalsIgnoreCase(PlatinumKey)) {
						checkMemberTierFlag = "PLATINUM";
					} else if(MemberTier.equalsIgnoreCase(SilverKey)) {
						checkMemberTierFlag = "SILVER";
					} else if(MemberTier.equalsIgnoreCase(BurgundyKey)) {
						checkMemberTierFlag = "BURGUNDY";
					} else if(MemberTier.equalsIgnoreCase(OneWorldKey)) {
						checkMemberTierFlag = "ONEWORLD";
					}


				}

			}

		} catch(Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | checkMemberTier | "+Arrays.toString(e.getStackTrace()), mySession);

		}
		setApiFlag(mySession, checkMemberTierFlag);



	}

	public void checkEnglishAgentAvail24(SCESession mySession) {

		CheckBusinessHours cbh = new CheckBusinessHours();
		String CEAAvail = "false";
		String WorkingDay = "";
		String WorkingHour = "";
		String Language = "ENGLISH";
		HashMap<String,Object> Lang = new HashMap<String,Object>();
		HashMap<String,String> CallHistory = new HashMap<String,String>();
		//HashMap<String,String> customerDetails = new HashMap<String,String>();

		//customerDetails = (HashMap<String, String>) mySession.getVariableField("HashMap", "customerDetails").getObjectValue();
		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		try {

			String ZoneID = CallHistory.get("VC_TIME_ZONE");
			Calendar NativeTime = new GregorianCalendar(TimeZone.getTimeZone(ZoneID));
			int int_day = NativeTime.get(Calendar.DAY_OF_WEEK);
			String CurrentDay= cbh.GetDay(int_day);

			Lang = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Language").getObjectValue();

			List<langauge> languages = (List<langauge>) Lang.get("Language");

			for(int i=0;i<languages.size();i++) {

				if(languages.get(i).getName().trim().equalsIgnoreCase(Language)) {

					WorkingDay = languages.get(i).getBusinessHours().getLangWorkingDay().toString().trim();
					WorkingHour = languages.get(i).getBusinessHours().getLangWorkingHour().toString().trim();
					break;

				}

			}

			if (WorkingDay.equalsIgnoreCase("CLOSED")) {
				CEAAvail = "false";
			} else if(WorkingDay.equalsIgnoreCase("ALL")&&WorkingHour.equalsIgnoreCase("ALL")) {
				CEAAvail = "true";
			} else if(WorkingDay.contains(CurrentDay)) {

				String[] days = WorkingDay.split(",");
				String[] time = WorkingHour.split(",");

				if(days.length==time.length) {

					for(int i=0;i<days.length;i++) {

						if(days[i].equalsIgnoreCase(CurrentDay)) {

							if(time[i].equalsIgnoreCase("00:00-23:59")) {
								CEAAvail = "true";
								break;
							}

						}

					}

				}

			}

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | checkEnglishAgentAvail24 Method Returns | "+CEAAvail, mySession);

		} catch(Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | checkEnglishAgentAvail24 | "+Arrays.toString(e.getStackTrace()), mySession);
		}


		/*if(CEAAvail.equalsIgnoreCase("true")) {

			customerDetails.put("CustomerLanguage", Language);
			CallHistory.put("VC_LANGUAGE", Language);

			mySession.getVariableField("ApplicationVariable", "Language").setValue(Language);
			mySession.getVariableField("DynamicPrompt","PromptLanguage").setValue(Language);

			mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);
			mySession.getVariableField("HashMap", "customerDetails").setValue(customerDetails);
		}*/

		setApiFlag(mySession, CEAAvail);

	}

	public void checkEnglishAvailability(SCESession mySession) {

		CheckBusinessHours cbh = new CheckBusinessHours();
		String CEAAvail = "false";
		String WorkingDay = "";
		String WorkingHour = "";
		String Language = "ENGLISH";
		HashMap<String,Object> Lang = new HashMap<String,Object>();
		HashMap<String,String> CallHistory = new HashMap<String,String>();
		HashMap<String,String> customerDetails = new HashMap<String,String>();

		customerDetails = (HashMap<String, String>) mySession.getVariableField("HashMap", "customerDetails").getObjectValue();
		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		try {

			String ZoneID = CallHistory.get("VC_TIME_ZONE");
			Calendar NativeTime = new GregorianCalendar(TimeZone.getTimeZone(ZoneID));
			int int_day = NativeTime.get(Calendar.DAY_OF_WEEK);
			String CurrentDay= cbh.GetDay(int_day);

			Lang = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Language").getObjectValue();

			List<langauge> languages = (List<langauge>) Lang.get("Language");

			for(int i=0;i<languages.size();i++) {

				if(languages.get(i).getName().trim().equalsIgnoreCase(Language)) {

					WorkingDay = languages.get(i).getBusinessHours().getLangWorkingDay().toString().trim();
					WorkingHour = languages.get(i).getBusinessHours().getLangWorkingHour().toString().trim();
					break;

				}

			}

			if (WorkingDay.equalsIgnoreCase("CLOSED")) {
				CEAAvail = "false";
			} else if(WorkingDay.equalsIgnoreCase("ALL")&&WorkingHour.equalsIgnoreCase("ALL")) {
				CEAAvail = "true";
			} else if(WorkingDay.contains(CurrentDay)) {

				String[] days = WorkingDay.split(",");
				String[] time = WorkingHour.split(",");

				if(days.length==time.length) {

					for(int i=0;i<days.length;i++) {

						if(days[i].equalsIgnoreCase(CurrentDay)) {

							if(time[i].equalsIgnoreCase("00:00-23:59")) {
								CEAAvail = "true";
								break;
							}

						}

					}

				}

			}

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | checkEnglishAgentAvail24 Method Returns | "+CEAAvail, mySession);

		} catch(Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | checkEnglishAgentAvail24 | "+Arrays.toString(e.getStackTrace()), mySession);
		}


		if(CEAAvail.equalsIgnoreCase("true")) {

			customerDetails.put("CustomerLanguage", Language);

			mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);
			mySession.getVariableField("HashMap", "customerDetails").setValue(customerDetails);
		}

		setApiFlag(mySession, CEAAvail);

	}

	public void checkCountryCode(String CLID, SCESession mySession) {

		String method = "checkCountryCode";
		mySession.getVariableField("MobileNumber").setValue(null);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "Start", mySession);

		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		String countryCode ="";
		String phInput = "";
		HashMap<String,String> Response = new HashMap<String,String>();
		String AppServer = "";
		HashMap<String,String> CallHistory = new HashMap<String,String>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();
		String appIP = CallHistory.get("VC_APP_SRVR_IP");
		String UKLocation = LoadApplicationProperties.getProperty("UKLocation", mySession);
		String QDCLocation = LoadApplicationProperties.getProperty("QDCLocation", mySession);
		String AMDLocation = LoadApplicationProperties.getProperty("AMDLocation", mySession);

		if(UKLocation.contains(appIP)) {
			AppServer = "UK";
		} else if(QDCLocation.contains(appIP)) {
			AppServer = "QDC";
		} else if(AMDLocation.contains(appIP)) {
			AppServer = "AMD";
		}

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "Actual CLID | "+CLID, mySession);
		try {

			if(CLID.contains("+")) {

				PhoneNumber ph = phoneUtil.parse(CLID, "");

				phInput = String.valueOf(ph.getNationalNumber());
				countryCode = "+" + String.valueOf(ph.getCountryCode());

			} else {

				if(CLID.startsWith("0")) {

					CLID = CLID.replaceFirst("0", "");
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "0 Replaced CLID | " + CLID, mySession);

				}

				if(AppServer.equalsIgnoreCase("UK")) {

					if(CLID.startsWith("0")) {

						CLID = CLID.replaceFirst("0", "");
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "0 Replaced CLID | UK | " + CLID, mySession);

					}

					String newCLID = "+" + CLID;
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "newCLID | UK | " + newCLID, mySession);

					PhoneNumber ph2 = phoneUtil.parse(newCLID, "");

					phInput = String.valueOf(ph2.getNationalNumber());
					countryCode = "+" + String.valueOf(ph2.getCountryCode());

				} else if(AppServer.equalsIgnoreCase("QDC")) {

					if(CLID.startsWith("0")) {

						CLID = CLID.replaceFirst("0", "");
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "0 Replaced CLID | QDC| " + CLID, mySession);

						String newCLID = "+" + CLID;
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "newCLID | QDC | " + newCLID, mySession);

						PhoneNumber ph2 = phoneUtil.parse(newCLID, "");

						phInput = String.valueOf(ph2.getNationalNumber());
						countryCode = "+" + String.valueOf(ph2.getCountryCode());

					} else {

						countryCode = "+974";
						phInput = CLID;
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "newCLID | QDC | " + countryCode+phInput, mySession);

					}

				} else if(AppServer.equalsIgnoreCase("AMD")) {

					if(CLID.length() == 10) {

						System.out.println("Market is India");
						countryCode = "+91";
						phInput = CLID;
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "newCLID | AMD | " + countryCode+phInput, mySession);

					} else {

						String newCLID = "+" + CLID;
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "newCLID | AMD | " + newCLID, mySession);

						PhoneNumber ph2 = phoneUtil.parse(newCLID, "");

						phInput = String.valueOf(ph2.getNationalNumber());
						countryCode = "+" + String.valueOf(ph2.getCountryCode());

					}

				}

			}

			Response.put("PhoneNumber", phInput);
			Response.put("CountryCode", countryCode);

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "phInput | "+phInput, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "countryCode | "+countryCode, mySession);

			mySession.getVariableField("MobileNumber").setValue(Response);

		} catch(Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+"\t|\t"+method+"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2 , mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+"\t|\t"+method+"\t|\t"+ Arrays.toString(e.getStackTrace()), mySession);

		}

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "End", mySession);

	}

	public boolean setOcdsData(HashMap<String, Object> jsonResposeMap, SCESession mySession) {

		String method = "setOcdsData";


		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "Start", mySession);

		boolean status = false;

		try {

			HashMap<String,String> customerDetails = new HashMap<String,String>();
			customerDetails = (HashMap<String, String>) mySession.getVariableField("HashMap", "customerDetails").getObjectValue();

			String CustomerID = parseJsonResponse1(jsonResposeMap, "CustomerID", mySession);

			if(CustomerID!=null&&!CustomerID.isEmpty()) {

				String CustomerName = "";
				String CustomerTier = "";
				String DefaultCustomerTier = mySession.getVariableField("CustTier").getStringValue();
				String CustomerStatus = "";
				String Blacklisted = "NO";
				String IsIdentifiedCustomer = "N";
				String DisruptionStatus = "N";
				String IsPreferredLanguage = "NO";
				String TravelWithin48Hours = "N";
				String PreferredLanguage = "";
				String FFPNumber = "";

				String FirstName = parseJsonResponse1(jsonResposeMap, "FirstName", mySession);
				String MiddleName = parseJsonResponse1(jsonResposeMap, "MiddleName", mySession);
				String LastName = parseJsonResponse1(jsonResposeMap, "LastName", mySession);
				CustomerTier = parseJsonResponse1(jsonResposeMap, "MemberTier", mySession);   
				CustomerStatus = parseJsonResponse1(jsonResposeMap, "MemberStatus", mySession);
				Blacklisted = parseJsonResponse1(jsonResposeMap, "Blacklisted", mySession);
				//DisruptionStatus = parseJsonResponse1(jsonResposeMap, "Disrupted", mySession);
				//IsPreferredLanguage = parseJsonResponse1(jsonResposeMap, "PreferredLanguage", mySession);
				PreferredLanguage = parseJsonResponse1(jsonResposeMap, "PreferredLanguage", mySession);
				//TravelWithin48Hours = parseJsonResponse1(jsonResposeMap, "TravelWithin48Hrs", mySession);
				FFPNumber = parseJsonResponse1(jsonResposeMap, "FFPNumber", mySession);

				CustomerName = (!FirstName.isEmpty()?
						(!MiddleName.isEmpty()?
								(!LastName.isEmpty()?FirstName+" "+MiddleName+" "+LastName:FirstName+" "+MiddleName)
								:(!LastName.isEmpty()?FirstName+" "+LastName:FirstName))
						:(!MiddleName.isEmpty()?
								(!LastName.isEmpty()?MiddleName+" "+LastName:MiddleName)
								:(!LastName.isEmpty()?LastName:"")));

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | PreferredLanguage |"+PreferredLanguage, mySession);

				CustomerTier = (CustomerTier!=null&&!CustomerTier.isEmpty()&&!CustomerTier.equalsIgnoreCase("null"))?CustomerTier:DefaultCustomerTier;
				//CustomerStatus = CustomerStatus!=null&&!CustomerStatus.isEmpty()&&!CustomerStatus.equalsIgnoreCase("null")?CustomerStatus:"INACTIVE";
				Blacklisted = Blacklisted!=null&&!Blacklisted.isEmpty()&&!Blacklisted.equalsIgnoreCase("null")?(Blacklisted.equalsIgnoreCase("Y")?"YES":"NO"):"NO";
				//DisruptionStatus = DisruptionStatus!=null&&!DisruptionStatus.isEmpty()&&!DisruptionStatus.equalsIgnoreCase("null")?DisruptionStatus:"N";
				//TravelWithin48Hours = TravelWithin48Hours!=null&&!TravelWithin48Hours.isEmpty()&&!TravelWithin48Hours.equalsIgnoreCase("null")?TravelWithin48Hours:"N";
				PreferredLanguage = PreferredLanguage!=null&&!PreferredLanguage.isEmpty()&&!PreferredLanguage.equalsIgnoreCase("null")?PreferredLanguage:"";
				IsIdentifiedCustomer = "Y";

				String CustomerTierFromProperty = LoadApplicationProperties.getProperty(CustomerTier, mySession);
				if(CustomerTierFromProperty.equalsIgnoreCase("NA")) {
					if(CustomerTier.isEmpty()) {
						CustomerTier = DefaultCustomerTier;
					}
				} else {
					CustomerTier = CustomerTierFromProperty;	
				}

				String Flowtype = mySession.getVariableField("WS_IdentifyCustomerDetails","RequestType").getStringValue();

				if(Flowtype.equalsIgnoreCase("FFP")) {

					String language = mySession.getVariableField("ApplicationVariable", "Language").getStringValue();
					String languageFromJson = "";

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+ "CheckPreferredLang\t|\tLanguage selected by Caller "+language, mySession);

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+"CheckPreferredLang\t|\tLanguage is "+PreferredLanguage, mySession);

					List<Object> languageList = (List<Object>) mySession.getVariableField("ApplicationVariable", "LanguageHashMap").getObjectValue();
					HashMap<String,String> languageMap = null;

					for(int i=0;i<languageList.size();i++) {

						languageMap = (HashMap<String, String>) languageList.get(i);
						if(languageMap.containsKey("VC_LANGUAGE_NAME")&&languageMap.containsKey("VC_LANGUAGE_1")) {

							if(PreferredLanguage.equalsIgnoreCase(languageMap.get("VC_LANGUAGE_1"))) {

								languageFromJson = languageMap.get("VC_LANGUAGE_NAME");
								TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+"CheckPreferredLang\t|\tLanguage Received as "+languageFromJson, mySession);

								break;
							}

						}

					}

					if(language.equalsIgnoreCase(languageFromJson)) {
						IsPreferredLanguage = "YES";
					}

				} else {
					IsPreferredLanguage = PreferredLanguage!=null&&!PreferredLanguage.isEmpty()&&!PreferredLanguage.equalsIgnoreCase("null")?"YES":"NO";
				}

				//setOceanaData
				customerDetails.put("FFPNumber", FFPNumber);
				customerDetails.put("CustomerName", CustomerName);
				customerDetails.put("CustomerTier",CustomerTier);
				mySession.getVariableField("CustTier").setValue(CustomerTier);
				customerDetails.put("CustomerStatus",CustomerStatus);
				customerDetails.put("IsIdentifiedCustomer",IsIdentifiedCustomer);
				customerDetails.put("IsBlacklisted",Blacklisted);
				customerDetails.put("DisruptionStatus",DisruptionStatus);
				customerDetails.put("IsPreferredLanguage",IsPreferredLanguage);
				customerDetails.put("TravelWithin48Hours",TravelWithin48Hours);

				mySession.getVariableField("HashMap", "customerDetails").setValue(customerDetails);

				mySession.getVariableField("PreferredLanguage").setValue(PreferredLanguage);

				status = true;

			}

		} catch(Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+"\t|\t"+method+"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+"\t|\t"+method+"\t|\t"+ Arrays.toString(e.getStackTrace()), mySession);

		}
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "End", mySession);

		return status;

	}

	private boolean setOcdsDataUsingCustomerDetails(HashMap<String, Object> jsonResposeMap, SCESession mySession) {

		String method = "setOcdsDataUsingCustomerDetails";


		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "Start", mySession);

		boolean status = false;

		try {

			HashMap<String,String> customerDetails = new HashMap<String,String>();
			customerDetails = (HashMap<String, String>) mySession.getVariableField("HashMap", "customerDetails").getObjectValue();

			String CustomerID = parseJsonResponse1(jsonResposeMap, "CustomerID", mySession);

			if(CustomerID!=null&&!CustomerID.isEmpty()) {

				String DisruptionStatus = "N";
				String TravelWithin48Hours = "N";

				DisruptionStatus = parseJsonResponse1(jsonResposeMap, "Disrupted", mySession);
				TravelWithin48Hours = parseJsonResponse1(jsonResposeMap, "TravelWithin48Hrs", mySession);

				DisruptionStatus = DisruptionStatus!=null&&!DisruptionStatus.isEmpty()&&!DisruptionStatus.equalsIgnoreCase("null")?DisruptionStatus:"N";
				TravelWithin48Hours = TravelWithin48Hours!=null&&!TravelWithin48Hours.isEmpty()&&!TravelWithin48Hours.equalsIgnoreCase("null")?TravelWithin48Hours:"N";

				//setOceanaData
				customerDetails.put("DisruptionStatus",DisruptionStatus);
				customerDetails.put("TravelWithin48Hours",TravelWithin48Hours);

				mySession.getVariableField("HashMap", "customerDetails").setValue(customerDetails);

				status = true;

			}

		} catch(Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+"\t|\t"+method+"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+"\t|\t"+method+"\t|\t"+ Arrays.toString(e.getStackTrace()), mySession);

		}
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "End", mySession);

		return status;

	}

	public void checkEwtPrompt(SCESession mySession) {
		String flag = "false";
		String MethodName = "checkEwtPrompt";


		try {

			Set set = new Set();
			flag = set.EWT(mySession);


		} catch(Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | "+MethodName+" | "+Arrays.toString(e.getStackTrace()), mySession);
		}
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+MethodName+" | "+flag, mySession);


		setApiFlag(mySession, flag);
	}

	public void updateCustomerProfile(SCESession mySession) {
		String flag = "false";
		String MethodName = "checkEwtPrompt";


		try {

			Set set = new Set();
			flag = set.EWT(mySession);


		} catch(Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | "+MethodName+" | "+Arrays.toString(e.getStackTrace()), mySession);
		}
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+MethodName+" | "+flag, mySession);


		setApiFlag(mySession, flag);
	}

	public void sendSMS(SCESession mySession, String message) {
		
		String method = "TriggerVBstatusSMS";
		successCode = LoadApplicationProperties.getProperty("SMS_SUCCESS_CODE", mySession);
		errorCode = LoadApplicationProperties.getProperty("SMS_ERROR_CODE", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ ********IVR API CALL START ********  | IVR METHOD NAME -> s  ", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR CALLING API FROM EXTERNAL JAR    | JAR_NAME        -> QatarWebRequestor  ", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR CALLING METHOD FROM EXTERNAL JAR | JAR METHOD_NAME- > sendSMS ", mySession);
		
	     TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"\t|\tsms to be sent  \t|\t"+ message,mySession);
			
			String countryCode = parseString(mySession.getVariableField("customerdetails","countrycode").getStringValue(), mySession);
			String PhoneNumber = parseString(mySession.getVariableField("customerdetails","mobile").getStringValue(), mySession);
			String requestBody ="";
			HashMap<String,String>  App_Prop = new HashMap<String, String>();
			App_Prop = (HashMap<String, String>) mySession.getVariableField("HashMap", "PropertyValue").getObjectValue();
			if("".equalsIgnoreCase(countryCode.trim()) || "NA".equalsIgnoreCase(countryCode) || "null".equalsIgnoreCase(countryCode) || countryCode ==null) {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"\t|\t sms not sent due to invalid country code  \t|\t",mySession);
			}else {
				if("".equalsIgnoreCase(PhoneNumber.trim()) || "NA".equalsIgnoreCase(PhoneNumber) || "null".equalsIgnoreCase(PhoneNumber) || PhoneNumber ==null) {
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"\t|\t sms not sent due to invalid phone number  \t|\t",mySession);
				}else {
					if(!countryCode.contains("+")) {
						countryCode = "+"+countryCode;
					}
					
						requestBody = App_Prop.get("SMS_CONTENT");
						requestBody = requestBody.replace("{CountryCode}", countryCode);
						requestBody = requestBody.replace("{MobileNumber}", PhoneNumber);
						requestBody = requestBody.replace("{VBStatus}", message);
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"\t|\t Request framed  \t|\t"+ requestBody,mySession);
				}
			}
			

		DataNode dataNode = new DataNode();

		String responseBody = "";
		String responseCode = "";
		String StartDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
		String EndDate = "";
		String url = LoadApplicationProperties.getProperty("Qatar.webservices.endPoint.url", mySession);
		String endPointUlr= url+LoadApplicationProperties.getProperty("sendsms", mySession);
		boolean tokenStatus = false;

		HashMap<String, Object> jsonResposeMap = null;

		mySession.getVariableField("QA_WebService","Success").setValue(false);
		mySession.getVariableField("QA_WebService","LinkDown").setValue(false);
		tokenStatus = mySession.getVariableField("TokenStatus").getBooleanValue();

		if(tokenStatus) {


			try {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR SENDING REQUEST TO EXT JAR |  FRAMED JSON REQUEST IN IVR | ->  "+requestBody, mySession);
				responseBody = "";//qwr.sendSMS(requestBody,mySession);


				String isAuthentication=LoadApplicationProperties.getProperty("Qatar.webservices.isAuthentication", mySession);
				int connectionTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("Qatar.webservices.connectionTimeout", mySession));
				int readTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("Qatar.webservices.readTimeout", mySession));

				try {

					WebServices webServices= new WebServices();
					responseBody=webServices.Webservice(mySession,endPointUlr, "POST",requestBody, connectionTimeOut, readTimeOut,isAuthentication);

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+method+" | Send Sms is called", mySession);

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+method+"Response: " + responseBody,mySession);


				} catch(Exception e) {
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+method+ " EXCEPTION: " +Arrays.toString(e.getStackTrace()), mySession);
				}


				EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR RECEIVED RESPONSE FROM EXT JAR |  RESPONSE JSON FROM JAR | ->  "+responseBody, mySession);

				if(responseBody!=null) {

					responseCode = "0";

					jsonResposeMap = new ObjectMapper().readValue(responseBody, HashMap.class);

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ JSON RESPONSE CONVERTED INTO HASHMAP | ->  "+jsonResposeMap, mySession);

					if(jsonResposeMap.containsKey("ResponseCode")) {
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+"contains ResponseCode",mySession);
						responseCode = jsonResposeMap.get("ResponseCode").toString();
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+"ResponseCode is "+responseCode,mySession);
						if(responseCode.equalsIgnoreCase(successCode)) {
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ API SUCCESS $$$$", mySession);
							mySession.getVariableField("QA_WebService","Success").setValue(true);
							mySession.getVariableField("WS_SendSMS","SuccessFlag").setValue(true);
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ PARSING START $$$$", mySession);
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ PARSING END $$$$", mySession);
						} else if (responseCode.equalsIgnoreCase(errorCode)){
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING API FAILURE  DUE TO RESPONSE CODE | ->"+responseCode, mySession);
							mySession.getVariableField("QA_WebService","Success").setValue(false);
						}  else {
							mySession.getVariableField("QA_WebService","LinkDown").setValue(true);
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING LINK DOWN FLAG ENABLED DUE TO RESPONSE CODE | ->"+responseCode, mySession);
						}
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ RESPONSE CODE | ->  "+responseCode, mySession);
					}else {
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ INVALID RESPONSE |  ", mySession);
					}

				} else {

					responseBody = "";
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ IVR RECEIVED NULL RESPONSE FROM EXTERNAL JAR", mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING LINK DOWN FLAG ENABLED DUE TO NULL RESPONSE", mySession);
					mySession.getVariableField("QA_WebService","LinkDown").setValue(true);

				}

				try {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "InsertingTransactionHistory", mySession);
					LinkedHashMap<String,Object> TransactionHistory = new LinkedHashMap<String,Object>();
					TransactionHistory = (LinkedHashMap<String, Object>) mySession.getVariableField("HashMap", "TransactionHistory").getObjectValue();

					if(EndDate.equalsIgnoreCase("")) {

						EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

					}

					TransactionHistory.put("DT_START_DATE", StartDate);
					TransactionHistory.put("DT_END_DATE", EndDate);
					TransactionHistory.put("VC_FUNCTION_NAME", method);
					TransactionHistory.put("VC_HOST_URL", endPointUlr);
					TransactionHistory.put("VC_HOST_REQUEST", requestBody);
					TransactionHistory.put("VC_HOST_RESPONSE", responseBody);
					TransactionHistory.put("VC_TRANS_STATUS", responseCode);
					//mySession.getVariableField("HashMap", "TransactionHistory").setValue(TransactionHistory);
					dataNode.setTransactionHistory(mySession);

				} catch(Exception e) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+"\t|\t"+method+"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

				}

			} catch(Exception e) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING API FAILURE DUE TO EXCEPTION IN IVR", mySession);
				mySession.getVariableField("QA_WebService","Success").setValue(false);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | sendSMS | "+Arrays.toString(e.getStackTrace()), mySession);

			}


		} else {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | Token is not Generated", mySession);
		}

		setApiStatus(mySession);

	}

	public void upDateVBStatus(SCESession mySession, String requestBody) {


		String method = "VBMETRICSTATUSUPDATE";
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ ********IVR API CALL START ********  | IVR METHOD NAME -> s  ", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR CALLING API FROM EXTERNAL JAR    | JAR_NAME        -> QatarWebRequestor  ", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR CALLING METHOD FROM EXTERNAL JAR | JAR METHOD_NAME- > upDateVBStatus ", mySession);


		DataNode dataNode = new DataNode();

		String responseBody = "";
		String responseCode = "";
		String StartDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
		String EndDate = "";
		String url = LoadApplicationProperties.getProperty("Qatar.webservices.endPoint.url", mySession);
		String endPointUlr=url+LoadApplicationProperties.getProperty("updateVBStatus", mySession);
		boolean tokenStatus = false;

		HashMap<String, Object> jsonResposeMap = null;

		mySession.getVariableField("QA_WebService","Success").setValue(false);
		mySession.getVariableField("QA_WebService","LinkDown").setValue(false);
		mySession.getVariableField("Ws_UpdateVB","SuccessFlag").setValue(false);
		tokenStatus = mySession.getVariableField("TokenStatus").getBooleanValue();

		if(tokenStatus) {
			successCode = LoadApplicationProperties.getProperty("UPDATEVB_SUCCESS_CODE", mySession);
			errorCode = LoadApplicationProperties.getProperty("UPDATEVB_ERROR_CODE", mySession);

			try {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR SENDING REQUEST TO EXT JAR |  FRAMED JSON REQUEST IN IVR | ->  "+requestBody, mySession);
				responseBody = "";//qwr.upDateVBStatus(requestBody,mySession);


				String isAuthentication=LoadApplicationProperties.getProperty("Qatar.webservices.isAuthentication", mySession);
				int connectionTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("Qatar.webservices.connectionTimeout", mySession));
				int readTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("Qatar.webservices.readTimeout", mySession));

				try {

					WebServices webServices= new WebServices();
					responseBody=webServices.Webservice(mySession,endPointUlr, "POST",requestBody, connectionTimeOut, readTimeOut,isAuthentication);

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+method+" | update VB Status is called", mySession);

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+method+"Response: " + responseBody,mySession);


				} catch(Exception e) {
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+method+ " EXCEPTION: " +Arrays.toString(e.getStackTrace()), mySession);
				}


				EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR RECEIVED RESPONSE FROM EXT JAR |  RESPONSE JSON FROM JAR | ->  "+responseBody, mySession);
				if(responseBody!=null) {

					responseCode = "0";

					jsonResposeMap = new ObjectMapper().readValue(responseBody, HashMap.class);

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ JSON RESPONSE CONVERTED INTO HASHMAP | ->  "+jsonResposeMap, mySession);

					if(jsonResposeMap.containsKey("ResponseCode")) {
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+"contains ResponseCode",mySession);
						responseCode = jsonResposeMap.get("ResponseCode").toString();
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+"ResponseCode is "+responseCode,mySession);
						if(responseCode.equalsIgnoreCase(successCode)) {
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ API SUCCESS $$$$", mySession);
							mySession.getVariableField("QA_WebService","Success").setValue(true);
							mySession.getVariableField("Ws_UpdateVB","SuccessFlag").setValue(true);
							//								jsonResposeMap = (HashMap<String, Object>) jsonResposeMap.get("Response");
							//								TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ VBUPDATE $$$$"+jsonResposeMap.get("IsUpdated"), mySession);
							//								mySession.setProperty("VBUPDATE", jsonResposeMap.get("IsUpdated"));
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ PARSING START $$$$", mySession);
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ PARSING END $$$$", mySession);
						} else if (responseCode.equalsIgnoreCase(errorCode)){
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING API FAILURE  DUE TO RESPONSE CODE | ->"+responseCode, mySession);
							mySession.getVariableField("QA_WebService","Success").setValue(false);
						}  else {
							mySession.getVariableField("QA_WebService","LinkDown").setValue(true);
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING LINK DOWN FLAG ENABLED DUE TO RESPONSE CODE | ->"+responseCode, mySession);
						}
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ RESPONSE CODE | ->  "+responseCode, mySession);
					}else {
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ INVALID RESPONSE |  ", mySession);
					}

				} else {

					responseBody = "";
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ IVR RECEIVED NULL RESPONSE FROM EXTERNAL JAR", mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING LINK DOWN FLAG ENABLED DUE TO NULL RESPONSE", mySession);
					mySession.getVariableField("QA_WebService","LinkDown").setValue(true);

				}

				try {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "InsertingTransactionHistory", mySession);
					LinkedHashMap<String,Object> TransactionHistory = new LinkedHashMap<String,Object>();
					TransactionHistory = (LinkedHashMap<String, Object>) mySession.getVariableField("HashMap", "TransactionHistory").getObjectValue();

					if(EndDate.equalsIgnoreCase("")) {

						EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

					}

					TransactionHistory.put("DT_START_DATE", StartDate);
					TransactionHistory.put("DT_END_DATE", EndDate);
					TransactionHistory.put("VC_FUNCTION_NAME", method);
					TransactionHistory.put("VC_HOST_URL", endPointUlr);
					TransactionHistory.put("VC_HOST_REQUEST", requestBody);
					TransactionHistory.put("VC_HOST_RESPONSE", responseBody);
					TransactionHistory.put("VC_TRANS_STATUS", responseCode);
					//mySession.getVariableField("HashMap", "TransactionHistory").setValue(TransactionHistory);
					dataNode.setTransactionHistory(mySession);

				} catch(Exception e) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+"\t|\t"+method+"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

				}

			} catch(Exception e) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING API FAILURE DUE TO EXCEPTION IN IVR", mySession);
				mySession.getVariableField("QA_WebService","Success").setValue(false);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | sendSMS | "+Arrays.toString(e.getStackTrace()), mySession);

			}


		} else {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | Token is not Generated", mySession);
		}

		//setApiStatus(mySession);

	}

	public void sendMail(SCESession mySession,String message) {


		String method = "sendMail";
		successCode = LoadApplicationProperties.getProperty("EMAIL_SUCCESS_CODE", mySession);
		errorCode = LoadApplicationProperties.getProperty("EMAIL_ERROR_CODE", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ ********IVR API CALL START ********  | IVR METHOD NAME -> s  ", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR CALLING API FROM EXTERNAL JAR    | JAR_NAME        -> QatarWebRequestor  ", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR CALLING METHOD FROM EXTERNAL JAR | JAR METHOD_NAME- > sendMail ", mySession);
		
        TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"\t|\t ID to be sent as email is   \t|\t"+ message,mySession);
        String requestBody="";
		String email = mySession.getVariableField("customerdetails","email").getStringValue();
		if("".equalsIgnoreCase(email.trim()) || "NA".equalsIgnoreCase(email) || "null".equalsIgnoreCase(email) || email ==null) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"\t|\t email not sent due to invalid email  \t|\t"+ message,mySession);
		}else {
			HashMap<String,String>  App_Prop = new HashMap<String, String>();
			App_Prop = (HashMap<String, String>) mySession.getVariableField("HashMap", "PropertyValue").getObjectValue();
					
				requestBody = App_Prop.get("EMAIL_CONTENT").toString();
				
				if("NA".equalsIgnoreCase(App_Prop.get("mockemail").toString())) {
					requestBody = requestBody.replace("{email}", email);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"\t|\t actual email  \t|\t",mySession);
				}else {
					requestBody = requestBody.replace("{email}", App_Prop.get("mockemail").toString());
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"\t|\t mock email  \t|\t",mySession);
				}
				
				if("vb_enr_success".equalsIgnoreCase(message))  {
					requestBody = requestBody.replace("{subject}", "VB Enrollment Success");	
				}else if("vb_enr_failed".equalsIgnoreCase(message)) {
					requestBody = requestBody.replace("{subject}", "VB Enrollment Failed");
				}
				
				requestBody = requestBody.replace("{id}", message);
				//requestBody = requestBody.replace("{body}", "SMS CONTENT-NA");
				requestBody = requestBody.replace("{ffp}", mySession.getVariableField(mySession.getVariableField("customerdetails","ffpnumber").getStringValue()).getStringValue());
				requestBody = requestBody.replace("{name}", mySession.getVariableField("customerdetails","name").getStringValue());
				//requestBody = requestBody.replace("{date}", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()).toString());
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"\t|\t Request framed for email \t|\t"+ requestBody,mySession);	
		}

		DataNode dataNode = new DataNode();

		String responseBody = "";
		String responseCode = "";
		String StartDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
		String EndDate = "";
		String url = LoadApplicationProperties.getProperty("Qatar.webservices.email.endPoint.url", mySession);
		String endPointUlr= url+LoadApplicationProperties.getProperty("email", mySession);
		boolean tokenStatus = false;

		HashMap<String, Object> jsonResposeMap = null;

		mySession.getVariableField("QA_WebService","Success").setValue(false);
		mySession.getVariableField("QA_WebService","LinkDown").setValue(false);
		tokenStatus = mySession.getVariableField("TokenStatus").getBooleanValue();

		if(tokenStatus) {


			try {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR SENDING REQUEST TO EXT JAR |  FRAMED JSON REQUEST IN IVR | ->  "+requestBody, mySession);
				responseBody = "";//qwr.sendSMS(requestBody,mySession);


				String isAuthentication=LoadApplicationProperties.getProperty("Qatar.webservices.isAuthentication", mySession);
				int connectionTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("Qatar.webservices.connectionTimeout", mySession));
				int readTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("Qatar.webservices.readTimeout", mySession));

				try {

					WebServices webServices= new WebServices();
					responseBody=webServices.Webservice(mySession,endPointUlr, "POST",requestBody, connectionTimeOut, readTimeOut,isAuthentication);

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+method+" | sendMail is called", mySession);

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+method+"Response: " + responseBody,mySession);


				} catch(Exception e) {
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+method+ " EXCEPTION: " +Arrays.toString(e.getStackTrace()), mySession);
				}


				EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR RECEIVED RESPONSE FROM EXT JAR |  RESPONSE JSON FROM JAR | ->  "+responseBody, mySession);

				if(responseBody!=null) {

					responseCode = "0";

					jsonResposeMap = new ObjectMapper().readValue(responseBody, HashMap.class);

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ JSON RESPONSE CONVERTED INTO HASHMAP | ->  "+jsonResposeMap, mySession);

					if(jsonResposeMap.containsKey("ResponseCode")) {
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+"contains ResponseCode",mySession);
						responseCode = jsonResposeMap.get("ResponseCode").toString();
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+"ResponseCode is "+responseCode,mySession);
						if(responseCode.equalsIgnoreCase(successCode)) {
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ API SUCCESS $$$$", mySession);
							mySession.getVariableField("QA_WebService","Success").setValue(true);
						} else if (responseCode.equalsIgnoreCase(errorCode)){
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING API FAILURE  DUE TO RESPONSE CODE | ->"+responseCode, mySession);
							mySession.getVariableField("QA_WebService","Success").setValue(false);
						}  else {
							mySession.getVariableField("QA_WebService","LinkDown").setValue(true);
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING LINK DOWN FLAG ENABLED DUE TO RESPONSE CODE | ->"+responseCode, mySession);
						}
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ RESPONSE CODE | ->  "+responseCode, mySession);
					}else {
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ INVALID RESPONSE |  ", mySession);
					}

				} else {

					responseBody = "";
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ IVR RECEIVED NULL RESPONSE FROM EXTERNAL JAR", mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING LINK DOWN FLAG ENABLED DUE TO NULL RESPONSE", mySession);
					mySession.getVariableField("QA_WebService","LinkDown").setValue(true);
				}

				try {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "InsertingTransactionHistory", mySession);
					LinkedHashMap<String,Object> TransactionHistory = new LinkedHashMap<String,Object>();
					TransactionHistory = (LinkedHashMap<String, Object>) mySession.getVariableField("HashMap", "TransactionHistory").getObjectValue();

					if(EndDate.equalsIgnoreCase("")) {

						EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

					}

					TransactionHistory.put("DT_START_DATE", StartDate);
					TransactionHistory.put("DT_END_DATE", EndDate);
					TransactionHistory.put("VC_FUNCTION_NAME", method);
					TransactionHistory.put("VC_HOST_URL", endPointUlr);
					TransactionHistory.put("VC_HOST_REQUEST", requestBody);
					TransactionHistory.put("VC_HOST_RESPONSE", responseBody);
					TransactionHistory.put("VC_TRANS_STATUS", responseCode);
					//mySession.getVariableField("HashMap", "TransactionHistory").setValue(TransactionHistory);
					dataNode.setTransactionHistory(mySession);

				} catch(Exception e) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+"\t|\t"+method+"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

				}

			} catch(Exception e) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING API FAILURE DUE TO EXCEPTION IN IVR", mySession);
				mySession.getVariableField("QA_WebService","Success").setValue(false);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | sendMail | "+Arrays.toString(e.getStackTrace()), mySession);

			}


		} else {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | Token is not Generated", mySession);
		}

		setApiStatus(mySession);

	}

	public void VBPreferEmail(SCESession mySession) {


		String method = "VBPreferEmail";
		successCode = LoadApplicationProperties.getProperty("VBPREFEREMAIL_SUCCESS_CODE", mySession);
		errorCode = LoadApplicationProperties.getProperty("VBPREFEREMAIL_ERROR_CODE", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ ********IVR API CALL START ********  | IVR METHOD NAME -> s  ", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR CALLING API FROM EXTERNAL JAR    | JAR_NAME        -> QatarWebRequestor  ", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR CALLING METHOD FROM EXTERNAL JAR | JAR METHOD_NAME- > VBPreferEmail ", mySession);
		
		HashMap<String,String>  App_Prop = new HashMap<String, String>();
		App_Prop = (HashMap<String, String>) mySession.getVariableField("HashMap", "PropertyValue").getObjectValue();
		String FFPNumber = parseString(mySession.getVariableField("customerdetails","ffpnumber").getStringValue(), mySession);
		String requestBody = App_Prop.get("verifyemail")!=null ? App_Prop.get("verifyemail").toString() : "NA";
		if(!"NA".equalsIgnoreCase(requestBody)) {
		requestBody = requestBody.replace("{FFPNumber}", FFPNumber);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"\t|\t Request framed  \t|\t"+ requestBody,mySession);

		DataNode dataNode = new DataNode();

		String responseBody = "";
		String responseCode = "";
		String StartDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
		String EndDate = "";
		String url = LoadApplicationProperties.getProperty("Qatar.webservices.endPoint.url", mySession);
		String endPointUlr= url+LoadApplicationProperties.getProperty("VBPreferEmail", mySession);
		boolean tokenStatus = false;

		HashMap<String, Object> jsonResposeMap = null;

		mySession.getVariableField("QA_WebService","Success").setValue(false);
		mySession.getVariableField("QA_WebService","LinkDown").setValue(false);
		tokenStatus = mySession.getVariableField("TokenStatus").getBooleanValue();


		if(tokenStatus) {


			try {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR SENDING REQUEST TO EXT JAR |  FRAMED JSON REQUEST IN IVR | ->  "+requestBody, mySession);
				responseBody = "";//qwr.sendSMS(requestBody,mySession);


				String isAuthentication=LoadApplicationProperties.getProperty("Qatar.webservices.isAuthentication", mySession);
				int connectionTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("Qatar.webservices.connectionTimeout", mySession));
				int readTimeOut = Integer.parseInt(LoadApplicationProperties.getProperty("Qatar.webservices.readTimeout", mySession));

				try {

					WebServices webServices= new WebServices();
					responseBody=webServices.Webservice(mySession,endPointUlr, "POST",requestBody, connectionTimeOut, readTimeOut,isAuthentication);

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+method+" | VBPreferEmail is called", mySession);
					//						String sanitizedJson = "{   \"EmailAddress\": \"sdxxxx@qatarairways.com.qa\",   \"MobileNo\": \"33966355\",   \"ErrorCode\": null}";
					//						responseCode = "200";
					//						
					//						responseBody="{\r\n"
					//								+ "\"ResponseCode\":\""+responseCode+"\",\r\n"
					//								+ "\"Response\":"+sanitizedJson+"\r\n"
					//								+ "}";
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+method+"Response: " + responseBody,mySession);


				} catch(Exception e) {
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | "+method+ " EXCEPTION: " +Arrays.toString(e.getStackTrace()), mySession);
				}


				EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ IVR RECEIVED RESPONSE FROM EXT JAR |  RESPONSE JSON FROM JAR | ->  "+responseBody, mySession);

				if(responseBody!=null) {

					responseCode = "0";

					jsonResposeMap = new ObjectMapper().readValue(responseBody, HashMap.class);

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ JSON RESPONSE CONVERTED INTO HASHMAP | ->  "+jsonResposeMap, mySession);

					if(jsonResposeMap.containsKey("ResponseCode")) {
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+"contains ResponseCode",mySession);
						responseCode = jsonResposeMap.get("ResponseCode").toString();
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+"ResponseCode is "+responseCode,mySession);
						if(responseCode.equalsIgnoreCase(successCode)) {

							jsonResposeMap = (HashMap<String, Object>) jsonResposeMap.get("Response");
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ EmailAddress $$$$"+jsonResposeMap.get("EmailAddress"), mySession);
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ MobileNo $$$$"+jsonResposeMap.get("MobileNo"), mySession);

							mySession.getVariableField("customerDetails","email").setValue(jsonResposeMap.get("EmailAddress")!=null?jsonResposeMap.get("EmailAddress"):"NA");
							mySession.getVariableField("customerDetails","mobile").setValue(jsonResposeMap.get("MobileNo")!=null?jsonResposeMap.get("MobileNo"):"NA");

							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ Response $$$$"+responseCode, mySession);
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ API SUCCESS $$$$", mySession);
							mySession.getVariableField("QA_WebService","Success").setValue(true);
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ PARSING START $$$$", mySession);
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ PARSING END $$$$", mySession);

						} else if (responseCode.equalsIgnoreCase(errorCode)){
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING API FAILURE  DUE TO RESPONSE CODE | ->"+responseCode, mySession);
							mySession.getVariableField("QA_WebService","Success").setValue(false);
							mySession.getVariableField("customerDetails","email").setValue("NA");
							mySession.getVariableField("customerDetails","mobile").setValue("NA");
						}  else {
							mySession.getVariableField("QA_WebService","LinkDown").setValue(true);
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING LINK DOWN FLAG ENABLED DUE TO RESPONSE CODE | ->"+responseCode, mySession);
							mySession.getVariableField("customerDetails","email").setValue("NA");
							mySession.getVariableField("customerDetails","mobile").setValue("NA");
						}
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ RESPONSE CODE | ->  "+responseCode, mySession);
					}else {
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | $$$$ INVALID RESPONSE |  ", mySession);
					}

				} else {

					responseBody = "";
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ IVR RECEIVED NULL RESPONSE FROM EXTERNAL JAR", mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING LINK DOWN FLAG ENABLED DUE TO NULL RESPONSE", mySession);
					mySession.getVariableField("QA_WebService","LinkDown").setValue(true);
					mySession.getVariableField("customerDetails","email").setValue("NA");
					mySession.getVariableField("customerDetails","mobile").setValue("NA");

				}
			
				
				try {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "InsertingTransactionHistory", mySession);
					LinkedHashMap<String,Object> TransactionHistory = new LinkedHashMap<String,Object>();
					TransactionHistory = (LinkedHashMap<String, Object>) mySession.getVariableField("HashMap", "TransactionHistory").getObjectValue();

					if(EndDate.equalsIgnoreCase("")) {

						EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

					}

					TransactionHistory.put("DT_START_DATE", StartDate);
					TransactionHistory.put("DT_END_DATE", EndDate);
					TransactionHistory.put("VC_FUNCTION_NAME", method);
					TransactionHistory.put("VC_HOST_URL", endPointUlr);
					TransactionHistory.put("VC_HOST_REQUEST", requestBody);
					TransactionHistory.put("VC_HOST_RESPONSE", responseBody);
					TransactionHistory.put("VC_TRANS_STATUS", responseCode);
					//mySession.getVariableField("HashMap", "TransactionHistory").setValue(TransactionHistory);
					dataNode.setTransactionHistory(mySession);

				} catch(Exception e) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+"\t|\t"+method+"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

				}
				

			} catch(Exception e) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | $$$$ SETTING API FAILURE DUE TO EXCEPTION IN IVR", mySession);
				mySession.getVariableField("QA_WebService","Success").setValue(false);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+" | EXCEPTION | VBPreferEmail | "+Arrays.toString(e.getStackTrace()), mySession);
				mySession.getVariableField("customerDetails","email").setValue("NA");
				mySession.getVariableField("customerDetails","mobile").setValue("NA");

			}


		} else {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+" | Token is not Generated", mySession);
		}
		}
		else {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"\t|\t Exception to Retrieve verifyemail in VB PreferMail method \t|\t"+ requestBody,mySession);
		}
		

		setApiStatus(mySession);

		Boolean response = mySession.getVariableField("QA_WebService","Success").getBooleanValue();
		if(response) {
			HashMap<String,String> customerDetails = new HashMap<String,String>();
			customerDetails = (HashMap<String, String>) mySession.getVariableField("HashMap", "customerDetails").getObjectValue();
			
			String mobile = mySession.getVariableField("customerdetails","mobile").getStringValue();
			if(mobile !=null && !"null".equalsIgnoreCase(mobile) && !"NA".equalsIgnoreCase(mobile) && !"".equalsIgnoreCase(mobile.trim())) {
				//checkCountryCode(mobile, mySession);
				mySession.getVariableField("customerdetails","countrycode").setValue("NA");
				mySession.getVariableField("customerdetails","mobile").setValue("NA");
				customerDetails.put("Email",mySession.getVariableField("customerdetails","email").getStringValue());
				customerDetails.put("Mobile",mySession.getVariableField("customerdetails","mobile").getStringValue());

				if(mobile.contains("+")) {
					mySession.getTraceOutput().writeln(ITraceInfo.TRACE_LEVEL_DEBUG, "+ is already present in mobile number obtained from VBPreferEmail api response");
					checkCountryCode(mobile, mySession);
					
				}else {
					mobile = "+"+mobile;
					mySession.getTraceOutput().writeln(ITraceInfo.TRACE_LEVEL_DEBUG, "+ is appened to mobile number obtained from VBPreferEmail api response");
					checkCountryCode(mobile, mySession);
				}
				
				HashMap<String,String> Response =(HashMap<String, String>) mySession.getVariableField("MobileNumber").getObjectValue();
				String countryCode = Response.get("CountryCode");
				mobile =  Response.get("PhoneNumber");
				mySession.getVariableField("customerdetails","countrycode").setValue(countryCode);
				mySession.getVariableField("customerdetails","mobile").setValue(mobile);
				
				customerDetails.put("CountryCode",mySession.getVariableField("customerdetails","countrycode").getStringValue());
				customerDetails.put("Mobile",mySession.getVariableField("customerdetails","mobile").getStringValue());
				mySession.getTraceOutput().writeln(ITraceInfo.TRACE_LEVEL_DEBUG, "Customer details updated for email , mobile and country code after  VBPreferEmail api and set in IVR : "+customerDetails.toString());
			}else {
				mySession.getVariableField("customerdetails","mobile").setValue("NA");
				mySession.getVariableField("customerdetails","countrycode").setValue("NA");
				mySession.getVariableField("customerdetails","email").setValue("NA");
			}

		}else {
			mySession.getTraceOutput().writeln(ITraceInfo.TRACE_LEVEL_DEBUG, "VBPreferEmail api failed so sms and email will not be sent");
			mySession.getVariableField("customerdetails","mobile").setValue("NA");
			mySession.getVariableField("customerdetails","countrycode").setValue("NA");
		}
		
	}
	
	public String parseString(String key,SCESession mySession) {

		try 
		{
		String str = "NA";
		String value;

		if(key!=null) {

			value = key;

			if(value!=null && !value.isEmpty() && value.trim()!="null") {
				str = value.trim();
			}

		} else {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,"parseStirngResponse | EXCEPTION | parseStringResponse | key is null or empty | "+key, mySession);
		}
		
		return str;
		}
		catch(Exception ex) {
			mySession.getTraceOutput().writeln(ITraceInfo.TRACE_LEVEL_ERROR,"Exception caught while checking the value:"+key+" "+ex);
		}
		return null;
	}

}