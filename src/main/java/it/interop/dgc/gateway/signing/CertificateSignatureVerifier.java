/*-
 * ---license-start
 * EU Digital Green Certificate Gateway Service / dgc-lib
 * ---
 * Copyright (C) 2021 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */
package it.interop.dgc.gateway.signing;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.bouncycastle.cert.CertException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.interop.dgc.gateway.dto.TrustListItemDto;
import it.interop.dgc.gateway.dto.ValidationBatchDto;
import it.interop.dgc.gateway.dto.ValidationRuleDto;
import it.interop.dgc.gateway.model.ValidationBatch;
import it.interop.dgc.gateway.model.ValidationRule;
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
            anchorStore.load(
                new FileInputStream(jksTrustPath),
                jksTrustPassword.toCharArray()
            );
            trustAnchor =
                certificateUtils.convertCertificate(
                    (X509Certificate) anchorStore.getCertificate(
                        trustAnchorAlias
                    )
                );
        } catch (
            KeyStoreException
            | NoSuchAlgorithmException
            | CertificateException
            | IOException e
        ) {
            log.error("Could not load EFGS-TrustAnchor from KeyStore.");
            throw e;
        }
    }

    public boolean trustListItemSignedByCa(
        TrustListItemDto certificate,
        TrustListItemDto certificateCa
    ) {
        ContentVerifierProvider verifier;

        X509CertificateHolder ca = getCertificateFromTrustListItem(
            certificateCa
        );
        try {
            verifier = new JcaContentVerifierProviderBuilder().build(ca);
        } catch (OperatorCreationException | CertificateException e) {
            log.error(
                "Failed to instantiate JcaContentVerifierProvider from cert. KID: {}, Country: {}",
                certificate.getKid(),
                certificate.getCountry()
            );
            return false;
        }

        X509CertificateHolder dcs;
        try {
            dcs =
                new X509CertificateHolder(
                    Base64.getDecoder().decode(certificate.getRawData())
                );
        } catch (IOException e) {
            log.error(
                "Could not parse certificate. KID: {}, Country: {}",
                certificate.getKid(),
                certificate.getCountry()
            );
            return false;
        }

        try {
            return dcs.isSignatureValid(verifier);
        } catch (CertException e) {
            log.debug(
                "Could not verify that certificate was issued by ca. Certificate: {}, CA: {}",
                dcs.getSubject().toString(),
                ca.getSubject().toString()
            );
            return false;
        }
    }

    public boolean checkTrustAnchorSignature(TrustListItemDto trustListItem) {
        SignedCertificateMessageParser parser = new SignedCertificateMessageParser(
            trustListItem.getSignature(),
            trustListItem.getRawData()
        );

        if (
            parser.getParserState() !=
            SignedCertificateMessageParser.ParserState.SUCCESS
        ) {
            log.error(
                "Could not parse trustListItem CMS. ParserState: {}",
                parser.getParserState()
            );
            return false;
        } else if (!parser.isSignatureVerified()) {
            log.error(
                "Could not verify trustListItem CMS Signature, KID: {}, Country: {}",
                trustListItem.getKid(),
                trustListItem.getCountry()
            );
            return false;
        }

        return parser.getSigningCertificate().equals(trustAnchor);
    }

    private X509CertificateHolder getCertificateFromTrustListItem(
        TrustListItemDto trustListItem
    ) {
        byte[] decodedBytes = Base64
            .getDecoder()
            .decode(trustListItem.getRawData());

        try {
            return new X509CertificateHolder(decodedBytes);
        } catch (IOException e) {
            log.error(
                "Failed to parse Certificate Raw Data. KID: {}, Country: {}",
                trustListItem.getKid(),
                trustListItem.getCountry()
            );
            return null;
        }
    }

    private boolean checkThumbprintIntegrity(TrustListItemDto trustListItem) {
        byte[] certificateRawData = Base64
            .getDecoder()
            .decode(trustListItem.getRawData());
        try {
            return trustListItem
                .getThumbprint()
                .equals(
                    certificateUtils.getCertThumbprint(
                        new X509CertificateHolder(certificateRawData)
                    )
                );
        } catch (IOException e) {
            log.error("Could not parse certificate raw data");
            return false;
        }
    }

    private boolean checkCmsSignature(
        ValidationRuleDto validationRuleDto,
        String countryCode
    ) {
        SignedStringMessageParser parser = new SignedStringMessageParser(
            validationRuleDto.getCms()
        );
        if (
            parser.getParserState() != SignedMessageParser.ParserState.SUCCESS
        ) {
            log.error(
                "Invalid CMS for Validation Rule of {​​​​​​​​}​​​​​​​​",
                countryCode
            );
            return false;
        }
        if (!parser.isSignatureVerified()) {
            log.error(
                "Invalid CMS Signature for Validation Rule of {​​​​​​​​}​​​​​​​​",
                countryCode
            );
            return false;
        }
        return true;
    }

    public boolean checkRuleUploadCertificate(
        ValidationRuleDto validationRule,
        List<TrustListItemDto> trustCountryList,
        String countryCode
    ) {
        if (!checkCmsSignature(validationRule, countryCode)) {
            return false;
        }

        List<X509CertificateHolder> trustedUploadCertificates = trustCountryList
            .stream()
            .filter(this::checkThumbprintIntegrity)
            .filter(c -> this.checkTrustAnchorSignature(c))
            .map(this::getCertificateFromTrustListItem)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if (
            trustedUploadCertificates == null ||
            trustedUploadCertificates.size() == 0
        ) {
            return false;
        }

        SignedStringMessageParser parser = new SignedStringMessageParser(
            validationRule.getCms()
        );
        X509CertificateHolder uploadCertificate = parser.getSigningCertificate();

        if (uploadCertificate == null) {
            return false;
        }

        return trustedUploadCertificates
            .stream()
            .anyMatch(uploadCertificate::equals);
    }

    public ValidationRule map(ValidationRuleDto dto) {
        SignedStringMessageParser parser = new SignedStringMessageParser(
            dto.getCms()
        );
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ValidationRule parsedRule = objectMapper.readValue(
                parser.getPayload(),
                ValidationRule.class
            );
            parsedRule.setRawJson(parser.getPayload());
            return parsedRule;
        } catch (JsonProcessingException e) {
            return null;
        }
    }   
    
	public boolean revocationCheckCmsSignature(ValidationBatchDto validationBatchDto) {
		SignedStringMessageParser parser = new SignedStringMessageParser(validationBatchDto.getCms());
		if (parser.getParserState() != SignedMessageParser.ParserState.SUCCESS) {
			log.error("Invalid CMS for Revocation EU​​​​ ​​​​​​​​");
			return false;
		}
		if (!parser.isSignatureVerified()) {
			log.error("Invalid CMS for Revocation EU​​​​ ");
			return false;
		}
		return true;
	}

	public ValidationBatch map(ValidationBatchDto dto) {
		SignedStringMessageParser parser = new SignedStringMessageParser(dto.getCms());
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			ValidationBatch parsedRule = objectMapper.readValue(parser.getPayload(), ValidationBatch.class);
			parsedRule.setRawJson(parser.getPayload());
			return parsedRule;
		} catch (JsonProcessingException e) {
			return null;
		}
	}
	
}
