package com.XML_Parsing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)

public class Dtmf {
	
	protected String Name;
	protected String Grammar;
	
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public String getGrammar() {
		return Grammar;
	}
	public void setGrammar(String grammar) {
		Grammar = grammar;
	}
	
}
