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
package it.interop.dgc.gateway.worker;

import java.lang.reflect.Type;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import it.interop.dgc.gateway.akamai.AkamaiFastPurge;
import it.interop.dgc.gateway.client.RestApiClient;
import it.interop.dgc.gateway.client.base.RestApiException;
import it.interop.dgc.gateway.client.base.RestApiResponse;
import it.interop.dgc.gateway.dto.RevocationBatchListItemDto;
import it.interop.dgc.gateway.dto.RevocationItemDto;
import it.interop.dgc.gateway.dto.TrustListItemDto;
import it.interop.dgc.gateway.dto.ValidationBatchDto;
import it.interop.dgc.gateway.dto.ValidationRuleDto;
import it.interop.dgc.gateway.entity.BatchesDownloadEntity;
import it.interop.dgc.gateway.entity.BusinessRuleEntity;
import it.interop.dgc.gateway.entity.BusinessRuleInvalidEntity;
import it.interop.dgc.gateway.entity.BusinessRuleUploadEntity;
import it.interop.dgc.gateway.entity.CountryListEntity;
import it.interop.dgc.gateway.entity.DgcLogAmount;
import it.interop.dgc.gateway.entity.DgcLogEntity;
import it.interop.dgc.gateway.entity.DgcLogEntity.OperationType;
import it.interop.dgc.gateway.entity.DgcLogInfo;
import it.interop.dgc.gateway.entity.DgcRuleLogAmount;
import it.interop.dgc.gateway.entity.DgcRuleLogEntity;
import it.interop.dgc.gateway.entity.DgcRuleLogInfo;
import it.interop.dgc.gateway.entity.RevocationBatchEntity;
import it.interop.dgc.gateway.entity.SignerInformationEntity;
import it.interop.dgc.gateway.entity.SignerInvalidInformationEntity;
import it.interop.dgc.gateway.entity.SignerUploadInformationEntity;
import it.interop.dgc.gateway.entity.ValueSetEntity;
import it.interop.dgc.gateway.enums.CertificateType;
import it.interop.dgc.gateway.mapper.DgcMapper;
import it.interop.dgc.gateway.model.ValidationBatch;
import it.interop.dgc.gateway.model.ValidationRule;
import it.interop.dgc.gateway.repository.BatchesDownloadRepository;
import it.interop.dgc.gateway.repository.BusinessRuleInvalidRepository;
import it.interop.dgc.gateway.repository.BusinessRuleRepository;
import it.interop.dgc.gateway.repository.BusinessRuleUploadRepository;
import it.interop.dgc.gateway.repository.CountryListRepository;
import it.interop.dgc.gateway.repository.DgcLogRepository;
import it.interop.dgc.gateway.repository.DgcRuleLogRepository;
import it.interop.dgc.gateway.repository.RevocationBatchRepository;
import it.interop.dgc.gateway.repository.SignerInformationRepository;
import it.interop.dgc.gateway.repository.SignerInvalidInformationRepository;
import it.interop.dgc.gateway.repository.SignerUploadInformationRepository;
import it.interop.dgc.gateway.repository.ValueSetRepository;
import it.interop.dgc.gateway.signing.CertificateSignatureVerifier;
import it.interop.dgc.gateway.signing.SignatureService;
import it.interop.dgc.gateway.util.BusinessRulesUtils;
import it.interop.dgc.gateway.util.DscUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DgcWorker {

    @Getter
    @Value("${dgc.origin_country}")
    private String originCountry;

    @Autowired(required = true)
    private RestApiClient client;

    @Autowired(required = true)
    private SignerUploadInformationRepository signerUploadInformationRepository;

    @Autowired(required = true)
    private SignerInvalidInformationRepository signerInvalidInformationRepository;

    @Autowired(required = true)
    private SignerInformationRepository signerInformationRepository;

    @Autowired(required = true)
    private DgcLogRepository dgcLogRepository;

    @Autowired(required = true)
    private DgcRuleLogRepository dgcRuleLogRepository;

    @Autowired(required = true)
    private BusinessRuleRepository businessRuleRepository;

    @Autowired(required = true)
    private BusinessRuleInvalidRepository businessRuleInvalidRepository;

    @Autowired(required = true)
    private BusinessRuleUploadRepository businessRuleUploadRepository;

    @Autowired(required = true)
    private CountryListRepository countryListRepository;

    @Autowired(required = true)
    private ValueSetRepository valueSetRepository;
    
    @Autowired(required = true)
    private BatchesDownloadRepository batchesDownloadRepository;

    @Autowired(required = true)
    private SignatureService signatureService;

    @Autowired(required = true)
    private CertificateSignatureVerifier signatureVerifier;

    @Autowired(required = true)
    private AkamaiFastPurge akamaiFastPurge;

    @Autowired(required = true)
    private BusinessRulesUtils businessRulesUtils;
    
    @Autowired(required = true)
    private RevocationBatchRepository revocationBatchRepository;

    @Scheduled(cron = "${dgc.worker.upload.schedul}")
    public void uploadWorker() {
        log.info("@@@  UPLOAD -> START Processing upload. @@@");

        log.info("@  UPLOAD CERT -> START Processing upload. @");
        List<SignerUploadInformationEntity> toSendSignerInformationList = signerUploadInformationRepository.getSignerInformationToSend();
        if (toSendSignerInformationList != null) {
            for (SignerUploadInformationEntity signerInformation : toSendSignerInformationList) {
                send(signerInformation);
            }
        }
        log.info("@  UPLOAD CERT -> END Processing upload. @");

        log.info("@  REVOKE CERT -> START Processing revoke. @");
        List<SignerUploadInformationEntity> toRevokeSignerInformationList = signerUploadInformationRepository.getSignerInformationToRevoke();
        if (toRevokeSignerInformationList != null) {
            for (SignerUploadInformationEntity signerInformation : toRevokeSignerInformationList) {
                revoke(signerInformation);
            }
        }
        log.info("@  REVOKE CERT -> END Processing revoke. @");

        log.info("@  UPLOAD RULE -> START Processing upload. @");
        List<BusinessRuleUploadEntity> toSendBusinessRuleList = businessRuleUploadRepository.getSignerInformationToSend();
        if (toSendBusinessRuleList != null) {
            for (BusinessRuleUploadEntity businessRuleUploadEntity : toSendBusinessRuleList) {
                sendBusinessRule(businessRuleUploadEntity);
            }
        }
        log.info("@  UPLOAD RULE -> END Processing upload. @");

        log.info("@  REVOKE RULE -> START Processing revoke. @");
        List<BusinessRuleUploadEntity> toRevokeBusinessRuleList = businessRuleUploadRepository.getSignerInformationToRevoke();
        if (toRevokeBusinessRuleList != null) {
            for (BusinessRuleUploadEntity businessRuleUploadEntity : toRevokeBusinessRuleList) {
                revokeBusinessRule(businessRuleUploadEntity);
            }
        }
        log.info("@  REVOKE RULE -> END Processing revoke. @");

        log.info("@@@  UPLOAD -> END Processing upload. @@@");
    }

    @Scheduled(cron = "${dgc.worker.download.schedul}")
    public void downloadWorker() {
        log.info("###  DOWNLOAD -> START Processing download. ###");

        log.info("#  DOWNLOAD CERT -> START Processing download. #");
        download();
        log.info("#  DOWNLOAD CERT -> END Processing download. #");

        log.info("#  DOWNLOAD COUNTIES -> START Processing download. #");
        downloadCountry();
        log.info("#  DOWNLOAD COUNTIES -> END Processing download. #");

        log.info("#  DOWNLOAD VALUES -> START Processing download. #");
        downloadValues();
        log.info("#  DOWNLOAD VALUES -> END Processing download. #");

        log.info("#  DOWNLOAD RULES -> START Processing download. #");
        downloadRules();
        log.info("#  DOWNLOAD RULES -> END Processing download. #");

        log.info("###  DOWNLOAD -> END Processing download. ###");
    }

    @Transactional
    private String send(SignerUploadInformationEntity signerInformationEntity) {
        String report = null;
        String batchTag = DscUtil.batchTagGenerator(OperationType.UPLOAD);

        DgcLogInfo dgcLogInfo = new DgcLogInfo(signerInformationEntity);
        try {
            if (signerInformationEntity != null) {
                String signedCertificate = signatureService.getSignatureForBytes(
                    signerInformationEntity.getRawData()
                );
                dgcLogInfo.setVerifiedSign(true);

                RestApiResponse<String> resp = client.postVerificationInformation(
                    signedCertificate,
                    originCountry
                );
                report = resp.getStatusCode().toString();

                if (
                    resp.getStatusCode() ==
                    RestApiClient.UPLOAD_STATUS_CREATED_201
                ) {
                    signerInformationEntity.setUploadBatchTag(batchTag);
                    signerUploadInformationRepository.save(
                        signerInformationEntity
                    );
                }
            }
        } catch (Exception e) {
            report = e.getMessage();
            log.error(
                "ERROR Processing upload Exception. -> batchTag: {} ",
                batchTag,
                e
            );
        }
        log.info("Upload INFO after sending -> batchTag: {} ", batchTag);

        dgcLogRepository.save(
            DgcLogEntity.buildUploadDgcLog(
                originCountry,
                batchTag,
                report,
                dgcLogInfo
            )
        );

        return report;
    }

    @Transactional
    private String revoke(
        SignerUploadInformationEntity signerInformationEntity
    ) {
        String report = null;
        String batchTag = DscUtil.batchTagGenerator(OperationType.REVOKE);

        DgcLogInfo dgcLogInfo = new DgcLogInfo(signerInformationEntity);
        try {
            if (signerInformationEntity != null) {
                String signedCertificate = signatureService.getSignatureForBytes(
                    signerInformationEntity.getRawData()
                );
                dgcLogInfo.setVerifiedSign(true);

                RestApiResponse<String> resp = client.revokeVerificationInformation(
                    signedCertificate,
                    originCountry
                );
                report = resp.getStatusCode().toString();

                if (
                    resp.getStatusCode() ==
                    RestApiClient.UPLOAD_STATUS_NO_CONTENT_204
                ) {
                    signerInformationEntity.setRevokedDate(new Date());
                    signerInformationEntity.setRevokedBatchTag(batchTag);
                    signerUploadInformationRepository.save(
                        signerInformationEntity
                    );
                }
            }
        } catch (Exception e) {
            report = e.getMessage();
            log.error(
                "ERROR Processing upload Exception. -> batchTag: {} ",
                batchTag,
                e
            );
        }
        log.info("Upload INFO after sending -> batchTag: {} ", batchTag);

        dgcLogRepository.save(
            DgcLogEntity.buildRevokeDgcLog(
                originCountry,
                batchTag,
                report,
                dgcLogInfo
            )
        );

        return report;
    }

    @Transactional
    private void download() {
        String report = null;
        String akamaiReport = null;

        String batchTag = DscUtil.batchTagGenerator(OperationType.DOWNLOAD);
        List<DgcLogInfo> dgcLogInfoList = new ArrayList<DgcLogInfo>();

        DgcLogAmount dgcLogAmount = new DgcLogAmount();

        try {
            RestApiResponse<List<TrustListItemDto>> resp = client.downloadTrustList();
            report = resp.getStatusCode().toString();

            if (
                resp.getStatusCode() ==
                RestApiClient.DOWNLOAD_STATUS_RETURNS_BATCH_200
            ) {
                if (resp.getData() != null) {
                    //Verifica firme
                    List<TrustListItemDto> trustList = resp
                        .getData()
                        .stream()
                        .filter(cer ->
                            cer.getCertificateType() == CertificateType.CSCA ||
                            cer.getCertificateType() == CertificateType.DSC
                        )
                        .collect(Collectors.toList());

                    List<TrustListItemDto> trustListCsca = trustList
                        .stream()
                        .filter(csca ->
                            csca.getCertificateType() == CertificateType.CSCA
                        )
                        .collect(Collectors.toList());

                    //Verifica firme CSCA
                    if (trustListCsca != null && trustListCsca.size() > 0) {
                        trustListCsca.forEach(csca -> {
                            csca.setVerifiedSign(
                                signatureVerifier.checkTrustAnchorSignature(
                                    csca
                                )
                            );
                        });

                        List<TrustListItemDto> trustListDsc = trustList
                            .stream()
                            .filter(dsc ->
                                dsc.getCertificateType() == CertificateType.DSC
                            )
                            .collect(Collectors.toList());

                        //Verifica firme DSC
                        if (trustListDsc != null && trustListDsc.size() > 0) {
                            trustListDsc.forEach(dsc -> {
                                dsc.setVerifiedSign(false);
                                List<TrustListItemDto> trustListCscaCountry = trustListCsca
                                    .stream()
                                    .filter(csca ->
                                        csca
                                            .getCountry()
                                            .equals(dsc.getCountry()) &&
                                        csca.isVerifiedSign()
                                    )
                                    .collect(Collectors.toList());
                                if (
                                    trustListCscaCountry != null &&
                                    trustListCscaCountry.size() > 0
                                ) {
                                    for (TrustListItemDto csca : trustListCscaCountry) {
                                        boolean isVerified = signatureVerifier.trustListItemSignedByCa(
                                            dsc,
                                            csca
                                        );
                                        dsc.setVerifiedSign(isVerified);
                                        if (isVerified) {
                                            break;
                                        }
                                    }
                                }
                            });
                        }

                        Integer numTotDocIntoDB = signerInformationRepository.setAllTrustedPartyRevoked(
                            batchTag
                        );
                        dgcLogAmount.setNumCsca(trustListCsca.size());
                        dgcLogAmount.setNumDsc(trustListDsc.size());

                        Long resumeToken = signerInformationRepository.maxResumeToken();

                        for (TrustListItemDto trustListItemDto : trustList) {
                            DgcLogInfo dgcLogInfo = new DgcLogInfo(
                                trustListItemDto
                            );
                            SignerInformationEntity trustedPartyEntity = signerInformationRepository.getByThumbprint(
                                trustListItemDto.getThumbprint(),
                                batchTag
                            );
                            dgcLogInfo.setAlreadyExists(
                                trustedPartyEntity != null
                            );
                            if (trustedPartyEntity != null) {
                                //I certificati già presenti nel DB vengono riabilitati
                                trustedPartyEntity.setRevoked(false);
                                trustedPartyEntity.setRevokedDate(null);
                                trustedPartyEntity.setRevokedBatchTag(null);
                                signerInformationRepository.save(
                                    trustedPartyEntity
                                );
                            } else {
                                //I certificati non presenti nel DB vengono inseriti e flaggati da pubblicare
                                if (trustListItemDto.isVerifiedSign()) {
                                    trustedPartyEntity =
                                        DgcMapper.trustListDtoToEntity(
                                            trustListItemDto
                                        );
                                    trustedPartyEntity.setDownloadBatchTag(
                                        batchTag
                                    );
                                    trustedPartyEntity.setCreatedAt(new Date());
                                    if (
                                        trustListItemDto.getCertificateType() ==
                                        CertificateType.CSCA
                                    ) {
                                        trustedPartyEntity.setResumeToken(null);
                                        dgcLogAmount.incNumNewCsca();
                                    } else {
                                        trustedPartyEntity.setResumeToken(
                                            ++resumeToken
                                        );
                                        dgcLogAmount.incNumNewDsc();
                                    }
                                    signerInformationRepository.save(
                                        trustedPartyEntity
                                    );
                                } else {
                                    SignerInvalidInformationEntity signerInvalidInformationEntity = DgcMapper.invalidTrustListDtoToEntity(
                                        trustListItemDto
                                    );
                                    signerInvalidInformationEntity.setDownloadBatchTag(
                                        batchTag
                                    );
                                    if (
                                        trustListItemDto.getCertificateType() ==
                                        CertificateType.CSCA
                                    ) {
                                        dgcLogAmount.incNumInvalidCsca();
                                    } else {
                                        dgcLogAmount.incNumInvalidDsc();
                                    }
                                    signerInvalidInformationRepository.save(
                                        signerInvalidInformationEntity
                                    );
                                }
                            }
                            dgcLogInfoList.add(dgcLogInfo);
                        }
                        Integer numRevoked =
                            numTotDocIntoDB -
                            (
                                dgcLogAmount.getNumCsca() -
                                dgcLogAmount.getNumNewCsca()
                            ) -
                            (
                                dgcLogAmount.getNumDsc() -
                                dgcLogAmount.getNumNewDsc()
                            );
                        dgcLogAmount.setNumRevoked(
                            (numRevoked > 0 ? numRevoked : 0)
                        );
                    }
                }

                try {
                    if (
                        akamaiFastPurge.getUrl() != null &&
                        !"".equals(akamaiFastPurge.getUrl())
                    ) {
                        akamaiReport = akamaiFastPurge.invalidateUrls();
                    }
                } catch (Exception e) {
                    akamaiReport = "ERROR INVALIDATING AKAMAI CACHE";
                    log.error(
                        "ERROR Invalidating akamai cache. -> batchTag: {} ",
                        batchTag,
                        e
                    );
                }
            }
        } catch (Exception e) {
            report = e.getMessage();
            log.error(
                "ERROR Processing download Exception. -> batchTag: {} ",
                batchTag,
                e
            );
        }
        log.info("Download INFO after reciving -> batchTag: {} ", batchTag);

        dgcLogRepository.save(
            DgcLogEntity.buildDownloadDgcLog(
                "ALL",
                batchTag,
                report,
                akamaiReport,
                dgcLogInfoList,
                dgcLogAmount
            )
        );
    }

    //BUSINESS RULE

    @Transactional
    public String sendBusinessRule(
        BusinessRuleUploadEntity businessRuleUploadEntity
    ) {
        String report = null;
        String batchTag = DscUtil.batchTagGenerator(OperationType.UPLOAD);

        try {
            if (businessRuleUploadEntity != null) {
                String base64RawData = Base64
                    .getEncoder()
                    .encodeToString(
                        businessRuleUploadEntity.getRawData().getBytes()
                    );
                String signedCertificate = signatureService.getSignatureForBytes(
                    base64RawData
                );

                RestApiResponse<String> resp = client.uploadValidationRule(
                    signedCertificate,
                    originCountry
                );
                report = resp.getStatusCode().toString();

                if (
                    resp.getStatusCode() ==
                    RestApiClient.UPLOAD_STATUS_CREATED_201
                ) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    ValidationRule parsedRule = objectMapper.readValue(
                        businessRuleUploadEntity.getRawData(),
                        ValidationRule.class
                    );
                    businessRuleUploadEntity.setIdentifier(
                        parsedRule.getIdentifier()
                    );
                    businessRuleUploadEntity.setVersion(
                        parsedRule.getVersion()
                    );
                    businessRuleUploadEntity.setUploadBatchTag(batchTag);
                    businessRuleUploadRepository.save(businessRuleUploadEntity);
                }
            }
        } catch (Exception e) {
            report = e.getMessage();
            log.error(
                "ERROR Processing upload RestApiException. -> batchTag: {} ",
                batchTag,
                e
            );
        }
        log.info("Upload INFO after sending -> batchTag: {} ", batchTag);

        dgcRuleLogRepository.save(
            DgcRuleLogEntity.buildUploadRuleLog(batchTag, report)
        );

        return report;
    }

    @Transactional
    private String revokeBusinessRule(
        BusinessRuleUploadEntity businessRuleUploadEntity
    ) {
        String report = null;
        String batchTag = DscUtil.batchTagGenerator(OperationType.REVOKE);

        try {
            if (businessRuleUploadEntity != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                ValidationRule parsedRule = objectMapper.readValue(
                    businessRuleUploadEntity.getRawData(),
                    ValidationRule.class
                );

                String base64RawData = Base64
                    .getEncoder()
                    .encodeToString(parsedRule.getIdentifier().getBytes());
                String signedCertificate = signatureService.getSignatureForBytes(
                    base64RawData
                );

                RestApiResponse<String> resp = client.deleteValidationRules(
                    signedCertificate,
                    originCountry
                );
                report = resp.getStatusCode().toString();

                if (
                    resp.getStatusCode() ==
                    RestApiClient.UPLOAD_STATUS_NO_CONTENT_204
                ) {
                    businessRuleUploadEntity.setRevokedDate(new Date());
                    businessRuleUploadEntity.setRevokedBatchTag(batchTag);
                    businessRuleUploadRepository.save(businessRuleUploadEntity);
                }
            }
        } catch (Exception e) {
            report = e.getMessage();
            log.error(
                "ERROR Processing upload Exception. -> batchTag: {} ",
                batchTag,
                e
            );
        }
        log.info("Upload INFO after sending -> batchTag: {} ", batchTag);

        dgcRuleLogRepository.save(
            DgcRuleLogEntity.buildRevokeRuleLog(batchTag, report)
        );

        return report;
    }

    public void downloadCountry() {
        String report = null;
        String batchTag = DscUtil.batchTagGenerator(OperationType.DOWNLOAD);

        try {
            RestApiResponse<String> resp = client.downloadCountryList();
            report = resp.getStatusCode().toString();

            if (
                resp.getStatusCode() ==
                RestApiClient.DOWNLOAD_STATUS_RETURNS_BATCH_200
            ) {
                String countries = resp.getData();
                String hash = businessRulesUtils.calculateHash(countries);

                CountryListEntity countryListEntity = new CountryListEntity();
                countryListEntity.setCountryListId(1L);
                countryListEntity.setHash(hash);
                countryListEntity.setRawData(countries);
                countryListEntity.setDownloadBatchTag(batchTag);
                countryListEntity.setCreatedAt(new Date());

                countryListRepository.deleteAll();
                countryListRepository.save(countryListEntity);
            }
        } catch (NoSuchAlgorithmException e) {
            report = e.getMessage();
            log.error(
                "ERROR Processing download NoSuchAlgorithmException. -> batchTag: {} ",
                batchTag,
                e
            );
        } catch (Exception e) {
            report = e.getMessage();
            log.error(
                "ERROR Processing download Exception. -> batchTag: {} ",
                batchTag,
                e
            );
        }
        log.info("Download INFO -> report: {} ", report);

        dgcRuleLogRepository.save(
            DgcRuleLogEntity.buildDownloadCountyLog(batchTag, report)
        );
    }

    public void downloadValues() {
        String report = null;
        String batchTag = DscUtil.batchTagGenerator(OperationType.DOWNLOAD);

        try {
            RestApiResponse<List<String>> resp = client.getValuesetIds();
            report = resp.getStatusCode().toString();

            List<ValueSetEntity> valueSetEntityList = new ArrayList<ValueSetEntity>();

            if (
                resp.getStatusCode() ==
                RestApiClient.DOWNLOAD_STATUS_RETURNS_BATCH_200
            ) {
                List<String> valuesetIds = resp.getData();
                log.info(
                    "Download INFO after reciving -> valuesetIds: {} ",
                    valuesetIds
                );
                if (valuesetIds != null) {
                    for (String identifier : valuesetIds) {
                        RestApiResponse<String> valuesResp = client.getValueset(
                            identifier
                        );
                        if (
                            resp.getStatusCode() ==
                            RestApiClient.DOWNLOAD_STATUS_RETURNS_BATCH_200
                        ) {
                            String values = valuesResp.getData();
                            String hash = businessRulesUtils.calculateHash(
                                values
                            );

                            ValueSetEntity valueSetEntity = new ValueSetEntity();
                            valueSetEntity.setIdentifier(identifier);
                            valueSetEntity.setHash(hash);
                            valueSetEntity.setRawData(values);
                            valueSetEntity.setDownloadBatchTag(batchTag);
                            valueSetEntity.setCreatedAt(new Date());

                            valueSetEntityList.add(valueSetEntity);
                        }
                    }
                }
            }

            if (valueSetEntityList.size() > 0) {
                valueSetRepository.deleteAll();
                valueSetRepository.saveAll(valueSetEntityList);
            }

            log.info("Download INFO after reciving -> batchTag: {} ", batchTag);
        } catch (NoSuchAlgorithmException e) {
            report = e.getMessage();
            log.error(
                "ERROR Processing download NoSuchAlgorithmException. -> batchTag: {} ",
                batchTag,
                e
            );
        } catch (Exception e) {
            report = e.getMessage();
            log.error(
                "ERROR Processing download Exception. -> batchTag: {} ",
                batchTag,
                e
            );
        }

        dgcRuleLogRepository.save(
            DgcRuleLogEntity.buildDownloadValueLog(batchTag, report)
        );
    }

    public void downloadRules() {
        String report = null;
        String akamaiReport = null;
        String batchTag = DscUtil.batchTagGenerator(OperationType.DOWNLOAD);

        Map<String, List<DgcRuleLogInfo>> logInfo = new HashMap<String, List<DgcRuleLogInfo>>();
        DgcRuleLogAmount amount = new DgcRuleLogAmount();

        try {
            RestApiResponse<List<TrustListItemDto>> resp = client.downloadTrustListFilteredByType(
                CertificateType.UPLOAD
            );
            report = resp.getStatusCode().toString();

            if (
                resp.getStatusCode() ==
                RestApiClient.DOWNLOAD_STATUS_RETURNS_BATCH_200
            ) {
                List<BusinessRuleEntity> businessRuleEntityList = new ArrayList<BusinessRuleEntity>();
                List<BusinessRuleInvalidEntity> businessRuleInvalidEntityList = new ArrayList<BusinessRuleInvalidEntity>();

                List<TrustListItemDto> trustUpload = resp.getData();

                if (trustUpload != null) {
                    Map<String, List<TrustListItemDto>> mapCountryTruest = new HashMap<>();
                    trustUpload.forEach(trust ->
                        mapCountryTruest
                            .computeIfAbsent(
                                trust.getCountry(),
                                k -> new ArrayList<>()
                            )
                            .add(trust)
                    );

                    List<String> countries = _getCountries();

                    for (String country : countries) {
                        RestApiResponse<Map<String, List<ValidationRuleDto>>> ruleResp = client.downloadValidationRules(
                            country
                        );

                        logInfo.put(country, new ArrayList<DgcRuleLogInfo>());

                        if (
                            ruleResp.getStatusCode() ==
                            RestApiClient.DOWNLOAD_STATUS_RETURNS_BATCH_200
                        ) {
                            Map<String, List<ValidationRuleDto>> mapRule = ruleResp.getData();
                            List<TrustListItemDto> trustCountryList = mapCountryTruest.get(
                                country
                            );

                            for (String identifier : mapRule.keySet()) {
                                List<ValidationRuleDto> rules = mapRule.get(
                                    identifier
                                );

                                for (ValidationRuleDto rule : rules) {
                                    ValidationRule validationRule = null;
                                    if (
                                        signatureVerifier.checkRuleUploadCertificate(
                                            rule,
                                            trustCountryList,
                                            country
                                        )
                                    ) {
                                        validationRule =
                                            signatureVerifier.map(rule);
                                    }

                                    if (validationRule != null) {
                                        BusinessRuleEntity businessRuleEntity = new BusinessRuleEntity();
                                        businessRuleEntity.setIdentifier(
                                            validationRule.getIdentifier()
                                        );
                                        businessRuleEntity.setCountry(
                                            validationRule.getCountry()
                                        );
                                        businessRuleEntity.setVersion(
                                            validationRule.getVersion()
                                        );
                                        businessRuleEntity.setHash(
                                            businessRulesUtils.calculateHash(
                                                validationRule.getRawJson()
                                            )
                                        );
                                        businessRuleEntity.setRawData(
                                            validationRule.getRawJson()
                                        );
                                        businessRuleEntity.setDownloadBatchTag(
                                            batchTag
                                        );
                                        businessRuleEntity.setCreatedAt(
                                            new Date()
                                        );

                                        businessRuleEntityList.add(
                                            businessRuleEntity
                                        );
                                    } else {
                                        BusinessRuleInvalidEntity businessRuleEntity = new BusinessRuleInvalidEntity();
                                        businessRuleEntity.setIdentifier(
                                            identifier
                                        );
                                        businessRuleEntity.setCountry(country);
                                        businessRuleEntity.setVersion(
                                            rule.getVersion()
                                        );
                                        businessRuleEntity.setRawData(
                                            rule.getCms()
                                        );
                                        businessRuleEntity.setDownloadBatchTag(
                                            batchTag
                                        );
                                        businessRuleEntity.setCreatedAt(
                                            new Date()
                                        );

                                        businessRuleInvalidEntityList.add(
                                            businessRuleEntity
                                        );
                                    }
                                }
                            }
                        }
                    }

                    if (businessRuleEntityList.size() > 0) {
                        Integer numTotDocIntoDB = businessRuleRepository.setAllBusinessRuleRevoked(
                            batchTag
                        );

                        for (BusinessRuleEntity businessRuleEntity : businessRuleEntityList) {
                            BusinessRuleEntity businessRuleEntityOld = businessRuleRepository.getByCountryAndHash(
                                businessRuleEntity.getCountry(),
                                businessRuleEntity.getHash(),
                                batchTag
                            );
                            if (businessRuleEntityOld != null) {
                                businessRuleEntityOld.setRevoked(false);
                                businessRuleEntityOld.setRevokedDate(null);
                                businessRuleEntityOld.setRevokedBatchTag(null);

                                businessRuleRepository.save(
                                    businessRuleEntityOld
                                );
                                logInfo
                                    .get(businessRuleEntityOld.getCountry())
                                    .add(
                                        new DgcRuleLogInfo(
                                            businessRuleEntityOld.getIdentifier(),
                                            true,
                                            true
                                        )
                                    );
                                amount.incNumOld();
                            } else {
                                businessRuleRepository.save(businessRuleEntity);
                                logInfo
                                    .get(businessRuleEntity.getCountry())
                                    .add(
                                        new DgcRuleLogInfo(
                                            businessRuleEntity.getIdentifier(),
                                            true,
                                            false
                                        )
                                    );
                                amount.incNumNew();
                            }
                        }
                        amount.setNumRevoked(
                            numTotDocIntoDB - amount.getNumOld()
                        );
                    }

                    if (businessRuleInvalidEntityList.size() > 0) {
                        for (BusinessRuleInvalidEntity businessRuleEntity : businessRuleInvalidEntityList) {
                            businessRuleInvalidRepository.save(
                                businessRuleEntity
                            );
                            logInfo
                                .get(businessRuleEntity.getCountry())
                                .add(
                                    new DgcRuleLogInfo(
                                        businessRuleEntity.getIdentifier(),
                                        false,
                                        false
                                    )
                                );
                            amount.incNumInvalid();
                        }
                    }

                    amount.setNum(
                        businessRuleEntityList.size() +
                        businessRuleInvalidEntityList.size()
                    );

                    log.info(
                        "Download INFO after reciving -> batchTag: {} ",
                        batchTag
                    );
                }

                try {
                    if (
                        akamaiFastPurge.getUrl() != null &&
                        !"".equals(akamaiFastPurge.getUrl())
                    ) {
                        akamaiReport = akamaiFastPurge.invalidateRulesUrls();
                    }
                } catch (Exception e) {
                    akamaiReport = "ERROR INVALIDATING AKAMAI CACHE";
                    log.error(
                        "ERROR Invalidating akamai cache. -> batchTag: {} ",
                        batchTag,
                        e
                    );
                }
            }
        } catch (NoSuchAlgorithmException e) {
            report = e.getMessage();
            log.error(
                "ERROR Processing download NoSuchAlgorithmException. -> batchTag: {} ",
                batchTag,
                e
            );
        } catch (RestApiException e) {
            report = e.getMessage();
            log.error(
                "ERROR Processing download Exception. -> batchTag: {} ",
                batchTag,
                e
            );
        }

        dgcRuleLogRepository.save(
            DgcRuleLogEntity.buildDownloadRuleLog(
                batchTag,
                report,
                akamaiReport,
                logInfo,
                amount
            )
        );
    }

    private List<String> _getCountries() {
        List<String> countries = null;

        CountryListEntity countryListEntity = countryListRepository.getCountries();

        if (countryListEntity != null) {
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<String>>() {}.getType();
            countries = gson.fromJson(countryListEntity.getRawData(), listType);
        }

        return countries;
    }
    
	public void downloadRevoche() {
		
		final String FIRST_DATE = "2021-06-01T00:00:00Z";
		final String COUNTRY_IT="IT";
		Boolean more = true;
		String dateHeader = null; 
		RestApiResponse<RevocationItemDto> downloadBatchList = null;
		while(more) {
			more=false;
			BatchesDownloadEntity bde = batchesDownloadRepository.getLastBatch();
			if(bde==null) {
				dateHeader = FIRST_DATE;
			}else {
				ZoneId zoneId = ZoneId.systemDefault();
				Instant instant = bde.getDate().toInstant();
				ZonedDateTime zonedDateTime = instant.atZone(zoneId);
				dateHeader = zonedDateTime.toString();
			}
			try {
				downloadBatchList = client.downloadRevocationList(dateHeader);				
			} catch (RestApiException e) {
				e.printStackTrace();
			}
			if(downloadBatchList.getStatusCode()==HttpStatus.OK) {
				RevocationItemDto revocationItemDto = downloadBatchList.getData();
				more = revocationItemDto.getMore();
			
				List<RevocationBatchListItemDto> undeletedBatches = revocationItemDto.getBatches()
						.stream()
						.filter(boo -> !boo.getDeleted())
						.filter(p1 -> !p1.getCountry().equals(COUNTRY_IT))
						.collect(Collectors.toList());
				
				List<RevocationBatchListItemDto> deletedBatches = revocationItemDto.getBatches()
						.stream()
						.filter(boo -> boo.getDeleted())
						.filter(p1 -> !p1.getCountry().equals(COUNTRY_IT))
						.collect(Collectors.toList());
					
				for(RevocationBatchListItemDto rbli : undeletedBatches ) {
					BatchesDownloadEntity batchesDownloadEntity = new BatchesDownloadEntity();
					batchesDownloadEntity.setBatchId(rbli.getBatchId());
					batchesDownloadEntity.setCountry(rbli.getCountry());
					batchesDownloadEntity.setDate(rbli.getDate());
					batchesDownloadEntity.setDeleted(rbli.getDeleted());
					batchesDownloadRepository.save(batchesDownloadEntity);
				}
				
				for(RevocationBatchListItemDto rbli : deletedBatches ) {
					BatchesDownloadEntity batchesDownloadEntity = new BatchesDownloadEntity();
					batchesDownloadEntity.setBatchId(rbli.getBatchId());
					batchesDownloadEntity.setCountry(rbli.getCountry());
					batchesDownloadEntity.setDate(rbli.getDate());
					batchesDownloadEntity.setDeleted(rbli.getDeleted());
					batchesDownloadRepository.remove(batchesDownloadEntity);
				}
				
				for(RevocationBatchListItemDto batch : undeletedBatches) {
					if(downloadBatch(batch.getBatchId())) {
					}
				}
				if(removeBatch(deletedBatches)) {
					
				}
			}
			
		}
	}


	public Boolean downloadBatch(String batchId) {
		RevocationBatchEntity revocationBatchEntity = new RevocationBatchEntity();
		ValidationBatchDto validationBatchDto = new ValidationBatchDto();

		if (uuidCheck(batchId)) {

			try {
				ValidationBatch validationBatch = null;

				RestApiResponse<String> cms = client.downloadBatch(batchId);
				validationBatchDto.setCms(cms.getData());

				if (signatureVerifier.revocationCheckCmsSignature(validationBatchDto)) {

					validationBatch = signatureVerifier.map(validationBatchDto);

					if (validationBatch != null) {

						revocationBatchEntity.setBatchId(batchId);
						revocationBatchEntity.setCreatedAt(new Date());
						revocationBatchEntity.setExpires(validationBatch.getExpires());
						revocationBatchEntity.setRawData(validationBatch.getRawJson());
						revocationBatchEntity.setEntries(validationBatch.getEntries());

					}else {
						log.error("Problem with validation batch: {}", batchId);
						return false;
					}

				} else {
					log.error("Invalid CMS for Revocation EU​​​​ ", batchId);
					return false;
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			log.error("Batch not valid regex error for ", batchId);
			return false;
		}
		
		// Save on MongoDB
		revocationBatchRepository.save(revocationBatchEntity);
		log.info("Saved on MongoDB -->: {}", batchId);

		
		
		return true;
	}
	
	public Boolean removeBatch(List<RevocationBatchListItemDto> revocationBatchListItemDto){
		
		for(RevocationBatchListItemDto rvlid : revocationBatchListItemDto) {
			RevocationBatchEntity revocationBatchEntity  = new RevocationBatchEntity();
			revocationBatchEntity.setBatchId(rvlid.getBatchId());
			revocationBatchRepository.remove(revocationBatchEntity);

		}
		return true;
	}
	
	public static final String UUID_REGEX = "^[0-9a-f]{8}\\b-[0-9a-f]{4}\\b-[0-9a-f]{4}\\b-[0-9a-f]{4}\\b-[0-9a-f]{12}$";

	private boolean uuidCheck(String UUID) {

		Pattern p = Pattern.compile(UUID_REGEX);

		Matcher m = p.matcher(UUID);

		return m.matches();
	}
}
