/*-
 *   Copyright (C) 2021 Ministero della Salute and all other contributors.
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
package it.interop.dgc.gateway.signing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.annotation.PostConstruct;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.bouncycastle.cms.CMSException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import it.interop.dgc.gateway.client.base.RestApiException;
import it.interop.dgc.gateway.util.DscUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SignatureService {

	@Value("${signature.external.url}")
	private String externalUrl;

	@Value("${signature.external.connectTimeout}")
	private String connectTimeout;

	@Value("${signature.external.readTimeout}")
	private String readTimeout;

	@Value("${ssldp.jks.path}")
	private String jksPath;
	
	@Value("${ssldp.jks.password}")
	private String jksPassword;

	@Value("${ssldp.cert.password}")
	private String certPassword;

	@Value("${truststore.jks.path}")
	private String jksTrustPath;
	
	@Value("${truststore.jks.password}")
	private String jksTrustPassword;


	private RestTemplate restTemplate;

	@PostConstruct
	private void intRestTemplate() throws RestApiException {
		try {
			KeyStore clientStore = KeyStore.getInstance("JKS");
			clientStore.load(new FileInputStream(jksPath), jksPassword.toCharArray());
			SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
			//sslContextBuilder.useProtocol("TLS");
			//La CA Actalis e certificato
			sslContextBuilder.loadKeyMaterial(clientStore, certPassword.toCharArray());
			
			//Il certificato del gateway
			sslContextBuilder.loadTrustMaterial(new File(jksTrustPath), jksTrustPassword.toCharArray());
			SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContextBuilder.build());

		    HttpClientBuilder clientBuilder = HttpClientBuilder.create();

		    clientBuilder.disableCookieManagement();

		    CloseableHttpClient httpClient = clientBuilder.setSSLSocketFactory(sslConnectionSocketFactory).build();
			
			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
			requestFactory.setConnectTimeout(DscUtil.parseWithDefault(connectTimeout, DscUtil.CONNECT_TIMEOUT_DEFAULT));
			requestFactory.setReadTimeout(DscUtil.parseWithDefault(readTimeout, DscUtil.READ_TIMEOUT_DEFAULT));
			
			restTemplate = new RestTemplate(requestFactory);
			
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException | KeyManagementException | IOException e) {
			throw new RestApiException(e);
		}
	}

	public String getSignatureForBytes(final String data) throws CMSException, IOException, CertificateSignatureException {
		log.info("START Signature process");
		HttpHeaders headers = new HttpHeaders();

		headers.set("User-Agent", "");
		headers.set("Content-Type", "application/json");
		
		InputData inputData = new InputData(data);
		
		HttpEntity<InputData> entity = new HttpEntity<InputData>(inputData, headers);

		ResponseEntity<OutputData> respEntity = restTemplate.exchange(externalUrl, HttpMethod.POST, entity, OutputData.class);
		
		if (respEntity != null) {
			if (respEntity.getStatusCode() == HttpStatus.OK) {
				OutputData outputData = respEntity.getBody();
				
				if (outputData != null) {
					log.info("END Signature process");
					return outputData.getSignature();
				} else {
					log.error("ERROR signature: empty body");
					throw new CertificateSignatureException(externalUrl+" return null");
				}

			} else {
				log.error("ERROR signature: response code: "+respEntity.getStatusCode());
				throw new CertificateSignatureException(externalUrl+" return status code: "+respEntity.getStatusCode());
			}
		} else {
			log.error("ERROR signature: empty response");
			throw new CertificateSignatureException(externalUrl+" return null");
		}
	}
}

@Data
class InputData {
	private String prehashed;
	private String input;

	public InputData(String input) {
		this.prehashed = "false";
		this.input = input;
	}
}

@Data
class OutputData {
	private String signature;

}


