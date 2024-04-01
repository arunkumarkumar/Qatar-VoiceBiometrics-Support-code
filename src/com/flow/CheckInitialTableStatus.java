package com.flow;

import java.io.*;
//import java.io.File;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.DBOperation.DBConnection;
import com.General.AppConstants;
import com.General.LoadApplicationProperties;
import com.XML_Parsing.langauge;
import com.avaya.sce.runtime.tracking.TraceInfo;
import com.avaya.sce.runtimecommon.ITraceInfo;
import com.avaya.sce.runtimecommon.SCESession;

public class CheckInitialTableStatus {

	static long DnisJsonModifiedTime = 0;
	String className = "CheckInitialTableStatus";
	static List<Object> languageList = new ArrayList<Object>();

	@SuppressWarnings("unchecked")
	public void getInitialTableStatus(SCESession mySession) {

		String method = "getInitialTableStatus";


		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);
		DBConnection db = new DBConnection();

		List<HashMap<Object, Object>> DBResult = null;
		//List<HashMap<Object, Object>> CommonConfigValues = null;
		LinkedHashMap<String, Object> input = new LinkedHashMap<String, Object>();
		List<String> DBStatus = new ArrayList<String>();
		LinkedHashMap<String,String> CallHistory = new LinkedHashMap<String,String>();

		try {

			CallHistory = (LinkedHashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

			input.put("VC_APP_SRVR_IP", CallHistory.get("VC_APP_SRVR_IP"));
			DBResult = db.getResultSet("SP_SELECT_MODIFIED_TABLE", input, mySession);
			//CommonConfigValues = db.getResultSet("SP_GET_COMMON_CONFIG_VALUES", null, mySession);
			//mySession.setProperty("CommonConfigValues", CommonConfigValues);
			//TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"CommonConfigValues : "+CommonConfigValues.toString(), mySession);
			DBResult = db.getModifiedTable(input, mySession);

			if(DBResult==null || DBResult.isEmpty()) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Empty", mySession);

			} else {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Non-Empty", mySession);
				
				for(int j=0;j<DBResult.size();j++) {
					DBStatus.add(DBResult.get(j).get("VC_TABLES").toString().trim());
				}

			}

		} catch(Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

		}

		mySession.getVariableField("ApplicationVariable", "DBStatusHashmap").setValue(DBStatus);

