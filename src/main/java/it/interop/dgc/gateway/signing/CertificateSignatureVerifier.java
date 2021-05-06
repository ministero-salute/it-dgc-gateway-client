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
package it.interop.dgc.gateway.signing;

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
import java.security.cert.X509Certificate;

import org.bouncycastle.cert.CertException;
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
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.util.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import it.interop.dgc.gateway.dto.TrustListItemDto;
import lombok.extern.slf4j.Slf4j;

/**
 * This class contains the methods to verify a batch signature.
 */
@Slf4j
@Service
public class CertificateSignatureVerifier {

	@Value("${truststore.jks.path}")
	private String jksTrustPath;

	@Value("${truststore.jks.password}")
	private String jksTrustPassword;

	@Value("${truststore.anchor.alias}")
	private String trustAnchorAlias;

	private X509CertificateHolder trustAnchor;

	@Autowired
    private CertificateUtils certificateUtils;

    public CertificateSignatureVerifier() {
		Security.addProvider(new BouncyCastleProvider());

	}

	@PostConstruct
	private void initAnchoPublicKey() throws Exception {
		try {
			KeyStore anchorStore = KeyStore.getInstance("JKS");
			anchorStore.load(new FileInputStream(jksTrustPath), jksTrustPassword.toCharArray());
			trustAnchor = certificateUtils.convertCertificate((X509Certificate) anchorStore.getCertificate(trustAnchorAlias));
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			log.error("Could not load EFGS-TrustAnchor from KeyStore.");
			throw e;
		}
	}

	
	
    public boolean trustListItemSignedByCa(TrustListItemDto certificate, TrustListItemDto certificateCa) {
        ContentVerifierProvider verifier;

        X509CertificateHolder ca = getCertificateFromTrustListItem(certificateCa);
        try {
            verifier = new JcaContentVerifierProviderBuilder().build(ca);
        } catch (OperatorCreationException | CertificateException e) {
            log.error("Failed to instantiate JcaContentVerifierProvider from cert. KID: {}, Country: {}",
                certificate.getKid(), certificate.getCountry());
            return false;
        }

        X509CertificateHolder dcs;
        try {
            dcs = new X509CertificateHolder(Base64.getDecoder().decode(certificate.getRawData()));
        } catch (IOException e) {
            log.error("Could not parse certificate. KID: {}, Country: {}",
                certificate.getKid(), certificate.getCountry());
            return false;
        }

        try {
            return dcs.isSignatureValid(verifier);
        } catch (CertException e) {
            log.debug("Could not verify that certificate was issued by ca. Certificate: {}, CA: {}",
                dcs.getSubject().toString(), ca.getSubject().toString());
            return false;
        }
    }

    public boolean checkTrustAnchorSignature(TrustListItemDto trustListItem) {
        SignedCertificateMessageParser parser = new SignedCertificateMessageParser(
            trustListItem.getSignature(), trustListItem.getRawData());

        if (parser.getParserState() != SignedCertificateMessageParser.ParserState.SUCCESS) {
            log.error("Could not parse trustListItem CMS. ParserState: {}", parser.getParserState());
            return false;
        } else if (!parser.isSignatureVerified()) {
            log.error("Could not verify trustListItem CMS Signature, KID: {}, Country: {}",
                trustListItem.getKid(), trustListItem.getCountry());
            return false;
        }

        return parser.getSigningCertificate().equals(trustAnchor);
    }

    private X509CertificateHolder getCertificateFromTrustListItem(TrustListItemDto trustListItem) {
        byte[] decodedBytes = Base64.getDecoder().decode(trustListItem.getRawData());

        try {
            return new X509CertificateHolder(decodedBytes);
        } catch (IOException e) {
            log.error("Failed to parse Certificate Raw Data. KID: {}, Country: {}",
                trustListItem.getKid(), trustListItem.getCountry());
            return null;
        }
    }	
	
	
	
	
}
