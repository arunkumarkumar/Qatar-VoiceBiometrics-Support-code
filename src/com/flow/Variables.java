package com.flow;

import com.General.AppConstants;
import com.avaya.sce.runtime.tracking.TraceInfo;
import com.avaya.sce.runtimecommon.ITraceInfo;
import com.avaya.sce.runtimecommon.SCESession;

public class Variables {

	String className = "Variables";

	public void initializeDynamicVariables(SCESession mySession) {

		String method = "initializeDynamicVariables";
		

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Start", mySession);

		com.avaya.sce.runtimecommon.IVariable variable = null;

		String Complexvariable = "";
		String SimpleVariable = "";
		String[] ComplexvariableList = null;
		String[] SimpleVariableList = null;
		String[] field = null;

		Complexvariable = AppConstants.ComplexVariable;

		ComplexvariableList = Complexvariable.split(",");

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Complexvariable : "+Complexvariable, mySession);
		
		try{

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"ComplexvariableList.length : "+ComplexvariableList.length, mySession);
			
			for(int i=0;i<ComplexvariableList.length;i++){

				if(ComplexvariableList[i].equalsIgnoreCase("ApplicationVariable")){
					field = AppConstants.ApplicationVariable.split(",");
				} else if(ComplexvariableList[i].equalsIgnoreCase("IsTableModified")){
					field = AppConstants.IsTableModified.split(",");
				} else if(ComplexvariableList[i].equalsIgnoreCase("DynamicPrompt")){
					field = AppConstants.DynamicPrompt.split(",");
				} else if(ComplexvariableList[i].equalsIgnoreCase("QA_WebService")){
					field = AppConstants.QA_WebService.split(",");
				} else if(ComplexvariableList[i].equalsIgnoreCase("WS_IdentifyCustomerDetails")){
					field = AppConstants.WS_IdentifyCustomerDetails.split(",");
				} else if(ComplexvariableList[i].equalsIgnoreCase("WS_IdentifyStaff")){
					field = AppConstants.WS_IdentifyStaff.split(",");
				} else if(ComplexvariableList[i].equalsIgnoreCase("WS_VerifyStaff")){
					field = AppConstants.WS_VerifyStaff.split(",");
				} else if(ComplexvariableList[i].equalsIgnoreCase("WS_UpdateLanguagePreference")){
					field = AppConstants.WS_UpdateLanguagePreference.split(",");
				}

				variable = com.avaya.sce.runtime.ComplexVariable.createComplexVariable(ComplexvariableList[i], field, null, mySession, false, false);
				mySession.putVariable(variable);

			}

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Complex Dynamic Variables are Created", mySession);

		} catch (Exception e){

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

		}

		SimpleVariable = AppConstants.SimpleVariable;

		//if(SimpleVariable.contains(",")) {

		SimpleVariableList = SimpleVariable.split(",");

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"SimpleVariable : "+SimpleVariable, mySession);
		
		try{
			
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"SimpleVariableList.length : "+SimpleVariableList.length, mySession);
			
			for(int i=0;i<SimpleVariableList.length;i++){

				if(SimpleVariableList[i].contains("-")) {

					field = SimpleVariableList[i].split("-");

					int count = Integer.parseInt(field[1]);

					for(int j=0;j<=count;j++) {

						variable = com.avaya.sce.runtime.SimpleVariable.createSimpleVariable(field[0]+j, "NA", null, mySession, false, false );
						mySession.putVariable(variable);

					}

				} else {

					variable = com.avaya.sce.runtime.SimpleVariable.createSimpleVariable(SimpleVariableList[i], "NA", null, mySession, false, false );
					mySession.putVariable(variable);

				}

			}

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Simple Dynamic Variables are Created", mySession);

		} catch (Exception e){

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

		}

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"End", mySession);

	}

}