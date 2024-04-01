package com.XML_Parsing;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "BCPTransferMaster")

public class BCPTransferMaster {
	
	protected List<VDN> VDN;

	public List<VDN> getVDN() {
		return VDN;
	}

	public void setVDN(List<VDN> vDN) {
		VDN = vDN;
	}

}
