package com.DBOperation;

import java.io.*;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.General.AppConstants;
import com.avaya.sce.runtime.tracking.TraceInfo;
import com.avaya.sce.runtimecommon.ITraceInfo;
import com.avaya.sce.runtimecommon.SCESession;

public class FlatFile {

	@SuppressWarnings("unchecked")
	public void writeToFile( String insertQuery, String flatFileName, LinkedHashMap<String, Object> inputMap, SCESession mySession) {

		String DBValues = "";
		String Header = "";
		String className = "FlatFile";
		String method = "writeToFile";
		
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Creating Flat File Started", mySession);

		//String CLASS_NAME = "CallHistoryFlatFileLogging";
		try {

			//Get Application Property from Property file.
			HashMap<String,String> PropValue = new HashMap<String,String>();
			PropValue = (HashMap<String, String>) mySession.getVariableField("HashMap", "PropertyValue").getObjectValue();
			
			String flatfilepath = PropValue.get("FlatFilePath");

			String flatFileLocation = flatfilepath
					+(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
					+"_"+flatFileName+".sql";

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+"Creating Flat File in the Location\t|\t"+flatFileLocation, mySession);

			File fileObjMenuHistory = new File(new File(flatFileLocation).getCanonicalPath());

			for (Entry<String, Object> entry : inputMap.entrySet()) {
				Header = Header+entry.getKey().toString()+",";
				//changed
				if(entry.getValue()!=null) {
				DBValues = DBValues+"'"+entry.getValue().toString()+"',";
				}else {
					DBValues = DBValues+entry.getValue()+",";
				}


				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+entry.getKey()+"\t|\t"+entry.getValue(), mySession);
				
			}
			
			DBValues = DBValues.substring(0, DBValues.length()-1);
			
			if (fileObjMenuHistory.exists()) {

				FileWriter fwMH = new FileWriter(fileObjMenuHistory, true);
				BufferedWriter bwMH = new BufferedWriter(fwMH);

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "FileAlreadyExist", mySession);
				
				bwMH.newLine();
				DBValues = ",(" + DBValues + ")";

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "Final DBValues\t|\t"+DBValues, mySession);
				
				bwMH.write(DBValues);
				bwMH.flush();
				bwMH.close();
				
			} else {

				FileWriter fwMH = new FileWriter(fileObjMenuHistory, true);
				BufferedWriter bwMH = new BufferedWriter(fwMH);

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "New File Created", mySession);
				
				DBValues =  DBValues + ")";
				Header = insertQuery+ " (" +Header.substring(0, Header.length()-1)+ ") values";
				bwMH.write(Header+"\r\n\r\n(");

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "Final DBValues\t|\t"+DBValues, mySession);
				
				bwMH.write(DBValues);
				bwMH.flush();
				bwMH.close();
				
			}

		} catch (IOException e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
			
		} catch (Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className +"\t|\t"+ method +"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
			
		}
		
	}

}
