package com.General;

import java.io.*;
//import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;

import com.avaya.sce.runtime.tracking.TraceInfo;
import com.avaya.sce.runtimecommon.ITraceInfo;
import com.avaya.sce.runtimecommon.SCESession;

public class HoursOfOperation {

	@SuppressWarnings("unchecked")
	public void checkWorkingHour(SCESession mySession) {

		String CLASS_NAME = "checkWorkingHour";

		boolean result = false;

		HashMap<String,String> loadProp = new HashMap<String,String>();
		loadProp = (HashMap<String, String>) mySession.getVariableField("HashMap", "PropertyValue").getObjectValue();

		String PropertyLocation = "";
		if(loadProp.containsKey("ExternalProperty")) {

			PropertyLocation = loadProp.get("ExternalProperty");

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME+" : ExternalProperty : "+PropertyLocation, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME+" : ExternalProperty : "+new File(PropertyLocation).exists(), mySession);

					String ZoneID = mySession.getVariableField("ApplicationVariable", "ZoneID").getStringValue();
					String WorkingDays = mySession.getVariableField("ApplicationVariable", "WorkingDay").getStringValue();
					String WorkingTime = mySession.getVariableField("ApplicationVariable", "WorkingHour").getStringValue();

					Calendar NativeTime = new GregorianCalendar(TimeZone.getTimeZone(ZoneID));
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME+" : Native Time : "+NativeTime, mySession);

					int int_day = NativeTime.get(Calendar.DAY_OF_WEEK);

					String str_day= GetDay(int_day);

					if(WorkingDays.equalsIgnoreCase("CLOSED")){

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME+" : LINE IS CLOSED : ", mySession);
						result = false;
						

					} else if(WorkingDays.equalsIgnoreCase("ALL") && WorkingTime.equalsIgnoreCase("ALL")) {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME+" : LINE IS 24*7 OPEN : ", mySession);
						result = true;
					

					} else if(WorkingDays.contains(str_day)) {

						if(WorkingDays.contains(",")) {

							String[] WorkDay = WorkingDays.split(",");

							for(int i=0;i<WorkDay.length;i++) {

								if(WorkDay[i].equalsIgnoreCase(str_day)) {

									String WorkTime = WorkingTime.split(",")[i];

									result = HourValidation(WorkTime, NativeTime, mySession);

									if(result) {
										
										break;
									}

								}

							}

						} else {

							result = HourValidation(WorkingDays, NativeTime, mySession);

						}

					} else {

						result = false;
					
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME+" LINE IS CLOSED2 " , mySession);

					}

		} else {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, CLASS_NAME+" EXTERNAL PROPERTY FILE KEY IS NOT EXIST " , mySession);
			result = false;
			
			// Support Services changes
			mySession.setProperty("checkWorkingHourValidation",result);
		}
		

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME+" : Result from Working Hour : "+result, mySession);

	}

	public boolean HourValidation(String WorkTime, Calendar NativeTime, SCESession mySession) {

		String CLASS_NAME = "HourValidation";
		boolean result = false;

		try {

			String[] time = WorkTime.split("-");

			int hour = NativeTime.get(Calendar.HOUR_OF_DAY);
			int minute = NativeTime.get(Calendar.MINUTE);

			DecimalFormat formatter = new DecimalFormat("00");
			String curr_time=formatter.format(hour)+formatter.format(minute);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME+" : BUSINESS WorkTime : "+WorkTime, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME+" : CURRENT TIME : "+curr_time, mySession);
			SimpleDateFormat sdf=new SimpleDateFormat("HH:mm");
			SimpleDateFormat format = new SimpleDateFormat("HHmm");

			Date startDate = format.parse(time[0]);
			String newDateString = sdf.format(startDate);

			Date endDate = format.parse(time[1]);
			String newEndDateString = sdf.format(endDate) ;

			Date curentTime = format.parse(curr_time);
			String newCurrDateString = sdf.format(curentTime) ;

			DateTimeFormatter format_tome = DateTimeFormatter.ofPattern("HH:mm");

			LocalTime startTime = LocalTime.parse(newDateString, format_tome);
			LocalTime endTime = LocalTime.parse(newEndDateString, format_tome);
			LocalTime targetTime = LocalTime.parse(newCurrDateString, format_tome);

			if (startTime.isAfter(endTime)) {
				if (targetTime.isBefore(endTime) || targetTime.isAfter(startTime)) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME+" Working Hours", mySession);

					result = true;

				} else {
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME+" Non Working Hours", mySession);
					result = false;
				}
			} else {
				if (targetTime.isBefore(endTime) && targetTime.isAfter(startTime)) {
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME+" Working Hours", mySession);
					result = true;
				} else {
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, CLASS_NAME+" Non Working Hours", mySession);
					result = false;
				}
			}

		} catch(Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, CLASS_NAME+" : EXCEPTION in Working Hour Validation : "+e.getMessage(), mySession);
			result = false;
		}

		return result;

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

}
