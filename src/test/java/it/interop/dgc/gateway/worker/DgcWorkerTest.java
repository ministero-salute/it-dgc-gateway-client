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

package it.interop.dgc.gateway.worker;

import it.interop.dgc.gateway.client.RestApiClient;
import it.interop.dgc.gateway.client.base.RestApiClientBase;
import it.interop.dgc.gateway.client.base.RestApiResponse;
import it.interop.dgc.gateway.dto.TrustListItemDto;
import it.interop.dgc.gateway.dto.ValidationRuleDto;
import it.interop.dgc.gateway.entity.BusinessRuleEntity;
import it.interop.dgc.gateway.entity.BusinessRuleInvalidEntity;
import it.interop.dgc.gateway.entity.BusinessRuleUploadEntity;
import it.interop.dgc.gateway.entity.CountryListEntity;
import it.interop.dgc.gateway.entity.DgcLogEntity;
import it.interop.dgc.gateway.entity.DgcLogEntity.OperationType;
import it.interop.dgc.gateway.entity.DgcLogInfo;
import it.interop.dgc.gateway.entity.DgcRuleLogEntity;
import it.interop.dgc.gateway.entity.SignerInformationEntity;
import it.interop.dgc.gateway.entity.SignerUploadInformationEntity;
import it.interop.dgc.gateway.entity.ValueSetEntity;
import it.interop.dgc.gateway.enums.CertificateType;
import it.interop.dgc.gateway.signing.CertificateSignatureVerifier;
import it.interop.dgc.gateway.signing.SignatureService;
import it.interop.dgc.gateway.util.DscUtil;
import it.interop.dgc.gateway.worker.testdata.DgcWorkerTestHelper;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;

