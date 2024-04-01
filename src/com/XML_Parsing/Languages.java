package com.XML_Parsing;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)

public class Languages {
	
	protected List<langauge> langauge;
	
	public List<langauge> getLangauge() {
		return langauge;
	}

	public void setLangauge(List<langauge> langauge) {
		this.langauge = langauge;
	}
	
}
