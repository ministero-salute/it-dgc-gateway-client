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

import java.util.Date;
import java.util.List;

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

import it.interop.dgc.gateway.client.RestApiClient;
import it.interop.dgc.gateway.client.base.RestApiClientBase;
import it.interop.dgc.gateway.client.base.RestApiResponse;
import it.interop.dgc.gateway.dto.TrustListItemDto;
import it.interop.dgc.gateway.entity.DgcLogEntity;
import it.interop.dgc.gateway.entity.DgcLogEntity.OperationType;
import it.interop.dgc.gateway.entity.DgcLogInfo;
import it.interop.dgc.gateway.entity.SignerInformationEntity;
import it.interop.dgc.gateway.entity.SignerUploadInformationEntity;
import it.interop.dgc.gateway.enums.CertificateType;
import it.interop.dgc.gateway.signing.CertificateSignatureVerifier;
import it.interop.dgc.gateway.signing.SignatureService;
import it.interop.dgc.gateway.util.DscUtil;
import it.interop.dgc.gateway.worker.testdata.DgcWorkerTestHelper;

@SpringBootTest(properties = {"dgc.worker.upload.schedul=0 0 0 29 2 ?","dgc.worker.download.schedul=0 0 0 29 2 ?"})
@AutoConfigureDataMongo
//prevent @PostConstruct execution
@MockBean(classes = { SignatureService.class, CertificateSignatureVerifier.class, RestApiClientBase.class, RestApiClient.class })
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

		mongoTemplate.remove(new Query(), "signer_information");
		mongoTemplate.remove(new Query(), "signer_upload_information");
		mongoTemplate.remove(new Query(), "dgc_log");

		MockitoAnnotations.initMocks(this);

	}

	@Test
	void testUploadWorker_uploadNewCert() throws Exception {

		Mockito.when(signatureService.getSignatureForBytes(DgcWorkerTestHelper.DSC_TO_UPLOAD))
				.thenReturn(DgcWorkerTestHelper.SIGNATURE_SERVICE_MOCKDATA);
		Mockito.when(
				restApiClient.postVerificationInformation(DgcWorkerTestHelper.SIGNATURE_SERVICE_MOCKDATA, "IT"))
				.thenReturn(new RestApiResponse<String>(HttpStatus.CREATED, null, "201 CREATED"));

		// entity to upload
		SignerUploadInformationEntity entityToUpload = new SignerUploadInformationEntity();
		entityToUpload.setCertificateType(CertificateType.DSC);
		entityToUpload.setCreatedAt(new Date());
		entityToUpload.setRevoked(false);
		entityToUpload.setRawData(DgcWorkerTestHelper.DSC_TO_UPLOAD);
		mongoTemplate.save(entityToUpload);

		worker.uploadWorker();

		Query query = new Query();
		query.addCriteria(Criteria.where("raw_data").is(DgcWorkerTestHelper.DSC_TO_UPLOAD));
		SignerUploadInformationEntity uploadedCert = mongoTemplate.findOne(query, SignerUploadInformationEntity.class);
		Assertions.assertNotNull(uploadedCert.getUploadBatchTag());

		Query queryLog = new Query();
		queryLog.addCriteria(Criteria.where("batch_tag").is(uploadedCert.getUploadBatchTag()));
		DgcLogEntity log = mongoTemplate.findOne(queryLog, DgcLogEntity.class);
		Assertions.assertEquals("IT", log.getCountry());
		Assertions.assertEquals(DgcLogEntity.OperationType.UPLOAD, log.getOperation());
		Assertions.assertEquals("201 CREATED", log.getExecutionReport());
		List<DgcLogInfo> logInfo = log.getDgcLogInfoList();
		Assertions.assertEquals(1, logInfo.size());
		Assertions.assertEquals(CertificateType.DSC, logInfo.get(0).getCertificateType());
	}

	@Test
	void testUploadWorker_revokeExistingCert() throws Exception {

		Mockito.when(signatureService.getSignatureForBytes(DgcWorkerTestHelper.DSC_TO_REVOKE))
				.thenReturn(DgcWorkerTestHelper.SIGNATURE_SERVICE_MOCKDATA);
		Mockito.when(
				restApiClient.revokeVerificationInformation(DgcWorkerTestHelper.SIGNATURE_SERVICE_MOCKDATA, "IT"))
				.thenReturn(new RestApiResponse<String>(HttpStatus.NO_CONTENT, null, "204 NO_CONTENT"));

		// entity to revoke
		SignerUploadInformationEntity entityToRevoke = new SignerUploadInformationEntity();
		entityToRevoke.setCertificateType(CertificateType.DSC);
		entityToRevoke.setCreatedAt(new Date());
		entityToRevoke.setRevoked(true);
		entityToRevoke.setRawData(DgcWorkerTestHelper.DSC_TO_REVOKE);
		entityToRevoke.setUploadBatchTag(DscUtil.batchTagGenerator(OperationType.UPLOAD));
		mongoTemplate.save(entityToRevoke);

		worker.uploadWorker();

		Query query = new Query();
		query.addCriteria(Criteria.where("raw_data").is(DgcWorkerTestHelper.DSC_TO_REVOKE));
		SignerUploadInformationEntity revokedCert = mongoTemplate.findOne(query, SignerUploadInformationEntity.class);
		Assertions.assertNotNull(revokedCert.getRevokedBatchTag());
		Assertions.assertNotNull(revokedCert.getRevokedDate());

		Query queryLog = new Query();
		queryLog.addCriteria(Criteria.where("batch_tag").is(revokedCert.getRevokedBatchTag()));
		DgcLogEntity log = mongoTemplate.findOne(queryLog, DgcLogEntity.class);
		Assertions.assertEquals("IT", log.getCountry());
		Assertions.assertEquals(DgcLogEntity.OperationType.REVOKE, log.getOperation());
		Assertions.assertEquals("204 NO_CONTENT", log.getExecutionReport());
		List<DgcLogInfo> logInfo = log.getDgcLogInfoList();
		Assertions.assertEquals(1, logInfo.size());
		Assertions.assertEquals(CertificateType.DSC, logInfo.get(0).getCertificateType());
	}

	@Test
	void testDownloadWorker() throws Exception {

		RestApiResponse<List<TrustListItemDto>> trustListResponse = DgcWorkerTestHelper.getTrustListResponse();
		Mockito.when(restApiClient.downloadTrustList()).thenReturn(trustListResponse);
		for (TrustListItemDto trustCert : trustListResponse.getData()) {
			if (trustCert.getCertificateType() == CertificateType.CSCA) {
				Mockito.when(signatureVerifier.checkTrustAnchorSignature(trustCert)).thenReturn(true);
			}
		}
		Mockito.when(signatureVerifier.trustListItemSignedByCa(trustListResponse.getData().get(1), trustListResponse.getData().get(0))).thenReturn(true);
		worker.downloadWorker();

		Query queryCSCA = new Query();
		queryCSCA.addCriteria(Criteria.where("raw_data").is(DgcWorkerTestHelper.CSCA_TO_DOWNLOAD_RAW_DATA));
		queryCSCA.addCriteria(Criteria.where("certificate_type").is(CertificateType.CSCA));
		SignerInformationEntity CSCADownloaded = mongoTemplate.findOne(queryCSCA, SignerInformationEntity.class);

		Assertions.assertNotNull(CSCADownloaded);
		Assertions.assertNotNull(CSCADownloaded.getDownloadBatchTag());
		String batchTag = CSCADownloaded.getDownloadBatchTag();

		Query queryDSC = new Query();
		queryDSC.addCriteria(Criteria.where("raw_data").is(DgcWorkerTestHelper.DSC_TO_DOWNLOAD_RAW_DATA));
		queryDSC.addCriteria(Criteria.where("certificate_type").is(CertificateType.DSC));
		SignerInformationEntity DSCDownloaded = mongoTemplate.findOne(queryDSC, SignerInformationEntity.class);

		Assertions.assertNotNull(DSCDownloaded);
		Assertions.assertNotNull(DSCDownloaded.getDownloadBatchTag());
		Assertions.assertEquals(batchTag, DSCDownloaded.getDownloadBatchTag());

		Query queryLog = new Query();
		queryLog.addCriteria(Criteria.where("batch_tag").is(batchTag));
		DgcLogEntity log = mongoTemplate.findOne(queryLog, DgcLogEntity.class);
		Assertions.assertEquals("ALL", log.getCountry());
		Assertions.assertEquals(DgcLogEntity.OperationType.DOWNLOAD, log.getOperation());
		Assertions.assertEquals("200 OK", log.getExecutionReport());
		List<DgcLogInfo> logInfo = log.getDgcLogInfoList();
		Assertions.assertEquals(2, logInfo.size());
		Assertions.assertEquals(CertificateType.CSCA, logInfo.get(0).getCertificateType());
		Assertions.assertEquals(CertificateType.DSC, logInfo.get(1).getCertificateType());
	}

	@Test
	void testGetOriginCountry() {
		Assertions.assertEquals("IT", worker.getOriginCountry());
	}

}
