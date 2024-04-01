package com.XML_Parsing;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)

public class Condition {
	
	protected List<Values> Values;

	public List<Values> getValues() {
		return Values;
	}

	public void setValues(List<Values> values) {
		Values = values;
	}
	
}
