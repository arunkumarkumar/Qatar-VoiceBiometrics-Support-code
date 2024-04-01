package com.flow;

import java.io.*;
//import java.io.File;
import java.util.HashMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.General.AppConstants;
import com.XML_Parsing.BCPTransferMaster;
import com.XML_Parsing.Flow;
import com.avaya.sce.runtime.tracking.TraceInfo;
import com.avaya.sce.runtimecommon.ITraceInfo;
import com.avaya.sce.runtimecommon.SCESession;

public class Parse_XML {

	String className = "Parse_XML";
	JAXBContext context = null;
	Unmarshaller unm = null;

	HashMap<String,Object> bcpTransVdn=new HashMap<String,Object>();
	HashMap<String,Object> Language=new HashMap<String,Object>();
	HashMap<String,Object> Node=new HashMap<String,Object>();
	HashMap<String,Object> Application=new HashMap<String,Object>();

	public void loadFlow(String FlowPath, String Type, SCESession mySession){

		String method = "loadFlow";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);

		try{

			File XMLPath = new File(new File(FlowPath).getCanonicalPath());

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Load Flow from location\t|\t"+XMLPath, mySession);
			
			if(XMLPath.exists()) {
				
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Parsing XML Started", mySession);
				
				JAXBContext context = JAXBContext.newInstance(Flow.class);
				Unmarshaller unm = context.createUnmarshaller();
				Flow flow = (Flow) unm.unmarshal(XMLPath);
				Application.put(FlowPath, flow);
				getFlowValues(FlowPath, Type, mySession);

			} else{

				mySession.getVariableField("Error").setValue(true);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_WARN, className +"\t|\t"+ method +"\t|\t"+ "Application Flow is Missing", mySession);


			}

		} catch (JAXBException e) {

			mySession.getVariableField("Error").setValue(true);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

		} catch (Exception e) {

			mySession.getVariableField("Error").setValue(true);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

		}

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);

	}

	public void getFlowValues(String FlowPath, String Type, SCESession mySession){

		String method = "getFlowValues";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);

		Flow flow = (Flow) Application.get(FlowPath);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Line Type is\t|\t"+flow.getLineType().toString(), mySession);

		if(Type.equalsIgnoreCase("Language")) {
			Language.put("Language", flow.getLanguages().getLangauge());
			Node.put("Node", flow.getNode());
		} else if (Type.equalsIgnoreCase("Flow")) {
			Node.put("Node1", flow.getNode());
		}

		mySession.getVariableField("HashMap", "LineType").setValue(flow.getLineType().toString());
		mySession.getVariableField("HashMap", "InitialNode").setValue(flow.getInitialNode().toString());
		mySession.getVariableField("HashMap", "Language").setValue(Language);
		mySession.getVariableField("HashMap", "Node").setValue(Node);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);

	}

	@SuppressWarnings("unchecked")
	public String loadBCPTransferXML(String bcpFilePath, SCESession mySession) {

		String method = "loadBCPTransferXML";
		String vdnResponse = "flase";
		
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);
		
		try {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"BCP XML Path\t|\t"+bcpFilePath, mySession);
			
			HashMap<String,String> CallHistory = new HashMap<String,String>();
			CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();
			String LineType = CallHistory.get("VC_LINE_TYPE");
			String Language =CallHistory.get("VC_LANGUAGE");
			String GDPRStatus = CallHistory.get("VC_GDPR_STATUS");
			String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");
			String[] exitLocation = MenuDescription.split("\\|");
			
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"LineType | "+LineType, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Language | "+Language, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"MenuDescription | "+MenuDescription, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"exitLocation | "+exitLocation[0], mySession);
			
			//String UUI = CallHistory.get("VC_UCID").toString()+"|"+Language+"|"+CallHistory.get("VC_SURVEY_CONSENT").toString();

			//TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"UUI\t|\t"+UUI, mySession);
			
			File file = new File(new File(bcpFilePath).getCanonicalPath());
			JAXBContext context = JAXBContext.newInstance(BCPTransferMaster.class);
			Unmarshaller unm = context.createUnmarshaller();
			BCPTransferMaster btm = (BCPTransferMaster) unm.unmarshal(file);
			
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"XML Parsing Completed", mySession);
			
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Size of XML | "+btm.getVDN().size(), mySession);
			
			outerLoop :
				
			for(int i=0;i<btm.getVDN().size();i++) {
				
				if(LineType.equalsIgnoreCase(btm.getVDN().get(i).getLineType().toString())) {
					
					if(Language.equalsIgnoreCase(btm.getVDN().get(i).getLanguage().toString())) {

							for(int j = exitLocation.length-1;j>=0;j--) {

								if(btm.getVDN().get(i).getExitLocation().equalsIgnoreCase(exitLocation[j])){

									String TransferVDN = "";
									
									if(GDPRStatus.equalsIgnoreCase("YES")) {

										TransferVDN = btm.getVDN().get(i).getGDPRTransferVDN().toString();
										
									} else {

										TransferVDN = btm.getVDN().get(i).getTransferVDN().toString();
										
									}
									mySession.getVariableField("TransferVDN").setValue(TransferVDN);

									vdnResponse = "true";

									TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"BCP Transfer VDN for the ExitLocation\t|\t"+exitLocation[j]+"\t|\tGDPRStatus\t|\t"+GDPRStatus+"\t|\tLanguage\t|\t"+Language+"\t|\tLineType\t|\t"+LineType+" | is | "+mySession.getVariableField("TransferVDN").getStringValue(), mySession);

									CallHistory.put("VC_TRANSFER_VDN", TransferVDN);
									CallHistory.put("VC_EXIT_LOCATION", exitLocation[j]); 
									CallHistory.put("VC_CALL_END_TYPE", "TRANSFER");

									break outerLoop;

								}

							}

					}

				}
				
			}
			
			mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);
			
		} catch (JAXBException e) {

			mySession.getVariableField("Error").setValue(true);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

		} catch (Exception e) {

			mySession.getVariableField("Error").setValue(true);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

		}
		
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);
		
		return vdnResponse;
		
	}

}
