package com.XML_Parsing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)

public class VDN {

	protected String LineType;
	protected String Language;
	protected String ExitLocation;
	protected String GDPRTransferVDN;
	protected String TransferVDN;
	public String getLineType() {
		return LineType;
	}
	public void setLineType(String lineType) {
		LineType = lineType;
	}
	public String getLanguage() {
		return Language;
	}
	public void setLanguage(String language) {
		Language = language;
	}
	public String getExitLocation() {
		return ExitLocation;
	}
	public void setExitLocation(String exitLocation) {
		ExitLocation = exitLocation;
	}
	public String getGDPRTransferVDN() {
		return GDPRTransferVDN;
	}
	public void setGDPRTransferVDN(String gDPRTransferVDN) {
		GDPRTransferVDN = gDPRTransferVDN;
	}
	public String getTransferVDN() {
		return TransferVDN;
	}
	public void setTransferVDN(String transferVDN) {
		TransferVDN = transferVDN;
	}
	
}