@SpringBootTest(
    properties = {
        "dgc.worker.upload.schedul=0 0 0 29 2 ?",
        "dgc.worker.download.schedul=0 0 0 29 2 ?",
    }
)
@AutoConfigureDataMongo
//prevent @PostConstruct execution
@MockBean(
    classes = {
        SignatureService.class,
        CertificateSignatureVerifier.class,
        RestApiClientBase.class,
        RestApiClient.class,
    }
)
class DgcWorkerTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Mock
    private SignatureService signatureService;

    @Mock
    private RestApiClient restApiClient;

    @Mock
    private CertificateSignatureVerifier signatureVerifier;

    @Autowired
    @InjectMocks
    private DgcWorker worker;

    @BeforeEach
    void clearRepositoryData() {
        mongoTemplate.remove(new Query(), "business_rules");
        mongoTemplate.remove(new Query(), "business_rules_invalid");
        mongoTemplate.remove(new Query(), "business_upload_rules");
        mongoTemplate.remove(new Query(), "countrylist");
        mongoTemplate.remove(new Query(), "valuesets");
        mongoTemplate.remove(new Query(), "signer_upload_information");
        mongoTemplate.remove(new Query(), "signer_information");
        mongoTemplate.remove(new Query(), "signer_upload_information");
        mongoTemplate.remove(new Query(), "dgc_log");
        mongoTemplate.remove(new Query(), "dgc_rule_log");

        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testUploadWorker_uploadNewCert() throws Exception {
        Mockito
            .when(
                signatureService.getSignatureForBytes(
                    DgcWorkerTestHelper.DSC_TO_UPLOAD
                )
            )
            .thenReturn(DgcWorkerTestHelper.SIGNATURE_SERVICE_MOCKDATA);
        Mockito
            .when(
                restApiClient.postVerificationInformation(
                    DgcWorkerTestHelper.SIGNATURE_SERVICE_MOCKDATA,
                    "IT"
                )
            )
            .thenReturn(
                new RestApiResponse<String>(
                    HttpStatus.CREATED,
                    null,
                    "201 CREATED"
                )
            );

        // entity to upload
        SignerUploadInformationEntity entityToUpload = new SignerUploadInformationEntity();
        entityToUpload.setCertificateType(CertificateType.DSC);
        entityToUpload.setCreatedAt(new Date());
        entityToUpload.setRevoked(false);
        entityToUpload.setRawData(DgcWorkerTestHelper.DSC_TO_UPLOAD);
        mongoTemplate.save(entityToUpload);

        worker.uploadWorker();

        Query query = new Query();
        query.addCriteria(
            Criteria.where("raw_data").is(DgcWorkerTestHelper.DSC_TO_UPLOAD)
        );
        SignerUploadInformationEntity uploadedCert = mongoTemplate.findOne(
            query,
            SignerUploadInformationEntity.class
        );
        Assertions.assertNotNull(uploadedCert.getUploadBatchTag());

        Query queryLog = new Query();
        queryLog.addCriteria(
            Criteria.where("batch_tag").is(uploadedCert.getUploadBatchTag())
        );
        DgcLogEntity log = mongoTemplate.findOne(queryLog, DgcLogEntity.class);
        Assertions.assertEquals("IT", log.getCountry());
        Assertions.assertEquals(
            DgcLogEntity.OperationType.UPLOAD,
            log.getOperation()
        );
        Assertions.assertEquals("201 CREATED", log.getExecutionReport());
        List<DgcLogInfo> logInfo = log.getDgcLogInfoList();
        Assertions.assertEquals(1, logInfo.size());
        Assertions.assertEquals(
            CertificateType.DSC,
            logInfo.get(0).getCertificateType()
        );
    }

    @Test
    void testUploadWorker_revokeExistingCert() throws Exception {
        Mockito
            .when(
                signatureService.getSignatureForBytes(
                    DgcWorkerTestHelper.DSC_TO_REVOKE
                )
            )
            .thenReturn(DgcWorkerTestHelper.SIGNATURE_SERVICE_MOCKDATA);
        Mockito
            .when(
                restApiClient.revokeVerificationInformation(
                    DgcWorkerTestHelper.SIGNATURE_SERVICE_MOCKDATA,
                    "IT"
                )
            )
            .thenReturn(
                new RestApiResponse<String>(
                    HttpStatus.NO_CONTENT,
                    null,
                    "204 NO_CONTENT"
                )
            );

        // entity to revoke
        SignerUploadInformationEntity entityToRevoke = new SignerUploadInformationEntity();
        entityToRevoke.setCertificateType(CertificateType.DSC);
        entityToRevoke.setCreatedAt(new Date());
        entityToRevoke.setRevoked(true);
        entityToRevoke.setRawData(DgcWorkerTestHelper.DSC_TO_REVOKE);
        entityToRevoke.setUploadBatchTag(
            DscUtil.batchTagGenerator(OperationType.UPLOAD)
        );
        mongoTemplate.save(entityToRevoke);

        worker.uploadWorker();

        Query query = new Query();
        query.addCriteria(
            Criteria.where("raw_data").is(DgcWorkerTestHelper.DSC_TO_REVOKE)
        );
        SignerUploadInformationEntity revokedCert = mongoTemplate.findOne(
            query,
            SignerUploadInformationEntity.class
        );
        Assertions.assertNotNull(revokedCert.getRevokedBatchTag());
        Assertions.assertNotNull(revokedCert.getRevokedDate());

        Query queryLog = new Query();
        queryLog.addCriteria(
            Criteria.where("batch_tag").is(revokedCert.getRevokedBatchTag())
        );
        DgcLogEntity log = mongoTemplate.findOne(queryLog, DgcLogEntity.class);
        Assertions.assertEquals("IT", log.getCountry());
        Assertions.assertEquals(
            DgcLogEntity.OperationType.REVOKE,
            log.getOperation()
        );
        Assertions.assertEquals("204 NO_CONTENT", log.getExecutionReport());
        List<DgcLogInfo> logInfo = log.getDgcLogInfoList();
        Assertions.assertEquals(1, logInfo.size());
        Assertions.assertEquals(
            CertificateType.DSC,
            logInfo.get(0).getCertificateType()
        );
    }

    @Test
    void testDownloadWorker() throws Exception {
        RestApiResponse<List<TrustListItemDto>> trustListResponse = DgcWorkerTestHelper.getTrustListResponse();
        Mockito
            .when(restApiClient.downloadTrustList())
            .thenReturn(trustListResponse);
        for (TrustListItemDto trustCert : trustListResponse.getData()) {
            if (trustCert.getCertificateType() == CertificateType.CSCA) {
                Mockito
                    .when(
                        signatureVerifier.checkTrustAnchorSignature(trustCert)
                    )
                    .thenReturn(true);
            }
        }

        RestApiResponse<String> emptyList = DgcWorkerTestHelper.getEmptyCountryListResponse();
        Mockito.when(restApiClient.downloadCountryList()).thenReturn(emptyList);

        RestApiResponse<List<String>> emptyValueSets = DgcWorkerTestHelper.getEmptyValueSetsResponse();
        Mockito.when(restApiClient.getValuesetIds()).thenReturn(emptyValueSets);

        RestApiResponse<List<TrustListItemDto>> emptyUploadCerts = DgcWorkerTestHelper.getEmptyUploadCertsResponse();
        Mockito
            .when(
                restApiClient.downloadTrustListFilteredByType(
                    CertificateType.UPLOAD
                )
            )
            .thenReturn(emptyUploadCerts);

        Mockito
            .when(
                signatureVerifier.trustListItemSignedByCa(
                    trustListResponse.getData().get(1),
                    trustListResponse.getData().get(0)
                )
            )
            .thenReturn(true);
        worker.downloadWorker();

        Query queryCSCA = new Query();
        queryCSCA.addCriteria(
            Criteria
                .where("raw_data")
                .is(DgcWorkerTestHelper.CSCA_TO_DOWNLOAD_RAW_DATA)
        );
        queryCSCA.addCriteria(
            Criteria.where("certificate_type").is(CertificateType.CSCA)
        );
        SignerInformationEntity CSCADownloaded = mongoTemplate.findOne(
            queryCSCA,
            SignerInformationEntity.class
        );

        Assertions.assertNotNull(CSCADownloaded);
        Assertions.assertNotNull(CSCADownloaded.getDownloadBatchTag());
        String batchTag = CSCADownloaded.getDownloadBatchTag();

        Query queryDSC = new Query();
        queryDSC.addCriteria(
            Criteria
                .where("raw_data")
                .is(DgcWorkerTestHelper.DSC_TO_DOWNLOAD_RAW_DATA)
        );
        queryDSC.addCriteria(
            Criteria.where("certificate_type").is(CertificateType.DSC)
        );
        SignerInformationEntity DSCDownloaded = mongoTemplate.findOne(
            queryDSC,
            SignerInformationEntity.class
        );

        Assertions.assertNotNull(DSCDownloaded);
        Assertions.assertNotNull(DSCDownloaded.getDownloadBatchTag());
        Assertions.assertEquals(batchTag, DSCDownloaded.getDownloadBatchTag());

        Query queryLog = new Query();
        queryLog.addCriteria(Criteria.where("batch_tag").is(batchTag));
        DgcLogEntity log = mongoTemplate.findOne(queryLog, DgcLogEntity.class);
        Assertions.assertEquals("ALL", log.getCountry());
        Assertions.assertEquals(
            DgcLogEntity.OperationType.DOWNLOAD,
            log.getOperation()
        );
        Assertions.assertEquals("200 OK", log.getExecutionReport());
        List<DgcLogInfo> logInfo = log.getDgcLogInfoList();
        Assertions.assertEquals(2, logInfo.size());
        Assertions.assertEquals(
            CertificateType.CSCA,
            logInfo.get(0).getCertificateType()
        );
        Assertions.assertEquals(
            CertificateType.DSC,
            logInfo.get(1).getCertificateType()
        );
    }

    @Test
    void testDownloadWorker_notDeleteUKCerts() throws Exception {
        SignerInformationEntity entity = new SignerInformationEntity();
        entity.setCertificateType(CertificateType.DSC);
        entity.setKid(DgcWorkerTestHelper.UK_DSC_TO_DOWNLOAD_KID);
        entity.setRawData(DgcWorkerTestHelper.UK_DSC_TO_DOWNLOAD_RAW_DATA);
        entity.setThumbprint(DgcWorkerTestHelper.UK_DSC_TO_DOWNLOAD_THUMBPRINT);
        entity.setSignature(DgcWorkerTestHelper.UK_DSC_TO_DOWNLOAD_SIGNATURE);
        entity.setId("1");
        entity.setCountry("UK");
        entity.setRevoked(false);
        entity.setCreatedAt(new Date());
        entity.setDownloadBatchTag("MANUALLY");
        mongoTemplate.save(entity);

        RestApiResponse<List<TrustListItemDto>> trustListResponse = DgcWorkerTestHelper.getTrustListResponse();
        Mockito
            .when(restApiClient.downloadTrustList())
            .thenReturn(trustListResponse);
        for (TrustListItemDto trustCert : trustListResponse.getData()) {
            if (trustCert.getCertificateType() == CertificateType.CSCA) {
                Mockito
                    .when(
                        signatureVerifier.checkTrustAnchorSignature(trustCert)
                    )
                    .thenReturn(true);
            }
        }

        RestApiResponse<String> emptyList = DgcWorkerTestHelper.getEmptyCountryListResponse();
        Mockito.when(restApiClient.downloadCountryList()).thenReturn(emptyList);

        RestApiResponse<List<String>> emptyValueSets = DgcWorkerTestHelper.getEmptyValueSetsResponse();
        Mockito.when(restApiClient.getValuesetIds()).thenReturn(emptyValueSets);

        RestApiResponse<List<TrustListItemDto>> emptyUploadCerts = DgcWorkerTestHelper.getEmptyUploadCertsResponse();
        Mockito
            .when(
                restApiClient.downloadTrustListFilteredByType(
                    CertificateType.UPLOAD
                )
            )
            .thenReturn(emptyUploadCerts);

        Mockito
            .when(
                signatureVerifier.trustListItemSignedByCa(
                    trustListResponse.getData().get(1),
                    trustListResponse.getData().get(0)
                )
            )
            .thenReturn(true);
        worker.downloadWorker();

        Query queryCSCA = new Query();
        queryCSCA.addCriteria(
            Criteria
                .where("raw_data")
                .is(DgcWorkerTestHelper.CSCA_TO_DOWNLOAD_RAW_DATA)
        );
        queryCSCA.addCriteria(
            Criteria.where("certificate_type").is(CertificateType.CSCA)
        );
        SignerInformationEntity CSCADownloaded = mongoTemplate.findOne(
            queryCSCA,
            SignerInformationEntity.class
        );

        Assertions.assertNotNull(CSCADownloaded);
        Assertions.assertNotNull(CSCADownloaded.getDownloadBatchTag());
        String batchTag = CSCADownloaded.getDownloadBatchTag();

        Query queryDSC = new Query();
        queryDSC.addCriteria(
            Criteria
                .where("raw_data")
                .is(DgcWorkerTestHelper.DSC_TO_DOWNLOAD_RAW_DATA)
        );
        queryDSC.addCriteria(
            Criteria.where("certificate_type").is(CertificateType.DSC)
        );
        SignerInformationEntity DSCDownloaded = mongoTemplate.findOne(
            queryDSC,
            SignerInformationEntity.class
        );

        Query queryUKDSC = new Query();
        queryUKDSC.addCriteria(
            Criteria
                .where("raw_data")
                .is(DgcWorkerTestHelper.UK_DSC_TO_DOWNLOAD_RAW_DATA)
        );
        queryUKDSC.addCriteria(
            Criteria.where("certificate_type").is(CertificateType.DSC)
        );
        queryUKDSC.addCriteria(Criteria.where("revoked").is(false));
        SignerInformationEntity UKDSCDownloaded = mongoTemplate.findOne(
            queryUKDSC,
            SignerInformationEntity.class
        );

        Assertions.assertNotNull(DSCDownloaded);
        Assertions.assertNotNull(DSCDownloaded.getDownloadBatchTag());
        Assertions.assertEquals(batchTag, DSCDownloaded.getDownloadBatchTag());

        Assertions.assertNotNull(UKDSCDownloaded);
        Assertions.assertNotNull(UKDSCDownloaded.getDownloadBatchTag());
        Assertions.assertEquals(
            "MANUALLY",
            UKDSCDownloaded.getDownloadBatchTag()
        );
    }

    @Test
    void testDownloadWorker_getCountryList() throws Exception {
        RestApiResponse<List<TrustListItemDto>> emptyTrustList = DgcWorkerTestHelper.getEmptyTrustListResponse();
        Mockito
            .when(restApiClient.downloadTrustList())
            .thenReturn(emptyTrustList);

        RestApiResponse<String> countryListResponse = DgcWorkerTestHelper.getCountryList();
        Mockito
            .when(restApiClient.downloadCountryList())
            .thenReturn(countryListResponse);

        RestApiResponse<List<String>> emptyValueSets = DgcWorkerTestHelper.getEmptyValueSetsResponse();
        Mockito.when(restApiClient.getValuesetIds()).thenReturn(emptyValueSets);

        RestApiResponse<List<TrustListItemDto>> emptyUploadCerts = DgcWorkerTestHelper.getEmptyUploadCertsResponse();
        Mockito
            .when(
                restApiClient.downloadTrustListFilteredByType(
                    CertificateType.UPLOAD
                )
            )
            .thenReturn(emptyUploadCerts);
        worker.downloadWorker();

        Query queryCountryList = new Query();
        queryCountryList.addCriteria(
            Criteria.where("raw_data").is(DgcWorkerTestHelper.COUNTRY_LIST)
        );
        CountryListEntity countryList = mongoTemplate.findOne(
            queryCountryList,
            CountryListEntity.class
        );

        String batchTag = countryList.getDownloadBatchTag();

        Assertions.assertNotNull(countryList);
        Assertions.assertNotNull(batchTag);
        Assertions.assertEquals(
            countryList.getHash(),
            DgcWorkerTestHelper.COUNTRY_LIST_HASH
        );

        Query queryLog = new Query();
        queryLog.addCriteria(Criteria.where("batch_tag").is(batchTag));
        queryLog.addCriteria(
            Criteria.where("item_type").is(DgcRuleLogEntity.ItemType.COUNTRIES)
        );
        DgcRuleLogEntity log = mongoTemplate.findOne(
            queryLog,
            DgcRuleLogEntity.class
        );
        Assertions.assertEquals(
            DgcRuleLogEntity.OperationType.DOWNLOAD,
            log.getOperation()
        );
        Assertions.assertEquals("200 OK", log.getExecutionReport());
    }

    @Test
    void testDownloadWorker_getValuesets() throws Exception {
        RestApiResponse<List<TrustListItemDto>> emptyTrustList = DgcWorkerTestHelper.getEmptyTrustListResponse();
        Mockito
            .when(restApiClient.downloadTrustList())
            .thenReturn(emptyTrustList);

        RestApiResponse<String> countryListResponse = DgcWorkerTestHelper.getEmptyCountryListResponse();
        Mockito
            .when(restApiClient.downloadCountryList())
            .thenReturn(countryListResponse);

        RestApiResponse<List<String>> valueSetsIds = DgcWorkerTestHelper.getValueSetIdsResponse();
        Mockito.when(restApiClient.getValuesetIds()).thenReturn(valueSetsIds);

        List<String> valueSetValues = DgcWorkerTestHelper.getValueSetValuesResponse();

        for (int i = 0; i < valueSetsIds.getData().size(); i++) {
            Mockito
                .when(restApiClient.getValueset(valueSetsIds.getData().get(i)))
                .thenReturn(
                    new RestApiResponse<String>(
                        HttpStatus.OK,
                        null,
                        valueSetValues.get(i)
                    )
                );
        }

        RestApiResponse<List<TrustListItemDto>> emptyUploadCerts = DgcWorkerTestHelper.getEmptyUploadCertsResponse();
        Mockito
            .when(
                restApiClient.downloadTrustListFilteredByType(
                    CertificateType.UPLOAD
                )
            )
            .thenReturn(emptyUploadCerts);
        worker.downloadWorker();

        Query queryValuesets = new Query();
        queryValuesets.addCriteria(
            Criteria.where("hash").is(DgcWorkerTestHelper.VALUESET_1_HASH)
        );
        ValueSetEntity valueset = mongoTemplate.findOne(
            queryValuesets,
            ValueSetEntity.class
        );

        Assertions.assertNotNull(valueset);
        String batchTag = valueset.getDownloadBatchTag();
        Assertions.assertNotNull(batchTag);
        Assertions.assertEquals(
            valueset.getRawData(),
            DgcWorkerTestHelper.VALUESET_1
        );

        Query queryValuesets2 = new Query();
        queryValuesets2.addCriteria(
            Criteria.where("hash").is(DgcWorkerTestHelper.VALUESET_2_HASH)
        );
        ValueSetEntity valueset2 = mongoTemplate.findOne(
            queryValuesets2,
            ValueSetEntity.class
        );

        Assertions.assertNotNull(valueset2);
        String batchTag2 = valueset2.getDownloadBatchTag();
        Assertions.assertNotNull(batchTag2);
        Assertions.assertEquals(batchTag2, batchTag);
        Assertions.assertEquals(
            valueset2.getRawData(),
            DgcWorkerTestHelper.VALUESET_2
        );

        Query queryValuesets3 = new Query();
        queryValuesets3.addCriteria(
            Criteria.where("hash").is(DgcWorkerTestHelper.VALUESET_3_HASH)
        );
        ValueSetEntity valueset3 = mongoTemplate.findOne(
            queryValuesets3,
            ValueSetEntity.class
        );

        Assertions.assertNotNull(valueset3);
        String batchTag3 = valueset3.getDownloadBatchTag();
        Assertions.assertNotNull(batchTag3);
        Assertions.assertEquals(batchTag3, batchTag);
        Assertions.assertEquals(
            valueset3.getRawData(),
            DgcWorkerTestHelper.VALUESET_3
        );

        Query queryLog = new Query();
        queryLog.addCriteria(Criteria.where("batch_tag").is(batchTag));
        queryLog.addCriteria(
            Criteria.where("item_type").is(DgcRuleLogEntity.ItemType.VALUES)
        );
        DgcRuleLogEntity log = mongoTemplate.findOne(
            queryLog,
            DgcRuleLogEntity.class
        );
        Assertions.assertEquals(
            DgcRuleLogEntity.OperationType.DOWNLOAD,
            log.getOperation()
        );
        Assertions.assertEquals("200 OK", log.getExecutionReport());
    }

    @Test
    void testDownloadWorker_getBusinessRules() throws Exception {
        RestApiResponse<List<TrustListItemDto>> emptyTrustList = DgcWorkerTestHelper.getEmptyTrustListResponse();
        Mockito
            .when(restApiClient.downloadTrustList())
            .thenReturn(emptyTrustList);

        RestApiResponse<String> countryListResponse = DgcWorkerTestHelper.getCountryListItOnly();
        Mockito
            .when(restApiClient.downloadCountryList())
            .thenReturn(countryListResponse);

        RestApiResponse<List<String>> valueSetsIds = DgcWorkerTestHelper.getValueSetIdsResponse();
        Mockito.when(restApiClient.getValuesetIds()).thenReturn(valueSetsIds);

        List<String> valueSetValues = DgcWorkerTestHelper.getValueSetValuesResponse();

        for (int i = 0; i < valueSetsIds.getData().size(); i++) {
            Mockito
                .when(restApiClient.getValueset(valueSetsIds.getData().get(i)))
                .thenReturn(
                    new RestApiResponse<String>(
                        HttpStatus.OK,
                        null,
                        valueSetValues.get(i)
                    )
                );
        }

        RestApiResponse<List<TrustListItemDto>> uploadCerts = DgcWorkerTestHelper.getUploadCertsResponse();
        Mockito
            .when(
                restApiClient.downloadTrustListFilteredByType(
                    CertificateType.UPLOAD
                )
            )
            .thenReturn(uploadCerts);

        RestApiResponse<Map<String, List<ValidationRuleDto>>> ruleResp = DgcWorkerTestHelper.getRulesResponse();
        Mockito
            .when(restApiClient.downloadValidationRules("EU"))
            .thenReturn(ruleResp);

        Map<String, List<TrustListItemDto>> mapCountryTrust = new HashMap<>();
        uploadCerts
            .getData()
            .forEach(
                trust ->
                    mapCountryTrust
                        .computeIfAbsent(
                            trust.getCountry(),
                            k -> new ArrayList<>()
                        )
                        .add(trust)
            );

        Mockito
            .when(
                signatureVerifier.checkRuleUploadCertificate(
                    ruleResp.getData().get("GR-EU-0001").get(0),
                    mapCountryTrust.get("EU"),
                    "EU"
                )
            )
            .thenReturn(true);
        Mockito
            .when(
                signatureVerifier.map(
                    ruleResp.getData().get("GR-EU-0001").get(0)
                )
            )
            .thenReturn(DgcWorkerTestHelper.getDummyValidationRule());
        worker.downloadWorker();

        List<BusinessRuleEntity> businessRules = mongoTemplate.findAll(
            BusinessRuleEntity.class
        );

        Assertions.assertNotNull(businessRules);
        Assertions.assertEquals(businessRules.size(), 1);
        BusinessRuleEntity businessRule = businessRules.get(0);
        String batchTag = businessRule.getDownloadBatchTag();
        Assertions.assertNotNull(batchTag);

        List<BusinessRuleInvalidEntity> businessRulesInvalid = mongoTemplate.findAll(
            BusinessRuleInvalidEntity.class
        );
        Assertions.assertNotNull(businessRulesInvalid);
        Assertions.assertEquals(businessRulesInvalid.size(), 0);

        Query queryLog = new Query();
        queryLog.addCriteria(Criteria.where("batch_tag").is(batchTag));
        queryLog.addCriteria(
            Criteria.where("item_type").is(DgcRuleLogEntity.ItemType.RULES)
        );
        DgcRuleLogEntity log = mongoTemplate.findOne(
            queryLog,
            DgcRuleLogEntity.class
        );
        Assertions.assertEquals(
            DgcRuleLogEntity.OperationType.DOWNLOAD,
            log.getOperation()
        );
        Assertions.assertEquals("200 OK", log.getExecutionReport());
    }

    @Test
    void testDownloadWorker_getBusinessRulesInvalid() throws Exception {
        RestApiResponse<List<TrustListItemDto>> emptyTrustList = DgcWorkerTestHelper.getEmptyTrustListResponse();
        Mockito
            .when(restApiClient.downloadTrustList())
            .thenReturn(emptyTrustList);

        RestApiResponse<String> countryListResponse = DgcWorkerTestHelper.getCountryListItOnly();
        Mockito
            .when(restApiClient.downloadCountryList())
            .thenReturn(countryListResponse);

        RestApiResponse<List<String>> valueSetsIds = DgcWorkerTestHelper.getValueSetIdsResponse();
        Mockito.when(restApiClient.getValuesetIds()).thenReturn(valueSetsIds);

        List<String> valueSetValues = DgcWorkerTestHelper.getValueSetValuesResponse();

        for (int i = 0; i < valueSetsIds.getData().size(); i++) {
            Mockito
                .when(restApiClient.getValueset(valueSetsIds.getData().get(i)))
                .thenReturn(
                    new RestApiResponse<String>(
                        HttpStatus.OK,
                        null,
                        valueSetValues.get(i)
                    )
                );
        }

        RestApiResponse<List<TrustListItemDto>> uploadCerts = DgcWorkerTestHelper.getUploadCertsResponse();
        Mockito
            .when(
                restApiClient.downloadTrustListFilteredByType(
                    CertificateType.UPLOAD
                )
            )
            .thenReturn(uploadCerts);

        RestApiResponse<Map<String, List<ValidationRuleDto>>> ruleResp = DgcWorkerTestHelper.getRulesResponse();
        Mockito
            .when(restApiClient.downloadValidationRules("EU"))
            .thenReturn(ruleResp);

        Map<String, List<TrustListItemDto>> mapCountryTrust = new HashMap<>();
        uploadCerts
            .getData()
            .forEach(
                trust ->
                    mapCountryTrust
                        .computeIfAbsent(
                            trust.getCountry(),
                            k -> new ArrayList<>()
                        )
                        .add(trust)
            );

        Mockito
            .when(
                signatureVerifier.checkRuleUploadCertificate(
                    ruleResp.getData().get("GR-EU-0001").get(0),
                    mapCountryTrust.get("EU"),
                    "EU"
                )
            )
            .thenReturn(true);
        worker.downloadWorker();

        List<BusinessRuleEntity> businessRules = mongoTemplate.findAll(
            BusinessRuleEntity.class
        );

        Assertions.assertNotNull(businessRules);
        Assertions.assertEquals(businessRules.size(), 0);

        List<BusinessRuleInvalidEntity> businessRulesInvalid = mongoTemplate.findAll(
            BusinessRuleInvalidEntity.class
        );
        Assertions.assertNotNull(businessRulesInvalid);
        Assertions.assertEquals(businessRulesInvalid.size(), 1);
        BusinessRuleInvalidEntity businessRuleInvalid = businessRulesInvalid.get(
            0
        );
        String batchTag = businessRuleInvalid.getDownloadBatchTag();
        Assertions.assertNotNull(batchTag);

        Query queryLog = new Query();
        queryLog.addCriteria(Criteria.where("batch_tag").is(batchTag));
        queryLog.addCriteria(
            Criteria.where("item_type").is(DgcRuleLogEntity.ItemType.RULES)
        );
        DgcRuleLogEntity log = mongoTemplate.findOne(
            queryLog,
            DgcRuleLogEntity.class
        );
        Assertions.assertEquals(
            DgcRuleLogEntity.OperationType.DOWNLOAD,
            log.getOperation()
        );
        Assertions.assertEquals("200 OK", log.getExecutionReport());
    }

    @Test
    void testUploadWorker_uploadNewBusinessRule() throws Exception {
        String base64RawData = Base64
            .getEncoder()
            .encodeToString(DgcWorkerTestHelper.RULE_TO_UPLOAD.getBytes());
        Mockito
            .when(signatureService.getSignatureForBytes(base64RawData))
            .thenReturn(DgcWorkerTestHelper.SIGNATURE_SERVICE_MOCKDATA);
        Mockito
            .when(
                restApiClient.uploadValidationRule(
                    DgcWorkerTestHelper.SIGNATURE_SERVICE_MOCKDATA,
                    "IT"
                )
            )
            .thenReturn(
                new RestApiResponse<String>(
                    HttpStatus.CREATED,
                    null,
                    "201 CREATED"
                )
            );

        // entity to upload
        BusinessRuleUploadEntity entityToUpload = new BusinessRuleUploadEntity();
        entityToUpload.setVersion("1.0.0");
        entityToUpload.setCreatedAt(new Date());
        entityToUpload.setRevoked(false);
        entityToUpload.setRawData(DgcWorkerTestHelper.RULE_TO_UPLOAD);
        mongoTemplate.save(entityToUpload);

        worker.uploadWorker();

        Query query = new Query();
        query.addCriteria(
            Criteria.where("raw_data").is(DgcWorkerTestHelper.RULE_TO_UPLOAD)
        );
        BusinessRuleUploadEntity uploadedCert = mongoTemplate.findOne(
            query,
            BusinessRuleUploadEntity.class
        );
        Assertions.assertNotNull(uploadedCert.getUploadBatchTag());
        Assertions.assertNull(uploadedCert.getRevokedDate());
        Assertions.assertNull(uploadedCert.getRevokedBatchTag());

        Query queryLog = new Query();
        queryLog.addCriteria(
            Criteria.where("batch_tag").is(uploadedCert.getUploadBatchTag())
        );
        queryLog.addCriteria(
            Criteria.where("item_type").is(DgcRuleLogEntity.ItemType.RULES)
        );
        DgcRuleLogEntity log = mongoTemplate.findOne(
            queryLog,
            DgcRuleLogEntity.class
        );
        Assertions.assertEquals(
            DgcRuleLogEntity.OperationType.UPLOAD,
            log.getOperation()
        );
        Assertions.assertEquals("201 CREATED", log.getExecutionReport());
    }

    @Test
    void testUploadWorker_revokeExistingBusinessRule() throws Exception {
        String base64RawData = Base64
            .getEncoder()
            .encodeToString("GR-IT-0001".getBytes());
        Mockito
            .when(signatureService.getSignatureForBytes(base64RawData))
            .thenReturn(DgcWorkerTestHelper.SIGNATURE_SERVICE_MOCKDATA);
        Mockito
            .when(
                restApiClient.deleteValidationRules(
                    DgcWorkerTestHelper.SIGNATURE_SERVICE_MOCKDATA,
                    "IT"
                )
            )
            .thenReturn(
                new RestApiResponse<String>(
                    HttpStatus.NO_CONTENT,
                    null,
                    "204 NO CONTENT"
                )
            );

        // entity to upload
        BusinessRuleUploadEntity entityToRevoke = new BusinessRuleUploadEntity();
        entityToRevoke.setVersion("1.0.0");
        entityToRevoke.setCreatedAt(new Date());
        entityToRevoke.setRevoked(true);
        entityToRevoke.setUploadBatchTag("UPLOAD_BATCH_TAG");
        entityToRevoke.setRawData(DgcWorkerTestHelper.RULE_TO_UPLOAD);
        mongoTemplate.save(entityToRevoke);

        worker.uploadWorker();

        Query query = new Query();
        query.addCriteria(
            Criteria.where("raw_data").is(DgcWorkerTestHelper.RULE_TO_UPLOAD)
        );
        BusinessRuleUploadEntity uploadedCert = mongoTemplate.findOne(
            query,
            BusinessRuleUploadEntity.class
        );
        Assertions.assertNotNull(uploadedCert.getRevokedBatchTag());
        Assertions.assertNotNull(uploadedCert.getRevokedDate());

        Query queryLog = new Query();
        queryLog.addCriteria(
            Criteria.where("batch_tag").is(uploadedCert.getRevokedBatchTag())
        );
        queryLog.addCriteria(
            Criteria.where("item_type").is(DgcRuleLogEntity.ItemType.RULES)
        );
        DgcRuleLogEntity log = mongoTemplate.findOne(
            queryLog,
            DgcRuleLogEntity.class
        );
        Assertions.assertEquals(
            DgcRuleLogEntity.OperationType.REVOKE,
            log.getOperation()
        );
        Assertions.assertEquals("204 NO_CONTENT", log.getExecutionReport());
    }

    @Test
    void testGetOriginCountry() {
        Assertions.assertEquals("IT", worker.getOriginCountry());
    }
}
