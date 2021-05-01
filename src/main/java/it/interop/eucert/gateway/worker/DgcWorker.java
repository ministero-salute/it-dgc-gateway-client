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
package it.interop.eucert.gateway.worker;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.interop.eucert.gateway.batchsigning.BatchSignatureVerifier;
import it.interop.eucert.gateway.batchsigning.SignatureGenerator;
import it.interop.eucert.gateway.client.RestApiClient;
import it.interop.eucert.gateway.client.base.RestApiException;
import it.interop.eucert.gateway.client.base.RestApiResponse;
import it.interop.eucert.gateway.dto.SignedCertificateDto;
import it.interop.eucert.gateway.dto.TrustListDto;
import it.interop.eucert.gateway.entity.DgcLogEntity;
import it.interop.eucert.gateway.entity.DgcLogEntity.OperationType;
import it.interop.eucert.gateway.entity.DgcLogInfo;
import it.interop.eucert.gateway.entity.SignerInformationEntity;
import it.interop.eucert.gateway.entity.SignerInvalidInformationEntity;
import it.interop.eucert.gateway.entity.SignerUploadInformationEntity;
import it.interop.eucert.gateway.mapper.EucertMapper;
import it.interop.eucert.gateway.repository.DgcLogRepository;
import it.interop.eucert.gateway.repository.SignerInformationRepository;
import it.interop.eucert.gateway.repository.SignerInvalidInformationRepository;
import it.interop.eucert.gateway.repository.SignerUploadInformationRepository;
import it.interop.eucert.gateway.util.Util;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Slf4j
@Service
public class DgcWorker {

	@Getter
	@Value("${dgc.origin_country}")
	private String originCountry;
	
	@Value("${dgc.data_retention_days}")
	private String dataRetentionDays;

	@Autowired(required=true)
	private RestApiClient client;

	@Autowired(required=true)
	private SignerUploadInformationRepository signerUploadInformationRepository;

	@Autowired(required=true)
	private SignerInvalidInformationRepository signerInvalidInformationRepository;
	
	@Autowired(required=true)
	private SignerInformationRepository signerInformationRepository;

	@Autowired(required=true)
	private DgcLogRepository dgcLogRepository;

	@Autowired(required=true)
	private SignatureGenerator signatureGenerator;

	@Autowired(required=true)
	private BatchSignatureVerifier batchSignatureVerifier;
	
	@Scheduled(cron = "${dgc.worker.upload.schedul}")
	@SchedulerLock(name = "DgcWorker_uploadWorker")
	public void uploadWorker() {
		log.info("@@@  UPLOAD -> START Processing upload. @@@");

		List<SignerUploadInformationEntity> toSendSignerInformationList = signerUploadInformationRepository.getSignerInformationToSend();
		if (toSendSignerInformationList != null) {
			for (SignerUploadInformationEntity signerInformation:toSendSignerInformationList) {
				send(signerInformation);
			}
			
		}
		
		List<SignerUploadInformationEntity> toRevokeSignerInformationList = signerUploadInformationRepository.getSignerInformationToRevoke();
		if (toRevokeSignerInformationList != null) {
			for (SignerUploadInformationEntity signerInformation:toRevokeSignerInformationList) {
				revoke(signerInformation);
			}
			
		}

		log.info("@@@  UPLOAD -> END Processing upload. @@@");
	}

	
	@Scheduled(cron = "${dgc.worker.download.schedul}")
	@SchedulerLock(name = "DgcWorker_downloadWorker")
	public void downloadWorker() {
		log.info("###  DOWNLOAD -> START Processing download. ###");
		download();
		log.info("###  DOWNLOAD -> END Processing download. ####");
	}


	
	
	@Transactional
	private String send(SignerUploadInformationEntity signerInformationEntity) {
		String report = null;
		String batchTag = Util.batchTagGenerator(OperationType.UPLOAD);

		try {
			
			if (signerInformationEntity != null) {
				SignedCertificateDto signedCertificateDto = null;
				RestApiResponse<String> resp = client.postVerificationInformation(signedCertificateDto, originCountry);
				report = resp.getStatusCode().toString();
				
				if (resp.getStatusCode() == RestApiClient.DOWNLOAD_STATUS_RETURNS_BATCH_200) {
					signerInformationEntity.setUploadBatchTag(batchTag);
					signerUploadInformationRepository.save(signerInformationEntity);
				}

			}
			
		} catch (RestApiException e) {
			report = e.getMessage();
			log.error("ERROR Processing upload RestApiException. -> batchTag: {} ", batchTag, e);
		}
		log.info("Upload INFO after sending -> batchTag: {} ", batchTag);
		
		dgcLogRepository.save(DgcLogEntity.buildUploadDgcLog(originCountry, batchTag, report));
		
		return report;
	}
	
