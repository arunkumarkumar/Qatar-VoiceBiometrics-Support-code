package com.XML_Parsing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)

public class Day {
	
	protected String Name;
	protected String Order;
	protected String IsEnabled;
	protected String OpeningHour;
	protected String ClosingHours;

	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public String getOrder() {
		return Order;
	}
	public void setOrder(String order) {
		Order = order;
	}
	public String getIsEnabled() {
		return IsEnabled;
	}
	public void setIsEnabled(String isEnabled) {
		IsEnabled = isEnabled;
	}
	public String getOpeningHour() {
		return OpeningHour;
	}
	public void setOpeningHour(String openingHour) {
		OpeningHour = openingHour;
	}
	public String getClosingHours() {
		return ClosingHours;
	}
	public void setClosingHours(String closingHours) {
		ClosingHours = closingHours;
	}
	
}
