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
package it.interop.dgc.gateway.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.interop.dgc.gateway.client.base.RestApiClientBase;
import it.interop.dgc.gateway.client.base.RestApiException;
import it.interop.dgc.gateway.client.base.RestApiResponse;
import it.interop.dgc.gateway.dto.RevocationListItemDto;
import it.interop.dgc.gateway.dto.TrustListItemDto;
import it.interop.dgc.gateway.dto.ValidationRuleDto;
import it.interop.dgc.gateway.enums.CertificateType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
public class RestApiClientImpl
    extends RestApiClientBase
    implements RestApiClient {

    public static final String REQUEST_PROP_COUNTRY = "reqPropCountry";
    public static final String REQUEST_PROP_THUMBPRINT =
        "reqPropCertThumbprint";

    @Override
    public RestApiResponse<String> postVerificationInformation(
        String cms,
        String countryCode
    ) throws RestApiException {
        URI uri = UriComponentsBuilder
            .fromHttpUrl(
                new StringBuffer(getBaseUrl())
                    .append("/signerCertificate")
                    .toString()
            )
            .queryParam(REQUEST_PROP_COUNTRY, countryCode)
            .build()
            .encode()
            .toUri();

        log.info("START REST Client calling-> {}", uri.toString());

        HttpHeaders headers = makeBaseHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, "application/cms");
        headers.set(HttpHeaders.CONTENT_ENCODING, "base64");

        HttpEntity<String> entity = new HttpEntity<String>(cms, headers);

        ResponseEntity<Void> respEntity = getRestTemplate()
            .exchange(uri, HttpMethod.POST, entity, Void.class);

        RestApiResponse<String> restApiResponse = null;

        if (respEntity != null) {
            String esito = respEntity.getStatusCode().toString();

            log.info(
                "REST Client response-> {} : message: {}",
                respEntity.getStatusCode(),
                esito
            );

            restApiResponse =
                new RestApiResponse<String>(
                    respEntity.getStatusCode(),
                    headersToMap(respEntity.getHeaders()),
                    esito
                );
        }

        log.info("END REST Client calling-> {}", uri.toString());
        return restApiResponse;
    }

    @Override
    public RestApiResponse<String> revokeVerificationInformation(
        String cms,
        String countryCode
    ) throws RestApiException {
        URI uri = UriComponentsBuilder
            .fromHttpUrl(
                new StringBuffer(getBaseUrl())
                    .append("/signerCertificate")
                    .toString()
            )
            .queryParam(REQUEST_PROP_COUNTRY, countryCode)
            .build()
            .encode()
            .toUri();

        log.info("START REST Client calling-> {}", uri.toString());

        HttpHeaders headers = makeBaseHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, "application/cms");
        headers.set(HttpHeaders.CONTENT_ENCODING, "base64");

        HttpEntity<String> entity = new HttpEntity<String>(cms, headers);

        ResponseEntity<Void> respEntity = getRestTemplate()
            .exchange(uri, HttpMethod.DELETE, entity, Void.class);

        RestApiResponse<String> restApiResponse = null;

        if (respEntity != null) {
            String esito = respEntity.getStatusCode().toString();

            log.info(
                "REST Client response-> {} : message: {}",
                respEntity.getStatusCode(),
                esito
            );

            restApiResponse =
                new RestApiResponse<String>(
                    respEntity.getStatusCode(),
                    headersToMap(respEntity.getHeaders()),
                    esito
                );
        }

        log.info("END REST Client calling-> {}", uri.toString());
        return restApiResponse;
    }

    @Override
    public RestApiResponse<List<TrustListItemDto>> downloadTrustList()
        throws RestApiException {
        URI uri = UriComponentsBuilder
            .fromHttpUrl(
                new StringBuffer(getBaseUrl()).append("/trustList").toString()
            )
            .build()
            .encode()
            .toUri();

        return _downloadTrustList(uri);
    }

    @Override
    public RestApiResponse<List<TrustListItemDto>> downloadTrustListFilteredByType(
        CertificateType type
    ) throws RestApiException {
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("type", type.name());

        URI uri = UriComponentsBuilder
            .fromUriString(
                new StringBuffer(getBaseUrl())
                    .append("/trustList/{type}")
                    .toString()
            )
            .buildAndExpand(urlParams)
            .encode()
            .toUri();

        return _downloadTrustList(uri);
    }

    @Override
    public RestApiResponse<List<TrustListItemDto>> downloadTrustListFilteredByCountryAndType(
        CertificateType type,
        String countryCode
    ) throws RestApiException {
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("type", type.name());
        urlParams.put("country", countryCode);

        URI uri = UriComponentsBuilder
            .fromUriString(
                new StringBuffer(getBaseUrl())
                    .append("/trustList/{type}/{country}")
                    .toString()
            )
            .buildAndExpand(urlParams)
            .encode()
            .toUri();

        return _downloadTrustList(uri);
    }

    private RestApiResponse<List<TrustListItemDto>> _downloadTrustList(URI uri)
        throws RestApiException {
        log.info("START REST Client calling-> {}", uri.toString());

        HttpHeaders headers = makeBaseHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpEntity<Void> entity = new HttpEntity<Void>(headers);

        ResponseEntity<byte[]> respEntity = getRestTemplate()
            .exchange(uri, HttpMethod.GET, entity, byte[].class);

        RestApiResponse<List<TrustListItemDto>> restApiResponse = null;

        List<TrustListItemDto> listTrust = null;
        if (respEntity != null) {
            log.info("REST Client response-> {}", respEntity.getStatusCode());

            if (respEntity.getStatusCode() == HttpStatus.OK) {
                Gson gson = new Gson();
                Type trustListType = new TypeToken<ArrayList<TrustListItemDto>>() {}
                    .getType();
                listTrust =
                    gson.fromJson(
                        new String(respEntity.getBody()),
                        trustListType
                    );
            }

            restApiResponse =
                new RestApiResponse<List<TrustListItemDto>>(
                    respEntity.getStatusCode(),
                    headersToMap(respEntity.getHeaders()),
                    listTrust
                );
        }

        log.info("END REST Client calling-> {}", uri.toString());
        return restApiResponse;
    }

    @Override
    public RestApiResponse<String> downloadCountryList()
        throws RestApiException {
        URI uri = UriComponentsBuilder
            .fromHttpUrl(
                new StringBuffer(getBaseUrl()).append("/countrylist").toString()
            )
            .build()
            .encode()
            .toUri();

        log.info("START REST Client calling-> {}", uri.toString());

        HttpHeaders headers = makeBaseHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpEntity<Void> entity = new HttpEntity<Void>(headers);

        ResponseEntity<byte[]> respEntity = getRestTemplate()
            .exchange(uri, HttpMethod.GET, entity, byte[].class);

        RestApiResponse<String> restApiResponse = null;

        String listCountry = null;
        if (respEntity != null) {
            log.info("REST Client response-> {}", respEntity.getStatusCode());

            if (respEntity.getStatusCode() == HttpStatus.OK) {
                listCountry = new String(respEntity.getBody());
            }

            restApiResponse =
                new RestApiResponse<String>(
                    respEntity.getStatusCode(),
                    headersToMap(respEntity.getHeaders()),
                    listCountry
                );
        }

        log.info("END REST Client calling-> {}", uri.toString());
        return restApiResponse;
    }

    @Override
    public RestApiResponse<List<String>> getValuesetIds()
        throws RestApiException {
        URI uri = UriComponentsBuilder
            .fromHttpUrl(
                new StringBuffer(getBaseUrl()).append("/valuesets").toString()
            )
            .build()
            .encode()
            .toUri();

        log.info("START REST Client calling-> {}", uri.toString());

        HttpHeaders headers = makeBaseHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpEntity<Void> entity = new HttpEntity<Void>(headers);

        ResponseEntity<byte[]> respEntity = getRestTemplate()
            .exchange(uri, HttpMethod.GET, entity, byte[].class);

        RestApiResponse<List<String>> restApiResponse = null;

        List<String> listValues = null;
        if (respEntity != null) {
            log.info("REST Client response-> {}", respEntity.getStatusCode());

            if (respEntity.getStatusCode() == HttpStatus.OK) {
                Gson gson = new Gson();
                Type trustListType = new TypeToken<ArrayList<String>>() {}
                    .getType();
                listValues =
                    gson.fromJson(
                        new String(respEntity.getBody()),
                        trustListType
                    );
            }

            restApiResponse =
                new RestApiResponse<List<String>>(
                    respEntity.getStatusCode(),
                    headersToMap(respEntity.getHeaders()),
                    listValues
                );
        }

        log.info("END REST Client calling-> {}", uri.toString());
        return restApiResponse;
    }

    @Override
    public RestApiResponse<String> getValueset(String id)
        throws RestApiException {
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("id", id);

        URI uri = UriComponentsBuilder
            .fromUriString(
                new StringBuffer(getBaseUrl())
                    .append("/valuesets/{id}")
                    .toString()
            )
            .buildAndExpand(urlParams)
            .encode()
            .toUri();

        log.info("START REST Client calling-> {}", uri.toString());

        HttpHeaders headers = makeBaseHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpEntity<Void> entity = new HttpEntity<Void>(headers);

        ResponseEntity<byte[]> respEntity = getRestTemplate()
            .exchange(uri, HttpMethod.GET, entity, byte[].class);

        RestApiResponse<String> restApiResponse = null;

        String values = null;
        if (respEntity != null) {
            log.info("REST Client response-> {}", respEntity.getStatusCode());

            if (respEntity.getStatusCode() == HttpStatus.OK) {
                values = new String(respEntity.getBody());
            }

            restApiResponse =
                new RestApiResponse<String>(
                    respEntity.getStatusCode(),
                    headersToMap(respEntity.getHeaders()),
                    values
                );
        }

        log.info("END REST Client calling-> {}", uri.toString());
        return restApiResponse;
    }

    @Override
    public RestApiResponse<Map<String, List<ValidationRuleDto>>> downloadValidationRules(
        String country
    ) throws RestApiException {
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("country", country);

        URI uri = UriComponentsBuilder
            .fromUriString(
                new StringBuffer(getBaseUrl())
                    .append("/rules/{country}")
                    .toString()
            )
            .buildAndExpand(urlParams)
            .encode()
            .toUri();

        log.info("START REST Client calling-> {}", uri.toString());

        HttpHeaders headers = makeBaseHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpEntity<Void> entity = new HttpEntity<Void>(headers);

        ResponseEntity<byte[]> respEntity = getRestTemplate()
            .exchange(uri, HttpMethod.GET, entity, byte[].class);

        RestApiResponse<Map<String, List<ValidationRuleDto>>> restApiResponse =
            null;

        Map<String, List<ValidationRuleDto>> mapRules = null;
        if (respEntity != null) {
            log.info("REST Client response-> {}", respEntity.getStatusCode());

            if (respEntity.getStatusCode() == HttpStatus.OK) {
                Gson gson = new Gson();
                Type trustListType = new TypeToken<HashMap<String, List<ValidationRuleDto>>>() {}
                    .getType();
                mapRules =
                    gson.fromJson(
                        new String(respEntity.getBody()),
                        trustListType
                    );
            }

            restApiResponse =
                new RestApiResponse<Map<String, List<ValidationRuleDto>>>(
                    respEntity.getStatusCode(),
                    headersToMap(respEntity.getHeaders()),
                    mapRules
                );
        }

        log.info("END REST Client calling-> {}", uri.toString());
        return restApiResponse;
    }

    @Override
    public RestApiResponse<String> uploadValidationRule(
        String cms,
        String countryCode
    ) throws RestApiException {
        URI uri = UriComponentsBuilder
            .fromHttpUrl(
                new StringBuffer(getBaseUrl()).append("/rules").toString()
            )
            .queryParam(REQUEST_PROP_COUNTRY, countryCode)
            .build()
            .encode()
            .toUri();

        log.info("START REST Client calling-> {}", uri.toString());

        HttpHeaders headers = makeBaseHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, "application/cms");
        headers.set(HttpHeaders.CONTENT_ENCODING, "base64");

        HttpEntity<String> entity = new HttpEntity<String>(cms, headers);

        ResponseEntity<Void> respEntity = getRestTemplate()
            .exchange(uri, HttpMethod.POST, entity, Void.class);

        RestApiResponse<String> restApiResponse = null;

        if (respEntity != null) {
            String esito = respEntity.getStatusCode().toString();

            log.info(
                "REST Client response-> {} : message: {}",
                respEntity.getStatusCode(),
                esito
            );

            restApiResponse =
                new RestApiResponse<String>(
                    respEntity.getStatusCode(),
                    headersToMap(respEntity.getHeaders()),
                    esito
                );
        }

        log.info("END REST Client calling-> {}", uri.toString());
        return restApiResponse;
    }

    @Override
    public RestApiResponse<String> deleteValidationRules(
        String cms,
        String countryCode
    ) throws RestApiException {
        URI uri = UriComponentsBuilder
            .fromHttpUrl(
                new StringBuffer(getBaseUrl()).append("/rules").toString()
            )
            .queryParam(REQUEST_PROP_COUNTRY, countryCode)
            .build()
            .encode()
            .toUri();

        log.info("START REST Client calling-> {}", uri.toString());

        HttpHeaders headers = makeBaseHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, "application/cms");
        headers.set(HttpHeaders.CONTENT_ENCODING, "base64");

        HttpEntity<String> entity = new HttpEntity<String>(cms, headers);

        ResponseEntity<Void> respEntity = getRestTemplate()
            .exchange(uri, HttpMethod.DELETE, entity, Void.class);

        RestApiResponse<String> restApiResponse = null;

        if (respEntity != null) {
            String esito = respEntity.getStatusCode().toString();

            log.info(
                "REST Client response-> {} : message: {}",
                respEntity.getStatusCode(),
                esito
            );

            restApiResponse =
                new RestApiResponse<String>(
                    respEntity.getStatusCode(),
                    headersToMap(respEntity.getHeaders()),
                    esito
                );
        }

        log.info("END REST Client calling-> {}", uri.toString());
        return restApiResponse;
    }

	@Override
	public RestApiResponse<List<RevocationListItemDto>> downloadRevocationList() throws RestApiException {
		// TODO Auto-generated method stub
//		 Map<String, String> urlParams = new HashMap<>();
//	        urlParams.put("country", country);

	        URI uri = UriComponentsBuilder
	            .fromUriString(
	                new StringBuffer(getBaseUrl())
	                    .append("/revocation-list")
	                    .toString()
	            )
	            .build()
	            .encode()
	            .toUri();

	        log.info("START REST Client calling-> {}", uri.toString());

	        HttpHeaders headers = makeBaseHeaders();
	        headers.set(HttpHeaders.CONTENT_TYPE, "application/json");
	        headers.set(HttpHeaders.IF_MODIFIED_SINCE,"2021-06-01T00:00:00Z");
	        
	        HttpEntity<Void> entity = new HttpEntity<Void>(headers);

	        ResponseEntity<byte[]> respEntity = getRestTemplate()
	            .exchange(uri, HttpMethod.GET, entity, byte[].class);

	        if (respEntity != null) {
	            log.info("REST Client response-> {}", respEntity.getStatusCode());

	            if (respEntity.getStatusCode() == HttpStatus.OK) {
	            	System.out.println(new String(respEntity.getBody()));
//	                Gson gson = new Gson();
//	                Type trustListType = new TypeToken<HashMap<String, List<ValidationRuleDto>>>() {}
//	                    .getType();
//	                mapRules =
//	                    gson.fromJson(
//	                        new String(respEntity.getBody()),
//	                        trustListType
//	                    );
	            }

//	            restApiResponse =
//	                new RestApiResponse<Map<String, List<ValidationRuleDto>>>(
//	                    respEntity.getStatusCode(),
//	                    headersToMap(respEntity.getHeaders()),
//	                    mapRules
//	                );
	        }

	        log.info("END REST Client calling-> {}", uri.toString());
//	        return restApiResponse;
		return null;
	}
    
    
}