	@Transactional
	private String revoke(SignerUploadInformationEntity signerInformationEntity) {
		String report = null;
		String batchTag = Util.batchTagGenerator(OperationType.REVOKE);

		try {
			
			if (signerInformationEntity != null) {
				SignedCertificateDto signedCertificateDto = null;
				RestApiResponse<String> resp = client.revokeVerificationInformation(signedCertificateDto, originCountry);
				report = resp.getStatusCode().toString();
				
				if (resp.getStatusCode() == RestApiClient.DOWNLOAD_STATUS_RETURNS_BATCH_200) {
					signerInformationEntity.setRevokedBatchTag(batchTag);
					signerUploadInformationRepository.save(signerInformationEntity);
				}

			}
			
		} catch (RestApiException e) {
			report = e.getMessage();
			log.error("ERROR Processing upload RestApiException. -> batchTag: {} ", batchTag, e);
		}
		log.info("Upload INFO after sending -> batchTag: {} ", batchTag);
		
		dgcLogRepository.save(DgcLogEntity.buildRevokeDgcLog(originCountry, batchTag, report));
		
		return report;
	}
	
	
	@Transactional
	private void download() {
		String report = null;
		
		String batchTag = Util.batchTagGenerator(OperationType.DOWNLOAD);
		DgcLogInfo dgcLogInfo = new DgcLogInfo();
		
		try {
			RestApiResponse<List<TrustListDto>> resp = client.downloadTrustList();
			report = resp.getStatusCode().toString();
			
			if (resp.getStatusCode() == RestApiClient.DOWNLOAD_STATUS_RETURNS_BATCH_200) {
				
				if (resp.getData() != null) {
					//Revoco tutti icertificati
					Integer numNewDoc = 0;
					Integer numInvalidDoc = 0;
					Integer numOldDoc = 0;
					Integer numTotDoc = signerInformationRepository.setAllTrustedPartyRevoked(batchTag);
					List<TrustListDto> trustList = resp.getData();
					dgcLogInfo.setNumTotDoc(numTotDoc);
					dgcLogInfo.setNumDocFlusso(trustList.size());
					
					Long index = signerInformationRepository.maxIndex();
					
					for (TrustListDto trustListDto:trustList) {
						SignerInformationEntity trustedPartyEntity = signerInformationRepository.getByKid(trustListDto.getKid());
						if (trustedPartyEntity!=null) {
							//I certificati già presenti nel DB vengono riabilitati
							trustedPartyEntity.setRevoked(false);
							trustedPartyEntity.setRevokedDate(null);
							trustedPartyEntity.setRevokedBatchTag(null);
							signerInformationRepository.save(trustedPartyEntity);
							numOldDoc++;
						} else {
							//I certificati non presenti nel DB vengono inseriti e flaggati da pubblicare
							boolean verifiedSign = batchSignatureVerifier.verify(trustListDto.getRawData(), trustListDto.getSignature(), trustListDto.getThumbprint());
							if (verifiedSign) {
								trustedPartyEntity = EucertMapper.trustListDtoToEntity(trustListDto);
								trustedPartyEntity.setDownloadBatchTag(batchTag);
								trustedPartyEntity.setIndex(++index);
								signerInformationRepository.save(trustedPartyEntity);
							} else {
								SignerInvalidInformationEntity signerInvalidInformationEntity = EucertMapper.invalidTrustListDtoToEntity(trustListDto);
								signerInvalidInformationEntity.setDownloadBatchTag(batchTag);
								signerInvalidInformationRepository.save(signerInvalidInformationEntity);
								numInvalidDoc++;
							}
							numNewDoc++;
						}
					}
					dgcLogInfo.setNumInvalidDoc(numInvalidDoc);
					dgcLogInfo.setNumRevokedDoc(numTotDoc - numNewDoc - numOldDoc - numInvalidDoc);
				}
			}

		} catch (RestApiException e) {
			report = e.getMessage();
			log.error("ERROR Processing download RestApiException. -> batchTag: {} ", batchTag, e);
		}
		log.info("Download INFO after reciving -> batchTag: {} ", batchTag);

		dgcLogRepository.save(DgcLogEntity.buildDownloadDgcLog(originCountry, batchTag, dgcLogInfo, report));
	}
	
	
}
