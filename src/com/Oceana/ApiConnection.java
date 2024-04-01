package com.Oceana;

//import java.io.File;
import java.io.*;
import java.net.URL;
import java.util.HashMap;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
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

public class ApiConnection {

	public HashMap<String, Object> httpGetResponse(String requestFrom, String URL, int connectionTimeout, int readTimeout,SCESession mySession) {

		AES aes = new AES();
		CloseableHttpClient httpclient = null;;
		CloseableHttpResponse response = null;
		int responseCode = 0;
		String Response = "";
		String sanitizedJson = "";
		HashMap<String,Object> responseValues = null;

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"URL : "+ URL, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"connectionTimeout : "+ connectionTimeout, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"readTimeout : "+ readTimeout, mySession);
		
		try {

			URL endPointUrl = new URL(URL);
			HttpGet httpGet = new HttpGet(URL);
			httpGet.setHeader("Content-Type","application/json");
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
			response = httpclient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			responseCode = response.getStatusLine().getStatusCode();
			Response = EntityUtils.toString(entity);
			sanitizedJson = JsonSanitizer.sanitize(Response);

			responseValues = new HashMap<String,Object>();
			responseValues.put(requestFrom+"Code", responseCode);
			responseValues.put(requestFrom+"Response", sanitizedJson);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO," Response Received ", mySession);
			
		} catch(Exception e) {
			
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR,"Exception :"+e, mySession);
			
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
		
		return responseValues;
		
	}
	
	public HashMap<String, Object> httpPostResponse(String requestFrom, String URL, int connectionTimeout, int readTimeout, String Request,SCESession mySession) {

		AES aes = new AES();
		CloseableHttpClient httpclient = null;;
		CloseableHttpResponse response = null;
		int responseCode = 0;
		String Response = "";
		String sanitizedJson = "";
		HashMap<String,Object> responseValues = null;

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"URL : "+ URL, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"connectionTimeout : "+ connectionTimeout, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"readTimeout : "+ readTimeout, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"Request : "+ Request, mySession);
		
		try {

			URL endPointUrl = new URL(URL);
			StringEntity stringEntity = new StringEntity(Request);
			HttpPost httpPost = new HttpPost(URL);
			httpPost.setHeader("Content-Type","application/json");
			httpPost.setEntity(stringEntity);
			RequestConfig config = RequestConfig.custom()
					.setConnectionRequestTimeout(connectionTimeout*1000)
					.setConnectTimeout(readTimeout*1000)
					.setSocketTimeout(connectionTimeout*1000)
					.build();
			HttpClientBuilder clientbuilder = HttpClients.custom();

			if ("https".equals(endPointUrl.getProtocol())) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO," URL is HTTPS ", mySession);
				String jksPath = LoadApplicationProperties.getProperty("JksPath", mySession);
				String jksPassword = aes.decrypt(LoadApplicationProperties.getProperty("JksPassword", mySession), mySession);

				SSLContextBuilder SSLBuilder = SSLContexts.custom();
				File file = new File(new File(jksPath).getCanonicalPath());
				SSLBuilder = SSLBuilder.loadTrustMaterial(file, jksPassword.toCharArray());
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
			
			responseValues = new HashMap<String,Object>();
			responseValues.put(requestFrom+"Code", responseCode);
			responseValues.put(requestFrom+"Response", sanitizedJson);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO," Response Received ", mySession);
			
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
		
		return responseValues;
		
	}
	
	public HashMap<String, Object> httpPutResponse(String requestFrom, String URL, int connectionTimeout, int readTimeout, String Request,SCESession mySession) {

		AES aes = new AES();
		CloseableHttpClient httpclient = null;;
		CloseableHttpResponse response = null;
		int responseCode = 0;
		String Response = "";
		String sanitizedJson = "";
		HashMap<String,Object> responseValues = null;

		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"URL : "+ URL, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"connectionTimeout : "+ connectionTimeout, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"readTimeout : "+ readTimeout, mySession);
		TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO,"Request : "+ Request, mySession);
		
		try {

			URL endPointUrl = new URL(URL);
			StringEntity stringEntity = new StringEntity(Request);
			HttpPut httpPost = new HttpPut(URL);
			httpPost.setHeader("Content-Type","application/json");
			httpPost.setEntity(stringEntity);
			RequestConfig config = RequestConfig.custom()
					.setConnectionRequestTimeout(connectionTimeout*1000)
					.setConnectTimeout(readTimeout*1000)
					.setSocketTimeout(connectionTimeout*1000)
					.build();
			HttpClientBuilder clientbuilder = HttpClients.custom();

			if ("https".equals(endPointUrl.getProtocol())) {

				TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO," URL is HTTPS ", mySession);
				String jksPath = LoadApplicationProperties.getProperty("JksPath", mySession);
				String jksPassword = aes.decrypt(LoadApplicationProperties.getProperty("JksPassword", mySession), mySession);

				SSLContextBuilder SSLBuilder = SSLContexts.custom();
				File file = new File(new File(jksPath).getCanonicalPath());
				SSLBuilder = SSLBuilder.loadTrustMaterial(file, jksPassword.toCharArray());
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
			
			responseValues = new HashMap<String,Object>();
			responseValues.put(requestFrom+"Code", responseCode);
			responseValues.put(requestFrom+"Response", sanitizedJson);
			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_INFO," Response Received ", mySession);
			
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
		
		return responseValues;
		
	}

}


