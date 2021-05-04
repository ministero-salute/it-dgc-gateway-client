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
package it.interop.dgc.gateway.client;

import java.util.List;

import org.springframework.http.HttpStatus;

import it.interop.dgc.gateway.client.base.RestApiException;
import it.interop.dgc.gateway.client.base.RestApiResponse;
import it.interop.dgc.gateway.dto.SignedCertificateDto;
import it.interop.dgc.gateway.dto.TrustListDto;
import it.interop.dgc.gateway.enums.CertificateType;

public interface RestApiClient {
	public static String NEXT_BATCH_TAG = "nextBatchTag";
	public static String BATCH_TAG = "batchTag";
	
	//OK. Returns selected batch. - OK store data and try downloading next batch
	public static HttpStatus DOWNLOAD_STATUS_RETURNS_BATCH_200 = HttpStatus.OK;
	//Invalid BatchTag used. - KO throw exception
	public static HttpStatus DOWNLOAD_STATUS_INVALID_BATCHTAG_400 = HttpStatus.BAD_REQUEST;
	//Forbidden call in cause of missing or invalid client certificate. KO throw exception
	public static HttpStatus DOWNLOAD_STATUS_FORBIDDEN_403 = HttpStatus.FORBIDDEN;
	//BatchTag not found or no data exists. KO return next tag = null
	public static HttpStatus DOWNLOAD_STATUS_BATCHTAG_NOT_FOUND_404 = HttpStatus.NOT_FOUND;
	//Data format or content is not valid. KO
	public static HttpStatus DOWNLOAD_STATUS_INVALID_CONTENT_406 = HttpStatus.NOT_ACCEPTABLE;
	//Date for download expired. Date does not more exists. KO
	public static HttpStatus DOWNLOAD_STATUS_EXPIRED_DATE_410 = HttpStatus.GONE;

	//OK. Returns the audit information to the selected batch.
	public static HttpStatus AUDIT_STATUS_RETURNS_AUDIT_200 = HttpStatus.OK;
	//Invalid BatchTag used.
	public static HttpStatus AUDIT_STATUS_INVALID_BATCHTAG_400 = HttpStatus.BAD_REQUEST;
	//Forbidden call in cause of missing or invalid client certificate.
	public static HttpStatus AUDIT_STATUS_FORBIDDEN_403 = HttpStatus.FORBIDDEN;
	//BatchTag not found or no data exists.
	public static HttpStatus AUDIT_STATUS_BATCHTAG_NOT_FOUND_404 = HttpStatus.NOT_FOUND;
	//Data format or content is not valid.
	public static HttpStatus AUDIT_STATUS_INVALID_CONTENT_406 = HttpStatus.NOT_ACCEPTABLE;
	//Date for download expired. Date does not more exists.
	public static HttpStatus AUDIT_STATUS_EXPIRED_DATE_410 = HttpStatus.GONE;

	//Database Entries created. - OK marked as sent
	public static HttpStatus UPLOAD_STATUS_CREATED_201 = HttpStatus.CREATED;
	//Data partially added with warnings. More details in document. - OK marked as sent
	public static HttpStatus UPLOAD_STATUS_WARNING_207 = HttpStatus.MULTI_STATUS;
	//Signature not valid. Bad request. - KO retry
	public static HttpStatus UPLOAD_STATUS_INVALID_SIGNATURE_400 = HttpStatus.BAD_REQUEST;
	//Bad request. - KO retry
	public static HttpStatus UPLOAD_STATUS_BAD_REQUEST_400 = HttpStatus.BAD_REQUEST;
	//Forbidden call in cause of missing or invalid client certificate. - KO retry
	public static HttpStatus UPLOAD_STATUS_FORBIDDEN_403 = HttpStatus.FORBIDDEN;
	//Data format or content is not valid. - KO retry
	public static HttpStatus UPLOAD_STATUS_INVALID_CONTENT_406 = HttpStatus.NOT_ACCEPTABLE;
	//Data already exist. - KO retry
	public static HttpStatus UPLOAD_STATUS_BATCHTAG_ALREADY_EXIST_409 = HttpStatus.CONFLICT;
	//Not able to write data. Retry please. - KO retry
	public static HttpStatus UPLOAD_STATUS_RETRY_500 = HttpStatus.INTERNAL_SERVER_ERROR;
	
	//UPLOAD
	public RestApiResponse<String> postVerificationInformation(SignedCertificateDto cms, String countryCode)  throws RestApiException;
	public RestApiResponse<String> revokeVerificationInformation(SignedCertificateDto cms, String countryCode)  throws RestApiException;
	//DOWNLOAD
	public RestApiResponse<List<TrustListDto>> downloadTrustList()  throws RestApiException;
	public RestApiResponse<List<TrustListDto>> downloadTrustListFilteredByType(CertificateType type)  throws RestApiException;
	public RestApiResponse<List<TrustListDto>> downloadTrustListFilteredByCountryAndType(CertificateType type, String countryCode) throws RestApiException;
}
