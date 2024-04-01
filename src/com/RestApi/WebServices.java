package com.RestApi;

import java.io.*;
//import java.io.File;
import java.net.URL;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import com.DBOperation.AES;
import com.General.LoadApplicationProperties;
import com.avaya.sce.runtime.tracking.TraceInfo;
import com.avaya.sce.runtimecommon.ITraceInfo;
import com.avaya.sce.runtimecommon.SCESession;
import com.google.json.JsonSanitizer;

public class WebServices {

	String CLASS_NAME = "WebServices";

	public String Webservice(SCESession mySession, String URL ,String requestFrom, String Request, int connectionTimeout, int readTimeout,String isAuthentication) {

		AES aes = new AES();
		CloseableHttpClient httpclient = null;;
		CloseableHttpResponse response = null;
		int responseCode = 0;
		String Response = null;
		String sanitizedJson = null;
		boolean isAuthFail = false;

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"URL : "+ URL, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"connectionTimeout : "+ connectionTimeout, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"readTimeout : "+ readTimeout, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"Request : "+ Request, mySession);
		
		try {

			URL endPointUrl = new URL(URL);
			StringEntity stringEntity = new StringEntity(Request);
			
			HttpPost httpPost = new HttpPost(URL);
			httpPost.setHeader("Content-Type","application/json");
			
			if(isAuthentication.equalsIgnoreCase("true")) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO," Token Authentication ", mySession);
				String Authorization = "bearer "+mySession.getVariableField("authToken").getStringValue();
				String  qid = mySession.getVariableField("qid").getStringValue();
				if(Authorization.isEmpty()) {
					isAuthFail = true;
				}
				httpPost.setHeader("Authorization", Authorization);
				httpPost.setHeader("qid", qid);
				
				if(URL.contains("sendMail")){
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"URL contains sendMail so ucid is added as requestId", mySession);
					httpPost.setHeader("requestId", mySession.getVariableField("session", "ucid").getStringValue());
				}
				else {
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"URL does not contains sendMail", mySession);
				}
				
			}
			httpPost.setEntity(stringEntity);
			
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO," Token Authentication Status | "+isAuthFail, mySession);
			
			if(!isAuthFail) {

				RequestConfig config = RequestConfig.custom()
						.setConnectionRequestTimeout(connectionTimeout*1000)
						.setConnectTimeout(readTimeout*1000)
						.setSocketTimeout(connectionTimeout*1000)
						.build();
				HttpClientBuilder clientbuilder = HttpClients.custom();

				if ("https".equals(endPointUrl.getProtocol())) {

					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO," URL is HTTPS ", mySession);

					SSLContextBuilder SSLBuilder = SSLContexts.custom();
					File file = new File(new File(LoadApplicationProperties.getProperty("JksPath", mySession)).getCanonicalPath());
					SSLBuilder = SSLBuilder.loadTrustMaterial(file, (aes.decrypt(LoadApplicationProperties.getProperty("JksPassword", mySession), mySession)).toCharArray());
					SSLContext sslcontext = SSLBuilder.build();
					SSLConnectionSocketFactory sslConSocFactory = new SSLConnectionSocketFactory(sslcontext, new NoopHostnameVerifier());
					clientbuilder = clientbuilder.setSSLSocketFactory(sslConSocFactory);
					TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO," HTTPS Connection Success ", mySession);

				}
				httpclient = clientbuilder.setDefaultRequestConfig(config).build();
				response = httpclient.execute(httpPost);

				HttpEntity entity = response.getEntity();
				responseCode = response.getStatusLine().getStatusCode();
				Response = EntityUtils.toString(entity);
				sanitizedJson = JsonSanitizer.sanitize(Response);
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO," Response Received |"+sanitizedJson, mySession);
				
			} else {
				sanitizedJson = null;
			}
			
			sanitizedJson="{\r\n"
					+ "\"ResponseCode\":\""+responseCode+"\",\r\n"
					+ "\"Response\":"+sanitizedJson+"\r\n"
					+ "}";
			
		} catch(Exception e) {
			
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR," Exception | "+e.getMessage(), mySession);
			
		} finally {

			try {
				
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"Closing httpclient", mySession);
				if(httpclient != null) {
					httpclient.close();
				}
				
				if(response != null) {
					response.close();
				}

			} catch (Exception e) {
				
				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,"Exception :"+e, mySession);
				
			}

		}
		
		return sanitizedJson;
		
	}
	
	
	
	

}