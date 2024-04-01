package com.XML_Parsing;

import java.util.HashMap;
import java.util.List;

import com.avaya.sce.runtime.tracking.TraceInfo;
import com.avaya.sce.runtimecommon.ITraceInfo;
import com.avaya.sce.runtimecommon.SCESession;

public class Fetch {
	
	String NextNode = "NA";
	String NextNodeDescription = "NA";
	String MenuID = "";
	String MenuDescription = "";
	String MethodName = "NA";
	
	HashMap<String,Object> Nodes = new HashMap<String,Object>();
	List<Node> NodeValues = null;
	
	
	@SuppressWarnings("unchecked")
	public List<Node> getNodeValue(SCESession mySession){
		
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "Fetch Inside GetNodeValue ", mySession);
		try{
			Nodes = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Node").getObjectValue();
			NodeValues = (List<Node>) Nodes.get("Node");
		} catch(Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, "Exception Inside GetNodeValue "+e.getMessage(), mySession);
		}
		return NodeValues;
	}
	
	@SuppressWarnings("unchecked")
	public String NextNode(String CLASS_NAME, String NodeDescription, SCESession mySession){

		String IsEnabled = "false";
		
		HashMap<String,String> CallHistory = new HashMap<String,String>();
		
		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();
		
		NodeValues = getNodeValue(mySession);
		MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");
		MenuID = CallHistory.get("VC_MENU_ID");
		//TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME+" : NodeValues : "+NodeValues, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME+" : B4 while IsEnabled : "+IsEnabled, mySession);
		
		while(IsEnabled.equalsIgnoreCase("false")){

			for(int i=0;i<NodeValues.size();i++){
				
				if(NodeDescription.equalsIgnoreCase("OnDisconnect")){
					
					NextNode=NodeDescription;
					IsEnabled = "true";
					CallHistory.put("VC_CALL_END_TYPE", "DISCONNECT");
					
				}else{
					
					if(NodeValues.get(i).getNodeDescription().equalsIgnoreCase(NodeDescription)){
						
						if(NodeValues.get(i).getIsEnabled().equalsIgnoreCase("true")){
							
							NextNode = NodeValues.get(i).getType();
							MenuID = MenuID+NodeValues.get(i).getId()+":";
							MenuDescription = MenuDescription+NodeDescription+":";
							IsEnabled = "true";
							break;
							
						} else{
							
							NodeDescription = NodeValues.get(i).getNextNodeIfDisabled();
							IsEnabled = "false";
							break;
							
						}
						
					}
					
				}

			}
			
		}
		
		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		CallHistory.put("VC_MENU_DESCRIPTION",MenuDescription);
		CallHistory.put("VC_MENU_ID",MenuID);
		
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME+" : MENUID : "+MenuID, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME+" : MenuDescription : "+MenuDescription, mySession);
		return NextNode;

	}
	
	public String NodeDescription(String CLASS_NAME, String NodeDescription, String MenuValue, SCESession mySession){
		
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "Fetch Inside NodeDescription ", mySession);
		
		HashMap<String,String> CallHistory = new HashMap<String,String>();
		
		NodeValues = getNodeValue(mySession);
		MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");
		
		if(MenuValue.equalsIgnoreCase("NA")){
			
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "Fetch Inside MenuValue "+MenuValue+" and NodeValues.size : "+NodeValues.size(), mySession);
			for(int i=0;i<NodeValues.size();i++){
				
				if(NodeValues.get(i).getNodeDescription().equalsIgnoreCase(NodeDescription)){
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, " NodeDescription "+NodeDescription, mySession);
					NextNodeDescription = NodeValues.get(i).getNextNode();
					break;
				}
				
			}
			
		} else if(MenuValue.equalsIgnoreCase("NI") || MenuValue.equalsIgnoreCase("NM") || MenuValue.equalsIgnoreCase("MT")){
			
			for(int i=0;i<NodeValues.size();i++){
				
				if(NodeValues.get(i).getNodeDescription().equalsIgnoreCase(NodeDescription)){
					
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "Fetch Inside MenuValue "+MenuValue+" and NodeValues.size : "+NodeValues.size(), mySession);
					if(MenuValue.equalsIgnoreCase("NI")){ 
						NextNodeDescription = NodeValues.get(i).getBranches().getNoInput().getNextNode();
						//MenuDescription = MenuDescription+MenuValue+":";
					} else if(MenuValue.equalsIgnoreCase("NM")){ 
						NextNodeDescription = NodeValues.get(i).getBranches().getNoMatch().getNextNode();
						//MenuDescription = MenuDescription+MenuValue+":";
					} else if(MenuValue.equalsIgnoreCase("MT")){
						NextNodeDescription = NodeValues.get(i).getBranches().getMaxtries().getNextNode();
						//MenuDescription = MenuDescription+MenuValue+":";
					}
					break;
					
				}
			}
		}else{
			for(int i=0;i<NodeValues.size();i++){
				
				if(NodeValues.get(i).getNodeDescription().equalsIgnoreCase(NodeDescription)){
					
					List<Values> values = NodeValues.get(i).getCondition().getValues();
					//List<Dtmf> Dtmf = null;
					
					if(!MenuValue.equalsIgnoreCase("true")&&!MenuValue.equalsIgnoreCase("false")){
						//Dtmf = null;
					}
					
					for(int k=0;k<values.size();k++){
						
						if(values.get(k).getReturn().equalsIgnoreCase(MenuValue)){
							
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "Fetch Inside MenuValue "+MenuValue+" and NodeValues.size : "+NodeValues.size(), mySession);
							NextNodeDescription = values.get(k).getNextNode();
							if(!MenuValue.equalsIgnoreCase("true")&&!MenuValue.equalsIgnoreCase("false")){
								//MenuDescription = MenuDescription+Dtmf.get(k).getName()+":";
							}else{
								MenuDescription = MenuDescription+MenuValue+":";
							}
							break;
							
						}
						
					}
					
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME+" : NextNodeDiscription ifEnable : "+NextNodeDescription, mySession);
					break;
				}
			}
		}
		
		if(NextNodeDescription.equalsIgnoreCase("NA"))
			NextNodeDescription = mySession.getVariableField("ApplicationVariable", "BackupNodeDescription").getStringValue();
		
		CallHistory.put("VC_MENU_DESCRIPTION",MenuDescription);
		
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME+" : MenuDescription : "+MenuDescription, mySession);
		
		return NextNodeDescription;
	}
	
	public String MethodName(String CLASS_NAME, String NodeDescription, SCESession mySession){
		
		NodeValues = getNodeValue(mySession);
		
		if(NodeValues.equals(null)){
			MethodName = "NA";
		} else {
			for(int i=0;i<NodeValues.size();i++){

				if(NodeDescription.equalsIgnoreCase(NodeValues.get(i).getNodeDescription())){
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME+" : NodeDescription : "+NodeDescription, mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME+" : NodeValues.get(i).getNodeDescription() : "+NodeValues.get(i).getNodeDescription(), mySession);
					MethodName = NodeValues.get(i).getMethodName();
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME+" : NodeValues.get(i).getMethodName() : "+NodeValues.get(i).getMethodName(), mySession);
				}
			}
		}
		return MethodName;
	}
	
}
