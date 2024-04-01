package com.XML_Parsing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)

public class langauge {
	
	protected String Name;
	protected BusinessHours BusinessHours;
	
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public BusinessHours getBusinessHours() {
		return BusinessHours;
	}
	public void setBusinessHours(BusinessHours businessHours) {
		BusinessHours = businessHours;
	}
	
}
