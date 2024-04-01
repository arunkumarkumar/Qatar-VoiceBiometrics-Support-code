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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.DBOperation.DBConnection;
import com.General.AppConstants;
import com.General.Load;
import com.General.LoadApplicationProperties;
import com.XML_Parsing.Fetch;
import com.XML_Parsing.Node;
import com.XML_Parsing.langauge;
import com.avaya.sce.runtime.tracking.TraceInfo;
import com.avaya.sce.runtimecommon.ITraceInfo;
import com.avaya.sce.runtimecommon.SCESession;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Set {

	String className = "Set";

	public void Flowtype(SCESession mySession) {

		String NodeType = mySession.getVariableField("ApplicationVariable", "NextNode").getStringValue();
		//String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

		String method = "Flowtype";
		

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"NodeType\t|\t"+NodeType, mySession);

		switch(NodeType) {

		case "DATA":
			NodeType = "DynamicData";
			mySession.getVariableField("Method").setValue("preData");
			break;
		case "ANN":
			NodeType = "PreAnnouncement";
			mySession.getVariableField("Method").setValue("preAnnouncement");
			break;
		case "MENU":
			NodeType = "PreMenu";
			mySession.getVariableField("Method").setValue("preMenu");
			break;
		case "PC":
			NodeType = "PrePC";
			mySession.getVariableField("Method").setValue("prePC");
			break;
		case "TRANSFER":
			NodeType = "SFDynamicTransfer";
			mySession.getVariableField("Method").setValue("preData");
			break;
			//changes
		case "CONNECTBACK":
			NodeType = "SFConnectBack";
			mySession.getVariableField("Method").setValue("preData");
			break;
		case "CONNECTBACKVBSTATUS":
			NodeType = "SFVbstatus";
			mySession.getVariableField("Method").setValue("preData");
			break;
		case "CONNECTBACKVBSTATUS_2":
			NodeType = "SFVbstatus";
			mySession.getVariableField("Method").setValue("preData");
			break;
		case "CONNECTBACKVBSELFSERVE":
			NodeType = "SFVbselfserve";
			mySession.getVariableField("Method").setValue("preData");
			break;
			
		case "TechnicalDifficulties":
			NodeType = "TechnicalDifficulties";
			break;
		default :
			NodeType = "IsCallHistoryInserted";
			break;

		}

		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NodeType);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);
	}

	@SuppressWarnings({ "unchecked" })
	public void Language(SCESession mySession) {

		String method = "Language";
		

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);

		String NodeDescription="NA";
		String MultiLingual = "";
		String NextNode = "NA";
		String Langkey = "NA";
		String returnValue = "NA";

		HashMap<String,Object> language = new HashMap<String,Object>();

		Fetch fetch = new Fetch();

		//Check whether flow is MultiLingual or not.
		language = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Language").getObjectValue();
		List<langauge> lang = (List<langauge>) language.get("Language");

		NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"NodeDescription\t|\t"+NodeDescription, mySession);

		if(lang.size()>1){

			for(int i=0;i<lang.size();i++){
				MultiLingual = MultiLingual + lang.get(i).getName() +"/,";
			}
			mySession.getVariableField("ApplicationVariable", "MultiLingual").setValue(MultiLingual);
			returnValue = "true";

		}
		else{
			//Get Language from XML
			Langkey = lang.get(0).getName();
			mySession.getVariableField("ApplicationVariable", "Language").setValue(Langkey);
			NextNode = "CheckWorkingHour";
			returnValue = "false";
		}

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"MultiLingual\t|\t"+MultiLingual, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Langkey\t|\t"+Langkey, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"NextNode\t|\t"+NextNode, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"returnValue\t|\t"+returnValue, mySession);

		//Fetch Next Node Description
		NodeDescription = fetch.NodeDescription(className, NodeDescription, returnValue, mySession);

		//Fetch Next Node
		NextNode = fetch.NextNode(className, NodeDescription, mySession);

		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void Flow(SCESession mySession) {

		String method = "Flow";
		

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);

		PrepareFlow preFlow = new PrepareFlow();

		HashMap<String,Object> flow= new HashMap<String,Object>();
		HashMap<String,Object> values = new HashMap<String,Object>();

		String NodeDescription = "";

		HashMap<String,Object> Nodes = new HashMap<String,Object>();
		Nodes = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Node").getObjectValue();
		List<Node> NodeValues = (List<Node>) Nodes.get("Node");
		List<Node> NodeValues1 = (List<Node>) Nodes.get("Node1");

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Loading Values of Market", mySession);

		for(int i=0;i<NodeValues.size();i++) {

			values = new HashMap<String,Object>();

			String NodeType = NodeValues.get(i).getType().toString();

			NodeDescription = NodeValues.get(i).getNodeDescription().toString();

			switch(NodeType) {

			case "DATA":
				values = preFlow.dynData(NodeValues,i);
				break;
			case "ANN":
				values = preFlow.dynAnnouncement(NodeValues,i);
				break;
			case "MENU":
				values = preFlow.dynMenu(NodeValues,i);
				break;
			case "PC":
				values = preFlow.dynPC(NodeValues,i);
				break;
			case "TRANSFER":
				values = preFlow.dynTransfer(NodeValues,i);
				break;
				//changes
			case "CONNECTBACK":
				values = preFlow.dynTransfer(NodeValues,i);
				break;
			case "CONNECTBACKVBSTATUS":
				values = preFlow.dynTransfer(NodeValues,i);
				break;
			case "CONNECTBACKVBSTATUS_2":
				values = preFlow.dynTransfer(NodeValues,i);
				break;	
			case "CONNECTBACKVBSELFSERVE":
				values = preFlow.dynTransfer(NodeValues,i);
				break;
			default :
				values = preFlow.dynData(NodeValues,i);
				break;

			}

			flow.put(NodeDescription, values);

		}

		for(int i=0;i<NodeValues1.size();i++) {

			values = new HashMap<String,Object>();

			String NodeType = NodeValues1.get(i).getType().toString();

			NodeDescription = NodeValues1.get(i).getNodeDescription().toString();

			switch(NodeType) {

			case "DATA":
				values = preFlow.dynData(NodeValues1,i);
				break;
			case "ANN":
				values = preFlow.dynAnnouncement(NodeValues1,i);
				break;
			case "MENU":
				values = preFlow.dynMenu(NodeValues1,i);
				break;
			case "PC":
				values = preFlow.dynPC(NodeValues1,i);
				break;
			case "TRANSFER":
				values = preFlow.dynTransfer(NodeValues1,i);
				break;
			default :
				values = preFlow.dynData(NodeValues1,i);
				break;

			}

			flow.put(NodeDescription, values);

		}

		mySession.getVariableField("HashMap", "Flow").setValue(flow);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void preData(SCESession mySession) {

		String method = "preData";
		

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);

		Load load = new Load();

		HashMap<String,String> CallHistory = new HashMap<String,String>();
		HashMap<String,Object> flow= new HashMap<String,Object>();
		HashMap<String,Object> flowvalues = new HashMap<String,Object>();
		HashMap<String,String> customerDetails = new HashMap<String,String>();

		customerDetails = (HashMap<String, String>) mySession.getVariableField("HashMap", "customerDetails").getObjectValue();
		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();
		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");
		String MenuID = CallHistory.get("VC_MENU_ID");

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if(flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			mySession.getVariableField("IsEnabled").setValue(flowvalues.get("IsEnabled").toString());

			//if(flowvalues.get("IsEnabled").toString().equalsIgnoreCase("true")) {

				try {

					String serviceType = flowvalues.get("ServiceType").toString().trim();

					if(!serviceType.equalsIgnoreCase("NA")) {

						customerDetails.put("ServiceType", serviceType);
						mySession.getVariableField("Service").setValue(serviceType);
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"serviceType | "+serviceType, mySession);

					}

					String customerTier = "";

					if(flowvalues.get("CustomerTier")==null)  {
						customerTier = "NA";
					} else {
						customerTier = flowvalues.get("CustomerTier").toString().trim();
					}

					if(!customerTier.isEmpty()&&!customerTier.equalsIgnoreCase("NA")) {

						customerDetails.put("CustomerTier", customerTier);
						mySession.getVariableField("CustTier").setValue(customerTier);
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"customerTier | "+customerTier, mySession);

					}

				} catch(Exception e) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ Arrays.toString(e.getStackTrace()), mySession);

				}

				MenuDescription += NodeDescription+"|";
				MenuID += flowvalues.get("Id").toString()+"|"; 

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"MenuDescription | "+MenuDescription, mySession);
				CallHistory.put("VC_MENU_ID", MenuID);
				CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
				mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);
				mySession.getVariableField("HashMap", "customerDetails").setValue(customerDetails);

				load.values(flowvalues.get("MethodName").toString(), mySession);
				
			//}

		}

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void preAnnouncement(SCESession mySession) {

		String method = "preAnnouncement";
		

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);

		HashMap<String,String> CallHistory = new HashMap<String,String>();
		HashMap<String,Object> flow= new HashMap<String,Object>();
		HashMap<String,Object> flowvalues = new HashMap<String,Object>();
		HashMap<Integer,String> promptlist = new HashMap<Integer,String>();
		HashMap<String,String> customerDetails = new HashMap<String,String>();

		customerDetails = (HashMap<String, String>) mySession.getVariableField("HashMap", "customerDetails").getObjectValue();
		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();
		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");
		String MenuID = CallHistory.get("VC_MENU_ID");

		boolean isOOHPrompt = mySession.getVariableField("IsOOHPrompt").getBooleanValue();
		
		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if(flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			mySession.getVariableField("IsEnabled").setValue(flowvalues.get("IsEnabled").toString());

			//if(flowvalues.get("IsEnabled").toString().equalsIgnoreCase("true")) {

				try {

					String serviceType = flowvalues.get("ServiceType").toString().trim();
					if(!NodeDescription.contains("TRANSFER")&&!NodeDescription.contains("DISCLAIMER")&&!MenuDescription.contains("TRANSFER")&&!MenuDescription.contains("DISCLAIMER")) {
						CallHistory.put("VC_EXIT_LOCATION",NodeDescription);
						customerDetails.put("CallReason", NodeDescription);
					}
					if(!serviceType.equalsIgnoreCase("NA")) {

						customerDetails.put("ServiceType", serviceType);
						mySession.getVariableField("Service").setValue(serviceType);
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"serviceType | "+serviceType, mySession);

					}

					String customerTier = "";

					if(flowvalues.get("CustomerTier")==null)  {
						customerTier = "NA";
					} else {
						customerTier = flowvalues.get("CustomerTier").toString().trim();
					}

					if(!customerTier.isEmpty()&&!customerTier.equalsIgnoreCase("NA")) {

						customerDetails.put("CustomerTier", customerTier);
						customerDetails.put("ServiceType", serviceType);
						mySession.getVariableField("CustTier").setValue(customerTier);
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"customerTier | "+customerTier, mySession);

					}

				} catch(Exception e) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ Arrays.toString(e.getStackTrace()), mySession);

				}

				List<String> prompts = null;

				if(isOOHPrompt) {
					prompts = (List<String>) mySession.getVariableField("OOHTiming").getObjectValue();
					mySession.getVariableField("IsOOHPrompt").setValue(false);
				} else {
					prompts = (List<String>) flowvalues.get("PhraseList");
				}
				
				//String TransVDN = mySession.getVariableField("TransferVDN").getStringValue();
				/*if(TransVDN.equalsIgnoreCase("NA")) {
					CallHistory.put("VC_EXIT_LOCATION",NodeDescription);
				}*/
				

				MenuDescription += NodeDescription+"|";
				MenuID += flowvalues.get("Id").toString()+"|"; 

				for(int i=0;i<prompts.size();i++) {

					String prompt = prompts.get(i);
					promptlist.put(i, prompt);

				}

			//} else {

				//promptlist.put(0, "0");

			//}

		} else {

			promptlist.put(0, "0");
			mySession.getVariableField("Error").setValue(true);

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ "NodeDescription "+NodeDescription+" is Missing", mySession);

		}

		mySession.getVariableField("DynamicPrompt", "Prompt").setValue(promptlist);

		mySession.getVariableField("Method").setValue("postAnnouncement");

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"MenuDescription | "+MenuDescription, mySession);
		CallHistory.put("VC_MENU_ID", MenuID);
		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);
		mySession.getVariableField("HashMap", "customerDetails").setValue(customerDetails);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void postAnnouncement(SCESession mySession) {

		String method = "preAnnouncement";
		

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);

		Load load = new Load();

		HashMap<String,Object> flow= new HashMap<String,Object>();
		HashMap<String,Object> flowvalues = new HashMap<String,Object>();

		String NextNode = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();
		String PreviousNodeDescription = "";

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if(flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if(flowvalues.get("IsEnabled").toString().equalsIgnoreCase("true")) {

				if(flowvalues.get("MethodName").toString().equalsIgnoreCase("NA")||flowvalues.get("MethodName").toString().isEmpty()) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG, className +"\t|\t"+ method +"\t|\t"+"There is no Method to Load", mySession);

				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG, className +"\t|\t"+ method +"\t|\t"+"method to load is\t|\t"+flowvalues.get("MethodName").toString(), mySession);
					load.values(flowvalues.get("MethodName").toString(), mySession);

				}

				PreviousNodeDescription = NodeDescription;
				NodeDescription = flowvalues.get("NextNode").toString();

			} else {
				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();
			}


			if(NodeDescription.equalsIgnoreCase("Disconnect")) {

				NextNode = NodeDescription;

			} else {

				if(flow.containsKey(NodeDescription)) {

					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
					mySession.getVariableField("ApplicationVariable", "IsEnabled").setValue(flowvalues.get("IsEnabled").toString());
					mySession.getVariableField("IsEnabled").setValue(flowvalues.get("IsEnabled").toString());

				} else {

					mySession.getVariableField("Error").setValue(true);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ "Next NodeDescription "+NodeDescription+" is Missing", mySession);

				}

			}


		} else {

			mySession.getVariableField("Error").setValue(true);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ "NodeDescription "+NodeDescription+" is Missing", mySession);

		}

		mySession.getVariableField("ApplicationVariable", "BargeIn").setValue("true");
		mySession.getVariableField("ApplicationVariable", "Timeout").setValue("8");
		mySession.getVariableField("ApplicationVariable", "PreviousNodeDescription").setValue(PreviousNodeDescription);
		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void preMenu(SCESession mySession) {

		String method = "preMenu";
		

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);

		HashMap<String,String> CallHistory = new HashMap<String,String>();
		HashMap<String,Object> flow= new HashMap<String,Object>();
		HashMap<String,Object> flowvalues = new HashMap<String,Object>();
		HashMap<String,String> MenuList = new HashMap<String,String>();
		HashMap<Integer,String> promptlist = new HashMap<Integer,String>();
		LinkedHashMap<String,Object> MenuHistory = new LinkedHashMap<String,Object>();
		HashMap<String,Object> MenuHistoryValues = new HashMap<String,Object>();
		HashMap<String,String> customerDetails = new HashMap<String,String>();

		customerDetails = (HashMap<String, String>) mySession.getVariableField("HashMap", "customerDetails").getObjectValue();
		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();
		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");
		String MenuID = CallHistory.get("VC_MENU_ID");

		//Set MenuHistory Data

		MenuHistory = (LinkedHashMap<String, Object>) mySession.getVariableField("ApplicationVariable", "MenuHistory").getObjectValue();
		String MenuDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		int Sequence = mySession.getVariableField("ApplicationVariable", "MenuHistorySequence").getIntValue();

		MenuHistoryValues.put("MenuDate", MenuDate);
		MenuHistoryValues.put("MenuKey", NodeDescription);
		MenuHistory.put(Integer.toString(Sequence), MenuHistoryValues);

		mySession.getVariableField("ApplicationVariable", "MenuHistorySequence").setValue(++Sequence);
		mySession.getVariableField("ApplicationVariable", "MenuHistory").setValue(MenuHistory);
		mySession.getVariableField("ApplicationVariable", "isMenuKeyAdded").setValue(true);
		//Setting MenuHistory Completed

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if(flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			mySession.getVariableField("IsEnabled").setValue(flowvalues.get("IsEnabled").toString());

			//if(flowvalues.get("IsEnabled").toString().equalsIgnoreCase("true")) {

				try {

					String serviceType = flowvalues.get("ServiceType").toString().trim();
					if(!NodeDescription.contains("TRANSFER")&&!NodeDescription.contains("DISCLAIMER")&&!MenuDescription.contains("TRANSFER")&&!MenuDescription.contains("DISCLAIMER")) {
						CallHistory.put("VC_EXIT_LOCATION",NodeDescription);
						customerDetails.put("CallReason", NodeDescription);
					}
					if(!serviceType.equalsIgnoreCase("NA")) {

						customerDetails.put("ServiceType", serviceType);
						mySession.getVariableField("Service").setValue(serviceType);
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"serviceType | "+serviceType, mySession);

					}

					String customerTier = "";

					if(flowvalues.get("CustomerTier")==null)  {
						customerTier = "NA";
					} else {
						customerTier = flowvalues.get("CustomerTier").toString().trim();
					}

					if(!customerTier.isEmpty()&&!customerTier.equalsIgnoreCase("NA")) {

						customerDetails.put("CustomerTier", customerTier);
						mySession.getVariableField("CustTier").setValue(customerTier);
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"customerTier | "+customerTier, mySession);

					}

				} catch(Exception e) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ Arrays.toString(e.getStackTrace()), mySession);

				}

				mySession.getVariableField("ApplicationVariable", "BargeIn").setValue(flowvalues.get("IsBargingAllowed").toString());
				mySession.getVariableField("ApplicationVariable", "Timeout").setValue(flowvalues.get("TimeOut").toString());

				List<String> prompts = (List<String>) flowvalues.get("PhraseList");

				//String TransVDN = mySession.getVariableField("TransferVDN").getStringValue();
				/*if(TransVDN.equalsIgnoreCase("NA")) {
					CallHistory.put("VC_EXIT_LOCATION",NodeDescription);
				}*/
				
				MenuDescription += NodeDescription+"|";
				MenuID += flowvalues.get("Id").toString()+"|"; 

				for(int i=0;i<prompts.size();i++) {

					String prompt = prompts.get(i);
					promptlist.put(i, prompt);

				}

				String[] MenuValues = flowvalues.get("BranchNames").toString().split(",");
				String[] DTMF = flowvalues.get("Grammar").toString().split(",");

				for(int j=0;j<MenuValues.length;j++) {
					MenuList.put(DTMF[j], MenuValues[j]);
				}

			//} else {

				//promptlist.put(0, "0");
				//MenuList.put("0", "Empty");

			//}

		} else {

			promptlist.put(0, "0");
			MenuList.put("0", "Empty");
			mySession.getVariableField("Error").setValue(true);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ "NodeDescription "+NodeDescription+" is Missing", mySession);

		}

		mySession.getVariableField("DynamicPrompt", "Prompt").setValue(promptlist);

		mySession.getVariableField("ApplicationVariable", "MenuList").setValue(MenuList);

		mySession.getVariableField("Method").setValue("postMenu");

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"MenuDescription | "+MenuDescription, mySession);
		CallHistory.put("VC_MENU_ID", MenuID);
		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);
		mySession.getVariableField("HashMap", "customerDetails").setValue(customerDetails);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void postMenu(SCESession mySession) {

		String method = "postMenu";
		

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);

		Load load = new Load();

		HashMap<String,String> CallHistory = new HashMap<String,String>();
		HashMap<String,Object> flow= new HashMap<String,Object>();
		HashMap<String,Object> flowvalues = new HashMap<String,Object>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String NextNode = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();
		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");
		String BranchName = "";
		String PreviousNodeDescription = "";

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if(flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if(flowvalues.get("IsEnabled").toString().equalsIgnoreCase("true")) {

				if(flowvalues.get("MethodName").toString().equalsIgnoreCase("NA")||flowvalues.get("MethodName").toString().isEmpty()) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG, className +"\t|\t"+ method +"\t|\t"+"There is no Method to Load", mySession);

				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG, className +"\t|\t"+ method +"\t|\t"+"method to load is\t|\t"+flowvalues.get("MethodName").toString(), mySession);
					load.values(flowvalues.get("MethodName").toString(), mySession);

				}

				PreviousNodeDescription = NodeDescription;
				String dtmfSelected = mySession.getVariableField("DynamicMenu", "value").getStringValue();
				String grammar = flowvalues.get("Grammar").toString();
				String nextNodeValues = flowvalues.get("NextNode").toString();
				BranchName = flowvalues.get("BranchNames").toString();

				String[] grammarList = grammar.split(",");
				String[] nextNodeValuesList = nextNodeValues.split(",");
				String[] branchnameList = BranchName.split(",");

				for(int z=0;z<grammarList.length;z++) {

					if(dtmfSelected.equalsIgnoreCase(grammarList[z])) {
						BranchName = branchnameList[z];
						NodeDescription = nextNodeValuesList[z];
						break;
					}

				}

				MenuDescription += BranchName+"|";

			} else {

				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

			}


			if(NodeDescription.equalsIgnoreCase("Disconnect")) {

				NextNode = NodeDescription;

			} else {

				if(flow.containsKey(NodeDescription)) {

					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
					mySession.getVariableField("ApplicationVariable", "IsEnabled").setValue(flowvalues.get("IsEnabled").toString());
					mySession.getVariableField("IsEnabled").setValue(flowvalues.get("IsEnabled").toString());

				} else {

					mySession.getVariableField("Error").setValue(true);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ "Next NodeDescription "+NodeDescription+" is Missing", mySession);

				}

			}

			CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"NodeDescription\t|\t"+NodeDescription, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"NextNode\t|\t"+NextNode, mySession);

		} else {

			mySession.getVariableField("Error").setValue(true);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ "NodeDescription "+NodeDescription+" is Missing", mySession);

		}

		mySession.getVariableField("ApplicationVariable", "BargeIn").setValue("true");
		mySession.getVariableField("ApplicationVariable", "Timeout").setValue("8");
		mySession.getVariableField("ApplicationVariable", "NoInput").setValue("1");
		mySession.getVariableField("ApplicationVariable", "NoMatch").setValue("1");
		mySession.getVariableField("ApplicationVariable", "PreviousNodeDescription").setValue(PreviousNodeDescription);
		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void prePC(SCESession mySession) {

		String method = "prePC";
		

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);

		HashMap<String,String> CallHistory = new HashMap<String,String>();
		HashMap<String,Object> flow= new HashMap<String,Object>();
		HashMap<String,Object> flowvalues = new HashMap<String,Object>();
		HashMap<String,String> MenuList = new HashMap<String,String>();
		HashMap<Integer,String> promptlist = new HashMap<Integer,String>();
		LinkedHashMap<String,Object> MenuHistory = new LinkedHashMap<String,Object>();
		HashMap<String,Object> MenuHistoryValues = new HashMap<String,Object>();
		HashMap<String,String> App_Prop = new HashMap<String,String>();
		HashMap<String,String> customerDetails = new HashMap<String,String>();

		customerDetails = (HashMap<String, String>) mySession.getVariableField("HashMap", "customerDetails").getObjectValue();
		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();
		App_Prop = (HashMap<String, String>) mySession.getVariableField("HashMap", "PropertyValue").getObjectValue();

		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();
		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");
		String MenuID = CallHistory.get("VC_MENU_ID");

		//Set MenuHistory Data
		MenuHistory = (LinkedHashMap<String, Object>) mySession.getVariableField("ApplicationVariable", "MenuHistory").getObjectValue();
		String MenuDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		int Sequence = mySession.getVariableField("ApplicationVariable", "MenuHistorySequence").getIntValue();

		MenuHistoryValues.put("MenuDate", MenuDate);
		MenuHistoryValues.put("MenuKey", NodeDescription);
		MenuHistory.put(Integer.toString(Sequence), MenuHistoryValues);

		mySession.getVariableField("ApplicationVariable", "MenuHistorySequence").setValue(++Sequence);
		mySession.getVariableField("ApplicationVariable", "MenuHistory").setValue(MenuHistory);
		mySession.getVariableField("ApplicationVariable", "isMenuKeyAdded").setValue(true);
		//Setting MenuHistory Completed

		if(App_Prop.containsKey(NodeDescription+"_RetryCount")) {
			mySession.getVariableField("ApplicationVariable", "retryCount").setValue(App_Prop.get(NodeDescription+"_RetryCount"));
		} else {
			mySession.getVariableField("ApplicationVariable", "retryCount").setValue(2);
		}

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if(flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			mySession.getVariableField("IsEnabled").setValue(flowvalues.get("IsEnabled").toString());

			//if(flowvalues.get("IsEnabled").toString().equalsIgnoreCase("true")) {

			try {

				String serviceType = flowvalues.get("ServiceType").toString().trim();
				if(!NodeDescription.contains("TRANSFER")&&!NodeDescription.contains("DISCLAIMER")&&!MenuDescription.contains("TRANSFER")&&!MenuDescription.contains("DISCLAIMER")) {
					CallHistory.put("VC_EXIT_LOCATION",NodeDescription);
					customerDetails.put("CallReason", NodeDescription);
				}
				if(!serviceType.equalsIgnoreCase("NA")) {

					customerDetails.put("ServiceType", serviceType);
					mySession.getVariableField("Service").setValue(serviceType);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"serviceType | "+serviceType, mySession);

				}

				String customerTier = "";

				if(flowvalues.get("CustomerTier")==null)  {
					customerTier = "NA";
				} else {
					customerTier = flowvalues.get("CustomerTier").toString().trim();
				}

				if(!customerTier.isEmpty()&&!customerTier.equalsIgnoreCase("NA")) {

					customerDetails.put("CustomerTier", customerTier);
					mySession.getVariableField("CustTier").setValue(customerTier);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"customerTier | "+customerTier, mySession);

				}

			} catch(Exception e) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ Arrays.toString(e.getStackTrace()), mySession);

			}

				List<String> prompts = (List<String>) flowvalues.get("PhraseList");

				mySession.getVariableField("ApplicationVariable", "BargeIn").setValue(flowvalues.get("IsBargingAllowed").toString());
				mySession.getVariableField("ApplicationVariable", "Timeout").setValue(flowvalues.get("TimeOut").toString());

				//String TransVDN = mySession.getVariableField("TransferVDN").getStringValue();
				/*if(TransVDN.equalsIgnoreCase("NA")) {
					CallHistory.put("VC_EXIT_LOCATION",NodeDescription);
				}*/
				
				MenuDescription += NodeDescription+"|";
				MenuID += flowvalues.get("Id").toString()+"|"; 

				for(int i=0;i<prompts.size();i++) {

					String prompt = prompts.get(i);
					promptlist.put(i, prompt);

				}
				
				String AppServerIP = CallHistory.get("VC_APP_SRVR_IP");
				String Grammar = flowvalues.get("Grammar").toString().replace("*", AppServerIP).trim();
				mySession.getVariableField("ApplicationVariable", "PCGrammarUrl").setValue(Grammar);
				mySession.getVariableField("ApplicationVariable", "InterDigitTimeOut").setValue(flowvalues.get("InterDigitTimeOut").toString());
				mySession.getVariableField("ApplicationVariable", "IsPrivate").setValue(flowvalues.get("IsPrivate").toString());
				mySession.getVariableField("ApplicationVariable", "Termchar").setValue(flowvalues.get("Termchar").toString());

			//} else {

				//promptlist.put(0, "0");
				//MenuList.put("0", "Empty");

			//}

		} else {

			promptlist.put(0, "0");
			MenuList.put("0", "Empty");
			mySession.getVariableField("Error").setValue(true);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ "NodeDescription "+NodeDescription+" is Missing", mySession);

		}

		mySession.getVariableField("DynamicPrompt", "Prompt").setValue(promptlist);

		mySession.getVariableField("ApplicationVariable", "MenuList").setValue(MenuList);

		mySession.getVariableField("Method").setValue("postPC");

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"MenuDescription | "+MenuDescription, mySession);
		CallHistory.put("VC_MENU_ID", MenuID);
		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);
		mySession.getVariableField("HashMap", "customerDetails").setValue(customerDetails);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void postPC(SCESession mySession) {

		String method = "postPC";
		

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);

		Load load = new Load();

		HashMap<String,String> CallHistory = new HashMap<String,String>();
		HashMap<String,Object> flow= new HashMap<String,Object>();
		HashMap<String,Object> flowvalues = new HashMap<String,Object>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String NextNode = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();
		String PreviousNodeDescription = "";
		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");
		String BranchName = "";

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if(flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if(flowvalues.get("IsEnabled").toString().equalsIgnoreCase("true")) {

				if(flowvalues.get("MethodName").toString().equalsIgnoreCase("NA")||flowvalues.get("MethodName").toString().isEmpty()) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG, className +"\t|\t"+ method +"\t|\t"+"There is no Method to Load", mySession);

				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG, className +"\t|\t"+ method +"\t|\t"+"method to load is\t|\t"+flowvalues.get("MethodName").toString(), mySession);
					load.values(flowvalues.get("MethodName").toString(), mySession);

				}

				PreviousNodeDescription = NodeDescription;
				NodeDescription = flowvalues.get("NextNode").toString();
				BranchName = flowvalues.get("BranchNames").toString();

				MenuDescription += BranchName+"|";

			} else {

				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

			}


			if(NodeDescription.equalsIgnoreCase("Disconnect")) {

				NextNode = NodeDescription;

			} else {

				if(flow.containsKey(NodeDescription)) {

					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
					mySession.getVariableField("ApplicationVariable", "IsEnabled").setValue(flowvalues.get("IsEnabled").toString());
					mySession.getVariableField("IsEnabled").setValue(flowvalues.get("IsEnabled").toString());

				} else {

					mySession.getVariableField("Error").setValue(true);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ "Next NodeDescription "+NodeDescription+" is Missing", mySession);

				}

			}

			CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"NodeDescription\t|\t"+NodeDescription, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"NextNode\t|\t"+NextNode, mySession);

		} else {

			mySession.getVariableField("Error").setValue(true);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ "NodeDescription "+NodeDescription+" is Missing", mySession);

		}
		mySession.getVariableField("ApplicationVariable", "BargeIn").setValue("true");
		mySession.getVariableField("ApplicationVariable", "Timeout").setValue("8");
		mySession.getVariableField("ApplicationVariable", "NoInput").setValue("1");
		mySession.getVariableField("ApplicationVariable", "NoMatch").setValue("1");
		mySession.getVariableField("ApplicationVariable", "PreviousNodeDescription").setValue(PreviousNodeDescription);
		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void noInputNoMatch(SCESession mySession) {

		String method = "noInputNoMatch";
		

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);

		HashMap<String,Object> flow= new HashMap<String,Object>();
		HashMap<String,Object> flowvalues = new HashMap<String,Object>();
		HashMap<String,String> GeneralValues = new HashMap<String,String>();
		HashMap<String,String> CallHistory = new HashMap<String,String>();

		String NextNode = "";
		String RetryCumulativeFlag = "";
		String MethodName = mySession.getVariableField("Method").getStringValue();
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();
		String noInputCount = mySession.getVariableField("DynamicMenu", "noinputcount").getStringValue();
		String noMatchCount = mySession.getVariableField("DynamicMenu", "nomatchcount").getStringValue();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();
		
		if(MethodName.equalsIgnoreCase("postMenu")) {

			noInputCount = mySession.getVariableField("DynamicMenu", "noinputcount").getStringValue();
			noMatchCount = mySession.getVariableField("DynamicMenu", "nomatchcount").getStringValue();

		} else if(MethodName.equalsIgnoreCase("postPC")) {

			noInputCount = mySession.getVariableField("DynamicPC", "noinputcount").getStringValue();
			noMatchCount = mySession.getVariableField("DynamicPC", "nomatchcount").getStringValue();

		}
		
		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");
		String MenuID = CallHistory.get("VC_MENU_ID");

		String noinputRetryCount = mySession.getVariableField("ApplicationVariable", "NoInput").getStringValue();
		String nomatchRetryCount = mySession.getVariableField("ApplicationVariable", "NoMatch").getStringValue();

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if(flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if(flowvalues.get("IsEnabled").toString().equalsIgnoreCase("true")) {

				GeneralValues = (HashMap<String, String>) mySession.getVariableField("ApplicationVariable", "DBAppValues").getObjectValue();

				RetryCumulativeFlag = GeneralValues.get("VC_RETRY_CUMULATIVE_FLAG");
				String MaxtriesCount = flowvalues.get("NoOfRetries").toString();

				if(RetryCumulativeFlag.equalsIgnoreCase("Y")) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"RetryCumulativeFlag\t|\t"+RetryCumulativeFlag, mySession);

					if(noinputRetryCount.isEmpty()) {
						noinputRetryCount = "1";
					}
					if(nomatchRetryCount.isEmpty()) {
						nomatchRetryCount = "1";
					}

					if(Integer.parseInt(nomatchRetryCount)<Integer.parseInt(MaxtriesCount)) {

						if(noInputCount.equalsIgnoreCase("1")) {
							NodeDescription = flowvalues.get("NoInputNextNode").toString();
						} else {
							NodeDescription = flowvalues.get("NoMatchNextNode").toString();
						}

						noinputRetryCount = Integer.toString(Integer.parseInt(noinputRetryCount) + 1);
						nomatchRetryCount = Integer.toString(Integer.parseInt(nomatchRetryCount) + 1);

					} else {
						NodeDescription = flowvalues.get("MaxtriesNextNode").toString();
						noinputRetryCount = "1";
						nomatchRetryCount = "1";
					}

				} else {

					if(noInputCount.equalsIgnoreCase("1")) {

						if(noinputRetryCount.isEmpty()) {
							noinputRetryCount = "1";
						}

						if(Integer.parseInt(noinputRetryCount)<Integer.parseInt(MaxtriesCount)) {

							NodeDescription = flowvalues.get("NoInputNextNode").toString();

							noinputRetryCount = Integer.toString(Integer.parseInt(noinputRetryCount) + 1);

						} else {
							NodeDescription = flowvalues.get("MaxtriesNextNode").toString();
							noinputRetryCount = "1";
							nomatchRetryCount = "1";
						}

					} else if(noMatchCount.equalsIgnoreCase("1")) {

						if(nomatchRetryCount.isEmpty()) {
							nomatchRetryCount = "1";
						}

						if(Integer.parseInt(nomatchRetryCount)<Integer.parseInt(MaxtriesCount)) {

							NodeDescription = flowvalues.get("NoMatchNextNode").toString();

							nomatchRetryCount = Integer.toString(Integer.parseInt(nomatchRetryCount) + 1);

						} else {
							NodeDescription = flowvalues.get("MaxtriesNextNode").toString();
							noinputRetryCount = "1";
							nomatchRetryCount = "1";
						}

					} else {

						NodeDescription = flowvalues.get("MaxtriesNextNode").toString();
						noinputRetryCount = "1";
						nomatchRetryCount = "1";

					}

				}

				MenuDescription += NodeDescription+"|";
				MenuID += flowvalues.get("Id").toString()+"|"; 

				flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
				NextNode = flowvalues.get("Type").toString();
				mySession.getVariableField("ApplicationVariable", "IsEnabled").setValue(flowvalues.get("IsEnabled").toString());

			}

		}

		CallHistory.put("VC_MENU_ID", MenuID);
		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);
		
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"noinputRetryCount\t|\t"+noinputRetryCount, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"nomatchRetryCount\t|\t"+nomatchRetryCount, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"NodeDescription\t|\t"+NodeDescription, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"NextNode\t|\t"+NextNode, mySession);

		if(MethodName.equalsIgnoreCase("postPC")) {

			mySession.getVariableField("ApplicationVariable", "apiNoInput").setValue(noinputRetryCount);
			mySession.getVariableField("ApplicationVariable", "apiNoMatch").setValue(nomatchRetryCount);

		}
		
		mySession.getVariableField("ApplicationVariable", "NoInput").setValue(noinputRetryCount);
		mySession.getVariableField("ApplicationVariable", "NoMatch").setValue(nomatchRetryCount);
		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void flowLanguage(SCESession mySession) {

		String method = "flowLanguage";
		

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);

		String Language = "";
		String dtmfSelected = mySession.getVariableField("DynamicMenu", "value").getStringValue();

		HashMap<String,Object> flow= new HashMap<String,Object>();
		HashMap<String,Object> flowvalues = new HashMap<String,Object>();
		HashMap<String,String> CallHistory = new HashMap<String,String>();
		HashMap<String,String> customerDetails = new HashMap<String,String>();

		customerDetails = (HashMap<String, String>) mySession.getVariableField("HashMap", "customerDetails").getObjectValue();
		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();
		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if(flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if(flowvalues.get("IsEnabled").toString().equalsIgnoreCase("true")) {

				if(flowvalues.get("Type").toString().equalsIgnoreCase("MENU")){

					String grammar = flowvalues.get("Grammar").toString();
					String BranchNames = flowvalues.get("BranchNames").toString();

					String[] grammarList = grammar.split(",");
					String[] BranchNamesList = BranchNames.split(",");

					for(int z=0;z<grammarList.length;z++) {

						if(dtmfSelected.equalsIgnoreCase(grammarList[z])) {
							Language = BranchNamesList[z];
							break;
						}

					}

				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Caller Register Language is "+Language, mySession);
					Language = mySession.getVariableField("ApplicationVariable", "Language").getStringValue();
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Caller Register Language Empty Setting Default Language "+Language, mySession);

				}

				MenuDescription += Language+"|";

			}

		}

		customerDetails.put("CustomerLanguage", Language);
		CallHistory.put("VC_LANGUAGE", Language);
		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);

		mySession.getVariableField("ApplicationVariable", "Language").setValue(Language);
		mySession.getVariableField("DynamicPrompt","PromptLanguage").setValue(Language);

		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);
		mySession.getVariableField("HashMap", "customerDetails").setValue(customerDetails);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);

	}
	
	@SuppressWarnings("unchecked")
	public String Location(SCESession mySession) {

		String method = "Location";
		
		String Location = LoadApplicationProperties.getProperty("DefaultLocation", mySession);
		
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);

		try {
			
			List<String> DBStatus = new ArrayList<String>();
			DBStatus = (List<String>) mySession.getVariableField("ApplicationVariable", "DBStatusHashmap").getObjectValue();
			String LocationJsonName = LoadApplicationProperties.getProperty("LocationJsonName", mySession);
			String LocationJson = LoadApplicationProperties.getProperty("LocationJsonPath", mySession);
			String LocationBackup = LoadApplicationProperties.getProperty("LocationBackup", mySession);
			String LocationJsonPath = LocationJson+LocationJsonName;
			String LocationBackupPath = LocationJson+LocationBackup+new SimpleDateFormat("YYMMddHHmmssSSS").format(new Date())+"_"+LocationJsonName;
			String LocationFromDB = "";
			File LocationFile = new File(new File(LocationJsonPath).getCanonicalPath());
			File LocationBackupFile = new File(new File(LocationBackupPath).getCanonicalPath());
			
			if(DBStatus.contains("IVR_LOCATION_MASTER")) {

				if(!new File(LocationJson+LocationBackup).exists()) {
					new File(LocationJson+LocationBackup).mkdirs();
				}
				
				LocationFromDB = loadLocationFromDB(mySession);
				if(!LocationFromDB.equalsIgnoreCase("")) {
					if(LocationFile.exists()) {
						LocationFile.renameTo(LocationBackupFile);
					} else {
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Location Json File is not Exist", mySession);
					}
					try {
						FileWriter wr = new FileWriter(LocationFile);
						wr.write(LocationFromDB);
						wr.close();
					} catch (IOException e) {
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ Arrays.toString(e.getStackTrace()), mySession);
					}
				}
			} else {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Location is Not Modified in Data Base", mySession);
			}

			if(!LocationFile.exists()) {
				
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Location Json File is not Exist", mySession);
				
				LocationFromDB = loadLocationFromDB(mySession);
				
				try {
					FileWriter wr = new FileWriter(LocationFile);
					wr.write(LocationFromDB);
					wr.close();
				} catch (IOException e) {
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ Arrays.toString(e.getStackTrace()), mySession);
				}
				
			}
		
			Location = parseLocation(LocationFile, mySession);
			
		} catch(Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ Arrays.toString(e.getStackTrace()), mySession);
		}
		
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);

		return Location;
		
	}
	
	@SuppressWarnings("unchecked")
	private String loadLocationFromDB(SCESession mySession) {

		String method = "loadLocationFromDB";
		String finalJsonString = "";
		

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);

		try {
			
			boolean DBException = mySession.getVariableField("ApplicationVariable", "DbException").getBooleanValue();
			
			if(!DBException) {
				
				DBConnection db = new DBConnection();
				List<HashMap<Object, Object>> DBResult = null;
				LinkedHashMap<String, Object> input = new LinkedHashMap<String, Object>();
				LinkedHashMap<String,String> CallHistory = new LinkedHashMap<String,String>();
				
				CallHistory = (LinkedHashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();
				input.put("VC_APP_SRVR_IP", CallHistory.get("VC_APP_SRVR_IP"));
				DBResult = db.getResultSet("SP_GET_LOCATION_MASTER", input, mySession);
				
				String tempmarket ="NA";
				String tempLang = "NA";

				String lineEnd = "";
				String lineStart="";

				String newLine = "\n";
				String tab = "\t";
				String comma=",";
				String openBracket="{";
				String closedBracket="}";
				String quotation = "\"";
				String colon =": ";

				for(int i=0;i<DBResult.size();i++) {

					lineEnd = comma+newLine+tab;
					lineStart = openBracket+newLine+tab;
					String realMarketName = DBResult.get(i).get("VC_MARKET_NAME").toString();

					if(tempmarket.equalsIgnoreCase(realMarketName)) {
						lineStart=tab;

						if(DBResult.size()-1==i)  lineEnd = newLine+"    "+closedBracket;

					} else {

						tempLang="NA";
						if(DBResult.size()-1==i) lineEnd = newLine+"    "+closedBracket;
						tempmarket=realMarketName;
						finalJsonString+=tab+quotation+realMarketName+quotation+colon;

					}

					String realLineType = DBResult.get(i).get("VC_LINE_TYPE").toString();

					if(!tempLang.equalsIgnoreCase(realLineType)) {

						tempLang = realLineType;

						if(i+1<DBResult.size()) {

							if(!realMarketName.equalsIgnoreCase(DBResult.get(i+1).get("VC_MARKET_NAME").toString())) {

								lineEnd = newLine+tab+closedBracket+comma+newLine;
								tempLang="NA";

							}

						}

						finalJsonString += lineStart+quotation+realLineType+quotation+colon+openBracket+newLine+
								tab+tab+tab+"\"VC_PRIMARY_LOCATION\": \""+DBResult.get(i).get("VC_PRIMARY_LOCATION")+"\""+comma+newLine+
								tab+tab+tab+"\"NU_PRIMARY_TIMEOUT\": "+DBResult.get(i).get("NU_PRIMARY_TIMEOUT")+comma+newLine+
								tab+tab+tab+"\"VC_SECONDARY_LOCATION\": \""+DBResult.get(i).get("VC_SECONDARY_LOCATION")+"\""+comma+newLine+
								tab+tab+tab+"\"NU_SECONDARY_TIMEOUT\": "+DBResult.get(i).get("NU_SECONDARY_TIMEOUT")+comma+newLine+
								tab+tab+tab+"\"VC_TERNARY_LOCATION\": \""+DBResult.get(i).get("VC_TERNARY_LOCATION")+"\""+comma+newLine+
								tab+tab+tab+"\"NU_TERNARY_TIMEOUT\": "+DBResult.get(i).get("NU_TERNARY_TIMEOUT")+comma+newLine+
								tab+tab+tab+"\"VC_QATERNARY_LOCATION\": \""+DBResult.get(i).get("VC_QATERNARY_LOCATION")+"\""+comma+newLine+
								tab+tab+tab+"\"IS_ACTIVE\": \""+DBResult.get(i).get("IS_ACTIVE")+"\""+newLine+tab+tab+closedBracket+lineEnd;

					}		

				}

				finalJsonString = openBracket+newLine+finalJsonString+newLine+closedBracket;

			} else {
				
			}
			
		} catch(Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ Arrays.toString(e.getStackTrace()), mySession);
		}
		
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);
		
		return finalJsonString;
		
	}
	
	@SuppressWarnings("unchecked")
	private String parseLocation(File LocationFile, SCESession mySession) {

		String method = "parseLocation";
		
		
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);
		
		FileReader fr = null;
		String Location = LoadApplicationProperties.getProperty("DefaultLocation", mySession);
		HashMap<String,String> CallHistory = new HashMap<String,String>();
		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();
		String Market = CallHistory.get("VC_MARKET").toString().trim();
		String LineType = CallHistory.get("VC_LINE_TYPE").toString().trim();

		try {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Default Location | "+Location, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Market | "+Market, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"LineType | "+LineType, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Loading Location File | "+LocationFile, mySession);

			JSONParser jsonParser = new JSONParser();
			fr = new FileReader(LocationFile);

			JSONObject jsonObj = (JSONObject) jsonParser.parse(fr);
			
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"JSON OBJECT PARSED ", mySession);
			
			if(jsonObj.containsKey(Market)) {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Market is Present in Json | "+Market, mySession);
				JSONObject marketJson = (JSONObject) jsonParser.parse(jsonObj.get(Market).toString().trim());
				if(marketJson.containsKey(LineType)) {
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"LineType is Present in Json | "+LineType, mySession);
					JSONObject lineType = (JSONObject) jsonParser.parse(marketJson.get(LineType).toString().trim());
					if(lineType.containsKey("VC_PRIMARY_LOCATION")) {
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Location from Json Pre | ", mySession);
						Location = lineType.get("VC_PRIMARY_LOCATION").toString().trim();
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Location from Json | "+Location, mySession);
					}
				}
			}
			
		} catch (Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+Arrays.toString(e.getStackTrace()), mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
		} finally {
			try {
				fr.close();
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"File Reader is Closed", mySession);
			} catch(Exception e) {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+Arrays.toString(e.getStackTrace()), mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
			}
		}

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Location Finally Received is | "+Location, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);

		return Location;
		
	}
	
	@SuppressWarnings("unchecked")
	public String EWT(SCESession mySession) {
		
		String method = "EWT";
		String Ewt_Prompt = "";
		

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);

		try {

			List<String> DBStatus = new ArrayList<String>();
			DBStatus = (List<String>) mySession.getVariableField("ApplicationVariable", "DBStatusHashmap").getObjectValue();
			String EwtJsonName = LoadApplicationProperties.getProperty("EwtJsonName", mySession);
			String EwtJson = LoadApplicationProperties.getProperty("EwtJsonPath", mySession);
			String EwtBackup = LoadApplicationProperties.getProperty("EwtBackup", mySession);
			String EwtJsonPath = EwtJson+EwtJsonName;
			String EwtBackupPath = EwtJson+EwtBackup+new SimpleDateFormat("YYMMddHHmmssSSS").format(new Date())+"_"+EwtJsonName;
			String EwtFromDB = "";
			File EwtFile = new File(new File(EwtJsonPath).getCanonicalPath());
			File EwtBackupFile = new File(new File(EwtBackupPath).getCanonicalPath());

			if(DBStatus.contains("IVR_EWT_MASTER")) {

				if(!new File(EwtJson+EwtBackup).exists()) {
					new File(EwtJson+EwtBackup).mkdirs();
				}
				
				EwtFromDB = loadEwtFromDB(mySession);
				if(!EwtFromDB.equalsIgnoreCase("")) {
					if(EwtFile.exists()) {
						EwtFile.renameTo(EwtBackupFile);
					} else {
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"EWT Json File is not Exist", mySession);
					}
					try {
						FileWriter wr = new FileWriter(EwtFile);
						wr.write(EwtFromDB);
						wr.close();
					} catch (IOException e) {
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ Arrays.toString(e.getStackTrace()), mySession);
					}
				}
			} else {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"EWT is Not Modified in Data Base", mySession);
			}
			
			if(!EwtFile.exists()) {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"EWT Json File is not Exist", mySession);

				EwtFromDB = loadEwtFromDB(mySession);
				
				try {
					FileWriter wr = new FileWriter(EwtFile);
					wr.write(EwtFromDB);
					wr.close();
				} catch (IOException e) {
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ Arrays.toString(e.getStackTrace()), mySession);
				}
			}
			
			Ewt_Prompt = parseEwt(EwtFile, mySession);

		} catch(Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ Arrays.toString(e.getStackTrace()), mySession);
		}

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);

		return Ewt_Prompt;
	}

	@SuppressWarnings("unchecked")
	private String loadEwtFromDB(SCESession mySession) {
		
		String method = "EwtLocationFromDB";
		String finalJsonString = "";
		

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);

		try {

			boolean DBException = mySession.getVariableField("ApplicationVariable", "DbException").getBooleanValue();

			if(!DBException) {

				DBConnection db = new DBConnection();
				List<HashMap<Object, Object>> DBResult = null;
				LinkedHashMap<String, Object> input = new LinkedHashMap<String, Object>();
				LinkedHashMap<String,String> CallHistory = new LinkedHashMap<String,String>();
				LinkedHashMap<Object,Object> hashmap = new LinkedHashMap<Object,Object>();
				HashMap<Object,Object> params = null;

				CallHistory = (LinkedHashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();
				input.put("VC_APP_SRVR_IP", CallHistory.get("VC_APP_SRVR_IP"));
				DBResult = db.getResultSet("SP_GET_EWT_MASTER", input, mySession);


				String Language_ID ="";
				String Market_ID = "";
				String App_ID = "";
				String Language_name="";
				String Market_name="";
				String App_name="";
				String Ewt_Interval ="";
				String Ewt_Prompt = "";
				String Active_Status ="";
				String App_Param = "VC_APP_ID";
				String Market_Param = "VC_MARKET_ID";


				for(int i=0;i<DBResult.size();i++) {

					Language_ID = DBResult.get(i).get("NU_LANGUAGE_ID").toString();
					Language_name = DBResult.get(i).get("VC_LANGUAGE_NAME").toString();
					Market_ID =  DBResult.get(i).get("NU_MARKET_ID").toString();
					Market_name = DBResult.get(i).get("VC_MARKET_NAME").toString();
					App_ID =  DBResult.get(i).get("NU_APP_ID").toString();
					App_name = DBResult.get(i).get("VC_APP_NAME").toString();
					Ewt_Interval =  DBResult.get(i).get("VC_EWT_INTERVAL").toString();
					Ewt_Prompt = DBResult.get(i).get("VC_EWT_PROMPT").toString();
					Active_Status = DBResult.get(i).get("VC_ACTIVE_STATUS").toString();

					params = new HashMap<Object,Object>();
					params.put("NU_LANGUAGE_ID", Language_ID);
					params.put("VC_EWT_INTERVAL",Ewt_Interval);
					params.put("VC_EWT_PROMPT", Ewt_Prompt);
					params.put("VC_ACTIVE_STATUS",Active_Status);



					LinkedList<Object> list1 = new LinkedList<>();
					list1.add(params);

					HashMap<Object, Object> lang = new HashMap<Object, Object>();
					lang.put(App_Param, App_ID);
					lang.put(Language_name, list1);

					HashMap<Object, Object> app = new HashMap<Object, Object>();
					app.put(Market_Param, Market_ID);
					app.put(App_name, lang);

					if(hashmap.containsKey(Market_name)) {
						if(((HashMap<Object, Object>) hashmap.get(Market_name)).containsKey(App_name)) {
							if(((HashMap<Object,Object>) ((HashMap<Object,Object>) hashmap.get(Market_name)).get(App_name)).containsKey(Language_name)) {
								((LinkedList<Object>)(((HashMap<Object,Object>) ((HashMap<Object,Object>) hashmap.get(Market_name)).get(App_name)).get(Language_name))).add(params);
							} else {
								((HashMap<Object,Object>) ((HashMap<Object,Object>) hashmap.get(Market_name)).get(App_name)).putAll(lang);
							}
						}else {
							((HashMap<Object, Object>) hashmap.get(Market_name)).put(App_name, lang);
						}
					}
					else {
						hashmap.put(Market_name, app);
					}		

				}

				ObjectMapper mapper = new ObjectMapper();
				finalJsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(hashmap);

			} else {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"DataBase records are not Available ", mySession);
			}

		} catch(Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ Arrays.toString(e.getStackTrace()), mySession);
		}

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);

		return finalJsonString;

	}	

	private String parseEwt(File EwtFile, SCESession mySession) {

		String method = "parseEwt";
		String Ewt_Prompt = "";
		

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);


		String DBInterval ="";
		String Interval = "";
		int ApiInterval;
		String Api_Ewt = mySession.getVariableField("ApiEwt").getStringValue().trim();

		HashMap<String,String> EwtMap = new HashMap<String,String>();


		try {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"EWT from Api Response | "+Api_Ewt, mySession);

			EwtMap = JsonParseEwt(EwtFile,mySession);

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"JSON OBJECT PARSED ", mySession);


			if(!EwtMap.isEmpty()) {
				for (Map.Entry<String, String> mapset : EwtMap.entrySet()) {
					DBInterval = mapset.getKey();
					String [] interArray = DBInterval.split("-");

					int StartInterval = Integer.parseInt(interArray[0]);
					int EndInterval = Integer.parseInt(interArray[1]);

					ApiInterval = (int) Math.round(Double.parseDouble(Api_Ewt));

					if(ApiInterval>=StartInterval && ApiInterval<=EndInterval) {
						Interval = DBInterval;
						Ewt_Prompt = mapset.getValue();
						break;
					} 
				}
			} else {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Prompt is unavailable for the requested EWT interval and language | "+Api_Ewt, mySession);
			}

		} catch (Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+Arrays.toString(e.getStackTrace()), mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
		} finally {
			try {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"EWT Received from DB is | "+Interval, mySession);
			} catch(Exception e) {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+Arrays.toString(e.getStackTrace()), mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
			}
		}

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Final EWT Prompt Received is | "+Ewt_Prompt, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);

		return Ewt_Prompt;
	}

	@SuppressWarnings({ "unchecked" })
	private HashMap<String,String> JsonParseEwt(File EwtFile, SCESession mySession) {
		
		String method = "JsonParseEwt";
		

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);

		FileReader fr = null;
		String Interval = "";
		String Active_flag ="";
		String Prompt = "";

		HashMap<String,String> EwtMap = null;
		HashMap<Object,Object> Ewt_Attributes = null;

		HashMap<String,String> CallHistory = new HashMap<String,String>();
		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();
		String Market = CallHistory.get("VC_MARKET").toString().trim();
		String LineType = CallHistory.get("VC_LINE_TYPE").toString().trim();
		String Language = CallHistory.get("VC_LANGUAGE").toString().trim();


		try {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Market | "+Market, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"LineType | "+LineType, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Language | "+Language, mySession);

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Loading Location File | "+EwtFile, mySession);

			JSONParser jsonParser = new JSONParser();
			fr = new FileReader(EwtFile);

			JSONObject jsonObj = (JSONObject) jsonParser.parse(fr);
			JSONObject marketJson = null;
			JSONObject lineType = null;
			JSONArray LangArray = null;

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"JSON OBJECT PARSED ", mySession);

			if(jsonObj.containsKey(Market)) {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Market is Present in Json | "+Market, mySession);
				marketJson = (JSONObject) jsonParser.parse(jsonObj.get(Market).toString());
			}  else {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"| "+ Market+" | is not available in Ewt Json, Taking General Market", mySession);
				marketJson = (JSONObject) jsonParser.parse(jsonObj.get("GENERAL").toString());
			}

			if(marketJson.containsKey(LineType)) {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"LineType is Present in Json | "+LineType, mySession);
				lineType = (JSONObject) jsonParser.parse(marketJson.get(LineType).toString().trim());
			} else {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"| "+ LineType+" | is not available in Ewt Json", mySession);
				lineType = (JSONObject) jsonParser.parse(marketJson.get("GENERAL").toString().trim());
			}

			if(lineType.containsKey(Language)) {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Language is Present in Json | "+Language, mySession);
				LangArray = (JSONArray) lineType.get(Language);	
			} else {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"| "+ Language+" | is not available in Ewt Json", mySession);
				LangArray = (JSONArray) lineType.get("GENERAL");	
			}
			
			EwtMap = new HashMap<String, String>();
			for(int i=0;i<LangArray.size();i++) {
				
				Ewt_Attributes = (JSONObject) LangArray.get(i);
				Active_flag = Ewt_Attributes.get("VC_ACTIVE_STATUS").toString().trim();

				if(Active_flag.equalsIgnoreCase("Y")) {

					Interval = Ewt_Attributes.get("VC_EWT_INTERVAL").toString().trim();
					Prompt = Ewt_Attributes.get("VC_EWT_PROMPT").toString().trim();
					EwtMap.put(Interval, Prompt);
					
				} else {
					
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"EWT is not active for the required language", mySession);
					
				}
			}


		} catch (Exception e) {
			
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+Arrays.toString(e.getStackTrace()), mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
			
		} finally {
			try {
				fr.close();
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"File Reader is Closed", mySession);
			} catch(Exception e) {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+Arrays.toString(e.getStackTrace()), mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
			}
		}

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);

		return EwtMap;

	}

}
