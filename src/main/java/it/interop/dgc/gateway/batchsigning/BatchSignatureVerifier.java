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
package it.interop.dgc.gateway.batchsigning;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerId;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Store;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * This class contains the methods to verify a batch signature.
 */
@Slf4j
@Service
public class BatchSignatureVerifier {

	@Value("${truststore.jks.path}")
	private String jksTrustPath;

	@Value("${truststore.jks.password}")
	private String jksTrustPassword;

	@Value("${truststore.anchor.alias}")
	private String trustAnchorAlias;

	private PublicKey anchoPublicKey;

	public BatchSignatureVerifier() {
		Security.addProvider(new BouncyCastleProvider());

	}

	@PostConstruct
	private void initAnchoPublicKey() throws Exception {
		try {
			KeyStore anchorStore = KeyStore.getInstance("JKS");
			anchorStore.load(new FileInputStream(jksTrustPath), jksTrustPassword.toCharArray());
			anchoPublicKey = anchorStore.getCertificate(trustAnchorAlias).getPublicKey();
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			log.error("Could not load EFGS-TrustAnchor from KeyStore.");
			throw e;
		}
	}

	
	public boolean verify(final String rawData, final String base64Signature, final String thumbprint) {
		
		try {
			if (base64Signature != null) {
				log.info("START Signature verification...");
				final byte[] batchSignatureBytes = BatchSignatureUtils.b64ToBytes(base64Signature);

				final CMSSignedData signedData = new CMSSignedData(getBatchBytes(rawData), batchSignatureBytes);
				final SignerInformation signerInfo = getSignerInformation(signedData);

				if (signerInfo != null) {
					final X509CertificateHolder signerCert = getSignerCert(signedData.getCertificates(),
							signerInfo.getSID());

					if (signerCert == null) {
						log.error("Erore: no signer certificate");
						return false;
					}

					if (!isCertNotExpired(signerCert)) {
						log.error("Erore: signing certificate expired, certNotBefore={}, certNotAfter={}",
								signerCert.getNotBefore(), signerCert.getNotAfter());
						return false;
					}

					
					String thumbprintFromCert = ThumbPrintUtils.getThumbprint(signerCert);
					
					if (thumbprint.equalsIgnoreCase(thumbprintFromCert)) {
						log.error("Erore: signing certificate thumbprint dont match, thumbprint={}, thumbprintFromCert={}",
								thumbprint, thumbprintFromCert);
						return false;
					}
				
					boolean verified = verifySignerInfo(signerInfo, signerCert);

//					if (verified) {
//						JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
//						X509Certificate signerCertConv = converter.getCertificate(signerCert);
//
//						String signingCertificateFromData = BatchSignatureUtils.x509CertificateToPem(signerCertConv);
//
//						if (!signingCertificateFromData.equals(signingCertificate)) {
//							log.error("Erore: Certificate do not match.");
//							return false;
//						}
//
//						verified = verifySigner(signingCertificateFromData, signingCertificateOperatorSignature);
//					}
					log.info("END Signature verification... verified: {}", verified);
					return  verified;
				}

			}

		} catch (CertificateException | CMSException e) {
			log.error("Erore: Error verifying batch signature", e);
		} catch (OperatorCreationException e) {
			log.error("Erore: OperatorCreationException verifying batch signature", e);
		}
		return false;
	}

	private boolean verifySigner(String signingCertificateFromData, String signingCertificateOperatorSignature) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature verifier = Signature.getInstance("SHA256with" + anchoPublicKey.getAlgorithm());
		verifier.initVerify(anchoPublicKey);
		verifier.update(signingCertificateFromData.getBytes());

		byte[] signatureBytes = Base64.getDecoder().decode(signingCertificateOperatorSignature);

		return verifier.verify(signatureBytes);
	}

//	private boolean allOriginsMatchingCertCountry(DiagnosisKeyBatch batch, X509CertificateHolder certificate) {
//		String country = getCountryOfCertificate(certificate);
//
//		if (country == null) {
//			return false;
//		} else {
//			return batch.getKeysList().stream().allMatch(key -> key.getOrigin().equals(country));
//		}
//	}

	private boolean isCertNotExpired(X509CertificateHolder certificate) {
		Date now = new Date();

		return certificate.getNotBefore().before(now) && certificate.getNotAfter().after(now);
	}

//	private String getCountryOfCertificate(X509CertificateHolder certificate) {
//		RDN[] rdns = certificate.getSubject().getRDNs(BCStyle.C);
//		if (rdns.length != 1) {
//			log.info("Certificate has no valid country attribute");
//			return null;
//		} else {
//			return rdns[0].getFirst().getValue().toString();
//		}
//	}

	private CMSProcessableByteArray getBatchBytes(String rawData) {
		return new CMSProcessableByteArray(rawData.getBytes());
	}

	private SignerInformation getSignerInformation(final CMSSignedData signedData) {
		final SignerInformationStore signerInfoStore = signedData.getSignerInfos();

		if (signerInfoStore.size() > 0) {
			return signerInfoStore.getSigners().iterator().next();
		}
		return null;
	}

	private X509CertificateHolder getSignerCert(final Store<X509CertificateHolder> certificatesStore,
			final SignerId signerId) {
		final Collection certCollection = certificatesStore.getMatches(signerId);

		if (!certCollection.isEmpty()) {
			return (X509CertificateHolder) certCollection.iterator().next();
		}
		return null;
	}

	private boolean verifySignerInfo(final SignerInformation signerInfo, final X509CertificateHolder signerCert)
			throws CertificateException, OperatorCreationException, CMSException {
		return signerInfo.verify(createSignerInfoVerifier(signerCert));
	}

	private SignerInformationVerifier createSignerInfoVerifier(final X509CertificateHolder signerCert)
			throws OperatorCreationException, CertificateException {
		return new JcaSimpleSignerInfoVerifierBuilder().setProvider(BouncyCastleProvider.PROVIDER_NAME)
				.build(signerCert);
	}

//	public boolean validateDiagnosisKeyWithSignature(List<EfgsKey> efgsKeys, Audit audit) {
//		EfgsProto.DiagnosisKeyBatch diagnosisKeyBatchPerCountry = EfgsProto.DiagnosisKeyBatch.newBuilder()
//				.addAllKeys(DiagnosisKeyMapper.efgsKeyToProto(efgsKeys)).build();
//
//		return verify(diagnosisKeyBatchPerCountry, audit.getBatchSignature(),
//				audit.getSigningCertificateOperatorSignature(), audit.getSigningCertificate());
//	}

}
