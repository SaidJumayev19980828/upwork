package com.nasnav.service.sendpulse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.*;
import java.util.Map.Entry;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.ailis.pherialize.*;

import lombok.extern.slf4j.Slf4j;

// rest of the methods removed as they are unneeded and un tested
// can be found at in https://github.com/sendpulse/sendpulse-rest-api-java
@Slf4j
public class Sendpulse implements SendpulseInterface {
	private static String apiUrl = "https://api.sendpulse.com";
	private String userId = null;
	private String secret = null;
	private String tokenName = null;
	private int refreshToken = 0;

	public Sendpulse(String _userId, String _secret) {
		if (_userId == null || _secret == null) {
			log.debug("Empty ID or SECRET");
		}
		this.userId = _userId;
		this.secret = _secret;
		try {
			this.tokenName = md5(this.userId + "::" + this.secret);
		} catch (NoSuchAlgorithmException e) {
		} catch (UnsupportedEncodingException e) {
		}
		if (this.tokenName != null) {
			if (!this.getToken()) {
				log.error("Could not connect to api, check your ID and SECRET");
			}
		}
	}

	public String md5(String param) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		StringBuilder hexString = new StringBuilder();
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] thedigest = md.digest(param.getBytes());
			for (int i = 0; i < thedigest.length; i++) {
				hexString.append(Integer.toString((thedigest[i] & 0xff) + 0x100, 16).substring(1));
			}
		} catch (NoSuchAlgorithmException e) {
			return e.toString();
		}
		return hexString.toString();
	}

	private boolean getToken() {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("grant_type", "client_credentials");
		data.put("client_id", this.userId);
		data.put("client_secret", this.secret);
		Map<String, Object> requestResult = null;
		try {
			requestResult = this.sendRequest("oauth/access_token", "POST", data, false);
		} catch (IOException e) {
		}
		if (requestResult == null)
			return false;
		if (Integer.parseInt(requestResult.get("http_code").toString()) != 200) {
			return false;
		}
		this.refreshToken = 0;
		JSONObject jdata = (JSONObject) requestResult.get("data");
		if (jdata instanceof JSONObject) {
			this.tokenName = jdata.get("access_token").toString();
		}
		return true;
	}

	private StringBuilder makePostDataParamsString(Map<String, Object> data) throws UnsupportedEncodingException {
		StringBuilder postData = new StringBuilder();
		if (data != null) {
			for (Entry<String, Object> param : data.entrySet()) {
				if (postData.length() != 0)
					postData.append('&');
				postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				postData.append('=');
				postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
			}
		}
		return postData;
	}

	private Map<String, Object> sendRequest(String path, String method, Map<String, Object> data, boolean useToken)
			throws IOException {
		Map<String, Object> returndata = new HashMap<String, Object>();
		StringBuilder postData = new StringBuilder();
		if (data != null && data.size() > 0) {
			postData = this.makePostDataParamsString(data);
		}
		method = method.toUpperCase();
		if (method.equals("GET")) {
			path = path + "?" + postData.toString();
		}
		URL obj = new URL(apiUrl + "/" + path);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		if (useToken && this.tokenName != null) {
			con.setRequestProperty("Authorization", "Bearer " + this.tokenName);
		}
		con.setRequestMethod(method);
		if (!method.equals("GET")) {
			if (method.equals("PUT"))
				con.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
			con.setDoOutput(true);
			con.setDoInput(true);
			OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
			wr.write(postData.toString());
			wr.flush();
			wr.close();
		}
		InputStream inputStream = null;
		try {
			inputStream = con.getInputStream();
		} catch (IOException exception) {
			inputStream = con.getErrorStream();
		}
		int responseCode = con.getResponseCode();
		if (inputStream != null) {
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			if (responseCode == 401 && this.refreshToken == 0) {
				this.refreshToken += 1;
				this.getToken();
				returndata = this.sendRequest(path, method, data, false);
			} else {
				Object jo = null;
				try {
					jo = new JSONObject(response.toString());
				} catch (JSONException ex) {
					try {
						jo = new JSONArray(response.toString());
					} catch (JSONException ex1) {
					}
				}
				returndata.put("data", jo);
				returndata.put("http_code", responseCode);
			}
		}
		return returndata;
	}

	private Map<String, Object> handleResult(Map<String, Object> data) {
		if (data.get("data") == null) {
			data.put("data", null);
		}
		if (Integer.parseInt(data.get("http_code").toString()) != 200) {
			data.put("is_error", true);
		}
		return data;
	}

	private Map<String, Object> handleError(String customMessage) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("is_error", true);
		if (customMessage != null && customMessage.length() > 0) {
			data.put("message", customMessage);
		}
		return data;
	}

	@Override
	public Map<String, Object> smtpSendMail(Map<String, Object> emaildata) {
		if (emaildata.size() == 0)
			return this.handleError("Empty email data");
		String html = emaildata.get("html").toString();
		try {
			html = Base64.getEncoder().encodeToString(html.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		emaildata.put("html", html);
		Map<String, Object> data = new HashMap<String, Object>();
		String serialized = Pherialize.serialize(emaildata);
		data.put("email", serialized);
		Map<String, Object> result = null;
		try {
			result = this.sendRequest("smtp/emails", "POST", data, true);
		} catch (IOException e) {
		}
		return this.handleResult(result);
	}
}
