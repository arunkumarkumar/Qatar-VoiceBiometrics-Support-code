package com.DBOperation;

import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.General.AppConstants;
import com.General.LoadApplicationProperties;
import com.avaya.sce.runtime.tracking.TraceInfo;
import com.avaya.sce.runtimecommon.ITraceInfo;
import com.avaya.sce.runtimecommon.SCESession;

public class AES {

	public String decrypt(String strToDecrypt, SCESession mySession) {

		String SC_RT_YEK = null;
		String SA_LT = null;
		String SK = null;
		String C_IP_IN = null;
		String A_E_S = null;

		try {

			int T_LEN = 128;

			SC_RT_YEK = LoadApplicationProperties.getProperty("SC_RT_YEK", mySession);
			SA_LT = LoadApplicationProperties.getProperty("SA_LT", mySession);
			SK = LoadApplicationProperties.getProperty("SK", mySession);
			C_IP_IN = LoadApplicationProperties.getProperty("C_IP_IN", mySession);
			A_E_S = LoadApplicationProperties.getProperty("A_E_S", mySession);

			AES aesEncryption = new AES();

			byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
			GCMParameterSpec ivspec = new GCMParameterSpec(T_LEN, iv);
			SecretKeyFactory factory = SecretKeyFactory.getInstance(aesEncryption.dec(SK, mySession));
			KeySpec spec = new PBEKeySpec(aesEncryption.dec(SC_RT_YEK, mySession).toCharArray(), aesEncryption.dec(SA_LT, mySession).getBytes(), 65536, 256);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), aesEncryption.dec(A_E_S, mySession));

			Cipher cipher = Cipher.getInstance(aesEncryption.dec(C_IP_IN, mySession));
			cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
			return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));

		} catch (Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, "AES\t|\tdecrypt"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);
			return "NA";

		}

	}

	public String dec(String value, SCESession mySession) {

		try {

			byte[] decBytes = Base64.getDecoder().decode(value);
			String deString = new String(decBytes);
			return deString;

		} catch(Exception e) {

			TraceInfo.trace(ITraceInfo.TRACE_LEVEL_ERROR, "AES\t|\tdecrypt"+ AppConstants.EXCEPTION_1+e.getMessage()+AppConstants.EXCEPTION_2, mySession);

		}

		return null;
	}

}


