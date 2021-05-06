package it.interop.dgc.gateway.akamai;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
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
import com.akamai.edgegrid.signer.apachehttpclient.ApacheHttpClientEdgeGridRoutePlanner;
import com.google.api.client.util.Maps;
import com.google.gson.Gson;

import it.interop.dgc.gateway.client.base.RestApiException;
import it.interop.dgc.gateway.util.DscUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AkamaiFastPurge {
	
	@Getter
	@Value("${akamai.base_url}")
	private String baseUrl;
	
	@Value("${akamai.network}")
	private String network;

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

	private HttpClient httpClient;
	
	@Getter
	private RestTemplate restTemplate;
	
	@PostConstruct
	private void initRestTactory() throws RestApiException, GeneralSecurityException, IOException {
 
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
		        .addInterceptorFirst(new ApacheHttpClientEdgeGridInterceptor(credential))
		        .setRoutePlanner(new ApacheHttpClientEdgeGridRoutePlanner(credential));

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

		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
		requestFactory.setConnectTimeout(DscUtil.parseWithDefault(connectTimeout, DscUtil.CONNECT_TIMEOUT_DEFAULT));
		requestFactory.setReadTimeout(DscUtil.parseWithDefault(readTimeout, DscUtil.READ_TIMEOUT_DEFAULT));
		
		restTemplate = new RestTemplate(requestFactory);		
	}

    public boolean invalidateUrls(List<String> urls) {
    	HttpStatus status = HttpStatus.NOT_IMPLEMENTED;
    	
		Map<String, String> urlParams = new HashMap<>();
		urlParams.put("network", network);

		URI uri = UriComponentsBuilder
				.fromUriString(new StringBuffer(getBaseUrl()).append("/ccu/v3/invalidate/cpcode/{network}").toString())
				.buildAndExpand(urlParams).encode().toUri();

		log.info("START REST AkamaiFastPurge calling-> {}", uri.toString());

		
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.USER_AGENT, userAgent);
		headers.set(HttpHeaders.CONTENT_TYPE, "application/json");
		headers.set(HttpHeaders.ACCEPT, "application/json");
		
		HttpEntity<String> entity = new HttpEntity<String>(getStringRequestBody(urls), headers);

		ResponseEntity<Void> respEntity = getRestTemplate().exchange(uri, HttpMethod.POST, entity, Void.class);
		
		if (respEntity != null) {
			status = respEntity.getStatusCode();
		}

		log.info("END REST AkamaiFastPurge status-> {}", status);

		return status == HttpStatus.OK;
    }

    public static String getStringRequestBody(List<String> cpcodes) {
        Map<String, List<String>> akamaiRequestMap = Maps.newHashMap();
        akamaiRequestMap.put("objects", cpcodes);
        return new Gson().toJson(akamaiRequestMap);
    }

}