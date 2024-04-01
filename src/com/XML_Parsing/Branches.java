package com.XML_Parsing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)

public class Branches {
	
	protected String BranchNames;
	protected String Grammar;
	protected String NextNode;
	protected NoInput NoInput;
	protected NoMatch NoMatch;
	protected Maxtries Maxtries;
	
	public String getBranchNames() {
		return BranchNames;
	}
	public void setBranchNames(String branchNames) {
		BranchNames = branchNames;
	}
	public String getGrammar() {
		return Grammar;
	}
	public void setGrammar(String grammar) {
		Grammar = grammar;
	}
	public String getNextNode() {
		return NextNode;
	}
	public void setNextNode(String nextNode) {
		NextNode = nextNode;
	}
	public NoInput getNoInput() {
		return NoInput;
	}
	public void setNoInput(NoInput noInput) {
		NoInput = noInput;
	}
	public NoMatch getNoMatch() {
		return NoMatch;
	}
	public void setNoMatch(NoMatch noMatch) {
		NoMatch = noMatch;
	}
	public Maxtries getMaxtries() {
		return Maxtries;
	}
	public void setMaxtries(Maxtries maxtries) {
		Maxtries = maxtries;
	}
	
}
