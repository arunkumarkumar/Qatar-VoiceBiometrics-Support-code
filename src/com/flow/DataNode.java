package com.flow;

import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.security.cert.X509Certificate;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TimeZone;

import javax.net.ssl.SSLContext;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import com.DBOperation.AES;
import com.DBOperation.DBConnection;
import com.DBOperation.FlatFile;
import com.General.ApiCallFromExtJar;
import com.General.AppConstants;
import com.General.LoadApplicationProperties;
import com.HoursOfOperation.CheckBusinessHours;
import com.Oceana.ApiConnection;
import com.XML_Parsing.langauge;
import com.avaya.sce.runtime.tracking.TraceInfo;
import com.avaya.sce.runtimecommon.ITraceInfo;
import com.avaya.sce.runtimecommon.SCESession;
import com.util.Nuance.CustomAESEncryption;
import com.util.Nuance.VBHist;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class DataNode {

	String className = "DataNode";

	@SuppressWarnings("unchecked")
	public void loadApplication(SCESession mySession) {

		String method = "loadApplication";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "Check Application Active Status", mySession);

		String DBStatus = AppConstants.F;
		String NextNode = "";

		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();
		HashMap<String, String> DBAppValues = new HashMap<String, String>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
						mySession);

				DBAppValues = (HashMap<String, String>) mySession.getVariableField("ApplicationVariable", "DBAppValues")
						.getObjectValue();

				if (DBAppValues.containsKey("VC_IS_ACTIVE_APPLICATION_MASTER")) {

					if (DBAppValues.get("VC_IS_ACTIVE_APPLICATION_MASTER").equalsIgnoreCase("Y")) {

						DBStatus = AppConstants.T;

					}

				}

			} else {

				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

				if (flow.containsKey(NodeDescription)) {
					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
					mySession.getVariableField("Error").setValue(AppConstants.True);

				}

			}
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
					className + "\t|\t" + method + "\t|\t" + "Status | " + DBStatus, mySession);
			NodeDescription = flowvalues.get(DBStatus).toString().trim();

			if (NodeDescription.equalsIgnoreCase("Disconnect")) {

				NextNode = NodeDescription;

			} else {

				if (flow.containsKey(NodeDescription)) {
					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
					mySession.getVariableField("Error").setValue(AppConstants.True);

				}

			}

		} else {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + "NodeDescription " + NodeDescription + " is Missing",
					mySession);
			mySession.getVariableField("Error").setValue(AppConstants.True);

		}

		MenuDescription += DBStatus.toUpperCase() + "|";

		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
				mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void CheckBCPFlag(SCESession mySession) {

		String method = "CheckBCPFlag";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();
		HashMap<String, String> DBAppValues = new HashMap<String, String>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();
		HashMap<String, String> App_Prop = new HashMap<String, String>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();
		App_Prop = (HashMap<String, String>) mySession.getVariableField("HashMap", "PropertyValue").getObjectValue();

		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

		String Flag = AppConstants.F;
		String NextNode = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
						mySession);

				DBAppValues = (HashMap<String, String>) mySession.getVariableField("ApplicationVariable", "DBAppValues")
						.getObjectValue();

				if (App_Prop.containsKey("BCPGlobalFlag")) {

					if (App_Prop.get("BCPGlobalFlag").equalsIgnoreCase("NA")) {

						if (DBAppValues.containsKey("VC_BCP_FLAG")) {

							if (DBAppValues.get("VC_BCP_FLAG").equalsIgnoreCase("Y")) {
								Flag = AppConstants.T;
							}

						}

					} else {

						if (App_Prop.get("BCPGlobalFlag").equalsIgnoreCase("Y")) {

							Flag = AppConstants.T;

						}

					}

				} else {

					if (DBAppValues.containsKey("VC_BCP_FLAG")) {

						if (DBAppValues.get("VC_BCP_FLAG").equalsIgnoreCase("Y")) {
							Flag = AppConstants.T;
						}

					}

				}

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Status | " + Flag, mySession);
				NodeDescription = flowvalues.get(Flag).toString().trim();

				if (NodeDescription.equalsIgnoreCase("Disconnect")) {

					NextNode = NodeDescription;

				} else {

					if (flow.containsKey(NodeDescription)) {
						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
						mySession.getVariableField("Error").setValue(AppConstants.True);

					}

				}

			} else {

				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

				if (flow.containsKey(NodeDescription)) {
					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
					mySession.getVariableField("Error").setValue(AppConstants.True);

				}

			}

		} else {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + "NodeDescription " + NodeDescription + " is Missing",
					mySession);
			mySession.getVariableField("Error").setValue(AppConstants.True);

		}

		MenuDescription += Flag.toUpperCase() + "|";

		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
				mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void CheckBCPDisconnectFlag(SCESession mySession) {

		String method = "CheckBCPDisconnectFlag";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();
		HashMap<String, String> DBAppValues = new HashMap<String, String>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

		String Flag = AppConstants.F;
		String NextNode = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
						mySession);

				DBAppValues = (HashMap<String, String>) mySession.getVariableField("ApplicationVariable", "DBAppValues")
						.getObjectValue();

				if (DBAppValues.containsKey("VC_BCP_DISCONNECT_FLAG")) {

					if (DBAppValues.get("VC_BCP_DISCONNECT_FLAG").equalsIgnoreCase("Y")) {
						Flag = AppConstants.T;
					}

				}

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Status | " + Flag, mySession);
				NodeDescription = flowvalues.get(Flag).toString().trim();

				if (NodeDescription.equalsIgnoreCase("Disconnect")) {

					NextNode = NodeDescription;

				} else {

					if (flow.containsKey(NodeDescription)) {
						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
						mySession.getVariableField("Error").setValue(AppConstants.True);

					}

				}

			} else {

				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

				if (flow.containsKey(NodeDescription)) {
					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
					mySession.getVariableField("Error").setValue(AppConstants.True);

				}

			}

		} else {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + "Next NodeDescription " + NodeDescription + " is Missing",
					mySession);
			mySession.getVariableField("Error").setValue(AppConstants.True);

		}

		MenuDescription += Flag.toUpperCase() + "|";

		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
				mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	public void checkMultilingual(SCESession mySession) {

		String method = "checkMultilingual";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		String inputFlag = mySession.getVariableField("isMultilingual").getStringValue();

		ApiCallFromExtJar apiCall = new ApiCallFromExtJar();
		apiCall.setApiFlag(mySession, inputFlag);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void CheckPreferredLangWithMarketLang(SCESession mySession) {

		String method = "CheckPreferredLangWithMarketLang";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		// DBConnection db = new DBConnection();

		HashMap<String, Object> Lang = new HashMap<String, Object>();
		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();
		// List<HashMap<Object, Object>> DBResult = null;
		// LinkedHashMap<String, Object> input = new LinkedHashMap<String, Object>();

		Lang = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Language").getObjectValue();
		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");
		String Flag = AppConstants.F;
		String NextNode = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();
		String PreferredLanguage = mySession.getVariableField("PreferredLanguage").getStringValue();
		// boolean DBException = mySession.getVariableField("ApplicationVariable",
		// "DbException").getBooleanValue();

		try {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
					className + "\t|\t" + method + "\t|\t" + "PreferredLanguage is " + PreferredLanguage, mySession);

			List<Object> languageList = (List<Object>) mySession
					.getVariableField("ApplicationVariable", "LanguageHashMap").getObjectValue();
			HashMap<String, String> languageMap = null;

			for (int i = 0; i < languageList.size(); i++) {

				languageMap = (HashMap<String, String>) languageList.get(i);
				if (languageMap.containsKey("VC_LANGUAGE_NAME") && languageMap.containsKey("VC_LANGUAGE_1")) {

					if (PreferredLanguage.equalsIgnoreCase(languageMap.get("VC_LANGUAGE_1"))) {

						PreferredLanguage = languageMap.get("VC_LANGUAGE_NAME");
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t"
								+ "Language Name Received as " + PreferredLanguage, mySession);

						break;
					}

				}

			}

		} catch (Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
					+ AppConstants.EXCEPTION_1 + e.getMessage() + AppConstants.EXCEPTION_2, mySession);

		}

		List<langauge> languages = (List<langauge>) Lang.get("Language");

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
						mySession);

				for (int i = 0; i < languages.size(); i++) {
					if (PreferredLanguage.equalsIgnoreCase(languages.get(i).getName().trim())) {
						Flag = AppConstants.T;
					}
				}

				String promptLanguage = "";
				if (Flag.equalsIgnoreCase(AppConstants.F)) {

					HashMap<String, String> customerDetails = new HashMap<String, String>();
					customerDetails = (HashMap<String, String>) mySession.getVariableField("HashMap", "customerDetails")
							.getObjectValue();
					customerDetails.put("IsPreferredLanguage", "N");
					mySession.getVariableField("HashMap", "customerDetails").setValue(customerDetails);

					if (languages.size() > 0) {

						for (int x = 0; x < languages.size(); x++) {

							promptLanguage = promptLanguage + languages.get(x).getName().toString().trim();
							if (x != languages.size() - 1) {
								promptLanguage += ",";
							}

						}
						if (languages.size() > 1) {
							mySession.getVariableField("isMultilingual").setValue("true");
						} else {
							mySession.getVariableField("isMultilingual").setValue("false");
						}
					} else {
						mySession.getVariableField("isMultilingual").setValue("false");
					}
					mySession.getVariableField("DynamicPrompt", "PromptLanguage").setValue(promptLanguage);
				}

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Status | " + Flag, mySession);
				NodeDescription = flowvalues.get(Flag).toString().trim();

				if (NodeDescription.equalsIgnoreCase("Disconnect")) {

					NextNode = NodeDescription;

				} else {

					if (flow.containsKey(NodeDescription)) {
						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
						mySession.getVariableField("Error").setValue(AppConstants.True);

					}

				}

			} else {

				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

				if (flow.containsKey(NodeDescription)) {
					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
					mySession.getVariableField("Error").setValue(AppConstants.True);

				}

			}

		} else {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + "NodeDescription " + NodeDescription + " is Missing",
					mySession);
			mySession.getVariableField("Error").setValue(AppConstants.True);

		}

		MenuDescription += Flag.toUpperCase() + "|";

		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
				mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void checkIsArabicLanguage(SCESession mySession) {

		String method = "checkIsArabicLanguage";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");
		String Language = CallHistory.get("VC_LANGUAGE");
		String Flag = AppConstants.F;
		String NextNode = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
						mySession);

				if (Language.equalsIgnoreCase("ARABIC")) {
					Flag = AppConstants.T;
				}

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Status | " + Flag, mySession);
				NodeDescription = flowvalues.get(Flag).toString().trim();

				if (NodeDescription.equalsIgnoreCase("Disconnect")) {

					NextNode = NodeDescription;

				} else {

					if (flow.containsKey(NodeDescription)) {
						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
						mySession.getVariableField("Error").setValue(AppConstants.True);

					}

				}

			} else {

				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

				if (flow.containsKey(NodeDescription)) {
					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
					mySession.getVariableField("Error").setValue(AppConstants.True);

				}

			}

		} else {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + "NodeDescription " + NodeDescription + " is Missing",
					mySession);
			mySession.getVariableField("Error").setValue(AppConstants.True);

		}

		MenuDescription += Flag.toUpperCase() + "|";

		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
				mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void CheckIsProfileFound(SCESession mySession) {

		String method = "CheckIsProfileFound";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");
		String Language = CallHistory.get("VC_LANGUAGE");
		String Flag = AppConstants.F;
		String NextNode = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
						mySession);

				if (Language.equalsIgnoreCase("ARABIC")) {
					Flag = AppConstants.T;
				}

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Status | " + Flag, mySession);
				NodeDescription = flowvalues.get(Flag).toString().trim();

				if (NodeDescription.equalsIgnoreCase("Disconnect")) {

					NextNode = NodeDescription;

				} else {

					if (flow.containsKey(NodeDescription)) {
						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
						mySession.getVariableField("Error").setValue(AppConstants.True);

					}

				}

			} else {

				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

				if (flow.containsKey(NodeDescription)) {
					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
					mySession.getVariableField("Error").setValue(AppConstants.True);

				}

			}

		} else {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + "NodeDescription " + NodeDescription + " is Missing",
					mySession);
			mySession.getVariableField("Error").setValue(AppConstants.True);

		}

		MenuDescription += Flag.toUpperCase() + "|";

		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
				mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void CheckDSTFlag(SCESession mySession) {

		String method = "CheckDSTFlag";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();
		HashMap<String, String> DBAppValues = new HashMap<String, String>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

		String Flag = AppConstants.F;
		String NextNode = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
						mySession);

				DBAppValues = (HashMap<String, String>) mySession.getVariableField("ApplicationVariable", "DBAppValues")
						.getObjectValue();

				if (DBAppValues.containsKey("VC_DST_FLAG")) {

					if (DBAppValues.get("VC_DST_FLAG").equalsIgnoreCase("Y")) {
						Flag = AppConstants.T;
					}

				}

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Status | " + Flag, mySession);
				NodeDescription = flowvalues.get(Flag).toString().trim();

				if (NodeDescription.equalsIgnoreCase("Disconnect")) {

					NextNode = NodeDescription;

				} else {

					if (flow.containsKey(NodeDescription)) {
						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
						mySession.getVariableField("Error").setValue(AppConstants.True);

					}

				}

			} else {

				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

				if (flow.containsKey(NodeDescription)) {
					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
					mySession.getVariableField("Error").setValue(AppConstants.True);

				}

			}

		} else {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + "NodeDescription " + NodeDescription + " is Missing",
					mySession);
			mySession.getVariableField("Error").setValue(AppConstants.True);

		}

		MenuDescription += Flag.toUpperCase() + "|";

		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
				mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void CheckBCPMessageFlag(SCESession mySession) {

		String method = "CheckBCPMessageFlag";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();
		HashMap<String, String> DBAppValues = new HashMap<String, String>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

		String Flag = AppConstants.F;
		String NextNode = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
						mySession);

				DBAppValues = (HashMap<String, String>) mySession.getVariableField("ApplicationVariable", "DBAppValues")
						.getObjectValue();

				if (DBAppValues.containsKey("VC_BCP_MESSAGE_FLAG")) {

					if (DBAppValues.get("VC_BCP_MESSAGE_FLAG").equalsIgnoreCase("Y")) {
						Flag = AppConstants.T;
					}

				}

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Status | " + Flag, mySession);
				NodeDescription = flowvalues.get(Flag).toString().trim();

				if (NodeDescription.equalsIgnoreCase("Disconnect")) {

					NextNode = NodeDescription;

				} else {

					if (flow.containsKey(NodeDescription)) {
						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
						mySession.getVariableField("Error").setValue(AppConstants.True);

					}

				}

			} else {

				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

				if (flow.containsKey(NodeDescription)) {
					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
					mySession.getVariableField("Error").setValue(AppConstants.True);

				}

			}

		} else {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + "NodeDescription " + NodeDescription + " is Missing",
					mySession);
			mySession.getVariableField("Error").setValue(AppConstants.True);

		}

		MenuDescription += Flag.toUpperCase() + "|";

		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
				mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void CheckEmergencyFlag(SCESession mySession) {

		String method = "CheckEmergencyFlag";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();
		HashMap<String, String> DBAppValues = new HashMap<String, String>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

		String Flag = AppConstants.F;
		String NextNode = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
						mySession);

				DBAppValues = (HashMap<String, String>) mySession.getVariableField("ApplicationVariable", "DBAppValues")
						.getObjectValue();

				if (DBAppValues.containsKey("VC_EMERGENCY_FLAG")) {

					if (DBAppValues.get("VC_EMERGENCY_FLAG").equalsIgnoreCase("Y")) {
						Flag = AppConstants.T;
					}

				}

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Status | " + Flag, mySession);
				NodeDescription = flowvalues.get(Flag).toString().trim();

				if (NodeDescription.equalsIgnoreCase("Disconnect")) {

					NextNode = NodeDescription;

				} else {

					if (flow.containsKey(NodeDescription)) {
						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
						mySession.getVariableField("Error").setValue(AppConstants.True);

					}

				}

			} else {

				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

				if (flow.containsKey(NodeDescription)) {
					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
					mySession.getVariableField("Error").setValue(AppConstants.True);

				}

			}

		} else {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + "NodeDescription " + NodeDescription + " is Missing",
					mySession);
			mySession.getVariableField("Error").setValue(AppConstants.True);

		}

		MenuDescription += Flag.toUpperCase() + "|";

		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
				mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void CheckEmergencyDisconnectFlag(SCESession mySession) {

		String method = "CheckEmergencyDisconnectFlag";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();
		HashMap<String, String> DBAppValues = new HashMap<String, String>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

		String Flag = AppConstants.F;
		String NextNode = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
						mySession);

				DBAppValues = (HashMap<String, String>) mySession.getVariableField("ApplicationVariable", "DBAppValues")
						.getObjectValue();

				if (DBAppValues.containsKey("VC_IS_EMERGENCY_DISCONNECT_FLAG")) {

					if (DBAppValues.get("VC_IS_EMERGENCY_DISCONNECT_FLAG").equalsIgnoreCase("Y")) {
						Flag = AppConstants.T;
					}

				}

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Status | " + Flag, mySession);
				NodeDescription = flowvalues.get(Flag).toString().trim();

				if (NodeDescription.equalsIgnoreCase("Disconnect")) {

					NextNode = NodeDescription;

				} else {

					if (flow.containsKey(NodeDescription)) {
						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
						mySession.getVariableField("Error").setValue(AppConstants.True);

					}

				}

			} else {

				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

				if (flow.containsKey(NodeDescription)) {
					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
					mySession.getVariableField("Error").setValue(AppConstants.True);

				}

			}

		} else {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + "NodeDescription " + NodeDescription + " is Missing",
					mySession);
			mySession.getVariableField("Error").setValue(AppConstants.True);

		}

		MenuDescription += Flag.toUpperCase() + "|";

		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
				mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void CheckClosedTransfer(SCESession mySession) {

		String method = "CheckClosedTransfer";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();
		HashMap<String, String> DBAppValues = new HashMap<String, String>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

		String Flag = AppConstants.F;
		String NextNode = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
						mySession);

				DBAppValues = (HashMap<String, String>) mySession.getVariableField("ApplicationVariable", "DBAppValues")
						.getObjectValue();

				if (DBAppValues.containsKey("VC_IS_CLOSED_TRANS_FLAG")) {

					if (DBAppValues.get("VC_IS_CLOSED_TRANS_FLAG").equalsIgnoreCase("Y")) {
						Flag = AppConstants.T;
					}

				}

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Status | " + Flag, mySession);
				NodeDescription = flowvalues.get(Flag).toString().trim();

				if (NodeDescription.equalsIgnoreCase("Disconnect")) {

					NextNode = NodeDescription;

				} else {

					if (flow.containsKey(NodeDescription)) {
						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
						mySession.getVariableField("Error").setValue(AppConstants.True);

					}

				}

			} else {

				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

				if (flow.containsKey(NodeDescription)) {
					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
					mySession.getVariableField("Error").setValue(AppConstants.True);

				}

			}

		} else {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + "NodeDescription " + NodeDescription + " is Missing",
					mySession);
			mySession.getVariableField("Error").setValue(AppConstants.True);

		}

		MenuDescription += Flag.toUpperCase() + "|";

		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
				mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void CheckIataFlag(SCESession mySession) {

		String method = "CheckIataFlag";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();
		HashMap<String, String> DBAppValues = new HashMap<String, String>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

		String Flag = AppConstants.F;
		String NextNode = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
						mySession);

				DBAppValues = (HashMap<String, String>) mySession.getVariableField("ApplicationVariable", "DBAppValues")
						.getObjectValue();

				if (DBAppValues.containsKey("VC_IATA_VALIDATION_FLAG")) {

					if (DBAppValues.get("VC_IATA_VALIDATION_FLAG").equalsIgnoreCase("Y")) {
						Flag = AppConstants.T;
					}

				}

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Status | " + Flag, mySession);
				NodeDescription = flowvalues.get(Flag).toString().trim();

				if (NodeDescription.equalsIgnoreCase("Disconnect")) {

					NextNode = NodeDescription;

				} else {

					if (flow.containsKey(NodeDescription)) {
						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
						mySession.getVariableField("Error").setValue(AppConstants.True);

					}

				}

			} else {

				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

				if (flow.containsKey(NodeDescription)) {
					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
					mySession.getVariableField("Error").setValue(AppConstants.True);

				}

			}

		} else {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + "NodeDescription " + NodeDescription + " is Missing",
					mySession);
			mySession.getVariableField("Error").setValue(AppConstants.True);

		}

		MenuDescription += Flag.toUpperCase() + "|";

		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
				mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void checkGDPRFlag(SCESession mySession) {

		String method = "checkGDPRFlag";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();
		HashMap<String, String> DBAppValues = new HashMap<String, String>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

		String Flag = AppConstants.F;
		String NextNode = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
						mySession);

				DBAppValues = (HashMap<String, String>) mySession.getVariableField("ApplicationVariable", "DBAppValues")
						.getObjectValue();

				if (DBAppValues.containsKey("VC_GDPR_FLAG")) {

					if (DBAppValues.get("VC_GDPR_FLAG").equalsIgnoreCase("Y")) {
						Flag = AppConstants.T;
					}

				}

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Status | " + Flag, mySession);
				NodeDescription = flowvalues.get(Flag).toString().trim();

				if (NodeDescription.equalsIgnoreCase("Disconnect")) {

					NextNode = NodeDescription;

				} else {

					if (flow.containsKey(NodeDescription)) {
						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
						mySession.getVariableField("Error").setValue(AppConstants.True);

					}

				}

			} else {

				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

				if (flow.containsKey(NodeDescription)) {
					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
					mySession.getVariableField("Error").setValue(AppConstants.True);

				}

			}

		} else {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + "NodeDescription " + NodeDescription + " is Missing",
					mySession);
			mySession.getVariableField("Error").setValue(AppConstants.True);

		}

		MenuDescription += Flag.toUpperCase() + "|";

		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
				mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void setGDPRStatus(SCESession mySession) {

		String method = "setGDPRStatus";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		HashMap<String, String> CallHistory = new HashMap<String, String>();
		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();
		String BranchName = "";

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
					className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription, mySession);

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			String dtmfSelected = mySession.getVariableField("DynamicMenu", "value").getStringValue();
			String grammar = flowvalues.get("Grammar").toString();
			String nextNodeValues = flowvalues.get("NextNode").toString();
			BranchName = flowvalues.get("BranchNames").toString();

			String[] grammarList = grammar.split(",");
			String[] nextNodeValuesList = nextNodeValues.split(",");
			String[] branchnameList = BranchName.split(",");

			for (int z = 0; z < grammarList.length; z++) {

				if (dtmfSelected.equalsIgnoreCase(grammarList[z])) {
					BranchName = branchnameList[z];
					NodeDescription = nextNodeValuesList[z];
					break;
				}

			}

			CallHistory.put("VC_GDPR_STATUS", BranchName);

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
					className + "\t|\t" + method + "\t|\t" + "VC_GDPR_STATUS | " + BranchName, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
					className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

		} else {

			mySession.getVariableField("Error").setValue(AppConstants.True);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + "Key Description | " + NodeDescription + " | is Missing",
					mySession);

		}

		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void GdprNotRecord(SCESession mySession) {

		String method = "GdprNotRecord";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();
		HashMap<String, String> DBAppValues = new HashMap<String, String>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

		String Flag = AppConstants.F;
		String NextNode = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
						mySession);

				DBAppValues = (HashMap<String, String>) mySession.getVariableField("ApplicationVariable", "DBAppValues")
						.getObjectValue();

				if (DBAppValues.containsKey("GDPR_DISC")) {

					if (DBAppValues.get("GDPR_DISC").equalsIgnoreCase("Y")) {
						Flag = AppConstants.T;
					}

				}

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Status | " + Flag, mySession);
				NodeDescription = flowvalues.get(Flag).toString().trim();

				if (NodeDescription.equalsIgnoreCase("Disconnect")) {

					NextNode = NodeDescription;

				} else {

					if (flow.containsKey(NodeDescription)) {
						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
						mySession.getVariableField("Error").setValue(AppConstants.True);

					}

				}

			} else {

				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

				if (flow.containsKey(NodeDescription)) {
					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
					mySession.getVariableField("Error").setValue(AppConstants.True);

				}

			}

		} else {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + "NodeDescription " + NodeDescription + " is Missing",
					mySession);
			mySession.getVariableField("Error").setValue(AppConstants.True);

		}

		MenuDescription += Flag.toUpperCase() + "|";

		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
				mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void checkSpecialMessageFlag(SCESession mySession) {

		String method = "checkSpecialMessageFlag";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();
		HashMap<String, String> DBAppValues = new HashMap<String, String>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

		String Flag = AppConstants.F;
		String NextNode = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
						mySession);

				DBAppValues = (HashMap<String, String>) mySession.getVariableField("ApplicationVariable", "DBAppValues")
						.getObjectValue();

				if (DBAppValues.containsKey("VC_SPECIAL_MESSAGE_FLAG")) {

					if (DBAppValues.get("VC_SPECIAL_MESSAGE_FLAG").equalsIgnoreCase("Y")) {
						Flag = AppConstants.T;
					}

				}

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Status | " + Flag, mySession);
				NodeDescription = flowvalues.get(Flag).toString().trim();

				if (NodeDescription.equalsIgnoreCase("Disconnect")) {

					NextNode = NodeDescription;

				} else {

					if (flow.containsKey(NodeDescription)) {
						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
						mySession.getVariableField("Error").setValue(AppConstants.True);

					}

				}

			} else {

				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

				if (flow.containsKey(NodeDescription)) {
					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
					mySession.getVariableField("Error").setValue(AppConstants.True);

				}

			}

		} else {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + "NodeDescription " + NodeDescription + " is Missing",
					mySession);
			mySession.getVariableField("Error").setValue(AppConstants.True);

		}

		MenuDescription += Flag.toUpperCase() + "|";

		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
				mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void checkDisclaimerMessageFlag(SCESession mySession) {

		String method = "checkDisclaimerMessageFlag";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();
		HashMap<String, String> DBAppValues = new HashMap<String, String>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

		String Flag = AppConstants.F;
		String NextNode = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
						mySession);

				DBAppValues = (HashMap<String, String>) mySession.getVariableField("ApplicationVariable", "DBAppValues")
						.getObjectValue();

				if (DBAppValues.containsKey("VC_DISCLAIMER_MSG_FLAG")) {

					if (DBAppValues.get("VC_DISCLAIMER_MSG_FLAG").equalsIgnoreCase("Y")) {
						Flag = AppConstants.T;
					}

				}

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Status | " + Flag, mySession);
				NodeDescription = flowvalues.get(Flag).toString().trim();

				if (NodeDescription.equalsIgnoreCase("Disconnect")) {

					NextNode = NodeDescription;

				} else {

					if (flow.containsKey(NodeDescription)) {
						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
						mySession.getVariableField("Error").setValue(AppConstants.True);

					}

				}

			} else {

				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

				if (flow.containsKey(NodeDescription)) {
					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
					mySession.getVariableField("Error").setValue(AppConstants.True);

				}

			}

		} else {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + "NodeDescription " + NodeDescription + " is Missing",
					mySession);
			mySession.getVariableField("Error").setValue(AppConstants.True);

		}

		MenuDescription += Flag.toUpperCase() + "|";

		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
				mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void checkSurveyMandatoryFlag(SCESession mySession) {

		String method = "checkSurveyMandatoryFlag";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();
		HashMap<String, String> DBAppValues = new HashMap<String, String>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

		String Flag = AppConstants.F;
		String NextNode = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
						mySession);

				DBAppValues = (HashMap<String, String>) mySession.getVariableField("ApplicationVariable", "DBAppValues")
						.getObjectValue();

				if (DBAppValues.containsKey("VC_IS_SURVEY_MANDATORY_FLAG")) {

					if (DBAppValues.get("VC_IS_SURVEY_MANDATORY_FLAG").equalsIgnoreCase("Y")) {
						Flag = AppConstants.T;
					}

				}

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Status | " + Flag, mySession);
				NodeDescription = flowvalues.get(Flag).toString().trim();

				if (NodeDescription.equalsIgnoreCase("Disconnect")) {

					NextNode = NodeDescription;

				} else {

					if (flow.containsKey(NodeDescription)) {
						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
						mySession.getVariableField("Error").setValue(AppConstants.True);

					}

				}

			} else {

				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

				if (flow.containsKey(NodeDescription)) {
					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
					mySession.getVariableField("Error").setValue(AppConstants.True);

				}

			}

		} else {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + "NodeDescription " + NodeDescription + " is Missing",
					mySession);
			mySession.getVariableField("Error").setValue(AppConstants.True);

		}

		MenuDescription += Flag.toUpperCase() + "|";

		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
				mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void checkSurveyFlag(SCESession mySession) {

		String method = "checkSurveyFlag";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();
		HashMap<String, String> DBAppValues = new HashMap<String, String>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

		String Flag = AppConstants.F;
		String NextNode = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
						mySession);

				DBAppValues = (HashMap<String, String>) mySession.getVariableField("ApplicationVariable", "DBAppValues")
						.getObjectValue();

				if (DBAppValues.containsKey("VC_SURVEY_FLAG")) {

					if (DBAppValues.get("VC_SURVEY_FLAG").equalsIgnoreCase("Y")) {
						Flag = AppConstants.T;
					}

				}

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Status | " + Flag, mySession);
				NodeDescription = flowvalues.get(Flag).toString().trim();

				if (NodeDescription.equalsIgnoreCase("Disconnect")) {

					NextNode = NodeDescription;

				} else {

					if (flow.containsKey(NodeDescription)) {
						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
						mySession.getVariableField("Error").setValue(AppConstants.True);

					}

				}

			} else {

				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

				if (flow.containsKey(NodeDescription)) {
					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
					mySession.getVariableField("Error").setValue(AppConstants.True);

				}

			}

		} else {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + "NodeDescription " + NodeDescription + " is Missing",
					mySession);
			mySession.getVariableField("Error").setValue(AppConstants.True);

		}

		MenuDescription += Flag.toUpperCase() + "|";

		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
				mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void checkTransferTimingFlag(SCESession mySession) {

		String method = "checkTransferTimingFlag";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();
		HashMap<String, String> DBAppValues = new HashMap<String, String>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

		String Flag = AppConstants.F;
		String NextNode = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
						mySession);

				DBAppValues = (HashMap<String, String>) mySession.getVariableField("ApplicationVariable", "DBAppValues")
						.getObjectValue();

				if (DBAppValues.containsKey("VC_IS_TRANSFER_TIMING_FLAG")) {

					if (DBAppValues.get("VC_IS_TRANSFER_TIMING_FLAG").equalsIgnoreCase("Y")) {
						Flag = AppConstants.T;
					}

				}

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Status | " + Flag, mySession);
				NodeDescription = flowvalues.get(Flag).toString().trim();

				if (NodeDescription.equalsIgnoreCase("Disconnect")) {

					NextNode = NodeDescription;

				} else {

					if (flow.containsKey(NodeDescription)) {
						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
						mySession.getVariableField("Error").setValue(AppConstants.True);

					}

				}

			} else {

				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

				if (flow.containsKey(NodeDescription)) {
					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
					mySession.getVariableField("Error").setValue(AppConstants.True);

				}

			}

		} else {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + "NodeDescription " + NodeDescription + " is Missing",
					mySession);
			mySession.getVariableField("Error").setValue(AppConstants.True);

		}

		MenuDescription += Flag.toUpperCase() + "|";

		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
				mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void checkAgentAvailabilityFlag(SCESession mySession) {

		String method = "checkAgentAvailabilityFlag";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

		String Flag = AppConstants.F;
		String NextNode = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
						mySession);

				int agentAvailability = mySession.getVariableField("agentAvailability").getIntValue();

				if (agentAvailability == 1) {
					Flag = AppConstants.T;
				}

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Status | " + Flag, mySession);
				NodeDescription = flowvalues.get(Flag).toString().trim();

				if (NodeDescription.equalsIgnoreCase("Disconnect")) {

					NextNode = NodeDescription;

				} else {

					if (flow.containsKey(NodeDescription)) {
						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
						mySession.getVariableField("Error").setValue(AppConstants.True);

					}

				}

			} else {

				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

				if (flow.containsKey(NodeDescription)) {
					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
					mySession.getVariableField("Error").setValue(AppConstants.True);

				}

			}

		} else {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + "NodeDescription " + NodeDescription + " is Missing",
					mySession);
			mySession.getVariableField("Error").setValue(AppConstants.True);

		}

		MenuDescription += Flag.toUpperCase() + "|";

		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
				mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void checkEuRoutingFlag(SCESession mySession) {

		String method = "checkEuRoutingFlag";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();
		HashMap<String, String> DBAppValues = new HashMap<String, String>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

		String Flag = AppConstants.F;
		String NextNode = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
						mySession);

				DBAppValues = (HashMap<String, String>) mySession.getVariableField("ApplicationVariable", "DBAppValues")
						.getObjectValue();

				if (DBAppValues.containsKey("VC_EU_ROUTING_FLAG")) {

					if (DBAppValues.get("VC_EU_ROUTING_FLAG").equalsIgnoreCase("Y")) {
						Flag = AppConstants.T;
					}

				}

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Status | " + Flag, mySession);
				NodeDescription = flowvalues.get(Flag).toString().trim();

				if (NodeDescription.equalsIgnoreCase("Disconnect")) {

					NextNode = NodeDescription;

				} else {

					if (flow.containsKey(NodeDescription)) {
						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
						mySession.getVariableField("Error").setValue(AppConstants.True);

					}

				}

			} else {

				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

				if (flow.containsKey(NodeDescription)) {
					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
					mySession.getVariableField("Error").setValue(AppConstants.True);

				}

			}

		} else {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + "NodeDescription " + NodeDescription + " is Missing",
					mySession);
			mySession.getVariableField("Error").setValue(AppConstants.True);

		}

		MenuDescription += Flag.toUpperCase() + "|";

		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
				mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void checkEuRoutingMenu(SCESession mySession) {

		String method = "checkEuRoutingMenu";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

		String Flag = AppConstants.F;
		String NextNode = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
						mySession);

				Flag = mySession.getVariableField("isEURouting").getStringValue();

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Status | " + Flag, mySession);
				NodeDescription = flowvalues.get(Flag).toString().trim();

				if (NodeDescription.equalsIgnoreCase("Disconnect")) {

					NextNode = NodeDescription;

				} else {

					if (flow.containsKey(NodeDescription)) {
						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
						mySession.getVariableField("Error").setValue(AppConstants.True);

					}

				}

			} else {

				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

				if (flow.containsKey(NodeDescription)) {
					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
					mySession.getVariableField("Error").setValue(AppConstants.True);

				}

			}

		} else {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + "NodeDescription " + NodeDescription + " is Missing",
					mySession);
			mySession.getVariableField("Error").setValue(AppConstants.True);

		}

		MenuDescription += Flag.toUpperCase() + "|";

		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
				mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void setSurveyConsent(SCESession mySession) {

		String method = "setSurveyConsent";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		HashMap<String, String> CallHistory = new HashMap<String, String>();
		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();
		String BranchName = "";

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
					className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription, mySession);

			String dtmfSelected = mySession.getVariableField("DynamicMenu", "value").getStringValue();
			String grammar = flowvalues.get("Grammar").toString();
			String nextNodeValues = flowvalues.get("NextNode").toString();
			BranchName = flowvalues.get("BranchNames").toString();

			String[] grammarList = grammar.split(",");
			String[] nextNodeValuesList = nextNodeValues.split(",");
			String[] branchnameList = BranchName.split(",");

			for (int z = 0; z < grammarList.length; z++) {

				if (dtmfSelected.equalsIgnoreCase(grammarList[z])) {
					BranchName = branchnameList[z];
					NodeDescription = nextNodeValuesList[z];
					break;
				}

			}

			CallHistory.put("VC_SURVEY_CONSENT", BranchName);

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
					className + "\t|\t" + method + "\t|\t" + "VC_SURVEY_CONSENT | " + BranchName, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
					className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

		} else {

			mySession.getVariableField("Error").setValue(AppConstants.True);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + "Key Description | " + NodeDescription + " | is Missing",
					mySession);

		}

		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	public void isCallHistoryInserted(SCESession mySession) {

		String method = "isCallHistoryInserted";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		String isCallHistoryInserted = mySession.getVariableField("ApplicationVariable", "IsCallHistoryInserted")
				.getStringValue();

		if (isCallHistoryInserted.equalsIgnoreCase(AppConstants.T)) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
					className + "\t|\t" + method + "\t|\t" + "Already Call History is inserted", mySession);
		} else {
			CallHistory(mySession);
		}

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void WorkingHourValidation(SCESession mySession) {

		String method = "WorkingHourValidation";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		CheckBusinessHours checkBusinessHour = new CheckBusinessHours();

		HashMap<String, Object> Lang = new HashMap<String, Object>();
		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

		Lang = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Language").getObjectValue();

		List<langauge> languages = (List<langauge>) Lang.get("Language");

		String Language = mySession.getVariableField("ApplicationVariable", "Language").getStringValue();
		String WorkingHour = "";
		String WorkingDay = "";
		String ZoneID = "";
		String WorkingHourResponse = "";

		for (int i = 0; i < languages.size(); i++) {

			if (languages.get(i).getName().trim().equalsIgnoreCase(Language)) {

				ZoneID = CallHistory.get("VC_TIME_ZONE");
				WorkingDay = languages.get(i).getBusinessHours().getLangWorkingDay().toString().trim();
				WorkingHour = languages.get(i).getBusinessHours().getLangWorkingHour().toString().trim();
				break;

			}

		}
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "ZoneID " + ZoneID,
				mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "WorkingDay " + WorkingDay, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "WorkingHour " + WorkingHour, mySession);

		WorkingHourResponse = checkBusinessHour.checkWorkingHour(ZoneID, WorkingDay, WorkingHour, mySession);

		String NextNode = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
						mySession);

				NodeDescription = flowvalues.get(WorkingHourResponse).toString().trim();

				if (NodeDescription.equalsIgnoreCase("Disconnect")) {

					NextNode = NodeDescription;

				} else {

					if (flow.containsKey(NodeDescription)) {
						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
						mySession.getVariableField("Error").setValue(AppConstants.True);

					}

				}

			} else {

				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

				if (flow.containsKey(NodeDescription)) {
					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
					mySession.getVariableField("Error").setValue(AppConstants.True);

				}

			}

		} else {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + "NodeDescription " + NodeDescription + " is Missing",
					mySession);
			mySession.getVariableField("Error").setValue(AppConstants.True);

		}

		MenuDescription += WorkingHourResponse.toUpperCase() + "|";

		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
				mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	public void setOOHPrompt(SCESession mySession) {

		String method = "CallHistory";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		ApiCallFromExtJar apiCall = new ApiCallFromExtJar();

		mySession.getVariableField("IsOOHPrompt").setValue(true);

		apiCall.setApiFlag(mySession, "true");

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void TransferHourValidation(SCESession mySession) {

		String method = "TransferHourValidation";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		CheckBusinessHours checkBusinessHour = new CheckBusinessHours();

		HashMap<String, Object> Lang = new HashMap<String, Object>();
		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();

		Lang = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Language").getObjectValue();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		List<langauge> languages = (List<langauge>) Lang.get("Language");

		String Language = mySession.getVariableField("ApplicationVariable", "Language").getStringValue();
		String WorkingHour = "";
		String WorkingDay = "";
		String ZoneID = "";
		String WorkingHourResponse = "";
		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

		for (int i = 0; i < languages.size(); i++) {

			if (languages.get(i).getName().trim().equalsIgnoreCase(Language)) {

				ZoneID = CallHistory.get("VC_TIME_ZONE");
				WorkingDay = languages.get(i).getBusinessHours().getTransWorkingDay().toString().trim();
				WorkingHour = languages.get(i).getBusinessHours().getTransWorkingHour().toString().trim();
				break;

			}

		}
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "ZoneID " + ZoneID,
				mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "WorkingDay " + WorkingDay, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "WorkingHour " + WorkingHour, mySession);

		WorkingHourResponse = checkBusinessHour.checkWorkingHour(ZoneID, WorkingDay, WorkingHour, mySession);

		String NextNode = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
						mySession);

				NodeDescription = flowvalues.get(WorkingHourResponse).toString().trim();

				if (NodeDescription.equalsIgnoreCase("Disconnect")) {

					NextNode = NodeDescription;

				} else {

					if (flow.containsKey(NodeDescription)) {
						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
						mySession.getVariableField("Error").setValue(AppConstants.True);

					}

				}

			} else {

				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

				if (flow.containsKey(NodeDescription)) {
					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
					mySession.getVariableField("Error").setValue(AppConstants.True);

				}

			}

		} else {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + "NodeDescription " + NodeDescription + " is Missing",
					mySession);
			mySession.getVariableField("Error").setValue(AppConstants.True);

		}

		MenuDescription += WorkingHourResponse.toUpperCase() + "|";

		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
				mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void CallHistory(SCESession mySession) {

		String method = "CallHistory";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		DBConnection db = new DBConnection();
		ApiCallFromExtJar apiCall = new ApiCallFromExtJar();
		String isCallHistoryInserted = "";

		HashMap<String, String> CallHistory = new HashMap<String, String>();
		LinkedHashMap<String, Object> input = new LinkedHashMap<String, Object>();

		boolean DBException = mySession.getVariableField("ApplicationVariable", "DbException").getBooleanValue();
		String flag = "false";

		// int errorCount = mySession.getVariableField("ErrorCount").getIntValue();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String callEndType = CallHistory.get("VC_CALL_END_TYPE");

		CallHistory.put("DT_END_DATE", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));

		// Calculating Duration
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");

		try {

			Date StartDate = sdf.parse(CallHistory.get("DT_START_DATE"));
			Date EndDate = sdf.parse(CallHistory.get("DT_END_DATE"));

			long difference_In_Seconds = (EndDate.getTime() - StartDate.getTime()) / 1000;

			CallHistory.put("NU_CALL_DURATION", Long.toString(difference_In_Seconds));

			CallHistory.put("DT_ORIGIN_START_DATE",
					getGMTTime(CallHistory.get("DT_START_DATE"), CallHistory.get("VC_TIME_ZONE"), mySession));
			CallHistory.put("DT_ORIGIN_END_DATE",
					getGMTTime(CallHistory.get("DT_END_DATE"), CallHistory.get("VC_TIME_ZONE"), mySession));

		} catch (Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
					+ AppConstants.EXCEPTION_1 + e.getMessage() + AppConstants.EXCEPTION_2, mySession);

		}

		String PromptLanguage = mySession.getVariableField("DynamicPrompt", "PromptLanguage").getStringValue();

		if (PromptLanguage.contains(",")) {
			CallHistory.put("VC_LANGUAGE", "");
		}

		input.put("VC_UCID", CallHistory.get("VC_UCID"));
		input.put("VC_FLOW", CallHistory.get("VC_FLOW"));
		input.put("VC_MARKET", CallHistory.get("VC_MARKET"));
		input.put("VC_LINE_TYPE", CallHistory.get("VC_LINE_TYPE"));
		input.put("VC_CLI_NO", CallHistory.get("VC_CLI_NO"));
		input.put("VC_DNIS", CallHistory.get("VC_DNIS"));
		input.put("VC_LANGUAGE", CallHistory.get("VC_LANGUAGE"));
		input.put("VC_TIME_ZONE", CallHistory.get("VC_TIME_ZONE"));
		input.put("DT_START_DATE", CallHistory.get("DT_START_DATE"));
		input.put("DT_END_DATE", CallHistory.get("DT_END_DATE"));
		input.put("DT_ORIGIN_START_DATE", CallHistory.get("DT_ORIGIN_START_DATE"));
		input.put("DT_ORIGIN_END_DATE", CallHistory.get("DT_ORIGIN_END_DATE"));
		input.put("NU_CALL_DURATION", CallHistory.get("NU_CALL_DURATION"));
		input.put("VC_GDPR_STATUS", CallHistory.get("VC_GDPR_STATUS"));
		input.put("VC_SURVEY_CONSENT", CallHistory.get("VC_SURVEY_CONSENT"));
		input.put("VC_MENU_DESCRIPTION", CallHistory.get("VC_MENU_DESCRIPTION"));
		input.put("VC_CALLER_SEGMENT", CallHistory.get("VC_CALLER_SEGMENT"));
		input.put("VC_EXIT_LOCATION", CallHistory.get("VC_EXIT_LOCATION"));
		input.put("VC_CALL_END_TYPE", CallHistory.get("VC_CALL_END_TYPE"));
		input.put("VC_TRANSFER_VDN", CallHistory.get("VC_TRANSFER_VDN"));
		input.put("VC_UUI", CallHistory.get("VC_UUI"));
		input.put("VC_WORK_REQUEST_ID", CallHistory.get("VC_WORK_REQUEST_ID"));
		input.put("VC_SESSION_MPP_ID", CallHistory.get("VC_SESSION_MPP_ID"));
		input.put("VC_APP_SRVR_IP", CallHistory.get("VC_APP_SRVR_IP"));
		input.put("VC_CALL_STATUS", CallHistory.get("VC_CALL_STATUS"));
		input.put("VC_SERVICE_TYPE", mySession.getVariableField("Service").getStringValue());
		input.put("VC_CUSTOMER_TIER", mySession.getVariableField("CustTier").getStringValue());
		input.put("VC_DID", CallHistory.get("VC_DID"));
		input.put("VC_FAIR_FAMILY", CallHistory.get("VC_FAIR_FAMILY"));
		input.put("VC_OCDS_BUSINESS_UNIT", CallHistory.get("VC_OCDS_BUSINESS_UNIT"));

		try {

			if (!callEndType.equalsIgnoreCase("TRANSFER") && !callEndType.equalsIgnoreCase("HANGUP")
					&& !callEndType.equalsIgnoreCase("DISCONNECT")) {

				DBException = true;
				isCallHistoryInserted = mySession.getVariableField("ApplicationVariable", "IsCallHistoryInserted")
						.getStringValue();
				if (isCallHistoryInserted.equalsIgnoreCase(AppConstants.F)) {
					FlatFile ch = new FlatFile();
					String InsertQuery = "INSERT INTO IVR_CALL_HISTORY ";
					ch.writeToFile(InsertQuery, "CallHistory", input, mySession);
					mySession.getVariableField("ApplicationVariable", "IsCallHistoryInserted").setValue(AppConstants.T);
				} else {
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
							className + "\t|\t" + method + "\t|\t" + "CALL HISTORY ALREADY INSERTED | SUCCESS",
							mySession);
				}
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
						+ "DB Exception While inserting Call History | " + DBException, mySession);

			} else {

				if (DBException) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG,
							className + "\t|\t" + method + "\t|\t" + "DataBase is Un available", mySession);

					isCallHistoryInserted = mySession.getVariableField("ApplicationVariable", "IsCallHistoryInserted")
							.getStringValue();
					if (isCallHistoryInserted.equalsIgnoreCase(AppConstants.F)) {
						FlatFile ch = new FlatFile();
						String InsertQuery = "INSERT INTO IVR_CALL_HISTORY ";
						ch.writeToFile(InsertQuery, "CallHistory", input, mySession);
						mySession.getVariableField("ApplicationVariable", "IsCallHistoryInserted")
						.setValue(AppConstants.T);
					} else {
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
								className + "\t|\t" + method + "\t|\t" + "CALL HISTORY ALREADY INSERTED | SUCCESS",
								mySession);
					}
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "DB Exception While inserting Call History | " + DBException, mySession);

				} else {

					db.getInsertValues("SP_INSERT_CALL_HISTORY", input, mySession);

					mySession.getVariableField("ApplicationVariable", "IsCallHistoryInserted").setValue(AppConstants.T);

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
							className + "\t|\t" + method + "\t|\t" + "IsCallHistoryInserted | " + mySession
							.getVariableField("ApplicationVariable", "IsCallHistoryInserted").getStringValue(),
							mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
							className + "\t|\t" + method + "\t|\t" + "INSERT CALL HISTORY | SUCCESS", mySession);

					DBException = mySession.getVariableField("ApplicationVariable", "DbException").getBooleanValue();
					flag = "true";

					if (DBException) {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG,
								className + "\t|\t" + method + "\t|\t" + "DataBase is Un available", mySession);

						isCallHistoryInserted = mySession
								.getVariableField("ApplicationVariable", "IsCallHistoryInserted").getStringValue();
						if (isCallHistoryInserted.equalsIgnoreCase(AppConstants.F)) {
							FlatFile ch = new FlatFile();
							String InsertQuery = "INSERT INTO IVR_CALL_HISTORY ";
							ch.writeToFile(InsertQuery, "CallHistory", input, mySession);
							mySession.getVariableField("ApplicationVariable", "IsCallHistoryInserted")
							.setValue(AppConstants.T);
						} else {
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
									className + "\t|\t" + method + "\t|\t" + "CALL HISTORY ALREADY INSERTED | SUCCESS",
									mySession);
						}
						flag = "false";
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "DB Exception While inserting Call History | " + DBException, mySession);

					}

				}

			}

		} catch (Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG,
					className + "\t|\t" + method + "\t|\t" + "DataBase is Un available", mySession);

			isCallHistoryInserted = mySession.getVariableField("ApplicationVariable", "IsCallHistoryInserted")
					.getStringValue();
			if (isCallHistoryInserted.equalsIgnoreCase(AppConstants.F)) {
				FlatFile ch = new FlatFile();
				String InsertQuery = "INSERT INTO IVR_CALL_HISTORY ";
				ch.writeToFile(InsertQuery, "CallHistory", input, mySession);
				mySession.getVariableField("ApplicationVariable", "IsCallHistoryInserted").setValue(AppConstants.T);
			} else {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "CALL HISTORY ALREADY INSERTED | SUCCESS", mySession);
			}
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
					+ "DB Exception While inserting Call History | " + DBException, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
					+ AppConstants.EXCEPTION_1 + e.getMessage() + AppConstants.EXCEPTION_2, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + Arrays.toString(e.getStackTrace()), mySession);

		}

		if (callEndType.equalsIgnoreCase("TRANSFER") || callEndType.equalsIgnoreCase("DISCONNECT")) {

			apiCall.setApiFlag(mySession, flag);

		} else {
			ocdsCallCount(mySession);
		}

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void setTransactionHistory(SCESession mySession) {

		String method = "setTransactionHistory";

		String isTransHistoryInserted = AppConstants.F;
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		DBConnection db = new DBConnection();

		LinkedHashMap<String, Object> TransactionHistory = new LinkedHashMap<String, Object>();

		boolean DBException = mySession.getVariableField("ApplicationVariable", "DbException").getBooleanValue();

		try {

			TransactionHistory = (LinkedHashMap<String, Object>) mySession
					.getVariableField("HashMap", "TransactionHistory").getObjectValue();

			if (DBException) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG,
						className + "\t|\t" + method + "\t|\t" + "DataBase is Un available", mySession);
				isTransHistoryInserted = mySession.getVariableField("ApplicationVariable", "IsTransHistoryInserted")
						.getStringValue();

				if (isTransHistoryInserted.equalsIgnoreCase(AppConstants.F)) {
					FlatFile ch = new FlatFile();
					String InsertQuery = "INSERT INTO IVR_TRANSACTION_HISTORY ";
					ch.writeToFile(InsertQuery, "TransactionHistory", TransactionHistory, mySession);
					mySession.getVariableField("ApplicationVariable", "IsTransHistoryInserted")
					.setValue(AppConstants.T);
				} else {
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
							className + "\t|\t" + method + "\t|\t" + "TRANSACTION HISTORY ALREADY INSERTED | SUCCESS",
							mySession);
				}

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
						+ "DB Exception While inserting Transaction History | " + DBException, mySession);

			} else {

				db.getInsertValues("SP_INSERT_TRANSACTION_HISTORY", TransactionHistory, mySession);
				//db.InsertTransHistory(TransactionHistory, mySession);

				DBException = mySession.getVariableField("ApplicationVariable", "DbException").getBooleanValue();

				if (DBException) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG,
							className + "\t|\t" + method + "\t|\t" + "DataBase is Un available", mySession);
					isTransHistoryInserted = mySession.getVariableField("ApplicationVariable", "IsTransHistoryInserted")
							.getStringValue();

					if (isTransHistoryInserted.equalsIgnoreCase(AppConstants.F)) {
						FlatFile ch = new FlatFile();
						String InsertQuery = "INSERT INTO IVR_TRANSACTION_HISTORY ";
						ch.writeToFile(InsertQuery, "TransactionHistory", TransactionHistory, mySession);
						mySession.getVariableField("ApplicationVariable", "IsTransHistoryInserted")
						.setValue(AppConstants.T);
					} else {
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t"
								+ "TRANSACTION HISTORY ALREADY INSERTED | SUCCESS", mySession);
					}
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "DB Exception While inserting Transaction History | " + DBException, mySession);

				}

			}

		} catch (Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG,
					className + "\t|\t" + method + "\t|\t" + "DataBase is Un available", mySession);
			isTransHistoryInserted = mySession.getVariableField("ApplicationVariable", "IsTransHistoryInserted")
					.getStringValue();

			if (isTransHistoryInserted.equalsIgnoreCase(AppConstants.F)) {
				FlatFile ch = new FlatFile();
				String InsertQuery = "INSERT INTO IVR_TRANSACTION_HISTORY ";
				ch.writeToFile(InsertQuery, "TransactionHistory", TransactionHistory, mySession);
				mySession.getVariableField("ApplicationVariable", "IsTransHistoryInserted").setValue(AppConstants.T);
			} else {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "TRANSACTION HISTORY ALREADY INSERTED | SUCCESS",
						mySession);
			}
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
					+ "DB Exception While inserting Transaction History | " + DBException, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
					+ AppConstants.EXCEPTION_1 + e.getMessage() + AppConstants.EXCEPTION_2, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + Arrays.toString(e.getStackTrace()), mySession);

		} finally {
			mySession.getVariableField("ApplicationVariable", "IsTransHistoryInserted").setValue(AppConstants.F);
		}
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	public String getGMTTime(String Time, String Timezone, SCESession mySession) {

		String method = "getGMTTime";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		try {

			Date date1 = new Date();
			date1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(Time);

			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
			String GMT_Time = sdf1.format(date1);

			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			sdf2.setTimeZone(TimeZone.getTimeZone("GMT"));

			SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			sdf3.setTimeZone(TimeZone.getTimeZone(Timezone));

			Date date2 = sdf2.parse(GMT_Time);
			Time = sdf3.format(date2);

		} catch (Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
					+ AppConstants.EXCEPTION_1 + e.getMessage() + AppConstants.EXCEPTION_2, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + Arrays.toString(e.getStackTrace()), mySession);

		}
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

		return Time;
	}

	@SuppressWarnings("unchecked")
	public void marketBasedSpecialTransfer(SCESession mySession) {

		String method = "marketBasedSpecialTransfer";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		DBConnection db = new DBConnection();

		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();
		HashMap<String, String> DBAppValues = new HashMap<String, String>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();

		DBAppValues = (HashMap<String, String>) mySession.getVariableField("ApplicationVariable", "DBAppValues")
				.getObjectValue();
		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();
		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

		List<HashMap<Object, Object>> DBResult = null;
		// List<HashMap<Object, Object>> resultSet = null;
		// LinkedHashMap<String, Object> input = new LinkedHashMap<String, Object>();
		LinkedHashMap<String, Object> input1 = new LinkedHashMap<String, Object>();

		String marketId = DBAppValues.get("NU_MARKET_ID");
		String languageId = "";
		String TransferVDN = "";
		String Flag = AppConstants.F;
		boolean DBException = mySession.getVariableField("ApplicationVariable", "DbException").getBooleanValue();

		String language = mySession.getVariableField("ApplicationVariable", "Language").getStringValue();
		// input.put("VC_LANGUAGE", language);

		try {

			if (!DBException) {

				// resultSet = db.getResultSet("SP_GET_LANGUAGE_NAME", input, mySession);
				// languageId = (String) resultSet.get(0).get("NU_LANGUAGE_ID");

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Language is " + language, mySession);

				List<Object> languageList = (List<Object>) mySession
						.getVariableField("ApplicationVariable", "LanguageHashMap").getObjectValue();
				HashMap<String, String> languageMap = null;

				for (int i = 0; i < languageList.size(); i++) {

					languageMap = (HashMap<String, String>) languageList.get(i);
					if (languageMap.containsKey("VC_LANGUAGE_NAME") && languageMap.containsKey("NU_LANGUAGE_ID")) {

						if (language.equalsIgnoreCase(languageMap.get("VC_LANGUAGE_NAME"))) {

							languageId = languageMap.get("NU_LANGUAGE_ID");
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
									className + "\t|\t" + method + "\t|\t" + "Language ID Received as " + languageId,
									mySession);

							break;
						}

					}

				}

				input1.put("NU_MARKET_ID_IN", marketId);
				input1.put("NU_LANGUAGE_ID_IN", languageId);

				DBResult = db.getResultSet("SP_GET_SPECIAL_TRANSFER", input1, mySession);

			} else {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG,
						className + "\t|\t" + method + "\t|\t" + "DataBase is Un available", mySession);

			}

		} catch (Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
					+ AppConstants.EXCEPTION_1 + e.getMessage() + AppConstants.EXCEPTION_2, mySession);

		}
		String NextNode = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
						mySession);

				try {

					if (DBResult.get(0).containsKey("VC_TRANSFER_VDN_STATUS")) {

						if (DBResult.get(0).get("VC_TRANSFER_VDN_STATUS").toString().equalsIgnoreCase("Y")) {

							TransferVDN = DBResult.get(0).get("VC_TRANSFER_VDN").toString();

							CallHistory.put("VC_TRANSFER_VDN", TransferVDN);
							CallHistory.put("VC_CALL_END_TYPE", "TRANSFER");

							Flag = AppConstants.T;

						}

					}

				} catch (Exception e) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ AppConstants.EXCEPTION_1 + e.getMessage() + AppConstants.EXCEPTION_2, mySession);

				}

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Status | " + Flag, mySession);
				NodeDescription = flowvalues.get(Flag).toString().trim();

				if (NodeDescription.equalsIgnoreCase("Disconnect")) {

					NextNode = NodeDescription;

				} else {

					if (flow.containsKey(NodeDescription)) {
						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
						mySession.getVariableField("Error").setValue(AppConstants.True);

					}

				}

			} else {

				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

				if (flow.containsKey(NodeDescription)) {
					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
					mySession.getVariableField("Error").setValue(AppConstants.True);

				}

			}

		} else {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + "NodeDescription " + NodeDescription + " is Missing",
					mySession);
			mySession.getVariableField("Error").setValue(AppConstants.True);

		}

		MenuDescription += Flag.toUpperCase() + "|";

		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
				mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void setMenuHistory(SCESession mySession) {

		String method = "setMenuHistory";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		try {
			DBConnection db = new DBConnection();

			HashMap<String, String> CallHistory = new HashMap<String, String>();
			LinkedHashMap<String, Object> MenuHistory = new LinkedHashMap<String, Object>();
			HashMap<String, Object> MenuHistoryValues = new HashMap<String, Object>();

			boolean DBException = mySession.getVariableField("ApplicationVariable", "DbException").getBooleanValue();

			CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory")
					.getObjectValue();
			MenuHistory = (LinkedHashMap<String, Object>) mySession
					.getVariableField("ApplicationVariable", "MenuHistory").getObjectValue();

			String insertMenuHistory = "INSERT INTO IVR_MENU_HISTORY VALUES";
			String UCID = CallHistory.get("VC_UCID").toString();
			String CallEndReason = CallHistory.get("VC_CALL_END_TYPE").toString();

			boolean insertStatus = AppConstants.False;

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
					className + "\t|\t" + method + "\t|\t" + "Start Framing Menu History Data", mySession);

			for (int i = 0; i < MenuHistory.size(); i++) {

				int Menusize = MenuHistory.size();

				MenuHistoryValues = (HashMap<String, Object>) MenuHistory.get(Integer.toString(i));

				Menusize--;

				if (i == Menusize) {
					insertMenuHistory = insertMenuHistory + "('" + UCID + "','"
							+ MenuHistoryValues.get("MenuDate").toString() + "','"
							+ MenuHistoryValues.get("MenuKey").toString() + "','" + CallEndReason + "','" + i + "')";
				} else {
					insertMenuHistory = insertMenuHistory + "('" + UCID + "','"
							+ MenuHistoryValues.get("MenuDate").toString() + "','"
							+ MenuHistoryValues.get("MenuKey").toString() + "','','" + i + "'),";
				}

			}

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
					className + "\t|\t" + method + "\t|\t" + "Menu History Data Framed", mySession);

			try {

				if (DBException) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG,
							className + "\t|\t" + method + "\t|\t" + "DataBase is Un available", mySession);

					FlatFile ch = new FlatFile();
					String InsertQuery = "INSERT INTO IVR_MENU_HISTORY ";
					ch.writeToFile(InsertQuery, "MenuHistory", MenuHistory, mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "DB Exception While inserting Menu History | " + DBException, mySession);

				} else {

					insertStatus = db.insertMenuHistory(insertMenuHistory, mySession);

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
							className + "\t|\t" + method + "\t|\t" + "INSERT MENU HISTORY | SUCCESS", mySession);

					DBException = mySession.getVariableField("ApplicationVariable", "DbException").getBooleanValue();

					if (DBException) {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG,
								className + "\t|\t" + method + "\t|\t" + "DataBase is Un available", mySession);

						FlatFile ch = new FlatFile();
						String InsertQuery = "INSERT INTO IVR_MENU_HISTORY ";
						ch.writeToFile(InsertQuery, "MenuHistory", MenuHistory, mySession);
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "DB Exception While inserting Menu History | " + DBException, mySession);

					}
				}

			} catch (Exception e) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG,
						className + "\t|\t" + method + "\t|\t" + "DataBase is Un available", mySession);

				FlatFile ch = new FlatFile();
				String InsertQuery = "INSERT INTO IVR_MENU_HISTORY ";
				ch.writeToFile(InsertQuery, "MenuHistory", MenuHistory, mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
						+ "DB Exception While inserting Menu History | " + DBException, mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
						+ AppConstants.EXCEPTION_1 + e.getMessage() + AppConstants.EXCEPTION_2, mySession);

			}

			if (insertStatus) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Menu History is Inserted Successfully", mySession);

			} else {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
						className + "\t|\t" + method + "\t|\t" + "Menu History is Not Inserted", mySession);

			}

		} catch (Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
					+ AppConstants.EXCEPTION_1 + e.getMessage() + AppConstants.EXCEPTION_2, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + Arrays.toString(e.getStackTrace()), mySession);

		}

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void checkRetryCount(SCESession mySession) {

		String method = this.getClass().getName();

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();

		int retryCount = 0;
		int internalRetryCount = mySession.getVariableField("ApplicationVariable", "internalRetryCount").getIntValue();
		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

		String Flag = AppConstants.F;
		String NextNode = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();
		String PreviousNodeDescription = mySession.getVariableField("PreviousNodeReplay").getStringValue();

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

				if (flowvalues.get("NoOfRetries") == null) {
					retryCount = 2;
				} else {
					retryCount = Integer.parseInt(flowvalues.get("NoOfRetries").toString().trim());
				}

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "retryCount | " + retryCount, mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "internalRetryCount | " + internalRetryCount,
						mySession);

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
						mySession);

				if (internalRetryCount < retryCount) {
					internalRetryCount++;
					Flag = AppConstants.T;
				} else {
					internalRetryCount = 0;
				}

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Status | " + Flag, mySession);

				if (Flag.equalsIgnoreCase(AppConstants.T)) {
					NodeDescription = PreviousNodeDescription;
					flowvalues = (HashMap<String, Object>) flow.get(PreviousNodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {
					NodeDescription = flowvalues.get(Flag).toString().trim();
					if (NodeDescription.equalsIgnoreCase("Disconnect")) {

						NextNode = NodeDescription;

					} else {

						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();

					}
				}

			} else {

				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

				if (flow.containsKey(NodeDescription)) {
					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
					mySession.getVariableField("Error").setValue(AppConstants.T);

				}

			}

		} else {
			mySession.getVariableField("Error").setValue(AppConstants.T);
		}

		MenuDescription += Flag.toUpperCase() + "|";

		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
				mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

		mySession.getVariableField("ApplicationVariable", "internalRetryCount").setValue(internalRetryCount);
		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void checkAPINoMatchRetry(SCESession mySession) {

		String method = "checkAPINoMatchRetry";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();

		String Flag = AppConstants.F;
		String NextNode = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

		String nomatchRetryCount = mySession.getVariableField("ApplicationVariable", "apiNoMatch").getStringValue();

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase("true")) {

				String MaxtriesCount = "3";

				if (flowvalues.get("NoOfRetries") == null) {
					MaxtriesCount = "3";
				} else {
					MaxtriesCount = flowvalues.get("NoOfRetries").toString();
				}

				if (nomatchRetryCount.isEmpty()) {
					nomatchRetryCount = "1";
				}

				if (Integer.parseInt(nomatchRetryCount) < Integer.parseInt(MaxtriesCount)) {

					Flag = AppConstants.T;
					nomatchRetryCount = Integer.toString(Integer.parseInt(nomatchRetryCount) + 1);

				} else {

					nomatchRetryCount = "1";

				}

				NodeDescription = flowvalues.get(Flag).toString();

				flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
				NextNode = flowvalues.get("Type").toString();
				mySession.getVariableField("ApplicationVariable", "IsEnabled")
				.setValue(flowvalues.get("IsEnabled").toString());

			}

		}

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "apiNomatchRetryCount\t|\t" + nomatchRetryCount, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "NodeDescription\t|\t" + NodeDescription, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "NextNode\t|\t" + NextNode, mySession);

		mySession.getVariableField("ApplicationVariable", "NoInput")
		.setValue(mySession.getVariableField("ApplicationVariable", "apiNoInput").getStringValue());
		mySession.getVariableField("ApplicationVariable", "apiNoMatch").setValue(nomatchRetryCount);
		mySession.getVariableField("ApplicationVariable", "NoMatch").setValue(nomatchRetryCount);
		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	public void setPreviousNode(SCESession mySession) {

		String method = "extensionTransfer";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		ApiCallFromExtJar apiCall = new ApiCallFromExtJar();

		mySession.getVariableField("PreviousNodeReplay").setValue(
				mySession.getVariableField("ApplicationVariable", "PreviousNodeDescription").getStringValue());

		apiCall.setApiFlag(mySession, "true");

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void extensionTransfer(SCESession mySession) {

		String method = "extensionTransfer";
		String Status = "false";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		DBConnection db = new DBConnection();

		List<HashMap<Object, Object>> DBResult = null;
		// LinkedHashMap<String, Object> input0 = new LinkedHashMap<String, Object>();
		LinkedHashMap<String, Object> input = new LinkedHashMap<String, Object>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();
		HashMap<String, String> DBAppValues = new HashMap<String, String>();
		// List<HashMap<Object, Object>> resultSet = null;

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();
		DBAppValues = (HashMap<String, String>) mySession.getVariableField("ApplicationVariable", "DBAppValues")
				.getObjectValue();

		try {

			// input0.put("VC_LANGUAGE", CallHistory.get("VC_LANGUAGE"));
			// resultSet = db.getResultSet("SP_GET_LANGUAGE_NAME", input0, mySession);

			String transferVdn = "false";
			boolean DBException = mySession.getVariableField("ApplicationVariable", "DbException").getBooleanValue();

			if (!DBException) {

				String market = DBAppValues.get("NU_MARKET_ID");
				String lineType = DBAppValues.get("NU_APP_ID");
				String language = CallHistory.get("VC_LANGUAGE");
				String languageId = "1";
				// resultSet.get(0).get("NU_LANGUAGE_ID").toString();
				String exitLocation = CallHistory.get("VC_EXIT_LOCATION");
				String appIP = CallHistory.get("VC_APP_SRVR_IP");

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Language is " + language, mySession);

				List<Object> languageList = (List<Object>) mySession
						.getVariableField("ApplicationVariable", "LanguageHashMap").getObjectValue();
				HashMap<String, String> languageMap = null;

				for (int i = 0; i < languageList.size(); i++) {

					languageMap = (HashMap<String, String>) languageList.get(i);
					if (languageMap.containsKey("VC_LANGUAGE_NAME") && languageMap.containsKey("NU_LANGUAGE_ID")) {

						if (language.equalsIgnoreCase(languageMap.get("VC_LANGUAGE_NAME"))) {

							languageId = languageMap.get("NU_LANGUAGE_ID");
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
									className + "\t|\t" + method + "\t|\t" + "Language ID Received as " + languageId,
									mySession);

							break;
						}

					}

				}

				input.put("NU_MARKET", market);
				input.put("NU_LINE_TYPE", lineType);
				input.put("NU_LANGUAGE", languageId);
				input.put("VC_EXITLOCATION", exitLocation);
				input.put("VC_APP_IP", appIP);
				DBResult = db.getResultSet("SP_GET_EXTENSION", input, mySession);

				transferVdn = DBResult.get(0).get("VC_TRANSFER_VDN").toString().trim();

			} else {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG,
						className + "\t|\t" + method + "\t|\t" + "DataBase is Un available", mySession);

			}

			if (!transferVdn.equalsIgnoreCase("false")) {

				Status = "true";
				mySession.getVariableField("TransferVDN").setValue(transferVdn);
				CallHistory.put("VC_TRANSFER_VDN", transferVdn);
				CallHistory.put("VC_CALL_END_TYPE", "TRANSFER");
				mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

			}

		} catch (Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
					+ AppConstants.EXCEPTION_1 + e.getMessage() + AppConstants.EXCEPTION_2, mySession);

		}
		ApiCallFromExtJar apiCall = new ApiCallFromExtJar();

		mySession.getVariableField("isExtensionTransfer").setValue(Status);
		apiCall.setApiFlag(mySession, Status);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void getTokenAuth(SCESession mySession) {

		String method = "getTokenAuth";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		ApiCallFromExtJar apiCall = new ApiCallFromExtJar();
		ApiConnection api = new ApiConnection();
		HashMap<String, Object> responseValues = null;

		String TokenUrl = LoadApplicationProperties.getProperty("TokenUrl", mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Token URL obtained", mySession);
		String sanitizedJson = "";
		String token = "";
		String appid = "";
		String Flag = "false";
		int connectionTimeout = Integer.parseInt(LoadApplicationProperties.getProperty("TokenConnectionTimeout", mySession));
		int readTimeout = Integer.parseInt(LoadApplicationProperties.getProperty("TokenReadTimeout", mySession));
		String isAuthentication = LoadApplicationProperties.getProperty("Qatar.webservices.isAuthentication",mySession);
		int responseCode = 0;
		mySession.getVariableField("TokenStatus").setValue(Flag);

		HashMap<String, String> CallHistory = new HashMap<String, String>();
		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "isAuthentication : "+isAuthentication, mySession);
		if (isAuthentication.equalsIgnoreCase("true")) {

			try {

				responseValues = api.httpGetResponse(method, TokenUrl, connectionTimeout, readTimeout, mySession);

				if (responseValues != null) {

					responseCode = (int) responseValues.get(method + "Code");
					sanitizedJson = responseValues.get(method + "Response").toString();

					if (responseCode == 200) {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
								className + "\t|\t" + method + "\t|\t" + "Final Response | " + sanitizedJson + "\n",
								mySession);

						JSONObject responseObject = new JSONObject(sanitizedJson);

						if (responseObject.has("status")) {

							if (responseObject.get("status").toString().trim().equalsIgnoreCase("Success")) {

								if (responseObject.has("token") && responseObject.has("appid")) {
									token = responseObject.get("token").toString().trim();
									appid = responseObject.get("appid").toString().trim();
									Flag = "true";
								} else {
									CallHistory.put("VC_CALL_STATUS", "FAILURE");
									Flag = "false";
								}

							} else {
								CallHistory.put("VC_CALL_STATUS", "FAILURE");
							}

						} else {
							CallHistory.put("VC_CALL_STATUS", "FAILURE");
						}

					} else {
						CallHistory.put("VC_CALL_STATUS", "FAILURE");
					}

				} else {
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, " Response is not received ", mySession);
				}

			} catch (Exception e) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"+ AppConstants.EXCEPTION_1 + e.getMessage() + AppConstants.EXCEPTION_2, mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t" + Arrays.toString(e.getStackTrace()), mySession);

			}

		}

		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Token From Response | " + token, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Q ID from Response | " + appid, mySession);

		mySession.getVariableField("authToken").setValue(token);
		mySession.getVariableField("qid").setValue(appid);

		mySession.getVariableField("TokenStatus").setValue(Flag);
		apiCall.setApiFlag(mySession, Flag);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void isEnglish(SCESession mySession) {

		String method = "isEnglish";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		ApiCallFromExtJar apiCall = new ApiCallFromExtJar();

		String Flag = AppConstants.F;
		HashMap<String, String> CallHistory = new HashMap<String, String>();
		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();
		if (CallHistory.get("VC_LANGUAGE").trim().equalsIgnoreCase("English")) {
			Flag = AppConstants.T;
		}

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + Flag, mySession);
		apiCall.setApiFlag(mySession, Flag);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	// APIRelatedMethods
	public void setStaffNumber(SCESession mySession) {

		String method = "setStaffNumber";

		String inputFromPC = mySession.getVariableField("DynamicPC", "value").getStringValue();
		try {

			mySession.getVariableField("QA_WebService", "StaffNumber").setValue(inputFromPC);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
					className + "\t|\t" + method + "\t|\t" + "Setting StaffNumber Input from PC| " + inputFromPC,
					mySession);

		} catch (Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
					+ AppConstants.EXCEPTION_1 + e.getMessage() + AppConstants.EXCEPTION_2, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + Arrays.toString(e.getStackTrace()), mySession);

		}
	}

	public void setFFPNumber(SCESession mySession) {

		String method = "setFFPNumber";

		String inputFromPC = mySession.getVariableField("DynamicPC", "value").getStringValue();
		try {

			mySession.getVariableField("QA_WebService", "FFPNumber").setValue(inputFromPC);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
					className + "\t|\t" + method + "\t|\t" + "Setting FFPNumber Input from PC| " + inputFromPC,
					mySession);

		} catch (Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
					+ AppConstants.EXCEPTION_1 + e.getMessage() + AppConstants.EXCEPTION_2, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + Arrays.toString(e.getStackTrace()), mySession);

		}
	}

	public void setIATANumber(SCESession mySession) {

		String method = "setIATANumber";

		String inputFromPC = mySession.getVariableField("DynamicPC", "value").getStringValue();
		try {

			mySession.getVariableField("QA_WebService", "IATANumber").setValue(inputFromPC);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
					className + "\t|\t" + method + "\t|\t" + "Setting IATANumber Input from PC| " + inputFromPC,
					mySession);

		} catch (Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
					+ AppConstants.EXCEPTION_1 + e.getMessage() + AppConstants.EXCEPTION_2, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + Arrays.toString(e.getStackTrace()), mySession);

		}
	}

	public void setDateOfJoining(SCESession mySession) {

		String method = "setDateOfJoining";

		String inputFromPC = mySession.getVariableField("DynamicPC", "value").getStringValue();
		try {

			mySession.getVariableField("QA_WebService", "DateOfJoining").setValue(inputFromPC);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
					className + "\t|\t" + method + "\t|\t" + "Setting DateOfJoining Input from PC| " + inputFromPC,
					mySession);

		} catch (Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
					+ AppConstants.EXCEPTION_1 + e.getMessage() + AppConstants.EXCEPTION_2, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + Arrays.toString(e.getStackTrace()), mySession);

		}
	}

	@SuppressWarnings("unchecked")
	public void setDateOfBirth(SCESession mySession) {

		String method = "setDateOfBirth";

		String inputFromPC = mySession.getVariableField("DynamicPC", "value").getStringValue();
		HashMap<String, String> DBAppValues = new HashMap<String, String>();

		try {

			DBAppValues = (HashMap<String, String>) mySession.getVariableField("ApplicationVariable", "DBAppValues")
					.getObjectValue();
			String Market = DBAppValues.get("VC_MARKET");
			String MarketFromProperty = LoadApplicationProperties.getProperty("FFB_DOB_MARKET", mySession);

			try {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + " | setDateOfBirth | Market | " + Market, mySession);

				DateFormat df = null;
				if (MarketFromProperty.contains(Market)) {
					df = new SimpleDateFormat("MMddyy");
				} else {
					df = new SimpleDateFormat("ddMMyy");
				}
				df.setLenient(false);
				df.parse(inputFromPC);

			} catch (Exception e) {
				inputFromPC = "";
			}

			mySession.getVariableField("QA_WebService", "DateOfBirth").setValue(inputFromPC);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
					className + "\t|\t" + method + "\t|\t" + "Setting DateOfBirth Input from PC | " + inputFromPC,
					mySession);

		} catch (Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
					+ AppConstants.EXCEPTION_1 + e.getMessage() + AppConstants.EXCEPTION_2, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + Arrays.toString(e.getStackTrace()), mySession);

		}

	}

	public void setEURouting(SCESession mySession) {

		String method = "setEURouting";
		String inputEURouting = mySession.getVariableField("DynamicMenu", "value").getStringValue();
		String returnValue = AppConstants.F;

		try {

			if (inputEURouting.equalsIgnoreCase("1")) {
				returnValue = AppConstants.T;
			}

			mySession.getVariableField("isEURouting").setValue(returnValue);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
					className + "\t|\t" + method + "\t|\t" + "Setting EURouting Input from Menu| " + returnValue,
					mySession);

		} catch (Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
					+ AppConstants.EXCEPTION_1 + e.getMessage() + AppConstants.EXCEPTION_2, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + Arrays.toString(e.getStackTrace()), mySession);

		}
	}

	@SuppressWarnings("unchecked")
	public void getTransferVDN(SCESession mySession) {

		String method = "getTransferVDN";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		Parse_XML parse = new Parse_XML();

		String Flag = AppConstants.F;
		HashMap<String, String> DBAppValues = new HashMap<String, String>();
		HashMap<String, String> App_Prop = new HashMap<String, String>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();
		DBAppValues = (HashMap<String, String>) mySession.getVariableField("ApplicationVariable", "DBAppValues")
				.getObjectValue();
		App_Prop = (HashMap<String, String>) mySession.getVariableField("HashMap", "PropertyValue").getObjectValue();

		if (DBAppValues.containsKey("VC_BCP_FLAG")) {

			if (DBAppValues.get("VC_BCP_FLAG").equalsIgnoreCase("Y")) {
				Flag = AppConstants.T;
			}

		}

		if (Flag.equalsIgnoreCase(AppConstants.T)) {

			String BCPFilePath = App_Prop.get("XMLFilePath").toString() + App_Prop.get("BCPXmlName").toString();
			// App_Prop.get("XMLFilePath").toString()+CallHistory.get("VC_MARKET").toString()+"/BCP_TRANSFER_MASTER.xml";

			if (App_Prop.containsKey("BcpMarketBasedFlag")) {

				boolean marketBased = Boolean.parseBoolean(App_Prop.get("BcpMarketBasedFlag").toString());

				if (marketBased) {
					BCPFilePath = App_Prop.get("XMLFilePath").toString() + CallHistory.get("VC_MARKET").toString()
							+ App_Prop.get("BCPXmlName").toString();
				}
			}

			String response = parse.loadBCPTransferXML(BCPFilePath, mySession);

			if (response.equalsIgnoreCase(AppConstants.F)) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
						className + "\t|\t" + method + "\t|\t" + "VDN is Not found in BCP XML File | ", mySession);
				String TransferVDN = DBAppValues.get("VC_DEFAULT_TRANSFER_VDN").toString().trim();
				mySession.getVariableField("TransferVDN").setValue(TransferVDN);
				CallHistory.put("VC_TRANSFER_VDN", TransferVDN);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "DEFAULT TRANSFER VDN IS | " + TransferVDN, mySession);
				mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

			}

			String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription")
					.getStringValue();

			String NextNode = "";

			HashMap<String, Object> flow = new HashMap<String, Object>();
			HashMap<String, Object> flowvalues = new HashMap<String, Object>();

			flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();
			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
					className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription, mySession);

			if (flow.containsKey(NodeDescription)) {

				flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

				if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

					NodeDescription = flowvalues.get(response).toString();

					if (flow.containsKey(NodeDescription)) {
						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
						// mySession.getVariableField("Error").setValue(AppConstants.True);

					}

				} else {

					NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

					if (flow.containsKey(NodeDescription)) {
						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
						// mySession.getVariableField("Error").setValue(AppConstants.True);

					}

				}

			} else {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
						+ "NodeDescription | " + NodeDescription + " | is Missing | ", mySession);
				// mySession.getVariableField("Error").setValue(AppConstants.True);

			}

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
					className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
					className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

			mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
			mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		} else {

			List<String> ModifiedDBStatus = new ArrayList<String>();
			// LinkedHashMap<String, Object> input = new LinkedHashMap<String, Object>();
			ModifiedDBStatus = (List<String>) mySession.getVariableField("ApplicationVariable", "DBStatusHashmap")
					.getObjectValue();

			try {

				if (ModifiedDBStatus.contains("IVR_TRANSFER_MASTER")) {

					loadDBTransJson(mySession);

				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
							className + "\t|\t" + method + "\t|\t" + "TRANSFER TABLE IS NOT MODIFIED | ", mySession);

					String TransferVDNJsonLocation = App_Prop.get("TransferVDNPath")
							+ App_Prop.get("TransferVDNJsonName");

					File file = new File(new File(TransferVDNJsonLocation).getCanonicalPath());

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
							className + "\t|\t" + method + "\t|\t" + "Json file | " + file, mySession);

					if (!file.exists()) {

						loadDBTransJson(mySession);

					}

				}

			} catch (Exception e) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
						+ AppConstants.EXCEPTION_1 + e.getMessage() + AppConstants.EXCEPTION_2, mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
						className + "\t|\t" + method + "\t|\t" + Arrays.toString(e.getStackTrace()), mySession);

			}
			trans(mySession);

		}

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void loadDBTransJson(SCESession mySession) {

		String method = "loadDBTransJson";

		HashMap<String, String> App_Prop = new HashMap<String, String>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();
		LinkedHashMap<String, Object> input = new LinkedHashMap<String, Object>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();
		App_Prop = (HashMap<String, String>) mySession.getVariableField("HashMap", "PropertyValue").getObjectValue();

		DBConnection db = new DBConnection();
		boolean DBException = mySession.getVariableField("ApplicationVariable", "DbException").getBooleanValue();

		List<HashMap<Object, Object>> resultSet = null;

		try {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
					className + "\t|\t" + method + "\t|\t" + "TRANSFER TABLE IS MODIFIED ", mySession);

			if (!DBException) {

				input.put("VC_APP_SRVR_IP", CallHistory.get("VC_APP_SRVR_IP"));
				resultSet = db.getResultSet("SP_GET_TRANSFER_MASTER", input, mySession);

				
				JSONArray transferMasterJson = new JSONArray(resultSet);

				String TransferVDNJsonLocation = App_Prop.get("TransferVDNPath") + App_Prop.get("TransferVDNJsonName");

				File file = new File(new File(TransferVDNJsonLocation).getCanonicalPath());
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Json file | " + file, mySession);
				if (file.exists()) {

					TransferVDNJsonLocation = App_Prop.get("TransferVDNPath")
							+ App_Prop.get("TransferVDNBackupLocation");

					file.renameTo(new File(
							TransferVDNJsonLocation + new SimpleDateFormat("YYMMddHHmmssSSS").format(new Date()) + "_"
									+ App_Prop.get("TransferVDNJsonName")));

					if (!new File(TransferVDNJsonLocation).exists()) {
						new File(TransferVDNJsonLocation).mkdirs();
					}

					try {

						FileWriter wr = new FileWriter(file);
						wr.write(transferMasterJson.toString());
						wr.close();

					} catch (IOException e) {
						TraceInfo.trace(
								ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
										+ AppConstants.EXCEPTION_1 + e.getMessage() + AppConstants.EXCEPTION_2,
										mySession);
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
								className + "\t|\t" + method + "\t|\t" + Arrays.toString(e.getStackTrace()), mySession);
					}

				} else {

					try {

						FileWriter wr = new FileWriter(file);
						wr.write(transferMasterJson.toString());
						wr.close();

					} catch (IOException e) {
						TraceInfo.trace(
								ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
										+ AppConstants.EXCEPTION_1 + e.getMessage() + AppConstants.EXCEPTION_2,
										mySession);
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
								className + "\t|\t" + method + "\t|\t" + Arrays.toString(e.getStackTrace()), mySession);
					}

				}

			} else {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG,
						className + "\t|\t" + method + "\t|\t" + "DataBase is Un available", mySession);

			}

		} catch (Exception e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
					+ AppConstants.EXCEPTION_1 + e.getMessage() + AppConstants.EXCEPTION_2, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + Arrays.toString(e.getStackTrace()), mySession);
		}

	}
	
	@SuppressWarnings("unchecked")
	public void trans(SCESession mySession) {

		String method = "trans";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		HashMap<String, String> CallHistory = new HashMap<String, String>();
		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();
		String Language = CallHistory.get("VC_LANGUAGE");
		String LineType = CallHistory.get("VC_LINE_TYPE");

		// Support Services Changes
		String Day = CallHistory.get("VC_DAY_WISE");

		HashMap<String, String> App_Prop = new HashMap<String, String>();

		App_Prop = (HashMap<String, String>) mySession.getVariableField("HashMap", "PropertyValue").getObjectValue();

		String TransferJsonLocation = App_Prop.get("TransferVDNPath") + App_Prop.get("TransferVDNJsonName");
		String values = "";

		JSONObject transferVDNs = null;
		JSONObject finalVDNList = null;
		String finalValues = "";

		try {

			// Validate whether File is Exist or not
			File transferJsonFile = new File(new File(TransferJsonLocation).getCanonicalPath());
			if (transferJsonFile.exists()) {

				Scanner myReader = null;

				try {

					myReader = new Scanner(transferJsonFile);

					while (myReader.hasNextLine()) {

						String data = myReader.nextLine();
						values = values + data;

					}

					
					JSONArray transferVDN = new JSONArray(values);

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
							className + "\t|\t" + method + "\t|\t" + "Line Type | " + LineType, mySession);

					for(int i = 0; i < transferVDN.length();i++)
					{
						transferVDNs = new JSONObject(transferVDN.get(i).toString());

						if (transferVDNs.get("VC_APP_NAME").toString().equalsIgnoreCase(LineType)){
							//TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "TransferVDN (Application Name):"+transferVDNs, mySession);
							if (transferVDNs.get("VC_LANGUAGE_NAME").toString().equalsIgnoreCase(Language)){
								//TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "TransferVDN (Language):"+transferVDNs, mySession);
								if (transferVDNs.get("VC_DAY").toString().equalsIgnoreCase(Day)){
									finalVDNList = transferVDNs;
									//TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "TransferVDN (Day):"+finalVDNList, mySession);
									break;
								}
								else 
								{
									finalVDNList = transferVDNs;
									//TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "TransferVDN (Day):"+finalVDNList, mySession);
								}
							}
						}
					}
					
					finalValues = finalVDNList.toString();
					
		
				} catch (Exception e) {

					// mySession.getVariableField("Error").setValue(AppConstants.True);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ AppConstants.EXCEPTION_1 + e.getMessage() + AppConstants.EXCEPTION_2, mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
							className + "\t|\t" + method + "\t|\t" + Arrays.toString(e.getStackTrace()), mySession);

				} finally {

					myReader.close();

				}

			}

		} catch (Exception e) {

			// mySession.getVariableField("Error").setValue(AppConstants.True);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
					+ AppConstants.EXCEPTION_1 + e.getMessage() + AppConstants.EXCEPTION_2, mySession);

		}

		// Set Transfer VDN
		setTransferVDN(finalValues, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings("unchecked")
	public void setTransferVDN(String TransferVDNDetail, SCESession mySession) {

		String method = "setTransferVDN";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();
		HashMap<String, String> DBAppValues = new HashMap<String, String>();

		String NextNode = "";
		String DBStatus = AppConstants.F;
		String TransferVDN = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();
		boolean isOceanaSuccess = mySession.getVariableField("IsOceanaSuccess").getBooleanValue();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();
		String GDPRStatus = CallHistory.get("VC_GDPR_STATUS");

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
						mySession);

				DBAppValues = (HashMap<String, String>) mySession.getVariableField("ApplicationVariable", "DBAppValues")
						.getObjectValue();

				if (mySession.getVariableField("Error").getBooleanValue()) {

					DBStatus = AppConstants.F;
					// mySession.getVariableField("Error").setValue(AppConstants.True);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
							className + "\t|\t" + method + "\t|\t" + "Transfer VDN Missing ", mySession);

				} else {

					try {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
								className + "\t|\t" + method + "\t|\t" + "isOceanaSuccess | " + isOceanaSuccess,
								mySession);
						JSONObject VDNDetail = new JSONObject(TransferVDNDetail);
						if (isOceanaSuccess) {
							if (GDPRStatus.equalsIgnoreCase("YES")) {
								TransferVDN = VDNDetail.get("VC_GDPR_TRANSFER_VDN").toString();
							} else {
								TransferVDN = VDNDetail.get("VC_TRANSFER_VDN").toString();
							}
						} else {
							TransferVDN = VDNDetail.get("VC_FALLBACK_TRANSFER_VDN").toString();
						}
						mySession.getVariableField("TransferVDN").setValue(TransferVDN);
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
								className + "\t|\t" + method + "\t|\t" + "TransferVDN | " + TransferVDN, mySession);
						DBStatus = AppConstants.T;

					} catch (Exception e) {

						DBStatus = AppConstants.F;
						// mySession.getVariableField("Error").setValue(AppConstants.True);
						TraceInfo.trace(
								ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
										+ AppConstants.EXCEPTION_1 + e.getMessage() + AppConstants.EXCEPTION_2,
										mySession);

					}

				}

				if (DBStatus.equalsIgnoreCase(AppConstants.F)) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
							className + "\t|\t" + method + "\t|\t" + "EXIT LOCATION IS NOT FOUND | ", mySession);
					TransferVDN = DBAppValues.get("VC_DEFAULT_TRANSFER_VDN").toString().trim();
					mySession.getVariableField("TransferVDN").setValue(TransferVDN);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
							className + "\t|\t" + method + "\t|\t" + "DEFAULT TRANSFER VDN IS | " + TransferVDN,
							mySession);

				}

			}

		}

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "NodeDescription | " + NodeDescription, mySession);

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

				NodeDescription = flowvalues.get(DBStatus).toString();

				if (flow.containsKey(NodeDescription)) {
					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
					// mySession.getVariableField("Error").setValue(AppConstants.True);

				}

			} else {

				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

				if (flow.containsKey(NodeDescription)) {
					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
					// mySession.getVariableField("Error").setValue(AppConstants.True);

				}

			}

		} else {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NodeDescription | "
					+ NodeDescription + " | is Missing | ", mySession);
			// mySession.getVariableField("Error").setValue(AppConstants.True);

		}

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
				mySession);

		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		if (TransferVDN.equalsIgnoreCase("NA") || TransferVDN.isEmpty()) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + "TRANSFER VDN is not loaded | ", mySession);
			TransferVDN = DBAppValues.get("VC_DEFAULT_TRANSFER_VDN").toString().trim();
			mySession.getVariableField("TransferVDN").setValue(TransferVDN);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
					className + "\t|\t" + method + "\t|\t" + "DEFAULT TRANSFER VDN IS | " + TransferVDN, mySession);

		}

		CallHistory.put("VC_TRANSFER_VDN", TransferVDN);
		CallHistory.put("VC_CALL_END_TYPE", "TRANSFER");
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	@SuppressWarnings({ "unchecked" })
	public void ocdsCallCount(SCESession mySession) {

		String method = "ocdsCallCount";

		String flag = "false";
		// String count = "0";
		DBConnection db = new DBConnection();
		ApiCallFromExtJar apiCall = new ApiCallFromExtJar();

		// List<HashMap<Object, Object>> DBResult = null;
		LinkedHashMap<String, Object> input = new LinkedHashMap<String, Object>();
		LinkedHashMap<String, String> CallHistory = new LinkedHashMap<String, String>();

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		boolean DBException = mySession.getVariableField("ApplicationVariable", "DbException").getBooleanValue();

		try {

			CallHistory = (LinkedHashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory")
					.getObjectValue();

			if (!DBException) {

				input.put("VC_UCID", CallHistory.get("VC_UCID"));
				input.put("VC_CLI_NO", CallHistory.get("VC_CLI_NO"));
				input.put("DT_START_DATE", CallHistory.get("DT_START_DATE"));
				input.put("VC_MARKET", CallHistory.get("VC_MARKET"));
				input.put("VC_LINE_TYPE", CallHistory.get("VC_LINE_TYPE"));

				// db.insertOcdsCallCount(input, mySession);
				db.getInsertValues("SP_INSERT_IVR_CALL_COUNT", input, mySession);

				flag = "true";

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Call History inserted to Ocds Ivr Call count",
						mySession);

			} else {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG,
						className + "\t|\t" + method + "\t|\t" + "DataBase is Un available", mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
						+ "DB Exception While inserting IVRCallCount | " + DBException, mySession);
			}

		} catch (Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG,
					className + "\t|\t" + method + "\t|\t" + "DataBase is Un available", mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
					+ "DB Exception While inserting IVRCallCount | " + DBException, mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
					+ AppConstants.EXCEPTION_1 + e.getMessage() + AppConstants.EXCEPTION_2, mySession);

		}

		String callEndType = CallHistory.get("VC_CALL_END_TYPE");
		if (callEndType.equalsIgnoreCase("TRANSFER") || callEndType.equalsIgnoreCase("DISCONNECT")) {

			apiCall.setApiFlag(mySession, flag);

		}

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}

	public boolean isNumericClid(String CLID, SCESession mySession) {
		String method = "isNumericClid";
		CLID = CLID.replaceAll("[^a-zA-Z0-9]", "");
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "Checking whether CLID is Numeric or not", mySession);
		try {
			long cli;
			cli = Long.parseLong(CLID);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
					className + "\t|\t" + method + "\t|\t" + "CLID is Numeric Value | " + cli, mySession);
			return true;
		} catch (NumberFormatException e) {
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG,
					className + "\t|\t" + method + "\t|\t" + "CLID is Non-Numeric Value", mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG,
					className + "\t|\t" + method + "\t|\t" + Arrays.toString(e.getStackTrace()), mySession);
			return false;
		}
	}

	public void checkWorkingStatus(SCESession mySession) {

		String checkWorkingHoursStatus = mySession.getVariableField("ApplicationVariable", "checkWorkingHoursValidation").getStringValue();

		new ApiCallFromExtJar().setApiFlag(mySession, checkWorkingHoursStatus);

	}

	public void checkCurrentDay(SCESession mySession) {

		String checkCurrentDay = mySession.getVariableField("ApplicationVariable", "currentDay").getStringValue();

		new ApiCallFromExtJar().setApiFlag(mySession, checkCurrentDay);

	}

	//new changes

	@SuppressWarnings("unchecked")
	public void getEnrollData(SCESession mySession) {
		

		String method = "getEnrollData";

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

		HashMap<String, Object> flow = new HashMap<String, Object>();
		HashMap<String, Object> flowvalues = new HashMap<String, Object>();
		HashMap<String, String> CallHistory = new HashMap<String, String>();

		CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

		String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

		String Flag = AppConstants.F;
		String NextNode = "";
		String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

		flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

		if (flow.containsKey(NodeDescription)) {

			flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

			if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {


				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
						mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "enrollfailed : " + mySession.getVariableField("enrollfailed").getBooleanValue(),
						mySession);
				

				String enrolledStatus =mySession.getVariableField("TrainWS","trainStatus").getStringValue();
				HashMap<String,String> customerDetails = new HashMap<String,String>();
				customerDetails = (HashMap<String, String>) mySession.getVariableField("HashMap", "customerDetails").getObjectValue();
				if("DISCONNECT".equalsIgnoreCase(customerDetails.get(AppConstants.VB_enrollment_status).toString())) 
				{
					Flag = "DISCONNECT";
				}
				else if("Trained".equalsIgnoreCase(enrolledStatus) || "Succeeded".equalsIgnoreCase(enrolledStatus)) {
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "Enrolled vb is true" , mySession);
					Flag = "SUCCESS";//AppConstants.T;
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
							className + "\t|\t" + method + "\t|\t" + "Status FLAG is set to | " + Flag, mySession);
				}else {
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "Enrolled vb is false" , mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
							className + "\t|\t" + method + "\t|\t" + "VB Enrollment status is  | " + customerDetails.get(AppConstants.VB_enrollment_status).toString(), mySession);					
					
					if(mySession.getVariableField("enrollreject").getBooleanValue()) {
						Flag = "REJECT";
					}else if(mySession.getVariableField("enrollfailed").getBooleanValue()) {
						Flag = "FAILED";
					}else if(mySession.getVariableField("enrollmaxtries").getBooleanValue()) {
						Flag = "MAXTRIES";
					}
					
					else if("DISCONNECT".equalsIgnoreCase(customerDetails.get(AppConstants.VB_enrollment_status).toString())) 
					{
						Flag = "DISCONNECT";
					}
					else {
						Flag = AppConstants.F;//AppConstants.F;
					}
					
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
							className + "\t|\t" + method + "\t|\t" + "Status FLAG is set as | " + Flag, mySession);
				}
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "flowvalues | " + flowvalues.toString(), mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
						className + "\t|\t" + method + "\t|\t" + "Status | " + Flag, mySession);
				NodeDescription = flowvalues.get(Flag).toString().trim();

				if (NodeDescription.equalsIgnoreCase("Disconnect")) {

					NextNode = NodeDescription;

				} else {

					if (flow.containsKey(NodeDescription)) {
						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
						mySession.getVariableField("Error").setValue(AppConstants.True);

					}

				}

			
				
			} else {

				NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

				if (flow.containsKey(NodeDescription)) {
					flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
					NextNode = flowvalues.get("Type").toString();
				} else {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
							+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
					mySession.getVariableField("Error").setValue(AppConstants.True);

				}

			}

		} else {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
					className + "\t|\t" + method + "\t|\t" + "NodeDescription " + NodeDescription + " is Missing",
					mySession);
			mySession.getVariableField("Error").setValue(AppConstants.True);

		}

		MenuDescription += Flag.toUpperCase() + "|";

		CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
		mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
				mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
				className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

		mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
		mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

	}
		
		@SuppressWarnings("unchecked")
		public void getVbStatus(SCESession mySession) {

			String method = "getVbStatus";

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

			HashMap<String, Object> flow = new HashMap<String, Object>();
			HashMap<String, Object> flowvalues = new HashMap<String, Object>();
			HashMap<String, String> CallHistory = new HashMap<String, String>();

			CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

			String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

			String Flag = AppConstants.F;
			String NextNode = "";
			String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

			flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

			if (flow.containsKey(NodeDescription)) {

				flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

				if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
							className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
							mySession);

					String postCalibration = mySession.getVariableField("POST_CALIBRATION").getStringValue();
					String verifiedviavb = mySession.getVariableField("verify").getStringValue();
					String vbstatus = mySession.getVariableField("vbStatus").getStringValue();
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
							className + "\t|\t" + method + "\t|\t" + "postCalibration obtained from session as | " + postCalibration, mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
							className + "\t|\t" + method + "\t|\t" + "verifiedviavb obtained from session as | " + verifiedviavb, mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "VB Status is | " + vbstatus, mySession);
					//String NodeDescription  = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();
					//String NextNode = mySession.getVariableField("ApplicationVariable", "NextNode").getStringValue(); 
					
					if("EN".equalsIgnoreCase(vbstatus)) {
						
					if("TRUE".equalsIgnoreCase(postCalibration)) {
						if("TRUE".equalsIgnoreCase(verifiedviavb)) {
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "VErified via vb is true" , mySession);
							Flag = "VB_IDENT_POST";//AppConstants.T;
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
									className + "\t|\t" + method + "\t|\t" + "Status is set to | " + Flag, mySession);
						}else {
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "VErified via vb is false" , mySession);
							Flag = "TRANSFER";//AppConstants.F;
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
									className + "\t|\t" + method + "\t|\t" + "Status is defined as | " + Flag, mySession);
						}
					}
					else {
						if("failure".equalsIgnoreCase(verifiedviavb)) {
							Flag = "TRANSFER";//AppConstants.F;
						}else {
							Flag = "VB_IDENT_PRE";//
						}
						
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
								className + "\t|\t" + method + "\t|\t" + "Pre Calibration State and the Status is defined as | " + Flag, mySession);
					}
					
					}
					else {
						Flag = "OTHERS";//AppConstants.F;
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
								className + "\t|\t" + method + "\t|\t" + "Flag is | " + Flag, mySession);
					}
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
							className + "\t|\t" + method + "\t|\t" + "flowvalues | " + flowvalues.toString(), mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
							className + "\t|\t" + method + "\t|\t" + "Status | " + Flag, mySession);
					NodeDescription = flowvalues.get(Flag).toString().trim();

					if (NodeDescription.equalsIgnoreCase("Disconnect")) {

						NextNode = NodeDescription;

					} else {

						if (flow.containsKey(NodeDescription)) {
							flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
							NextNode = flowvalues.get("Type").toString();
						} else {

							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
									+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
							mySession.getVariableField("Error").setValue(AppConstants.True);

						}

					}

				} else {

					NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

					if (flow.containsKey(NodeDescription)) {
						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
						mySession.getVariableField("Error").setValue(AppConstants.True);

					}

				}

			} else {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
						className + "\t|\t" + method + "\t|\t" + "NodeDescription " + NodeDescription + " is Missing",
						mySession);
				mySession.getVariableField("Error").setValue(AppConstants.True);

			}

			MenuDescription += Flag.toUpperCase() + "|";

			CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
			mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
					mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
					className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

			mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
			mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

		}
		
		
		
		@SuppressWarnings("unchecked")
		public void getVbSelfServe(SCESession mySession) {

			String method = "getVbSelfServe";

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

			HashMap<String, Object> flow = new HashMap<String, Object>();
			HashMap<String, Object> flowvalues = new HashMap<String, Object>();
			HashMap<String, String> CallHistory = new HashMap<String, String>();
			CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();
			String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

			String Flag = AppConstants.F;
			String NextNode = "";
			String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

			flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

			if (flow.containsKey(NodeDescription)) {

				flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

				if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
							className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
							mySession);
					
					String vbselserve = mySession.getVariableField("vbselfserve").getStringValue();
					String vbstatus = mySession.getVariableField("vbstatus").getStringValue();
					String vbenrollstatus = mySession.getProperty("VB_ENROLL_STATUS").toString();
					String estatus = mySession.getVariableField("TrainWS","trainStatus").getStringValue();
					if("Trained".equalsIgnoreCase(estatus) || "Succeeded".equalsIgnoreCase(estatus)) {
						vbenrollstatus = "SUCCESS";
					}
					
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "vbselserve is | " + vbselserve, mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "vbstatus is | " + vbstatus, mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "vbenrollstatus is | " + vbenrollstatus, mySession);

					List<String> status = new ArrayList<String>();
					status.add("NA");
					status.add("NL");
					status.add("EN");
					String deelg = mySession.getProperty("DEELG").toString();
					String rjelg = mySession.getProperty("RJELG").toString();
					if("TRUE".equalsIgnoreCase(vbselserve)) {
						if(status.contains(vbstatus)) {
							Flag = "BAU";
						}else{
							if("DE".equalsIgnoreCase(vbstatus)) {
								
								if("YES".equalsIgnoreCase(deelg)) {
									if("SUCCESS".equalsIgnoreCase(vbenrollstatus)) {
										Flag = "ENSUCCESS";
									}else if("ENROLL_MAXTRIES".equalsIgnoreCase(vbenrollstatus)) {
										Flag = "ENROLL_MAXTRIES"; 
									}else if("CONCERN_MAXTRIES".equalsIgnoreCase(vbenrollstatus)) {
										Flag = "CONCERN_MAXTRIES"; 
									}
									else {
										if("LATER".equalsIgnoreCase(vbenrollstatus)) {
											Flag = "BAU";
										}else {
										Flag = "ENFAILURE";
										}
									}
								}
								else if("ENROLL_MAXTRIES".equalsIgnoreCase(vbenrollstatus)) {
									Flag = "ENROLL_MAXTRIES"; 
								}else if("CONCERN_MAXTRIES".equalsIgnoreCase(vbenrollstatus)) {
									Flag = "CONCERN_MAXTRIES"; 
								}
								else {
									Flag = "BAU";
								}
							}else if("RJ".equalsIgnoreCase(vbstatus)) {
								if("YES".equalsIgnoreCase(rjelg)) {
									if("SUCCESS".equalsIgnoreCase(vbenrollstatus)) {
										Flag = "ENSUCCESS";
									}else if("ENROLL_MAXTRIES".equalsIgnoreCase(vbenrollstatus)) {
										Flag = "ENROLL_MAXTRIES"; 
									}else if("CONCERN_MAXTRIES".equalsIgnoreCase(vbenrollstatus)) {
										Flag = "CONCERN_MAXTRIES"; 
									}
									else {
										if("LATER".equalsIgnoreCase(vbenrollstatus)) {
											Flag = "BAU";
										}else {
										Flag = "ENFAILURE";
										}
									}
								}else if("ENROLL_MAXTRIES".equalsIgnoreCase(vbenrollstatus)) {
									Flag = "ENROLL_MAXTRIES"; 
								}else if("CONCERN_MAXTRIES".equalsIgnoreCase(vbenrollstatus)) {
									Flag = "CONCERN_MAXTRIES"; 
								}
								else {
									Flag = "BAU";
								}
							}else if("EL".equalsIgnoreCase(vbstatus)) {
								if("SUCCESS".equalsIgnoreCase(vbenrollstatus)) {
									Flag = "ENSUCCESS";
								}else if("ENROLL_MAXTRIES".equalsIgnoreCase(vbenrollstatus)) {
									Flag = "ENROLL_MAXTRIES"; 
								}else if("CONCERN_MAXTRIES".equalsIgnoreCase(vbenrollstatus)) {
									Flag = "CONCERN_MAXTRIES"; 
								}
								else {
									if("LATER".equalsIgnoreCase(vbenrollstatus)) {
										Flag = "BAU";
									}else {
									Flag = "ENFAILURE";
									}
								}
							}else {
								Flag = "BAU";
							}
						}
						 
					}else {
						Flag = "FALSE";
					}
					
					
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
							className + "\t|\t" + method + "\t|\t" + "flowvalues | " + flowvalues.toString(), mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
							className + "\t|\t" + method + "\t|\t" + "Status | " + Flag, mySession);
					NodeDescription = flowvalues.get(Flag).toString().trim();

					if (NodeDescription.equalsIgnoreCase("Disconnect")) {

						NextNode = NodeDescription;

					} else {

						if (flow.containsKey(NodeDescription)) {
							flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
							NextNode = flowvalues.get("Type").toString();
						} else {

							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
									+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
							mySession.getVariableField("Error").setValue(AppConstants.True);

						}

					}

				} else {

					NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

					if (flow.containsKey(NodeDescription)) {
						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
						mySession.getVariableField("Error").setValue(AppConstants.True);

					}

				}

			} else {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
						className + "\t|\t" + method + "\t|\t" + "NodeDescription " + NodeDescription + " is Missing",
						mySession);
				mySession.getVariableField("Error").setValue(AppConstants.True);

			}

			MenuDescription += Flag.toUpperCase() + "|";

			CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
			mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
					mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
					className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

			mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
			mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

		}
		
		
		public void VBHistory(SCESession mySession) {
			String method = "VBHistory";
			try 
			{

		
			LinkedHashMap<String,Object> inputUpperCase = new LinkedHashMap<>();
			
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

			DBConnection db = new DBConnection();
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "db connection "+db.toString(), mySession);
			String IsVBHistoryInserted = "";

			VBHist VBHistory = new VBHist();
			LinkedHashMap<String, Object> input = new LinkedHashMap<String, Object>();

			boolean DBException = mySession.getVariableField("ApplicationVariable", "DbException").getBooleanValue();

			// int errorCount = mySession.getVariableField("ErrorCount").getIntValue();
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "input obtained from IVR : "
			+mySession.getProperty("vbmap").toString(), mySession);
			VBHistory =  (VBHist) mySession.getProperty("vbmap");

			input.put("VC_UCID", VBHistory.getVC_UCID());
			input.put("VC_FFP_NUMBER", VBHistory.getVC_FFP_NUMBER());
			input.put("VC_WORK_REQUEST_ID", VBHistory.getVC_WORK_REQUEST_ID());
			input.put("VC_IDENTIFY_TYPE", VBHistory.getVC_IDENTIFY_TYPE());
			input.put("VC_SEGMENT", VBHistory.getVC_SEGMENT());
			input.put("VC_CHANNEL", VBHistory.getVC_CHANNEL());
			input.put("VC_STATUS", VBHistory.getVC_STATUS());
			input.put("DT_STATUS_UPDATE_DATE", VBHistory.getDT_STATUS_UPDATE_DATE());
			input.put("VC_DE_ENROLL_REASON", VBHistory.getVC_DE_ENROLL_REASON());
			input.put("VC_TRANSFERRED_BY_AGENT_ID", VBHistory.getVC_TRANSFERRED_BY_AGENT_ID());
			input.put("VC_TRANSFERRED_BY_AGENT_ROLE", VBHistory.getVC_TRANSFERRED_BY_AGENT_ROLE());
			input.put("VC_DE_ENROLLED_AGENT_ID", VBHistory.getVC_DE_ENROLLED_AGENT_ID());
			input.put("VC_DE_ENROLLED_AGENT_ROLE", VBHistory.getVC_DE_ENROLLED_AGENT_ROLE());
			input.put("VC_FUNCTION_NAME", VBHistory.getVC_FUNCTION_NAME());
			input.put("VC_VB_REASON", VBHistory.getVC_VB_REASON());
			
			
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t completed processing the input ",mySession);
			
			mySession.getVariableField("IsVBHistoryInserted").setValue(AppConstants.F);
			
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG,
					className + "\t|\t" + method + "\t|\t" + "input saved"+input.toString(), mySession);

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG,
					className + "\t|\t" + method + "\t|\t" + "DBEXCEPTION to be checked : "+DBException, mySession);

			try {
				
					if (DBException) {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG,
								className + "\t|\t" + method + "\t|\t" + "DataBase is Un available due to the DBException is true at first if condition", mySession);

						IsVBHistoryInserted = mySession.getVariableField("IsVBHistoryInserted").getStringValue();
						if (IsVBHistoryInserted.equalsIgnoreCase(AppConstants.F)) {
							FlatFile ch = new FlatFile();
							
							for (Map.Entry<String, Object> entry : input.entrySet()) {
					            String key = entry.getKey();
					            Object value = entry.getValue();
					            if(key.contains("date")) {
					            	if("NA".equalsIgnoreCase(value.toString())) {
					            		value=null;
					            	}
					            }
					            inputUpperCase.put(key.toUpperCase(), value);
					            // Perform actions with key and value
					           //System.out.println("Key: " + key + ", Value: " + value);

					            // If you need to perform specific actions based on the type of value
					            if (value instanceof String) {
					                String stringValue = (String) value;
					                System.out.println("String value: " + stringValue);
					            } else {
					                System.out.println("Non-String value: " + value);
					            }
					        }
							
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG, className +"\t|\t"+ method +"\t|\tinputUpperCase passed as input to flatfile : "+inputUpperCase.toString(), mySession);
							
							
							String InsertQuery = "INSERT INTO IVR_VB_STATUS_HISTORY ";
							ch.writeToFile(InsertQuery, "VBHistory", inputUpperCase, mySession);
							mySession.getVariableField("IsVBHistoryInserted").setValue(AppConstants.T);
						} else {
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
									className + "\t|\t" + method + "\t|\t" + "VB HISTORY ALREADY INSERTED in if line 5449 | SUCCESS",mySession);
						}
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "DB Exception While inserting VB History | " + DBException, mySession);

					} else {
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
								className + "\t|\t" + method + "\t|\t" + "DBConnection to be called",mySession);
						db.getInsertValues("SP_WIDGET_INSERT_IVR_VB_STATUS_HISTORY", input, mySession);
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
								className + "\t|\t" + method + "\t|\t" + "DBConnection called",mySession);
					
						mySession.getVariableField("IsVBHistoryInserted").setValue(AppConstants.T);

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
								className + "\t|\t" + method + "\t|\t" + "IsVBHistoryInserted | " +
						mySession.getVariableField("IsVBHistoryInserted").getStringValue(),mySession);
						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
								className + "\t|\t" + method + "\t|\t" + "INSERT VB HISTORY | SUCCESS", mySession);

						DBException = mySession.getVariableField("ApplicationVariable", "DbException").getBooleanValue();

						if (DBException) {

							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG,
									className + "\t|\t" + method + "\t|\t" + "DataBase is Un available", mySession);

							IsVBHistoryInserted = mySession.getVariableField("IsVBHistoryInserted").getStringValue();
							if (IsVBHistoryInserted.equalsIgnoreCase(AppConstants.F)) {
								FlatFile ch = new FlatFile();
//								String InsertQuery = "INSERT INTO IVR_CALL_HISTORY ";
//								ch.writeToFile(InsertQuery, "CallHistory", input, mySession);
//								mySession.getVariableField("IsVBHistoryInserted")
//								.setValue(AppConstants.T);
								
								for (Map.Entry<String, Object> entry : input.entrySet()) {
						            String key = entry.getKey();
						            Object value = entry.getValue();
						            if(key.contains("date")) {
						            	if("NA".equalsIgnoreCase(value.toString())) {
						            		value=null;
						            	}
						            }
						            inputUpperCase.put(key.toUpperCase(), value);
						            // Perform actions with key and value
						           //System.out.println("Key: " + key + ", Value: " + value);

						            // If you need to perform specific actions based on the type of value
						            if (value instanceof String) {
						                String stringValue = (String) value;
						                System.out.println("String value: " + stringValue);
						            } else {
						                System.out.println("Non-String value: " + value);
						            }
						        }
								
								TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG, className +"\t|\t"+ method +"\t|\tinputUpperCase passed as input to flatfile : "+inputUpperCase.toString(), mySession);
								
								
								String InsertQuery = "INSERT INTO IVR_VB_STATUS_HISTORY ";
								ch.writeToFile(InsertQuery, "VBHistory", inputUpperCase, mySession);
								mySession.getVariableField("IsVBHistoryInserted").setValue(AppConstants.T);
							} else {
								TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
										className + "\t|\t" + method + "\t|\t" + "VB HISTORY ALREADY INSERTED in else line 5486 | SUCCESS",
										mySession);
							}
							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
									+ "DB Exception While inserting VB History | " + DBException, mySession);

						}

					}

				

			} catch (Exception e) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG,
						className + "\t|\t" + method + "\t|\t" + "DataBase is Un available due to Exception in DataNode ", mySession);

				IsVBHistoryInserted = mySession.getVariableField("IsVBHistoryInserted").getStringValue();
				if (IsVBHistoryInserted.equalsIgnoreCase(AppConstants.F)) {
					FlatFile ch = new FlatFile();
					for (Map.Entry<String, Object> entry : input.entrySet()) {
			            String key = entry.getKey();
			            Object value = entry.getValue();
			            if(key.contains("date")) {
			            	if("NA".equalsIgnoreCase(value.toString())) {
			            		value=null;
			            	}
			            }
			            inputUpperCase.put(key.toUpperCase(), value);
			            // Perform actions with key and value
			           //System.out.println("Key: " + key + ", Value: " + value);

			            // If you need to perform specific actions based on the type of value
			            if (value instanceof String) {
			                String stringValue = (String) value;
			                System.out.println("String value: " + stringValue);
			            } else {
			                System.out.println("Non-String value: " + value);
			            }
			        }
					
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG, className +"\t|\t"+ method +"\t|\tinputUpperCase passed as input to flatfile : "+inputUpperCase.toString(), mySession);
					
					
					String InsertQuery = "INSERT INTO IVR_VB_STATUS_HISTORY ";
					ch.writeToFile(InsertQuery, "VBHistory", inputUpperCase, mySession);
					mySession.getVariableField("IsVBHistoryInserted").setValue(AppConstants.T);
				} else {
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
							className + "\t|\t" + method + "\t|\t" + "VB HISTORY ALREADY INSERTED | SUCCESS", mySession);
				}
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
						+ "DB Exception While inserting Vb History | " + DBException, mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
						+ AppConstants.EXCEPTION_1 + e.getMessage() + AppConstants.EXCEPTION_2, mySession);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
						className + "\t|\t" + method + "\t|\t" + Arrays.toString(e.getStackTrace()), mySession);

			}


			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

			}
			catch (Exception e) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_DEBUG,
						className + "\t|\t" + method + "\t|\t" + "vb history not obtained ", mySession);

			}
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
		public JSONObject startConnect(SCESession mySession) 
		{
			JSONObject xmlJSONObj = null;
			String method = mySession.getVariableField("nuancemethod").getStringValue();
			try {
//			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "username : "+ LoadApplicationProperties.getProperty("NUANCE_WINDOWS_SERVER_USERNAME", mySession).toString(),mySession);
//			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "password : "+ LoadApplicationProperties.getProperty("NUANCE_WINDOWS_SERVER_PASSWORD", mySession).toString(),mySession);
//			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "domain : "+ LoadApplicationProperties.getProperty("NUANCE_WINDOWS_SERVER_DOMAIN", mySession).toString(),mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "URL : "+ mySession.getVariableField("vbInputs","wsURL").getStringValue(),mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "namespace : "+ LoadApplicationProperties.getProperty("NUANCE_WSNAMESPACE", mySession).toString(),mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "CONFIGSETNAME : "+ LoadApplicationProperties.getProperty("CONFIGSETNAME", mySession).toString(),mySession);
			
			HashMap<String, Object> map = (HashMap<String, Object>) mySession.getProperty("map");
			String className = "NuanceConnector";
			String StartDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
			String EndDate = "";
			mySession.setProperty("xmlJSONObj"+method, "NA"); 
			String endPointUlr= mySession.getVariableField("vbInputs","wsURL").getStringValue();
			DataNode dataNode = new DataNode();
			mySession.setProperty("xmlJSONObj"+method, null);
			String requestXML = "";
				String namespace = LoadApplicationProperties.getProperty("NUANCE_WSNAMESPACE", mySession).toString();//GlobalConstant.NUANCE_WSNAMESPACE;
				Map.Entry pair = null;
				SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				SOAPMessage soapResponse = null;
				SOAPMessage message = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createMessage();
				SOAPPart soapPart = message.getSOAPPart();
				SOAPEnvelope envelope = soapPart.getEnvelope();
				envelope.addNamespaceDeclaration("web", namespace);
				MimeHeaders headers = message.getMimeHeaders();
				headers.removeAllHeaders();
				headers.addHeader("SOAPAction", namespace + method);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "SOAPAction"+ namespace + method, mySession);
				SOAPBody body = envelope.getBody();
				SOAPElement soapBodyElem = body.addChildElement(method, "web");
				Iterator it2 = map.entrySet().iterator();
				while (it2.hasNext()) {
					pair = (Map.Entry) it2.next();
					soapBodyElem.addChildElement(pair.getKey() + "", "web").addTextNode(pair.getValue() + "");
				}

				message.saveChanges();

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				message.writeTo(out);
				requestXML = new String(out.toByteArray());
				//System.out.println("RequestXML: "+requestXML);
				//TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "RequestXML: "+requestXML, mySession);
			
			//CloseableHttpClient httpClient = HttpClients.custom().build();
			CloseableHttpClient httpClient = null;
			String jks = LoadApplicationProperties.getProperty("WITHJKS", mySession).toString();
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "load with jks : "+jks, mySession);
				httpClient = getHttpClientWithoutSslValidationWithNTLMJKS(mySession);
			
			
			
			HttpPost httpPost = new HttpPost(endPointUlr);
			httpPost.addHeader("User-Agent", "Apache-HttpClient/4.1.1");
			httpPost.addHeader("Content-Type", "application/soap+xml; charset=utf-8");
			
			httpPost.setEntity(new StringEntity(requestXML));
			
			CloseableHttpResponse httpResponse = httpClient.execute(httpPost);

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "POST Response Status: "+ httpResponse.getStatusLine().getStatusCode(), mySession);
			mySession.setProperty("RESPONSE_CODE",httpResponse.getStatusLine().getStatusCode());
			mySession.getVariableField("responseCode").setValue(httpResponse.getStatusLine().getStatusCode());
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					httpResponse.getEntity().getContent()));

			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = reader.readLine()) != null) {
				response.append(inputLine);
			}
			reader.close();

			// print result
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "ResponseXML: "+response.toString(), mySession);
			EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
			httpClient.close();
			
			int PRETTY_PRINT_INDENT_FACTOR = 4;
			
	        String jsonPrettyPrintString = "";

			if("200".equalsIgnoreCase(mySession.getVariableField("responseCode").getStringValue())){
				xmlJSONObj = XML.toJSONObject(response.toString());
		        jsonPrettyPrintString = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
		        TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "Response XML Converted into JSON :" +jsonPrettyPrintString, mySession);
			}else {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, "Invalid response ", mySession);
			}
	        //inserting Transaction history
	      

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "InsertingTransactionHistory", mySession);

				LinkedHashMap<String,Object> TransactionHistory = new LinkedHashMap<String,Object>();

				TransactionHistory = (LinkedHashMap<String, Object>) mySession.getVariableField("HashMap", "TransactionHistory").getObjectValue();

				if(EndDate.equalsIgnoreCase("")) {

					EndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

				}

				TransactionHistory.put("DT_START_DATE", StartDate);
				TransactionHistory.put("DT_END_DATE", EndDate);
				TransactionHistory.put("VC_FUNCTION_NAME", method);
				TransactionHistory.put("VC_HOST_URL", (endPointUlr+method));
				if(requestXML.contains("web:audio")) {
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+ method +"\t|\t"+ "contains web:audio and it is masked", mySession);
					TransactionHistory.put("VC_HOST_REQUEST", maskTagValue(requestXML, "web:audio", "***"));
				}else {
					TransactionHistory.put("VC_HOST_REQUEST", requestXML);	
				}
				if("200".equalsIgnoreCase(mySession.getVariableField("responseCode").getStringValue())){
					TransactionHistory.put("VC_HOST_RESPONSE", xmlJSONObj);
					TransactionHistory.put("VC_TRANS_STATUS", httpResponse.getStatusLine().getStatusCode());
				}else {
					TransactionHistory.put("VC_HOST_RESPONSE", "");
					TransactionHistory.put("VC_TRANS_STATUS", "0");
				}
				
				mySession.getVariableField("HashMap", "TransactionHistory").setValue(TransactionHistory);
				dataNode.setTransactionHistory(mySession);
				
				mySession.getVariableField("ApplicationVariable", "DbException").setValue(false);

			} catch(Exception e) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className+"\t|\t"+method+"\t|\t"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
				

			}
	        
	        mySession.setProperty("xmlJSONObj"+method, xmlJSONObj);
	        if(xmlJSONObj!=null) {
	        	TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+"\t|\t"+method+"\t|\t values set in property "+mySession.getProperty("xmlJSONObj"+method).toString() , mySession);
	        }else {
	        	TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className+"\t|\t"+method+"\t|\t null values is set in property xmlJSONObj", mySession);
	        }
	        
			return xmlJSONObj;
		}
		
		public CloseableHttpClient getHttpClientWithoutSslValidationWithNTLMJKS(SCESession mySession) {

			CloseableHttpClient httpclient = null;
			try {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+"Secure Conection",mySession);
				AES aes = new AES();
				int socketTimedout = Integer.parseInt(LoadApplicationProperties.getProperty("nuance-socketTimedout", mySession));
				int connectTimedout = Integer.parseInt(LoadApplicationProperties.getProperty("nuance-connectTimedout", mySession));
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+"\nsocketTimedout :"+socketTimedout+"\nconnectTimedout :"+connectTimedout,mySession);
				SSLContextBuilder builder = new SSLContextBuilder();

				RequestConfig requestConfig = RequestConfig.custom()
						.setSocketTimeout(socketTimedout*1000)
						.setConnectTimeout(connectTimedout*1000)
						.setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM))
						.build();

				CredentialsProvider credsProvider = new BasicCredentialsProvider();
				credsProvider.setCredentials(AuthScope.ANY, new NTCredentials(LoadApplicationProperties.getProperty("NUANCE_WINDOWS_SERVER_USERNAME", mySession).toString(),
						new CustomAESEncryption().decrypt(LoadApplicationProperties.getProperty("NUANCE_WINDOWS_SERVER_PASSWORD", mySession).toString(),AppConstants.SECRET_KEY), 
						InetAddress.getLocalHost().getHostName(), 
						LoadApplicationProperties.getProperty("NUANCE_WINDOWS_SERVER_DOMAIN", mySession).toString()));

				builder.loadTrustMaterial(null, new TrustStrategy() {
					@Override
					public boolean isTrusted(X509Certificate[] chain, String authType) {
						return true;
					}
				});

				HttpClientBuilder clientbuilder = HttpClients.custom();
				String endPointUlr= mySession.getVariableField("vbInputs","wsURL").getStringValue();
				URL endPointUrl = new URL(endPointUlr);
				if ("https".equals(endPointUrl.getProtocol())) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO," URL is HTTPS ", mySession);

					String fileName = LoadApplicationProperties.getProperty("JksPath", mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className +"\t|\t"+"Secure Conection",mySession);
					SSLContextBuilder SSLBuilder = SSLContexts.custom();
					File file = new File(new File(fileName).getCanonicalPath());
					SSLBuilder = SSLBuilder.loadTrustMaterial(file, (aes.decrypt(LoadApplicationProperties.getProperty("JksPassword", mySession), mySession)).toCharArray());
					SSLContext sslcontext = SSLBuilder.build();
					SSLConnectionSocketFactory sslConSocFactory = new SSLConnectionSocketFactory(sslcontext, new NoopHostnameVerifier());
					clientbuilder = clientbuilder.setSSLSocketFactory(sslConSocFactory);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO," HTTPS Connection Success ", mySession);
				}
				httpclient = clientbuilder.setDefaultCredentialsProvider(credsProvider)
						.setDefaultRequestConfig(requestConfig)
						.build();
			} catch(Exception e) {
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR," HTTPS Connection failed", mySession);
			}
			return httpclient;
		}


		
		@SuppressWarnings("unchecked")
		public void checkConnectBack(SCESession mySession) {

			String method = "checkConnectBack";

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "Start", mySession);

			HashMap<String, Object> flow = new HashMap<String, Object>();
			HashMap<String, Object> flowvalues = new HashMap<String, Object>();
			HashMap<String, String> CallHistory = new HashMap<String, String>();

			CallHistory = (HashMap<String, String>) mySession.getVariableField("HashMap", "CallHistory").getObjectValue();

			String MenuDescription = CallHistory.get("VC_MENU_DESCRIPTION");

			String Flag = AppConstants.F;
			String NextNode = "";
			String NodeDescription = mySession.getVariableField("ApplicationVariable", "NodeDescription").getStringValue();

			flow = (HashMap<String, Object>) mySession.getVariableField("HashMap", "Flow").getObjectValue();

			if (flow.containsKey(NodeDescription)) {

				flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);

				if (flowvalues.get("IsEnabled").toString().equalsIgnoreCase(AppConstants.T)) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
							className + "\t|\t" + method + "\t|\t" + "Current NodeDescription " + NodeDescription,
							mySession);

					String callType = mySession.getVariableField("connectback").getStringValue();
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "checkConnectBack is true or false  | " + callType, mySession);

					if(AppConstants.T.equalsIgnoreCase(callType)) {
						Flag = AppConstants.T;
					}else if(AppConstants.T.equalsIgnoreCase(callType)) {
						Flag = AppConstants.F;
					}else {
						Flag = AppConstants.F;
					}
					
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
							className + "\t|\t" + method + "\t|\t" + "flowvalues | " + flowvalues.toString(), mySession);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
							className + "\t|\t" + method + "\t|\t" + "Flag set as  | " + Flag, mySession);
					NodeDescription = flowvalues.get(Flag).toString().trim();

					if (NodeDescription.equalsIgnoreCase("Disconnect")) {

						NextNode = NodeDescription;

					} else {

						if (flow.containsKey(NodeDescription)) {
							flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
							NextNode = flowvalues.get("Type").toString();
						} else {

							TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
									+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
							mySession.getVariableField("Error").setValue(AppConstants.True);

						}

					}

				} else {

					NodeDescription = flowvalues.get("NextNodeIfDisabled").toString();

					if (flow.containsKey(NodeDescription)) {
						flowvalues = (HashMap<String, Object>) flow.get(NodeDescription);
						NextNode = flowvalues.get("Type").toString();
					} else {

						TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, className + "\t|\t" + method + "\t|\t"
								+ "Next NodeDescription " + NodeDescription + " is Missing", mySession);
						mySession.getVariableField("Error").setValue(AppConstants.True);

					}

				}

			} else {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,
						className + "\t|\t" + method + "\t|\t" + "NodeDescription " + NodeDescription + " is Missing",
						mySession);
				mySession.getVariableField("Error").setValue(AppConstants.True);

			}

			MenuDescription += Flag.toUpperCase() + "|";

			CallHistory.put("VC_MENU_DESCRIPTION", MenuDescription);
			mySession.getVariableField("HashMap", "CallHistory").setValue(CallHistory);

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "NextNode | " + NextNode,
					mySession);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,
					className + "\t|\t" + method + "\t|\t" + "NextNode Description | " + NodeDescription, mySession);

			mySession.getVariableField("ApplicationVariable", "NodeDescription").setValue(NodeDescription);
			mySession.getVariableField("ApplicationVariable", "NextNode").setValue(NextNode);

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO, className + "\t|\t" + method + "\t|\t" + "End", mySession);

		}
	
		
		public static String maskTagValue(String xmlString, String tagName, String mask) {
	        try {
	            // Parse the XML string
	            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	            DocumentBuilder builder = factory.newDocumentBuilder();
	            InputStream inputStream = new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8));
	            Document document = builder.parse(inputStream);

	            // Find and mask the value of the specified tag
	            NodeList nodeList = document.getElementsByTagName(tagName);
	            for (int i = 0; i < nodeList.getLength(); i++) {
	                Element element = (Element) nodeList.item(i);
	                // Set the text content of the element to the mask
	                element.setTextContent(mask);
	            }

	            // Serialize the modified document back to a string
	            TransformerFactory transformerFactory = TransformerFactory.newInstance();
	            Transformer transformer = transformerFactory.newTransformer();
	            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	            transformer.transform(new DOMSource(document), new StreamResult(outputStream));
	            return outputStream.toString(StandardCharsets.UTF_8.name());
	        } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
	            e.printStackTrace();
	            return null;
	        }
	    }

}
