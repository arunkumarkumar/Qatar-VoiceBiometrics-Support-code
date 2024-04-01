package com.XML_Parsing;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)

public class ApplicationProperties {
	
	protected List<Property> Property;

	public List<Property> getProperty() {
		return Property;
	}

	public void setProperty(List<Property> property) {
		Property = property;
	}
	
}
