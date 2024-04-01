package com.XML_Parsing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)

public class Node {
	
	protected String Id;
	protected String Type;
	protected String NodeDescription;
	protected String ServiceType;
	protected String CustomerTier;
	protected String IsEnabled;
	protected String NextNode;
	protected String IsBargingAllowed;
	protected String NextNodeIfDisabled;
	protected String NoOfRetries;
	protected String TimeOut;
	protected String InterDigitTimeOut;
	protected String IsPrivate;
	protected String Termchar;
	protected String MethodName;
	protected PhraseList PhraseList;
	protected Branches Branches;
	protected Condition Condition;
	
	public String getId() {
		return Id;
	}
	public void setId(String id) {
		Id = id;
	}
	public String getType() {
		return Type;
	}
	public void setType(String type) {
		Type = type;
	}
	public String getNodeDescription() {
		return NodeDescription;
	}
	public void setNodeDescription(String nodeDescription) {
		NodeDescription = nodeDescription;
	}
	public String getServiceType() {
		return ServiceType;
	}
	public void setServiceType(String serviceType) {
		ServiceType = serviceType;
	}
	public String getCustomerTier() {
		return CustomerTier;
	}
	public void setCustomerTier(String customerTier) {
		CustomerTier = customerTier;
	}
	public String getIsEnabled() {
		return IsEnabled;
	}
	public void setIsEnabled(String isEnabled) {
		IsEnabled = isEnabled;
	}
	public String getNextNode() {
		return NextNode;
	}
	public void setNextNode(String nextNode) {
		NextNode = nextNode;
	}
	public String getIsBargingAllowed() {
		return IsBargingAllowed;
	}
	public void setIsBargingAllowed(String isBargingAllowed) {
		IsBargingAllowed = isBargingAllowed;
	}
	public String getNextNodeIfDisabled() {
		return NextNodeIfDisabled;
	}
	public void setNextNodeIfDisabled(String nextNodeIfDisabled) {
		NextNodeIfDisabled = nextNodeIfDisabled;
	}
	public String getNoOfRetries() {
		return NoOfRetries;
	}
	public void setNoOfRetries(String noOfRetries) {
		NoOfRetries = noOfRetries;
	}
	public String getTimeOut() {
		return TimeOut;
	}
	public void setTimeOut(String timeOut) {
		TimeOut = timeOut;
	}
	public String getInterDigitTimeOut() {
		return InterDigitTimeOut;
	}
	public void setInterDigitTimeOut(String interDigitTimeOut) {
		InterDigitTimeOut = interDigitTimeOut;
	}
	public String getIsPrivate() {
		return IsPrivate;
	}
	public void setIsPrivate(String isPrivate) {
		IsPrivate = isPrivate;
	}
	public String getTermchar() {
		return Termchar;
	}
	public void setTermchar(String termchar) {
		Termchar = termchar;
	}
	public String getMethodName() {
		return MethodName;
	}
	public void setMethodName(String methodName) {
		MethodName = methodName;
	}
	public PhraseList getPhraseList() {
		return PhraseList;
	}
	public void setPhraseList(PhraseList phraseList) {
		PhraseList = phraseList;
	}
	public Branches getBranches() {
		return Branches;
	}
	public void setBranches(Branches branches) {
		Branches = branches;
	}
	public Condition getCondition() {
		return Condition;
	}
	public void setCondition(Condition condition) {
		Condition = condition;
	}
	
}
