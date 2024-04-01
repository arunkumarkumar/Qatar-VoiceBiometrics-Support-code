package com.XML_Parsing;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Flow")

public class Flow {
	
	protected String Name;
	protected String LineType;
	protected String InitialNode;
	protected Languages Languages;
	protected List<Node> Node;
	
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public String getLineType() {
		return LineType;
	}
	public void setLineType(String lineType) {
		LineType = lineType;
	}
	public String getInitialNode() {
		return InitialNode;
	}
	public void setInitialNode(String initialNode) {
		InitialNode = initialNode;
	}
	public Languages getLanguages() {
		return Languages;
	}
	public void setLanguages(Languages languages) {
		Languages = languages;
	}
	public List<Node> getNode() {
		return Node;
	}
	public void setNode(List<Node> node) {
		Node = node;
	}
	
}
