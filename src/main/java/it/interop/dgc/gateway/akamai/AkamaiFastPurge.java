/*
 *  Copyright (C) 2021 Ministero della Salute and all other contributors.
 *  Please refer to the AUTHORS file for more information. 
 *  This program is free software: you can redistribute it and/or modify 
 *  it under the terms of the GNU Affero General Public License as 
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 *  GNU Affero General Public License for more details.
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.   
*/

package it.interop.dgc.gateway.akamai;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.akamai.edgegrid.signer.ClientCredential;
import com.akamai.edgegrid.signer.apachehttpclient.ApacheHttpClientEdgeGridInterceptor;
import com.google.gson.Gson;

import it.interop.dgc.gateway.client.base.RestApiException;
import it.interop.dgc.gateway.util.DscUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AkamaiFastPurge {
	
	@Getter
	@Value("${akamai.url}")
	private String url;
	
	@Value("${akamai.network}")
	private String network;
	
	@Value("${akamai.cpcodes_to_purge}")
	private String cpcodes;

	@Value("${akamai.cpcodes_rules_to_purge}")
	private String rulescpcodes;

	@Value("${akamai.user_agent}")
	private String userAgent;
	
	@Value("${akamai.connectTimeout}")
	private String connectTimeout;

	@Value("${akamai.readTimeout}")
	private String readTimeout;
	
	@Value("${akamai.credential.accessToken}")
	private String accessToken;
	
	@Value("${akamai.credential.clientToken}")
	private String clientToken;

	@Value("${akamai.credential.clientSecret}")
	private String clientSecret;

	@Value("${akamai.credential.host}")
	private String host;

	@Value("${truststore.jks.path}")
	private String jksTrustPath;
	
	@Value("${truststore.jks.password}")
	private String jksTrustPassword;

	@Value("${proxy.host}")
	private String proxyHost;

	@Value("${proxy.port}")
	private String proxyPort;
	
	@Value("${proxy.user}")
	private String proxyUser;

	@Value("${proxy.password}")
	private String proxyPassword;

	@Getter
	private RestTemplate restTemplate;
	
	@PostConstruct
	private void initRestTactory() throws RestApiException, GeneralSecurityException, IOException {
 
		if (url != null && !"".equals(url)) {
			SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
			sslContextBuilder.loadTrustMaterial(new File(jksTrustPath), jksTrustPassword.toCharArray());
			SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContextBuilder.build());
	
	        ClientCredential credential = ClientCredential.builder()
					.accessToken(accessToken)
					.clientToken(clientToken)
					.clientSecret(clientSecret)
					.host(host)
					.build();
			
		    HttpClientBuilder clientBuilder = HttpClientBuilder.create()
		    		.setSSLSocketFactory(sslConnectionSocketFactory)
			        .addInterceptorFirst(new ApacheHttpClientEdgeGridInterceptor(credential));
//			        .setRoutePlanner(new ApacheHttpClientEdgeGridRoutePlanner(credential));
	
			if (!StringUtils.isEmpty(proxyHost) && !StringUtils.isEmpty(proxyPort)) {
			    HttpHost myProxy = new HttpHost(proxyHost, Integer.parseInt(proxyPort));
	
			    clientBuilder.setProxy(myProxy);
			    if (!StringUtils.isEmpty(proxyUser) && !StringUtils.isEmpty(proxyPassword)) {
				    CredentialsProvider credsProvider = new BasicCredentialsProvider();
				    credsProvider.setCredentials( 
				        new AuthScope(proxyHost, Integer.parseInt(proxyPort)), 
				        new UsernamePasswordCredentials(proxyUser, proxyPassword)
				    );
				    clientBuilder.setDefaultCredentialsProvider(credsProvider);
			    }
			}
		    clientBuilder.disableCookieManagement();

		    CloseableHttpClient httpClient = clientBuilder.setSSLSocketFactory(sslConnectionSocketFactory).build();

			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
			requestFactory.setConnectTimeout(DscUtil.parseWithDefault(connectTimeout, DscUtil.CONNECT_TIMEOUT_DEFAULT));
			requestFactory.setReadTimeout(DscUtil.parseWithDefault(readTimeout, DscUtil.READ_TIMEOUT_DEFAULT));
			
			restTemplate = new RestTemplate(requestFactory);
		}
	}

    public String invalidateUrls() {
    	HttpStatus status = null;
    	
		URI uri = UriComponentsBuilder
				.fromUriString(getUrl())
				.build().toUri();

		log.info("START REST AkamaiFastPurge calling-> {}", uri.toString());

		
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.USER_AGENT, userAgent);
		headers.set(HttpHeaders.CONTENT_TYPE, "application/json");
		
		
		
		HttpEntity<String> entity = new HttpEntity<String>(getStringRequestBody(cpcodes), headers);

		ResponseEntity<String> respEntity = getRestTemplate().exchange(uri, HttpMethod.POST, entity, String.class);
		
		if (respEntity != null) {
			status = respEntity.getStatusCode();
			log.info("RESPONSE AkamaiFastPurge -> {}", respEntity.getBody());
		}

		log.info("END REST AkamaiFastPurge status-> {}", status);

		return status.toString();
    }

    public String invalidateRulesUrls() {
    	HttpStatus status = null;
    	
		URI uri = UriComponentsBuilder
				.fromUriString(getUrl())
				.build().toUri();

		log.info("START REST AkamaiFastPurge calling-> {}", uri.toString());

		
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.USER_AGENT, userAgent);
		headers.set(HttpHeaders.CONTENT_TYPE, "application/json");
		
		
		
		HttpEntity<String> entity = new HttpEntity<String>(getStringRequestBody(rulescpcodes), headers);

		ResponseEntity<String> respEntity = getRestTemplate().exchange(uri, HttpMethod.POST, entity, String.class);
		
		if (respEntity != null) {
			status = respEntity.getStatusCode();
			log.info("RESPONSE AkamaiFastPurge -> {}", respEntity.getBody());
		}

		log.info("END REST AkamaiFastPurge status-> {}", status);

		return status.toString();
    }

    public static String getStringRequestBody(String urls) {
//        Map<String, int[]> akamaiRequestMap = new HashMap<String, int[]>();
//        akamaiRequestMap.put("objects", Stream.of(urls.split(",")).mapToInt(Integer::parseInt).toArray());
        Map<String, String[]> akamaiRequestMap = new HashMap<String, String[]>();
        akamaiRequestMap.put("objects", urls.split(","));
        return new Gson().toJson(akamaiRequestMap);
    }

}