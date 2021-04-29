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
package it.interop.dgc.gateway.worker;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.interop.dgc.gateway.batchsigning.BatchSignatureVerifier;
import it.interop.dgc.gateway.batchsigning.SignatureGenerator;
import it.interop.dgc.gateway.client.RestApiClient;
import it.interop.dgc.gateway.client.base.RestApiException;
import it.interop.dgc.gateway.client.base.RestApiResponse;
import it.interop.dgc.gateway.dto.SignedCertificateDto;
import it.interop.dgc.gateway.dto.TrustListDto;
import it.interop.dgc.gateway.entity.DgcLogEntity;
import it.interop.dgc.gateway.entity.DgcLogEntity.OperationType;
import it.interop.dgc.gateway.entity.DgcLogInfo;
import it.interop.dgc.gateway.entity.SignerInformationEntity;
import it.interop.dgc.gateway.entity.TrustedPartyEntity;
import it.interop.dgc.gateway.mapper.DgcMapper;
import it.interop.dgc.gateway.repository.DgcLogRepository;
import it.interop.dgc.gateway.repository.SignerInformationRepository;
import it.interop.dgc.gateway.repository.TrustedPartyRepository;
import it.interop.dgc.gateway.util.Util;
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
	private TrustedPartyRepository trustedPartyRepository;
	
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

		List<SignerInformationEntity> toSendSignerInformationList = signerInformationRepository.getSignerInformationToSend();
		if (toSendSignerInformationList != null) {
			for (SignerInformationEntity signerInformation:toSendSignerInformationList) {
				send(signerInformation);
			}
			
		}
		
		List<SignerInformationEntity> toRevokeSignerInformationList = signerInformationRepository.getSignerInformationToRevoke();
		if (toRevokeSignerInformationList != null) {
			for (SignerInformationEntity signerInformation:toRevokeSignerInformationList) {
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
	private String send(SignerInformationEntity signerInformationEntity) {
		String report = null;
		String batchTag = Util.batchTagGenerator(OperationType.UPLOAD);

		try {
			
			if (signerInformationEntity != null) {
				SignedCertificateDto signedCertificateDto = null;
				RestApiResponse<String> resp = client.postVerificationInformation(signedCertificateDto, originCountry);
				report = resp.getStatusCode().toString();
				
				if (resp.getStatusCode() == RestApiClient.DOWNLOAD_STATUS_RETURNS_BATCH_200) {
					signerInformationEntity.setSendBatchTag(batchTag);
					signerInformationRepository.setSendBatchTag(signerInformationEntity);
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
	private String revoke(SignerInformationEntity signerInformationEntity) {
		String report = null;
		String batchTag = Util.batchTagGenerator(OperationType.UPLOAD);

		try {
			
			if (signerInformationEntity != null) {
				SignedCertificateDto signedCertificateDto = null;
				RestApiResponse<String> resp = client.revokeVerificationInformation(signedCertificateDto, originCountry);
				report = resp.getStatusCode().toString();
				
				if (resp.getStatusCode() == RestApiClient.DOWNLOAD_STATUS_RETURNS_BATCH_200) {
					signerInformationEntity.setRevokedBatchTag(batchTag);
					signerInformationRepository.setRevokeBatchTag(signerInformationEntity);
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
					Integer numTotDoc = trustedPartyRepository.setAllTrustedPartyRevoked();
					List<TrustListDto> trustList = resp.getData();
					dgcLogInfo.setNumTotDoc(numTotDoc);
					dgcLogInfo.setNumDocFlusso(trustList.size());
					for (TrustListDto trustListDto:trustList) {
						TrustedPartyEntity trustedPartyEntity = trustedPartyRepository.getByKid(trustListDto.getKid());
						if (trustedPartyEntity!=null) {
							//I certificati già presenti nel DB vengono riabilitati
							trustedPartyEntity.setRevoked(false);
							trustedPartyEntity.setRevokedDate(null);
							numOldDoc++;
						} else {
							//I certificati non presenti nel DB vengono inseriti e flaggati da pubblicare
							boolean verifiedSign = batchSignatureVerifier.verify(trustListDto.getRawData(), trustListDto.getSignature(), trustListDto.getThumbprint());
							trustedPartyEntity = DgcMapper.trustListDtoToEntity(trustListDto);
							trustedPartyEntity.setBatchTag(batchTag);
							trustedPartyEntity.setVerifiedSign(verifiedSign);
							if (!verifiedSign) {
								trustedPartyEntity.setToPublish(false);
								numInvalidDoc++;
							} else {
								trustedPartyEntity.setToPublish(true);
							}
							numNewDoc++;
						}
						trustedPartyRepository.save(trustedPartyEntity);
					}
					dgcLogInfo.setNumRevokedDoc(numTotDoc - numNewDoc - numOldDoc);
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
