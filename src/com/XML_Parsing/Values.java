package com.XML_Parsing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)

public class Values {
	
	protected String Return;
	protected String NextNode;
	
	public String getReturn() {
		return Return;
	}
	public void setReturn(String return1) {
		Return = return1;
	}
	public String getNextNode() {
		return NextNode;
	}
	public void setNextNode(String nextNode) {
		NextNode = nextNode;
	}
	
}
