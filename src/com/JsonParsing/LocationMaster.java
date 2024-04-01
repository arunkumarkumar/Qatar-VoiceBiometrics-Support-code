package com.JsonParsing;

import java.util.ArrayList;

public class LocationMaster {
	
	protected String Market;
	protected ArrayList<LineType> LineType;
	
	public String getMarket() {
		return Market;
	}
	public void setMarket(String market) {
		Market = market;
	}
	public ArrayList<LineType> getLineType() {
		return LineType;
	}
	public void setLineType(ArrayList<LineType> lineType) {
		LineType = lineType;
	}
	
}
