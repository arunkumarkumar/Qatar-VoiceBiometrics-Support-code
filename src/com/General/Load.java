package com.General;

import java.util.Arrays;
import java.util.HashMap;

import com.Oceana.Api;
import com.avaya.sce.runtime.tracking.TraceInfo;
import com.avaya.sce.runtimecommon.ITraceInfo;
import com.avaya.sce.runtimecommon.SCESession;
import com.flow.DataNode;
import com.flow.LoadOnStartUp;
import com.flow.Set;

public class Load {

	@SuppressWarnings("unchecked")
	public void values(String method,SCESession mySession) {

		String ClassName = "Loadvalues";

		try {

			LoadOnStartUp loadOnStartUp = new LoadOnStartUp();
			Set set = new Set();
			DataNode dataNode = new DataNode();
			ApiCallFromExtJar apiCall = new ApiCallFromExtJar();
			Api api = new Api(); 

			mySession.getVariableField("Error").setValue(false);

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, ClassName+" : method called inside Jar is : "+method, mySession);

			//DynamicMethodCalling
			if(method.equalsIgnoreCase("setInitialValues")) {
				loadOnStartUp.setInitialValues(mySession);
			} else if(method.equalsIgnoreCase("preData")) {
				set.preData(mySession);
			} else if(method.equalsIgnoreCase("preAnnouncement")) {
				set.preAnnouncement(mySession);
			} else if(method.equalsIgnoreCase("preMenu")) {
				set.preMenu(mySession);
			} else if(method.equalsIgnoreCase("prePC")) {
				set.prePC(mySession);
			} else if(method.equalsIgnoreCase("postAnnouncement")) {
				set.postAnnouncement(mySession);
			} else if(method.equalsIgnoreCase("postMenu")) {
				set.postMenu(mySession);
			} else if(method.equalsIgnoreCase("postPC")) {
				set.postPC(mySession);
			} else if(method.equalsIgnoreCase("Language")) {
				set.Language(mySession);
			} else if(method.equalsIgnoreCase("flowLanguage")) {
				set.flowLanguage(mySession);
			} else if(method.equalsIgnoreCase("noInputNoMatch")) {
				set.noInputNoMatch(mySession);
			} else if(method.equalsIgnoreCase("setPreviousNode")) {
				dataNode.setPreviousNode(mySession);
			} else if(method.equalsIgnoreCase("checkRetryCount")) {
				dataNode.checkRetryCount(mySession);
			} else if(method.equalsIgnoreCase("CheckBusinessHours")) {
				dataNode.WorkingHourValidation(mySession);
			}  else if(method.equalsIgnoreCase("CheckTransferHours")) {
				dataNode.TransferHourValidation(mySession);
			}else if(method.equalsIgnoreCase("loadApplication")) {
				dataNode.loadApplication(mySession);
			} else if(method.equalsIgnoreCase("CheckBCPFlag")) {
				dataNode.CheckBCPFlag(mySession);
			} else if(method.equalsIgnoreCase("checkMultilingual")) {
				dataNode.checkMultilingual(mySession);
			} else if(method.equalsIgnoreCase("CheckBCPDisconnectFlag")) {
				dataNode.CheckBCPDisconnectFlag(mySession);
			} else if(method.equalsIgnoreCase("CheckPreferredLangWithMarketLang")) {
				dataNode.CheckPreferredLangWithMarketLang(mySession);
			} else if(method.equalsIgnoreCase("checkIsArabicLanguage")) {
				dataNode.checkIsArabicLanguage(mySession);
			} else if(method.equalsIgnoreCase("CheckDSTFlag")) {
				dataNode.CheckDSTFlag(mySession);
			} else if(method.equalsIgnoreCase("CheckBCPMessageFlag")) {
				dataNode.CheckBCPMessageFlag(mySession);
			} else if(method.equalsIgnoreCase("CheckEmergencyFlag")) {
				dataNode.CheckEmergencyFlag(mySession);
			} else if(method.equalsIgnoreCase("CheckEmergencyDisconnectFlag")) {
				dataNode.CheckEmergencyDisconnectFlag(mySession);
			} else if(method.equalsIgnoreCase("CheckClosedTransfer")) {
				dataNode.CheckClosedTransfer(mySession);
			} else if(method.equalsIgnoreCase("CheckIataFlag")) {
				dataNode.CheckIataFlag(mySession);
			} else if(method.equalsIgnoreCase("checkGDPRFlag")) {
				dataNode.checkGDPRFlag(mySession);
			} else if(method.equalsIgnoreCase("checkSpecialMessageFlag")) {
				dataNode.checkSpecialMessageFlag(mySession);
			} else if(method.equalsIgnoreCase("checkDisclaimerMessageFlag")) {
				dataNode.checkDisclaimerMessageFlag(mySession);
			} else if(method.equalsIgnoreCase("checkSurveyMandatoryFlag")) {
				dataNode.checkSurveyMandatoryFlag(mySession);
			} else if(method.equalsIgnoreCase("checkSurveyFlag")) {
				dataNode.checkSurveyFlag(mySession);
			} else if(method.equalsIgnoreCase("checkTransferTimingFlag")) {
				dataNode.checkTransferTimingFlag(mySession);
			} else if(method.equalsIgnoreCase("checkAgentAvailabilityFlag")) {
				dataNode.checkAgentAvailabilityFlag(mySession);
			} else if(method.equalsIgnoreCase("checkEuRoutingFlag")) {
				dataNode.checkEuRoutingFlag(mySession);
			} else if(method.equalsIgnoreCase("setGDPRStatus")) {
				dataNode.setGDPRStatus(mySession);
			} else if(method.equalsIgnoreCase("GdprNotRecord")) {
				dataNode.GdprNotRecord(mySession);
			} else if(method.equalsIgnoreCase("TransferHourValidation")) {
				dataNode.TransferHourValidation(mySession);
			} else if(method.equalsIgnoreCase("getTransferVDN")) {
				dataNode.getTransferVDN(mySession);
			} else if(method.equalsIgnoreCase("setSurveyConsent")) {
				dataNode.setSurveyConsent(mySession);
			} else if(method.equalsIgnoreCase("isCallHistoryInserted")) {
				dataNode.isCallHistoryInserted(mySession);
			} else if(method.equalsIgnoreCase("CallHistory")) {
				dataNode.CallHistory(mySession);
			} else if(method.equalsIgnoreCase("isEnglish")) {
				dataNode.isEnglish(mySession);
			} else if(method.equalsIgnoreCase("ICDUsingMobileNoCountryCode")) {
				apiCall.ICDUsingMobileNoCountryCode(mySession);
			} else if(method.equalsIgnoreCase("ICDUsingFFPNumber")) {
				apiCall.ICDUsingFFPNumber(mySession);
			} else if(method.equalsIgnoreCase("ICDUsingIATANumber")) {
				apiCall.ICDUsingIATANumber(mySession);
			} else if(method.equalsIgnoreCase("IdentifyStaff")) {
				apiCall.IdentifyStaff(mySession);
			} else if(method.equalsIgnoreCase("VerifyStaff")) {
				apiCall.VerifyStaff(mySession);
			} else if(method.equalsIgnoreCase("UpdateLanguagePreference")) {
				apiCall.UpdateLanguagePreference(mySession);
			} else if(method.equalsIgnoreCase("checkApiDown")) {
				apiCall.checkApiDown(mySession);
			} else if(method.equalsIgnoreCase("checkPNRDetail")) {
				apiCall.checkPNRDetail(mySession);
			} else if(method.equalsIgnoreCase("getCustomerDetail")) {
				apiCall.getCustomerDetail(mySession);
			} else if(method.equalsIgnoreCase("setStaffNumber")) {
				dataNode.setStaffNumber(mySession);
			} else if(method.equalsIgnoreCase("setFFPNumber")) {
				dataNode.setFFPNumber(mySession);
			} else if(method.equalsIgnoreCase("setIATANumber")) {
				dataNode.setIATANumber(mySession);
			} else if(method.equalsIgnoreCase("setDateOfBirth")) {
				dataNode.setDateOfBirth(mySession);
			} else if(method.equalsIgnoreCase("setDateOfJoining")) {
				dataNode.setDateOfJoining(mySession);
			} else if(method.equalsIgnoreCase("checkEuRoutingMenu")) {
				dataNode.checkEuRoutingMenu(mySession);
			} else if(method.equalsIgnoreCase("setEURouting")) {
				dataNode.setEURouting(mySession);
			} else if(method.equalsIgnoreCase("checkAPINoMatchRetry")) {
				dataNode.checkAPINoMatchRetry(mySession);
			} else if(method.equalsIgnoreCase("marketBasedSpecialTransfer")) {
				dataNode.marketBasedSpecialTransfer(mySession);
			} else if(method.equalsIgnoreCase("extensionTransfer")) {
				dataNode.extensionTransfer(mySession);
			} else if(method.equalsIgnoreCase("setOOHPrompt")) {
				dataNode.setOOHPrompt(mySession);
			} else if(method.equalsIgnoreCase("getTokenAuth")) {
				dataNode.getTokenAuth(mySession);
			} else if(method.equalsIgnoreCase("ocdsCallCount")) {
				dataNode.ocdsCallCount(mySession);
			} else if(method.equalsIgnoreCase("callGetCustomerId")) {
				api.callGetCustomerId(mySession);
			} else if(method.equalsIgnoreCase("callCheckEWT")) {
				api.callCheckEWT(mySession);
			} else if(method.equalsIgnoreCase("callCreateOCDS")) {
				api.callCreateOCDS(mySession);
			} else if(method.equalsIgnoreCase("callUpdateCustomerJourney")) {
				api.callUpdateCustomerJourney(mySession);
			} else if(method.equalsIgnoreCase("setLocation")) {
				api.setLocation(mySession);
			}  else if(method.equalsIgnoreCase("CheckPreferredLang")) {
				apiCall.CheckPreferredLang(mySession);
			} else if(method.equalsIgnoreCase("isProfileFound")) {
				apiCall.isProfileFound(mySession);
			} else if(method.equalsIgnoreCase("isCustomerIdentified")) {
				apiCall.isCustomerIdentified(mySession);
			} else if(method.equalsIgnoreCase("isFFNoMatching")) {
				apiCall.isFFNoMatching(mySession);
			} else if(method.equalsIgnoreCase("isIataMatching")) {
				apiCall.isIataMatching(mySession);
			} else if(method.equalsIgnoreCase("isDisrupted")) {
				apiCall.isDisrupted(mySession);
			} else if(method.equalsIgnoreCase("isProfileFound")) {
				apiCall.isProfileFound(mySession);
			} else if(method.equalsIgnoreCase("isIdentifiedProfile")) {
				apiCall.isIdentifiedProfile(mySession);
			} else if(method.equalsIgnoreCase("checkEnglishAgentAvail24")) {
				apiCall.checkEnglishAgentAvail24(mySession);
			} else if(method.equalsIgnoreCase("checkMemberTier")) {
				apiCall.checkMemberTier(mySession);
			} else if(method.equalsIgnoreCase("isFFPPinMatched")) {
				apiCall.isFFPPinMatched(mySession);
			} else if(method.equalsIgnoreCase("isStaffDOJMatched")) {
				apiCall.isStaffDOJMatched(mySession);
			} else if(method.equalsIgnoreCase("isStaffIDMatching")) {
				apiCall.isStaffIDMatching(mySession);
			} else if(method.equalsIgnoreCase("checkEwtPrompt")) {
				apiCall.checkEwtPrompt(mySession);
			}else if(method.equalsIgnoreCase("checkWorkingStatus")){
				dataNode.checkWorkingStatus(mySession);
			}else if(method.equalsIgnoreCase("checkCurrentDay")){
				dataNode.checkCurrentDay(mySession);
			} 
			else if(method.equalsIgnoreCase("getEnrollData")){
				dataNode.getEnrollData(mySession);
			}
			else if(method.equalsIgnoreCase("getVbStatus")){
				dataNode.getVbStatus(mySession);
			}
			else if(method.equalsIgnoreCase("getVbSelfServe")){
				dataNode.getVbSelfServe(mySession);
			}
			else if(method.equalsIgnoreCase("VBHistory")){
				dataNode.VBHistory(mySession);
			}
			else if(method.equalsIgnoreCase("startConnect")){
				dataNode.startConnect(mySession);
			}
			else if(method.equalsIgnoreCase("checkConnectBack")){
				dataNode.checkConnectBack(mySession);
			}else if(method.equalsIgnoreCase("callupdateOCDS")) {
				api.callupdateOCDS(mySession);
			} 
			
			else {

				mySession.getVariableField("Error").setValue(true);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, AppConstants.ERRORINCLASS+ClassName+" | method \""+method+"\" is not available", mySession);
				mySession.getVariableField("ApplicationVariable", "NextNode").setValue("Disconnect");
			}

		} catch(Exception e) {

			mySession.getVariableField("Error").setValue(true);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, "Load\t|\tvalues on methods\t|\t"+ AppConstants.EXCEPTION_1+ e.getMessage() +AppConstants.EXCEPTION_2 , mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, "Load\t|\tvalues on methods\t|\t"+ Arrays.toString(e.getStackTrace()), mySession);

		}

		try {

			//Check if any error occured or not
			boolean AppError = mySession.getVariableField("Error").getBooleanValue(); 

			if(AppError) {

				HashMap<String,String> App_Prop = new HashMap<String,String>();
				HashMap<String,String> CallHistory = new HashMap<String,String>();

				App_Prop = (HashMap<String, String>) mySession.getVariableField("HashMap", "PropertyValue").getObjectValue();
				CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

				String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

				String Prompt = "TECH_DIFF";

				if(App_Prop.containsKey("TechnicalDifficulties")) {
					Prompt = App_Prop.get("TechnicalDifficulties").toString().trim();
				}

				method = "getTransferVDN";
				mySession.getVariableField("method").setValue(method);

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, AppConstants.ERRORINCLASS+ClassName+" | Method Updated to | "+method, mySession);

				HashMap<Integer,String> promptlist = new HashMap<Integer,String>();
				promptlist.put(0, Prompt);
				mySession.getVariableField("DynamicPrompt", "Prompt").setValue(promptlist);
				mySession.getVariableField("ApplicationVariable", "NextNode").setValue("TechnicalDifficulties");
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, AppConstants.ERRORINCLASS+ClassName, mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, AppConstants.ERRORINCLASS+ClassName+" | Application is facing some Technical Difficulties : "+method, mySession);

				MenuDescription += "TechnicalDifficulties";

				CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
				mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

			}

		} catch(Exception e) {
			
			method = "getTransferVDN";
			mySession.getVariableField("method").setValue(method);
			mySession.getVariableField("Error").setValue(true);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, "Load\t|\tvalues on Error\t|\t"+ AppConstants.EXCEPTION_1+ e.getMessage() +AppConstants.EXCEPTION_2 , mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, "Load\t|\tvalues on Error\t|\t"+ Arrays.toString(e.getStackTrace()), mySession);

		}

	}

}
