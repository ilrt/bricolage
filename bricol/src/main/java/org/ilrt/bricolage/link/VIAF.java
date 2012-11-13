package org.ilrt.bricolage.link;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.ilrt.bricolage.Defaults;

public class VIAF {

	private static VIAF instance = null;

	public static VIAF getInstance() {
		if (instance == null) {
			instance = new VIAF();
		}
		return instance;
	}

	private VIAF() {
	}

	public String suggest(List<String> terms) throws ClientProtocolException, IOException {
		StringBuilder sb = new StringBuilder();
		for (String s : terms)
		{
			if(sb.length() > 0) {
			    sb.append(" ");
			}
		    sb.append(s);
		}
		return suggest(sb.toString());
	}
	
	public String suggest(String terms) throws ClientProtocolException, IOException {
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(Defaults.VIAF_SUGGEST + URLEncoder.encode(terms, "UTF-8"));

		ResponseHandler<String> handler = new ResponseHandler<String>() {
			public String handleResponse(HttpResponse response)
					throws ClientProtocolException, IOException {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					return EntityUtils.toString(entity, "UTF-8");
				} else {
					return null;
				}
			}
		};

		return httpclient.execute(httpget, handler);
	}
}
