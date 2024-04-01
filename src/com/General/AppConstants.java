package com.General;

public class AppConstants {

	public static final String T = "true";
	public static final String F = "false";
	public static final String VARCHAR = "VC";
	public static final String CHAR = "C";
	public static final String NUMBERIC = "NU";
	public static final String DATETIME = "DT";
	public static final String SQLDATETIMEFORMAT = "yyyy-mm-dd H:MM:s";
	public static final String CALLABLESTATEMNT = "{call dbo.*}";
	public static final String STATEMENTBEGIN = "(";
	public static final String STATEMENTBODY = "?,";
	public static final String STATEMENTEND = ")";
	public static final String SEPARATOR_COMMA = ",";
	public static final String SEPARATOR_UNDERSCORE = "_";
	

	public static final String VB_Status = "VB_Status";
	public static final String VB_Status_date = "VB_Status_Date";
	public static final String VB_Enrollment_Failed = "VB_Enrollment_Failed";
	public static final String Verified_via_vb = "Verified_via_vb";
	public static final String De_enrolled_caller_from_Vb = "enrolled_caller_from_Vb";
	public static final String VB_enrollment_status = "VB_enrollment_status";
	public static final String VB_enrollment_status_date = "VB_enrollment_status_date";
	public static final String VB_verification_status = "VB_verification_status";
	public static final String VB_verification_status_date = "VB_verification_status_date";
	public static final String configSetName = "configSetName";
	public static final String voiceprintTag = "voiceprintTag";
	public static final String CountryCode = "CountryCode";
	public static final String Email = "Email";
	public static final String CONNECTBACK = "CONNECTBACK";
	public static final String CustomerID = "CustomerID";
	
		
	//AppError
	public static final String ERRORINCLASS = " ERROR IN CLASS | ";
	
	//General
	public static final String EXCEPTION_1 = " | DUE TO AN ";
	public static final String EXCEPTION_2 = " ISSUE REQUEST IS NOT PROCESSED | ";
	public static final String CALL_HISTORY_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final boolean True = true;
	public static final boolean False = false;
	
	//Logging Levels
	public static final String INFO = "INFO";
	public static final String ERROR = "ERROR";
	public static final String WARN = "WARN";
	public static final String DEBUG = "DEBUG";
	public static final String FATAL = "FATAL";
	public static final String EXCEPTION = "EXCEPTION";
	
	//Days
	public static final String Monday = "MON";
	public static final String Tuesday = "TUE";
	public static final String Wednesday = "WED";
	public static final String Thursday = "THU";
	public static final String Friday = "FRI";
	
	//Variables
	//SimpleVariable
	public static final String SimpleVariable = "Prompt-50,Prompt,customerId,Service,CustTier,Location,PreferredLanguage,IsOceanaSuccess,agentAvailability,memberCategory,checkEnglishAgent24,isEURouting,isMultilingual,PreviousNodeReplay,OOHTiming,IsOOHPrompt,authToken,qid,MobileNumber,ApiEwt,TokenStatus,ocdsCallCount,connectback,ident_type,commonconfigvalues,timeout";
	
	//ComplexVariable
	public static final String ComplexVariable = "ApplicationVariable,IsTableModified,DynamicPrompt,QA_WebService,WS_IdentifyCustomerDetails,WS_IdentifyStaff,WS_VerifyStaff,WS_UpdateLanguagePreference,WS_GetCustomerDetail,WS_CheckPNRDetail,WS_SendSMS,Ws_UpdateVB";
	//Complex Fields
	public static final String ApplicationVariable = "DBStatusHashmap,NextNode,MenuList,DbException,ZoneID,WorkingDay,WorkingHour,NodeDescription,BackupNodeDescription,DBAppValues,Error,Language,MultiLingual,PhraseList,BargeIn,IsEnabled,NoInput,NoMatch,Maxtrice,IsCallHistoryInserted,Timeout,PCGrammarUrl,MenuHistory,MenuHistorySequence,retryCount,internalRetryCount,PreviousNodeDescription,isMenuKeyAdded,InterDigitTimeOut,IsPrivate,Termchar,apiNoMatch,apiNoInput,LanguageHashMap,IsTransHistoryInserted,currentDay,checkWorkingHoursValidation";
	public static final String DynamicPrompt = "PromptLocation,PromptLanguage,Prompt";
	public static final String IsTableModified = "DnisMaster,ApplicationMaster,FlowMaster,VdnMaster,TransferMaster";
	public static final String QA_WebService = "LinkDown,Success,FFPNumber,IATANumber,StaffNumber,DateOfJoining,DateOfBirth";	
	public static final String WS_IdentifyCustomerDetails = "MobileResponseMap,FfpResponseMap,IataResponseMap,responseMap,SuccessFlag,RequestType";
	public static final String WS_IdentifyStaff = "responseMap,SuccessFlag";
	public static final String WS_VerifyStaff = "responseMap,SuccessFlag";
	public static final String WS_UpdateLanguagePreference = "responseMap,SuccessFlag";
	public static final String WS_GetCustomerDetail = "responseMap,SuccessFlag";
	public static final String WS_CheckPNRDetail = "responseMap,SuccessFlag";
	public static final String WS_SendSMS = "responseMap,SuccessFlag";
	public static final String Ws_UpdateVB = "responseMap,SuccessFlag";
	public static final String Ws_SendMail = "responseMap,SuccessFlag";
	
	
	//VB
	public static String PRIMARYURL="";
	public static String SECONDARYURL="";
	public static String NUANCE_WSNAMESPACE="NUANCE_WSNAMESPACE";
	public static final String NUANCE_WINDOWS_SERVER_USERNAME = "NUANCE_WINDOWS_SERVER_USERNAME";
	public static final String NUANCE_WINDOWS_SERVER_PASSWORD = "NUANCE_WINDOWS_SERVER_PASSWORD";
	public static final String NUANCE_WINDOWS_SERVER_DOMAIN = "NUANCE_WINDOWS_SERVER_DOMAIN";
	
	//Secret key
	public static final String SECRET_KEY = "mysecretpassword";
	
	
}