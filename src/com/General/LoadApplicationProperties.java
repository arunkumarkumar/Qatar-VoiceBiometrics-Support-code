package com.General;

import java.io.*;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.avaya.sce.runtime.tracking.TraceInfo;
import com.avaya.sce.runtimecommon.ITraceInfo;
import com.avaya.sce.runtimecommon.SCESession;

public class LoadApplicationProperties {

	FileReader input=null;
	Properties prop = new Properties();
	public static HashMap<String,String> loadProp = new HashMap<String,String>();
	public static HashMap<String,Long> ModifiedTime = new HashMap<String,Long>();
	
	public void IsPropModified(String file, SCESession mySession){

		long LastModifiedTime=0;
		long PreviousModifiedTime=0;
		
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "******************************************", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "**********Inside_Property_Fetch***********", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "******************************************", mySession);
		
		LastModifiedTime = new File(file).lastModified();
		
		if(ModifiedTime.containsKey(file)) {
			
			PreviousModifiedTime = ModifiedTime.get(file);

			if(PreviousModifiedTime == 0 || PreviousModifiedTime != LastModifiedTime){

				ModifiedTime.put(file, LastModifiedTime);
				LoadProperty(file, mySession);
				mySession.getVariableField("IsPropertyFileModified").setValue(true);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "File_Modified", mySession);

			} else{

				setProperty(mySession);
				mySession.getVariableField("IsPropertyFileModified").setValue(false);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "File_Not_Modified", mySession);

			}
			
		} else {
			
			ModifiedTime.put(file, LastModifiedTime);
			LoadProperty(file, mySession);
			mySession.getVariableField("IsPropertyFileModified").setValue(true);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "File_Modified", mySession);
			
		}
		
		
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "******************************************", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "*********Property_Load_Completed**********", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "******************************************", mySession);
		
	}
	
	public void LoadProperty(String file, SCESession mySession){
		
		String ClassName = "LoadProperty";
		
		try {
			
			input = new FileReader(file);
			prop.load(input);
			
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "InputFile : "+file, mySession);
			
			for (Map.Entry<Object, Object> entry : prop.entrySet()) {
				loadProp.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
			}
			
			mySession.getVariableField("HashMap", "PropertyValue").setValue(loadProp);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, ClassName+" | Property Fetched", mySession);
			
		} catch (FileNotFoundException e) {
			
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, AppConstants.ERRORINCLASS+ClassName+" | Exception while Reading file : "+e.getMessage(), mySession);
			
		} catch (IOException e) {
			
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, AppConstants.ERRORINCLASS+ClassName+" | Exception while Loading file : "+e.getMessage(), mySession);
			
		} catch (Exception e) {
			
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, AppConstants.ERRORINCLASS+ClassName+" | Exception while Load Property : "+e.getMessage(), mySession);
			
		}
		
	}
	
	public void setProperty(SCESession mySession){
		
		mySession.getVariableField("HashMap", "PropertyValue").setValue(loadProp);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "Load_Property_Values", mySession);
		
	}
	
	public static String getProperty(String Key, SCESession mySession) {
		
		String value = "NA";

		if(loadProp.containsKey(Key)) {
			value = loadProp.get(Key).toString().trim();
		}
		
		return value;
		
	}

}
