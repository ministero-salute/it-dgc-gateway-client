/*-
 *   Copyright (C) 2020 Presidenza del Consiglio dei Ministri.
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
package it.interop.eucert.gateway.client;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import it.interop.eucert.gateway.client.base.RestApiClientBase;
import it.interop.eucert.gateway.client.base.RestApiException;
import it.interop.eucert.gateway.client.base.RestApiResponse;
import it.interop.eucert.gateway.dto.SignedCertificateDto;
import it.interop.eucert.gateway.dto.TrustListDto;
import it.interop.eucert.gateway.enums.CertificateType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RestApiClientImpl extends RestApiClientBase implements RestApiClient {
    public static final String REQUEST_PROP_COUNTRY = "reqPropCountry";
    public static final String REQUEST_PROP_THUMBPRINT = "reqPropCertThumbprint";

	@Override
	public RestApiResponse<String> postVerificationInformation(SignedCertificateDto cms, String countryCode) throws RestApiException {
		String localVarPath = "/signerCertificate";

		StringBuffer url = new StringBuffer(getBaseUrl()).append(localVarPath).append("?")
				.append(REQUEST_PROP_COUNTRY).append("=").append(countryCode);
		
		log.info("START REST Client calling-> {}", url.toString());

		HttpHeaders headers = makeBaseHeaders();
		headers.set(HttpHeaders.CONTENT_TYPE, "application/cms");
		headers.set(HttpHeaders.CONTENT_ENCODING, "base64");
		
		HttpEntity<SignedCertificateDto> entity = new HttpEntity<SignedCertificateDto>(cms, headers);

		ResponseEntity<Void> respEntity = getRestTemplate().exchange(url.toString(), HttpMethod.POST, entity, Void.class);
		
		RestApiResponse<String> restApiResponse = null;

		if (respEntity != null) {
			String esito = respEntity.getStatusCode().toString();
			
			log.info("REST Client response-> {} : message: {}", respEntity.getStatusCode(), esito);

			restApiResponse = new RestApiResponse<String>(respEntity.getStatusCode(), headersToMap(respEntity.getHeaders()), esito);
		}

		log.info("END REST Client calling-> {}", url.toString());
		return restApiResponse;
	}

	@Override
	public RestApiResponse<String> revokeVerificationInformation(SignedCertificateDto cms, String countryCode) throws RestApiException {
		String localVarPath = "/signerCertificate";

		StringBuffer url = new StringBuffer(getBaseUrl()).append(localVarPath).append("?")
				.append(REQUEST_PROP_COUNTRY).append("=").append(countryCode);
		
		log.info("START REST Client calling-> {}", url.toString());

		HttpHeaders headers = makeBaseHeaders();
		headers.set(HttpHeaders.CONTENT_TYPE, "application/cms");
		headers.set(HttpHeaders.CONTENT_ENCODING, "base64");
		
		HttpEntity<SignedCertificateDto> entity = new HttpEntity<SignedCertificateDto>(cms, headers);

		ResponseEntity<Void> respEntity = getRestTemplate().exchange(url.toString(), HttpMethod.DELETE, entity, Void.class);
		
		RestApiResponse<String> restApiResponse = null;

		if (respEntity != null) {
			String esito = respEntity.getStatusCode().toString();
			
			log.info("REST Client response-> {} : message: {}", respEntity.getStatusCode(), esito);

			restApiResponse = new RestApiResponse<String>(respEntity.getStatusCode(), headersToMap(respEntity.getHeaders()), esito);
		}

		log.info("END REST Client calling-> {}", url.toString());
		return restApiResponse;
	}

	@Override
	public RestApiResponse<List<TrustListDto>> downloadTrustList() throws RestApiException {
		String localVarPath = "/trustList";

		StringBuffer url = new StringBuffer(getBaseUrl()).append(localVarPath);

		return _downloadTrustList(url.toString());
	}

	@Override
	public RestApiResponse<List<TrustListDto>> downloadTrustListFilteredByType(CertificateType type) throws RestApiException {
		String localVarPath = "/trustList/{type}"
				.replaceAll("\\{type\\}", type.name());

		StringBuffer url = new StringBuffer(getBaseUrl()).append(localVarPath);

		return _downloadTrustList(url.toString());
	}

	@Override
	public RestApiResponse<List<TrustListDto>> downloadTrustListFilteredByCountryAndType(CertificateType type, String countryCode) throws RestApiException {
		String localVarPath = "/trustList/{type}/{country}"
				.replaceAll("\\{type\\}", type.name())
				.replaceAll("\\{country\\}", countryCode);
		
		StringBuffer url = new StringBuffer(getBaseUrl()).append(localVarPath);

		return _downloadTrustList(url.toString());

	}

	
	private RestApiResponse<List<TrustListDto>> _downloadTrustList(String url) throws RestApiException {
		log.info("START REST Client calling-> {}", url.toString());

		HttpHeaders headers = makeBaseHeaders();
		headers.set(HttpHeaders.CONTENT_TYPE, "application/json");
		
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		ResponseEntity<byte[]> respEntity = getRestTemplate().exchange(url.toString(), HttpMethod.GET, entity, byte[].class);

		RestApiResponse<List<TrustListDto>> restApiResponse = null;

		List<TrustListDto> listTrust = null;
		if (respEntity != null) {

			log.info("REST Client response-> {}", respEntity.getStatusCode());

			if (respEntity.getStatusCode() == HttpStatus.OK) {
				Gson gson = new Gson();
				Type auditListType = new TypeToken<ArrayList<TrustListDto>>(){}.getType();
				listTrust = gson.fromJson(new String(respEntity.getBody()), auditListType);
			}
			
			restApiResponse = new RestApiResponse<List<TrustListDto>>(respEntity.getStatusCode(), headersToMap(respEntity.getHeaders()), listTrust);
		}
		

		log.info("END REST Client calling-> {}", url.toString());
		return restApiResponse;
	}
	
	
	
	
}
