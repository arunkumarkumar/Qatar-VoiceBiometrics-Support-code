package com.flow;

import java.util.HashMap;
import java.util.List;

import com.XML_Parsing.Node;

public class PrepareFlow {
	
	public HashMap<String,Object> dynAnnouncement(List<Node> NodeValues, int i) {
		
		HashMap<String,Object> values = new HashMap<String,Object>();
		
		values.put("Id", NodeValues.get(i).getId());
		values.put("NodeDescription", NodeValues.get(i).getNodeDescription());
		values.put("ServiceType", NodeValues.get(i).getServiceType());
		values.put("CustomerTier", NodeValues.get(i).getCustomerTier());
		values.put("Type", NodeValues.get(i).getType());
		values.put("IsEnabled", NodeValues.get(i).getIsEnabled());
		values.put("IsBargingAllowed", NodeValues.get(i).getIsBargingAllowed());
		values.put("NextNodeIfDisabled", NodeValues.get(i).getNextNodeIfDisabled());
		values.put("MethodName", NodeValues.get(i).getMethodName());
		values.put("PhraseList", NodeValues.get(i).getPhraseList().getPrompt());
		values.put("NextNode", NodeValues.get(i).getNextNode());
		
		return values;
		
	}
	
	public HashMap<String,Object> dynMenu(List<Node> NodeValues, int i) {

		HashMap<String,Object> values = new HashMap<String,Object>();
		
		values.put("Id", NodeValues.get(i).getId());
		values.put("NodeDescription", NodeValues.get(i).getNodeDescription());
		values.put("ServiceType", NodeValues.get(i).getServiceType());
		values.put("CustomerTier", NodeValues.get(i).getCustomerTier());
		values.put("Type", NodeValues.get(i).getType());
		values.put("IsEnabled", NodeValues.get(i).getIsEnabled());
		values.put("IsBargingAllowed", NodeValues.get(i).getIsBargingAllowed());
		values.put("NextNodeIfDisabled", NodeValues.get(i).getNextNodeIfDisabled());
		values.put("NoOfRetries", NodeValues.get(i).getNoOfRetries());
		values.put("TimeOut", NodeValues.get(i).getTimeOut());
		values.put("MethodName", NodeValues.get(i).getMethodName());
		values.put("PhraseList", NodeValues.get(i).getPhraseList().getPrompt());
		values.put("BranchNames", NodeValues.get(i).getBranches().getBranchNames());
		values.put("Grammar", NodeValues.get(i).getBranches().getGrammar());
		values.put("NextNode", NodeValues.get(i).getBranches().getNextNode());
		values.put("NoInputNextNode", NodeValues.get(i).getBranches().getNoInput().getNextNode());
		values.put("NoMatchNextNode", NodeValues.get(i).getBranches().getNoMatch().getNextNode());
		values.put("MaxtriesNextNode", NodeValues.get(i).getBranches().getMaxtries().getNextNode());
		
		return values;
		
	}

	public HashMap<String,Object> dynPC(List<Node> NodeValues, int i) {

		HashMap<String,Object> values = new HashMap<String,Object>();
		
		values.put("Id", NodeValues.get(i).getId());
		values.put("NodeDescription", NodeValues.get(i).getNodeDescription());
		values.put("ServiceType", NodeValues.get(i).getServiceType());
		values.put("CustomerTier", NodeValues.get(i).getCustomerTier());
		values.put("Type", NodeValues.get(i).getType());
		values.put("IsEnabled", NodeValues.get(i).getIsEnabled());
		values.put("IsBargingAllowed", NodeValues.get(i).getIsBargingAllowed());
		values.put("NextNodeIfDisabled", NodeValues.get(i).getNextNodeIfDisabled());
		values.put("NoOfRetries", NodeValues.get(i).getNoOfRetries());
		values.put("TimeOut", NodeValues.get(i).getTimeOut());
		values.put("InterDigitTimeOut", NodeValues.get(i).getInterDigitTimeOut());
		values.put("IsPrivate", NodeValues.get(i).getIsPrivate());
		values.put("Termchar", NodeValues.get(i).getTermchar());
		values.put("MethodName", NodeValues.get(i).getMethodName());
		values.put("PhraseList", NodeValues.get(i).getPhraseList().getPrompt());
		values.put("BranchNames", NodeValues.get(i).getBranches().getBranchNames());
		values.put("Grammar", NodeValues.get(i).getBranches().getGrammar());
		values.put("NextNode", NodeValues.get(i).getBranches().getNextNode());
		values.put("NoInputNextNode", NodeValues.get(i).getBranches().getNoInput().getNextNode());
		values.put("NoMatchNextNode", NodeValues.get(i).getBranches().getNoMatch().getNextNode());
		values.put("MaxtriesNextNode", NodeValues.get(i).getBranches().getMaxtries().getNextNode());
		
		return values;
		
	}
	
	public HashMap<String,Object> dynData(List<Node> NodeValues, int i) {

		HashMap<String,Object> values = new HashMap<String,Object>();
		
		values.put("Id", NodeValues.get(i).getId());
		values.put("NodeDescription", NodeValues.get(i).getNodeDescription());
		values.put("ServiceType", NodeValues.get(i).getServiceType());
		values.put("CustomerTier", NodeValues.get(i).getCustomerTier());
		values.put("Type", NodeValues.get(i).getType());
		values.put("IsEnabled", NodeValues.get(i).getIsEnabled());
		values.put("NextNodeIfDisabled", NodeValues.get(i).getNextNodeIfDisabled());
		values.put("NoOfRetries", NodeValues.get(i).getNoOfRetries());
		values.put("MethodName", NodeValues.get(i).getMethodName());
		for(int j=0;j<NodeValues.get(i).getCondition().getValues().size();j++) {
			values.put(NodeValues.get(i).getCondition().getValues().get(j).getReturn(), NodeValues.get(i).getCondition().getValues().get(j).getNextNode());
		}
		
		return values;
		
	}
	
	public HashMap<String,Object> dynTransfer(List<Node> NodeValues, int i) {

		HashMap<String,Object> values = new HashMap<String,Object>();
		
		values.put("Id", NodeValues.get(i).getId());
		values.put("NodeDescription", NodeValues.get(i).getNodeDescription());
		values.put("ServiceType", NodeValues.get(i).getServiceType());
		values.put("CustomerTier", NodeValues.get(i).getCustomerTier());
		values.put("Type", NodeValues.get(i).getType());
		values.put("IsEnabled", NodeValues.get(i).getIsEnabled());
		values.put("NextNodeIfDisabled", NodeValues.get(i).getNextNodeIfDisabled());
		values.put("MethodName", NodeValues.get(i).getMethodName());
		for(int j=0;j<NodeValues.get(i).getCondition().getValues().size();j++) {
			values.put(NodeValues.get(i).getCondition().getValues().get(j).getReturn(), NodeValues.get(i).getCondition().getValues().get(j).getNextNode());
		}
		
		return values;
		
	}
	
}
