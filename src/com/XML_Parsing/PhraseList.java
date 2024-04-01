package com.XML_Parsing;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)

public class PhraseList {
	
	protected List<String> Prompt;

	public List<String> getPrompt() {
		return Prompt;
	}

	public void setPrompt(List<String> prompt) {
		Prompt = prompt;
	}
	
}