		if(DBStatus.contains("IVR_DNIS_MASTER")||DBStatus.contains("IVR_FLOW_MASTER")||DBStatus.contains("IVR_APPLICATION_MASTER")||DBStatus.contains("IVR_MARKET_MASTER")||DBStatus.contains("IVR_ROUTER_MASTER") ||DBStatus.contains("IVR_COMMON_CONFIG")) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Master tables are Modified", mySession);
			loadDnisTable(mySession);
			loadCommonConfigTable(mySession);

		} else {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"None of the Master table is Modified", mySession);

			HashMap<String,String> App_Prop = new HashMap<String,String>();
			App_Prop = (HashMap<String, String>) mySession.getVariableField("HashMap", "PropertyValue").getObjectValue();

			String DNISJsonLocation = App_Prop.get("DnisApplicationPath")+App_Prop.get("DnisApplicationJsonName");

			try {

				File file = new File(new File(DNISJsonLocation).getCanonicalPath());

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Json File Location\t|\t"+file, mySession);

				if(!file.exists()) {

					loadDnisTable(mySession);
					loadCommonConfigTable(mySession);

				} else {

					fetchApplication(mySession);

				}

			} catch(Exception e) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

			}

		}

		createLanguageJson(mySession);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void loadDnisTable(SCESession mySession){

		String method = "loadDnisTable";


		DBConnection db = new DBConnection();
		List<HashMap<Object, Object>> DBResult = null;
		LinkedHashMap<String, Object> input = new LinkedHashMap<String, Object>();
		HashMap<String,String> App_Prop = new HashMap<String,String>();
		LinkedHashMap<String,String> CallHistory = new LinkedHashMap<String,String>();

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);

		try {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Loading Master Table Values in Json", mySession);

			CallHistory = (LinkedHashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

			input.put("VC_APP_SRVR_IP", CallHistory.get("VC_APP_SRVR_IP"));
			DBResult = db.getResultSet("SP_GET_DNIS_APP_MASTER", input, mySession);

			String str = "";
			StringBuffer result = new StringBuffer();

			for (int i=0;i<DBResult.size();i++) {
				String comma ="";
				Map<Object, Object> map = DBResult.get(i);
				if(i!=0 && i!=DBResult.size()) {
					comma = ",";
				}
				str = "\n \""+map.get("VC_DNIS")+"\":{"+map.entrySet().stream() .map(e -> "\n\t\""+ e.getKey() + "\":\"" +
						String.valueOf(e.getValue()) + "\"") .collect(Collectors.joining(", "))+"\n\t}";
				result.append(comma+str);
			}

			String json = "{"+result+"\n}";

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Final Json Created on\t|\t"+new SimpleDateFormat("dd-MM-YY HH:mm:ss:SSS").format(new Date()), mySession);

			App_Prop = (HashMap<String, String>) mySession.getVariableField("HashMap", "PropertyValue").getObjectValue();

			String DNISJsonLocation = App_Prop.get("DnisApplicationPath")+App_Prop.get("DnisApplicationJsonName");

			File file = new File(new File(DNISJsonLocation).getCanonicalPath());


			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Json File Location\t|\t"+file, mySession);

			if(file.exists()) {

				DNISJsonLocation = App_Prop.get("DnisApplicationPath")+App_Prop.get("DnisBackupLocation");
				String renameFilePath = DNISJsonLocation+new SimpleDateFormat("YYMMddHHmmssSSS").format(new Date())+"_"+App_Prop.get("DnisApplicationJsonName");
				File renameFile = new File(new File(renameFilePath).getCanonicalPath());

				file.renameTo(renameFile);

				if(!new File(DNISJsonLocation).exists()) {
					new File(DNISJsonLocation).mkdirs();
				}
				
				try {

					FileWriter wr = new FileWriter(file);
					wr.write(json);
					wr.close();

				} catch (IOException e) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

				}

			} else {

				try {

					FileWriter wr = new FileWriter(file);
					wr.write(json);
					wr.close();

				} catch (IOException e) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

				}

			}

		} catch (Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

		}

		fetchApplication(mySession);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);

	}

	@SuppressWarnings("unchecked")
	private void fetchApplication(SCESession mySession) {

		String method = "fetchApplication";


		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);

		HashMap<String,String> customerDetails = new HashMap<String,String>();
		customerDetails = (HashMap<String, String>) mySession.getVariableField("HashMap", "customerDetails").getObjectValue();

		HashMap<String,String> CallHistory = new HashMap<String,String>();
		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();
		String DNIS = CallHistory.get("VC_DNIS");

		HashMap<String,String> App_Prop = new HashMap<String,String>();
		HashMap<String,String> DBAppValues = new HashMap<String,String>();
		HashMap<String,Object> AppLanguage=new HashMap<String,Object>();

		App_Prop = (HashMap<String, String>) mySession.getVariableField("HashMap", "PropertyValue").getObjectValue();

		String DNISJsonLocation = App_Prop.get("DnisApplicationPath")+App_Prop.get("DnisApplicationJsonName");
		String CommonConfigJsonLocation = App_Prop.get("CommonConfigApplicationPath")+App_Prop.get("CommonConfigApplicationJsonName");
		String DBAppValuesKey = App_Prop.get("DBAppValues");
		String DefaultLanguage = App_Prop.get("DefaultLanguage");
		//String values = "";
		String promptLanguage = "";

		mySession.getVariableField("ApplicationVariable", "Language").setValue(DefaultLanguage);
		mySession.getVariableField("DynamicPrompt","PromptLanguage").setValue(DefaultLanguage);
		CallHistory.put("VC_LANGUAGE",DefaultLanguage);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"DefaultLanguage From Property\t|\t"+DefaultLanguage, mySession);
		try {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"DNIS\t|\t"+DNIS, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"fetching Application details from the Location is\t|\t"+DNISJsonLocation, mySession);

			File dnisJsonFile = new File(new File(DNISJsonLocation).getCanonicalPath());
			File commonconfigJsonFile = new File(new File(CommonConfigJsonLocation).getCanonicalPath());
			
			if(dnisJsonFile.exists()) {

				FileReader fr = null;

				try {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"LOCADING JSON FILE | "+dnisJsonFile, mySession);

					JSONParser jsonParser = new JSONParser();
					fr = new FileReader(dnisJsonFile);

					JSONObject jsonObj = (JSONObject) jsonParser.parse(fr);

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"JSON OBJECT PARSED ", mySession);

					loadjsonFile(DNIS, jsonObj, DBAppValuesKey, mySession);

				}

				catch (Exception e) {

					mySession.getVariableField("Error").setValue(true);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+Arrays.toString(e.getStackTrace()), mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

				}
				
				
				

				

					fr = null;

					try {
						if(commonconfigJsonFile.exists()) {
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"LOCADING JSON FILE | "+commonconfigJsonFile, mySession);

						JSONParser jsonParser = new JSONParser();
						fr = new FileReader(commonconfigJsonFile);

						JSONObject jsonObj = (JSONObject) jsonParser.parse(fr);
						mySession.setProperty("common", jsonObj);
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"JSON OBJECT PARSED ", mySession);

						//loadjsonFile(DNIS, jsonObj, DBAppValuesKey, mySession);
						}else {
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Common config json not exists ", mySession);
						}

					}

					catch (Exception e) {

						mySession.getVariableField("Error").setValue(true);
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+Arrays.toString(e.getStackTrace()), mySession);
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

					}

				
				finally {

					if(fr!=null)fr.close();
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"File Reader is Closed", mySession);

				}

			}

			DBAppValues = (HashMap<String, String>) mySession.getVariableField("ApplicationVariable", "DBAppValues").getObjectValue();

		} catch (Exception e) {

			mySession.getVariableField("Error").setValue(true);

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

		}

		try{
			
		if(mySession.getVariableField("Error").getBooleanValue()) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ "Application Error Occured", mySession);

		} else {

			if(DBAppValues.get("VC_IS_ACTIVE_FLOW_MASTER").equalsIgnoreCase("Y")&&DBAppValues.get("VC_IS_ACTIVE_DNIS_MASTER").equalsIgnoreCase("Y")&&DBAppValues.get("VC_IS_ACTIVE_MARKET_MASTER").equalsIgnoreCase("Y")) {

				Parse_XML xml = new Parse_XML();
				Set setNextNode = new Set();

				HashMap<String,Object> flow= new HashMap<String,Object>();
				HashMap<String,Object> flowvalues = new HashMap<String,Object>();

				String NodeDescription = "";
				String NextNode = "";

				mySession.getVariableField("ApplicationVariable", "Language").setValue(DBAppValues.get("VC_DEFAULT_LANGUAGE"));
				mySession.getVariableField("DynamicPrompt","PromptLanguage").setValue(DBAppValues.get("VC_DEFAULT_LANGUAGE"));
				CallHistory.put("VC_LANGUAGE",DBAppValues.get("VC_DEFAULT_LANGUAGE"));

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"DefaultLanguage From DB\t|\t"+DBAppValues.get("VC_DEFAULT_LANGUAGE"), mySession);

				String FlowPath = App_Prop.get("XMLFilePath").toString();
				String Market = DBAppValues.get("VC_MARKET").toString();
				String XmlName = DBAppValues.get("VC_XML_NAME").toString();
				String LanguageXML = "";
				String MainXML = "";

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"FlowPath | "+FlowPath, mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"DBAppValues.get(\"VC_MARKET\") | "+DBAppValues.get("VC_MARKET"), mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"XmlName | "+XmlName, mySession);

				if(!FlowPath.endsWith("/")) {

					LanguageXML = FlowPath + "/"+Market+"/"+XmlName+"_LANGUAGE.xml";
					MainXML = FlowPath + "/"+Market+"/"+XmlName+"_MAIN.xml";

				} else {

					LanguageXML = FlowPath + Market+"/"+XmlName+"_LANGUAGE.xml";
					MainXML = FlowPath + Market+"/"+XmlName+"_MAIN.xml";

				}

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Application Language Loaded from the Location\t|\t"+LanguageXML, mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Application Flow Loaded from the Location\t|\t"+MainXML, mySession);

				mySession.getVariableField("CustTier").setValue(DBAppValues.get("VC_DEFAULT_CUSTOMER_TIER").toString().trim());

				CallHistory.put("VC_MARKET", Market.toString().trim());
				CallHistory.put("VC_LINE_TYPE", DBAppValues.get("VC_APP_NAME").toString().trim());
				CallHistory.put("VC_DID", DBAppValues.get("VC_DID").toString().trim());
				CallHistory.put("VC_OCDS_BUSINESS_UNIT", DBAppValues.get("VC_OCDS_APP_NAME").toString().trim());

				customerDetails.put("CountryName", Market.toString().trim());
				customerDetails.put("BusinessUnit", DBAppValues.get("VC_OCDS_APP_NAME").toString().trim());
				customerDetails.put("Dnis",DBAppValues.get("VC_DID").toString().trim());

				if(new File(LanguageXML).exists()&&new File(MainXML).exists()) {

					xml.loadFlow(LanguageXML, "Language", mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Language XML Loaded", mySession);

					xml.loadFlow(MainXML, "Flow", mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Main XML Loaded", mySession);

					if(mySession.getVariableField("Error").getBooleanValue()) {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ "Application Error Occured", mySession);

					} else {

						AppLanguage = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Language").getObjectValue();

						setNextNode.Flow(mySession);

						NodeDescription = mySession.getVariableField("HashMap", "InitialNode").getStringValue();

						flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

						List<langauge> lang = (List<langauge>) AppLanguage.get("Language");

						if(lang.size()>0) {

							for(int x=0;x<lang.size();x++) {

								promptLanguage = promptLanguage+lang.get(x).getName().toString().trim();
								if(x!=lang.size()-1) {
									promptLanguage += ",";
								}

							}
							if(lang.size()>1) {
								mySession.getVariableField("isMultilingual").setValue("true");
							} else {
								mySession.getVariableField("isMultilingual").setValue("false");
							}
						} else {
							mySession.getVariableField("isMultilingual").setValue("false");
						}

						mySession.getVariableField("DynamicPrompt","PromptLanguage").setValue(promptLanguage);

						if(flow.containsKey(NodeDescription)) {

							flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
							NextNode = flowvalues.get("Type").toString();

						} else {

							mySession.getVariableField("Error").setValue(true);
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_WARN, className +"\t|\t"+ method +"\t|\t"+ "Key in Flow is Missing\t|\t"+NodeDescription, mySession);

						}

						mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
						mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

						String TimeZone = "GMT+4";
						if(DBAppValues.get("VC_DST_FLAG").toString().equalsIgnoreCase("Y")) {

							CallHistory.put("VC_TIME_ZONE", DBAppValues.get("VC_DST_TIME_ZONE"));
							TimeZone = DBAppValues.get("VC_DST_TIME_ZONE").toString();

						} else {

							CallHistory.put("VC_TIME_ZONE", DBAppValues.get("VC_TIME_ZONE"));
							TimeZone = DBAppValues.get("VC_TIME_ZONE").toString();

						}
						mySession.getVariableField("ApplicationVariable", "ZoneID").setValue(TimeZone);

					}

				} else {

					mySession.getVariableField("Error").setValue(true);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_WARN, className +"\t|\t"+ method +"\t|\t"+ "Flow file is Missing\t|\t", mySession);

				}

			} else {

				mySession.getVariableField("Error").setValue(true);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ "DNIS "+DNIS+" or Market is Not in Active State", mySession);

			}

		}
		} catch(Exception e) {
			mySession.getVariableField("Error").setValue(true);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+Arrays.toString(e.getStackTrace()), mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
		}

		CallHistory.put("VC_LANGUAGE", mySession.getVariableField("ApplicationVariable", "Language").getStringValue());

		mySession.getVariableField("HashMap", "customerDetails").setValue(customerDetails);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);

	}

	private void loadjsonFile(String DNIS, JSONObject jsonObject, String DBAppValuesKey, SCESession mySession) {

		String method = "loadjsonFile";


		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);

		String DefaultDNIS = LoadApplicationProperties.getProperty("DefaultDnis", mySession);

		HashMap<String,String> DBAppValues = new HashMap<String,String>();
		try {

			//Validate whether Json has DNIS
			if(jsonObject.containsKey(DNIS)) {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"DNIS is present  : ", mySession);

				JSONObject Appvalues = (JSONObject) jsonObject.get(DNIS);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"VC_CONNECT_TYPE  : "+Appvalues.get("VC_CONNECT_TYPE").toString(), mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Appvalues : "+Appvalues.toJSONString(), mySession);

				if("CONNECTBACK".equalsIgnoreCase(Appvalues.get("VC_CONNECT_TYPE").toString())){
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Connect back call ", mySession);
					mySession.getVariableField("connectback").setValue(AppConstants.T);
				}else {
					mySession.getVariableField("connectback").setValue(AppConstants.F);
				}
				if(DBAppValuesKey.contains(",")) {

					String[] KeyValues = DBAppValuesKey.split(",");

					for(int i=0;i<KeyValues.length;i++) {

						if(Appvalues.containsKey(KeyValues[i].toString())) {

							DBAppValues.put(KeyValues[i], Appvalues.get(KeyValues[i]).toString());

						} else {

							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ "Key "+KeyValues[i]+" is Missing from Json", mySession);

						}

					}

				} else {

					if(Appvalues.containsKey(DBAppValuesKey)) {
						DBAppValues.put(DBAppValuesKey, Appvalues.get(DBAppValuesKey).toString());
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ "Key "+DBAppValuesKey+" is Missing from Json", mySession);

					}

				}

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Application Values are Loaded", mySession);

				mySession.getVariableField("ApplicationVariable", "DBAppValues").setValue(DBAppValues);

				mySession.getVariableField("Error").setValue(false);

			} else if(jsonObject.containsKey(DefaultDNIS)) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Default Dnis is | "+DefaultDNIS, mySession);

				JSONObject Appvalues = (JSONObject) jsonObject.get(DefaultDNIS);

				if(DBAppValuesKey.contains(",")) {

					String[] KeyValues = DBAppValuesKey.split(",");

					for(int i=0;i<KeyValues.length;i++) {

						if(Appvalues.containsKey(KeyValues[i].toString())) {

							DBAppValues.put(KeyValues[i], Appvalues.get(KeyValues[i]).toString());

						} else {

							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ "Key "+KeyValues[i]+" is Missing from Json", mySession);

						}

					}

				} else {

					if(Appvalues.containsKey(DBAppValuesKey)) {
						DBAppValues.put(DBAppValuesKey, Appvalues.get(DBAppValuesKey).toString());
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ "Key "+DBAppValuesKey+" is Missing from Json", mySession);

					}

				}

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Application Values are Loaded", mySession);

				mySession.getVariableField("ApplicationVariable", "DBAppValues").setValue(DBAppValues);

				mySession.getVariableField("Error").setValue(false);

			} else {

				mySession.getVariableField("Error").setValue(true);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ "Dnis "+DNIS+" is Missing from Json", mySession);

			}

		} catch (Exception e) {

			mySession.getVariableField("Error").setValue(true);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

		}

	}

	@SuppressWarnings("unchecked")
	private void createLanguageJson(SCESession mySession) {

		String method = "createLanguageJson";


		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);

		String langugaeJsonLocation = LoadApplicationProperties.getProperty("LanguageJsonLocation", mySession);
		String backupLanguageJsonLocation = LoadApplicationProperties.getProperty("LanguageJsonBackupLocation", mySession);
		String languageJsonName = LoadApplicationProperties.getProperty("LanguageJsonName", mySession);
		String folderName = new SimpleDateFormat("YYMMddHHmmssSSS").format(new Date());

		File jsonLocation = new File(langugaeJsonLocation+languageJsonName);
		File backupJsonLocation = new File(langugaeJsonLocation+backupLanguageJsonLocation+folderName+"_"+languageJsonName);

		if(!new File(langugaeJsonLocation+backupLanguageJsonLocation).exists()) {
			new File(langugaeJsonLocation+backupLanguageJsonLocation).mkdirs();
		}
		
		List<String> DBStatus = new ArrayList<String>();
		DBStatus = (List<String>) mySession.getVariableField("ApplicationVariable", "DBStatusHashmap").getObjectValue();
		String languageJson = "";

		if(DBStatus.contains("IVR_LANGUAGE_MASTER")) {

			if(jsonLocation.exists()) {

				jsonLocation.renameTo(backupJsonLocation);

			}

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Language Master is Modified", mySession);

			languageJson = fetchLanguageJson(jsonLocation, mySession);

			try {

				FileWriter wr = new FileWriter(jsonLocation);
				wr.write(languageJson);
				wr.close();

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Updated Language Json is Created", mySession);

			} catch (IOException e) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

			}

		} else {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Language Master is Not Modified in Data Base", mySession);

		}

		if(!jsonLocation.exists()) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Language Json is not exist", mySession);

			languageJson = fetchLanguageJson(jsonLocation, mySession);

			try {

				FileWriter wr = new FileWriter(jsonLocation);
				wr.write(languageJson);
				wr.close();

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Updated Language Json is Created", mySession);

			} catch (IOException e) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

			}

		}

		boolean languageJsonLoaded = loadLanguageJson(mySession);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Language Json Load Status | "+languageJsonLoaded, mySession);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);

	}

	@SuppressWarnings("unchecked")
	private String fetchLanguageJson(File jsonLocation, SCESession mySession) {

		String method = "fetchLanguageJson";


		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);

		String languageJson = "";

		try {

			boolean DBException = mySession.getVariableField("ApplicationVariable", "DbException").getBooleanValue();

			if(!DBException) {

				DBConnection db = new DBConnection();
				List<HashMap<Object, Object>> DBResult = null;
				LinkedHashMap<String, Object> input = new LinkedHashMap<String, Object>();
				LinkedHashMap<String,String> CallHistory = new LinkedHashMap<String,String>();

				CallHistory = (LinkedHashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

				input.put("VC_APP_SRVR_IP", CallHistory.get("VC_APP_SRVR_IP"));

				DBResult = db.getResultSet("SP_GET_LANGUAGE_MASTER", input, mySession);

				String jsonStart = "[";
				String jsonEnd = "\n]";

				for(int i=0;i<DBResult.size();i++) {

					languageJson += "\n\t{\n\t\t\"NU_LANGUAGE_ID\":\""+DBResult.get(i).get("NU_LANGUAGE_ID")+"\","
							+"\n\t\t\"VC_LANGUAGE_NAME\":\""+DBResult.get(i).get("VC_LANGUAGE_NAME")+"\","
							+"\n\t\t\"VC_LANGUAGE_1\":\""+DBResult.get(i).get("VC_LANGUAGE_1")+"\","
							+"\n\t\t\"VC_LANGUAGE_2\":\""+DBResult.get(i).get("VC_LANGUAGE_2")+"\","
							+"\n\t\t\"VC_LANGUAGE_3\":\""+DBResult.get(i).get("VC_LANGUAGE_3")+"\","
							+"\n\t\t\"VC_IS_ACTIVE\":\""+DBResult.get(i).get("VC_IS_ACTIVE")+"\""
							+"\n\t}";
					if(i<DBResult.size()-1) {
						languageJson +=",";
					}

				}
				languageJson = jsonStart+languageJson+jsonEnd;
			}

		} catch(Exception e) {

			languageJson = "";
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ Arrays.toString(e.getStackTrace()), mySession);

		}

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);

		return languageJson;

	}

	private Boolean loadLanguageJson(SCESession mySession) {

		String method = "createLanguageJson";


		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);

		boolean languageJsonLoaded = false;

		HashMap<String, String> languageMap = new HashMap<String, String>();
		String langugaeJsonLocation = LoadApplicationProperties.getProperty("LanguageJsonLocation", mySession);
		String languageJsonName = LoadApplicationProperties.getProperty("LanguageJsonName", mySession);

		File jsonLocation = new File(langugaeJsonLocation+languageJsonName);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Json Location File | "+jsonLocation, mySession);

		if(jsonLocation.exists()) {

			FileReader readLanguageJson = null;

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Json Location File is Exist", mySession);

			try {

				JSONParser jsonParser = new JSONParser();
				readLanguageJson = new FileReader(jsonLocation);

				JSONArray languageJsonArray = (JSONArray) jsonParser.parse(readLanguageJson);

				for(int i=0;i<languageJsonArray.size();i++) {

					languageMap = new HashMap<String, String>();
					JSONObject languageJson = (JSONObject) jsonParser.parse(languageJsonArray.get(i).toString());
					languageMap.put("NU_LANGUAGE_ID", languageJson.get("NU_LANGUAGE_ID").toString());
					languageMap.put("VC_LANGUAGE_NAME", languageJson.get("VC_LANGUAGE_NAME").toString());
					languageMap.put("VC_LANGUAGE_1", languageJson.get("VC_LANGUAGE_1").toString());
					languageMap.put("VC_LANGUAGE_2", languageJson.get("VC_LANGUAGE_2").toString());
					languageMap.put("VC_LANGUAGE_3", languageJson.get("VC_LANGUAGE_3").toString());
					languageMap.put("VC_IS_ACTIVE", languageJson.get("VC_IS_ACTIVE").toString());

					languageList.add(languageMap);

				}

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Json Location File is loaded", mySession);

				languageJsonLoaded = true;

			} catch(Exception e) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ Arrays.toString(e.getStackTrace()), mySession);

			} finally {

				try {
					readLanguageJson.close();
				} catch (IOException e) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ Arrays.toString(e.getStackTrace()), mySession);

				}

			}

		}

		mySession.getVariableField("ApplicationVariable", "LanguageHashMap").setValue(languageList);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);

		return languageJsonLoaded;

	}

	
	@SuppressWarnings("unchecked")
	public void loadCommonConfigTable(SCESession mySession){

		String method = "loadCommonConfigTable";


		DBConnection db = new DBConnection();

		List<HashMap<Object, Object>> DBResult = null;
		LinkedHashMap<String, Object> input = new LinkedHashMap<String, Object>();
		HashMap<String,String> App_Prop = new HashMap<String,String>();
		LinkedHashMap<String,String> CallHistory = new LinkedHashMap<String,String>();

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start in commonconfig json", mySession);

		try {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Loading Master Table Values in Json for common config", mySession);

			CallHistory = (LinkedHashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

			input.put("VC_APP_SRVR_IP", CallHistory.get("VC_APP_SRVR_IP"));
			DBResult = db.getResultSet("SP_GET_COMMON_CONFIG_VALUES", null, mySession);

			String str = "";
			StringBuffer result = new StringBuffer();

			for (int i=0;i<DBResult.size();i++) {
				String comma ="";
				Map<Object, Object> map = DBResult.get(i);
				if(i!=0 && i!=DBResult.size()) {
					comma = ",";
				}
				str = "\n \""+map.get("NU_PROP_ID")+"\":{"+map.entrySet().stream() .map(e -> "\n\t\""+ e.getKey() + "\":\"" +
						String.valueOf(e.getValue()) + "\"") .collect(Collectors.joining(", "))+"\n\t}";
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Values added "+str, mySession);
				result.append(comma+str);
			}

			String json = "{"+result+"\n}";

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Final Json Created on\t|\t"+new SimpleDateFormat("dd-MM-YY HH:mm:ss:SSS").format(new Date()), mySession);

			App_Prop = (HashMap<String, String>) mySession.getVariableField("HashMap", "PropertyValue").getObjectValue();

			String CommonConfigJsonLocation = App_Prop.get("CommonConfigApplicationPath")+App_Prop.get("CommonConfigApplicationJsonName");

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"DNISJsonLocation: "+CommonConfigJsonLocation, mySession);
			File file = new File(new File(CommonConfigJsonLocation).getCanonicalPath());


			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Json File Location\t|\t"+file, mySession);

			if(file.exists()) {

				CommonConfigJsonLocation = App_Prop.get("CommonConfigApplicationPath")+App_Prop.get("CommonConfigBackupLocation");
				String renameFilePath = CommonConfigJsonLocation+new SimpleDateFormat("YYMMddHHmmssSSS").format(new Date())+"_"+App_Prop.get("CommonConfigApplicationJsonName");
				File renameFile = new File(new File(renameFilePath).getCanonicalPath());
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"renameFile\t|\t"+renameFile, mySession);
				file.renameTo(renameFile);

				if(!new File(CommonConfigJsonLocation).exists()) {
					new File(CommonConfigJsonLocation).mkdirs();
				}
				
				try {

					FileWriter wr = new FileWriter(file);
					wr.write(json);
					wr.close();
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"file writting completed for commonconfig json \t|\t", mySession);

				} catch (IOException e) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

				}

			} else {

				try {

					FileWriter wr = new FileWriter(file);
					wr.write(json);
					wr.close();
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"file writting completed for commonconfig json in alternate else \t|\t", mySession);
				} catch (IOException e) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

				}

			}

		} catch (Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

		}

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);

	}
	
}