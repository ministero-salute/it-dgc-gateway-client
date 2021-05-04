/*-
 *   Copyright (C) 2021 Presidenza del Consiglio dei Ministri.
 *   Please refer to the AUTHORS file for more information. 
 *   This program is free software: you can redistribute it and/or modify 
 *   it under the terms of the GNU Affero General Public License as 
 *   published by the Free Software Foundation, either version 3 of the
 *   License, or (at your option) any later version.
 *   This program is distributed in the hope that it will be useful, 
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 *   GNU Affero General Public License for more details.
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program. If not, see <https://www.gnu.org/licenses/>.   
 */
package it.interop.dgc.gateway.client.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
//import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
//import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
//import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import lombok.Getter;

public class RestApiClientBase {

	private static Integer connectTimeout = 3000;
	private static Integer readTimeout = 3000;

	@Getter
	@Value("${dgc.base_url}")
	private String baseUrl;

	@Value("${dgc.user_agent}")
	private String userAgent;

	@Value("${ssldgc.jks.path}")
	private String jksPath;
	
	@Value("${ssldgc.jks.password}")
	private String jksPassword;

	@Value("${ssldgc.cert.password}")
	private String certPassword;
	
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
	private void initRestTemplate() throws RestApiException {
		try {
			KeyStore clientStore = KeyStore.getInstance("JKS");//PKCS12
			clientStore.load(new FileInputStream(jksPath), jksPassword.toCharArray());
			SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
			//sslContextBuilder.useProtocol("TLS");
			//La CA Actalis e certificato
			sslContextBuilder.loadKeyMaterial(clientStore, certPassword.toCharArray());
			
			//Il certificato del gateway
			sslContextBuilder.loadTrustMaterial(new File(jksTrustPath), jksTrustPassword.toCharArray());
			SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContextBuilder.build());

		    HttpClientBuilder clientBuilder = HttpClientBuilder.create();

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
			requestFactory.setConnectTimeout(connectTimeout);
			requestFactory.setReadTimeout(readTimeout);
			
			restTemplate = new RestTemplate(requestFactory);
			
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException | KeyManagementException | IOException e) {
			throw new RestApiException(e);
		}
	}

	protected Map<String, List<String>> headersToMap(HttpHeaders headers) {
		Map<String, List<String>> headersList = null;
		
		if (headers != null) {
			 headersList = new HashMap<String, List<String>>();
			Set<String> names = headers.keySet();
			
			for (String name: names) {
				headersList.put(name, headers.get(name));
			}
		}
		
		return headersList;
	}
	
	protected HttpHeaders makeBaseHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", userAgent);
		
		return headers;
	}
	
	
}
