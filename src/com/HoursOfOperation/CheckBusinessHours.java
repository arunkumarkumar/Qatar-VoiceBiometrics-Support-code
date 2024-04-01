package com.HoursOfOperation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import com.General.AppConstants;
import com.avaya.sce.runtime.tracking.TraceInfo;
import com.avaya.sce.runtimecommon.ITraceInfo;
import com.avaya.sce.runtimecommon.SCESession;

public class CheckBusinessHours {

	public String checkWorkingHour (String ZoneID, String WorkingDay, String WorkingHour, SCESession mySession) {

		String CLASS_NAME = "checkWorkingHour";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME+" | Entered Validate Working Hour", mySession);

		String businessHour = "false";

		try {

			Calendar NativeTime = new GregorianCalendar(TimeZone.getTimeZone(ZoneID));
			int int_day = NativeTime.get(Calendar.DAY_OF_WEEK);
			String CurrentDay= GetDay(int_day);
			
			// Support Services changes
			mySession.getVariableField("ApplicationVariable", "currentDay").setValue(CurrentDay);

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME+" | WorkingDay |"+WorkingDay, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME+" | CurrentDay |"+CurrentDay, mySession);

			if(WorkingDay.equalsIgnoreCase("ALL")) {

				if(WorkingHour.equalsIgnoreCase("ALL")) {
					
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME +" : Working Hour", mySession);
					businessHour = "true";
					
					//Support Services Changes
					mySession.getVariableField("ApplicationVariable", "checkWorkingHoursValidation").setValue(businessHour);
				} else if(WorkingHour.contains("-")) {

					String[] time = WorkingHour.split("-");
					String startTime = time[0];
					String endTime = time[1];
					businessHour = validateWorkingHour(startTime, endTime, ZoneID, mySession);
					
				}

			} else if(WorkingDay.equalsIgnoreCase("CLOSED")) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME +" : Working Hour", mySession);
				businessHour = "false";
				
				//Support Services Changes
				mySession.getVariableField("ApplicationVariable", "checkWorkingHoursValidation").setValue(businessHour);
				
			} else if(WorkingDay.contains(",")) {

				String[] days = WorkingDay.split(",");
				String[] time = WorkingHour.split(",");
				
				if(WorkingDay.contains(CurrentDay)) {

					if(days.length==time.length) {

						for(int i=0;i<days.length;i++) {

							if(days[i].equalsIgnoreCase(CurrentDay)) {

								String starttime = time[i].split("-")[0];
								String endtime = time[i].split("-")[1];
								
								TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME +" Setting OOH Prompt", mySession);
								
								setOOHTime(starttime, endtime, mySession);
								
								businessHour = validateWorkingHour(starttime, endtime, ZoneID, mySession);
								
								if(businessHour.equalsIgnoreCase("true")) {
									break;
								}

							}

						}

					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, AppConstants.ERRORINCLASS+CLASS_NAME +" | Configuration Error", mySession);
						businessHour = "false";

					}
					
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME +" | Non Working Day", mySession);
					businessHour = "false";

				}
				
			} else if(WorkingDay.equalsIgnoreCase(CurrentDay)) {

				String starttime = WorkingHour.split("-")[0];
				String endtime = WorkingHour.split("-")[1];

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME +" Setting OOH Prompt", mySession);
				
				setOOHTime(starttime, endtime, mySession);
				
				businessHour = validateWorkingHour(starttime, endtime, ZoneID, mySession);
				
			} else {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME +" | Non Working Day", mySession);
				businessHour = "false";

			}

		} catch (Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, AppConstants.ERRORINCLASS+CLASS_NAME +" | Excpetion While Fetching Working Hour | "+e.getMessage(), mySession);
			businessHour = "false";

		}

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME +" | Final Working Hour Result | "+businessHour, mySession);
		return businessHour;

	}


	public String validateWorkingHour(String starttime, String endtime, String ZoneID, SCESession mySession) {

		String CLASS_NAME = "validateWorkingHour";

		String businessHour = "";
		String businessHour1 = "false";
		String businessHour2 = "false";

		SimpleDateFormat gmtDateFormat = new SimpleDateFormat("HH:mm");
		gmtDateFormat.setTimeZone(TimeZone.getTimeZone(ZoneID));

		try {
			
			Date parseStarttime = new SimpleDateFormat("HH:mm").parse(starttime);
			Date parseEndtime = new SimpleDateFormat("HH:mm").parse(endtime);
			Date currentTime = new SimpleDateFormat("HH:mm").parse(gmtDateFormat.format(new Date()));

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME+" | starttime "+starttime, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME+" | endtime "+endtime, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME+" | currentTime "+currentTime, mySession);

			if(starttime.equalsIgnoreCase(endtime)){

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME +" | Holiday ", mySession);
				businessHour = "false";

			} else {

				if(Integer.parseInt(starttime.substring(0, 2))<Integer.parseInt(endtime.substring(0, 2))) {
					if(currentTime.after(parseStarttime)&&currentTime.before(parseEndtime)) {
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME +" : Working Hour", mySession);
						businessHour = "true";
					}
					else {
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME +" : NonWorking Hour", mySession);
						businessHour = "false";
					}
				}
				else {

					if(currentTime.after(parseStarttime)&&currentTime.after(parseEndtime)) {
						businessHour1 = "true";
					}
					if(currentTime.before(parseStarttime)&&currentTime.before(parseEndtime)) {
						businessHour2 = "true";
					}


					if(businessHour1.equalsIgnoreCase("true")&&businessHour2.equalsIgnoreCase("true")){
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME +" : Holiday ", mySession);
						businessHour = "false";
					} else if (businessHour1.equalsIgnoreCase("true")){
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME +" : Night Shift Working Hour", mySession);
						businessHour = "true";
					} else {
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME +" : Night Shift Working Hour", mySession);
						businessHour = businessHour2;
					}
				}

			}

			
		}	catch(Exception e) {
			
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, AppConstants.ERRORINCLASS+CLASS_NAME +" | Excpetion While Fetching Working Hour | "+e.getMessage(), mySession);
			businessHour = "false";
			
		}
		
		return businessHour;
		
	}


	public String GetDay(int day)  {
		
		String day_week="";
		switch(day)  {
		case 1:
			day_week="SUN";
			break;
		case 2:
			day_week="MON";
			break;
		case 3:
			day_week="TUE";
			break;
		case 4:
			day_week="WED";
			break;
		case 5:
			day_week="THU";
			break;
		case 6:
			day_week="FRI";
			break;
		case 7:
			day_week="SAT";
			break;

		}
		return day_week;
		
	}
	
	public void setOOHTime(String StartTime, String EndTime, SCESession mySession) {

		List<String> prompt = new ArrayList<String>();

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "CheckBusinessHours" +" Setting OOH Prompt from "+StartTime+" to "+EndTime, mySession);
		
		int start1 = Integer.parseInt(StartTime.split(":")[0]);
		int start2 = Integer.parseInt(StartTime.split(":")[1]);
		int end1 = Integer.parseInt(EndTime.split(":")[0]);
		int end2 = Integer.parseInt(EndTime.split(":")[1]);
		String day1 = "";
		String day2 = "";
		
		if(start1<12) {
			day1="AM";
		} else {
			start1 = start1-12;
			day1="PM";
		}
		
		if(end1<12) {
			day2="AM";
		} else {
			end1 = end1-12;
			day2="PM";
		}

		if(start1==0) {
			start1 = 12;
		}
		if(end1==0) {
			end1 = 12;
		}
		
		prompt.add(Integer.toString(start1));
		if(start2!=0) {
			prompt.add(Integer.toString(start2));
		}
		prompt.add(day1);
		prompt.add("TO");
		prompt.add(Integer.toString(end1));
		if(end2!=0) {
			prompt.add(Integer.toString(end2));
		}
		prompt.add(day2);

		
		for(int i=0;i<prompt.size();i++) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "CheckBusinessHours" +" prompt list are : "+prompt.get(i), mySession);
			
		}

		mySession.getVariableField("OOHTiming").setValue(prompt);
		
	}
	
}
