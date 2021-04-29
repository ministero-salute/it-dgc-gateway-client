/*-
 * ---license-start
 * EU Digital Green Certificate Gateway Service / dgc-gateway
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

package it.interop.dgc.gateway.entity;

import java.util.Date;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import it.interop.dgc.gateway.enums.CertificateType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "signer_information")
public class SignerInformationEntity {

    @Id
    private Long id;

    @Field(name="send_batch_tag")
    private String sendBatchTag;

    @Field(name="revoked_batch_tag")
    private String revokedBatchTag;
    /**
     * Timestamp of the Record.
     */
    @CreatedDate
    @Field(name = "created_at")
    private Date createdAt;

    /**
     * ISO 3166 Alpha-2 Country Code
     * (plus code "EU" for administrative European Union entries).
     */
    @Field(name = "country")
    private String country;

    /**
     * SHA-256 Thumbprint of the certificate (hex encoded).
     */
    @Field(name="thumbprint")
    private String thumbprint;

    /**
     * Base64 encoded certificate raw data.
     */
    @Field(name="raw_data")
    private String rawData;

    /**
     * Signature of the TrustAnchor.
     */
    @Field(name="signature")
    private String signature;

    /**
     * Type of the certificate (currently only DSC).
     */
    @Field(name = "certificate_type")
    private CertificateType certificateType;

	@Field("revoked")
	private boolean revoked;
	
}
