package com.XML_Parsing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)

public class BusinessHours {
	
	protected String LangWorkingDay;
	protected String LangWorkingHour;
	protected String TransWorkingDay;
	protected String TransWorkingHour;

	public String getLangWorkingDay() {
		return LangWorkingDay;
	}
	public void setLangWorkingDay(String langWorkingDay) {
		LangWorkingDay = langWorkingDay;
	}
	public String getLangWorkingHour() {
		return LangWorkingHour;
	}
	public void setLangWorkingHour(String langWorkingHour) {
		LangWorkingHour = langWorkingHour;
	}
	public String getTransWorkingDay() {
		return TransWorkingDay;
	}
	public void setTransWorkingDay(String transWorkingDay) {
		TransWorkingDay = transWorkingDay;
	}
	public String getTransWorkingHour() {
		return TransWorkingHour;
	}
	public void setTransWorkingHour(String transWorkingHour) {
		TransWorkingHour = transWorkingHour;
	}
	
}
