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

package it.interop.eucert.gateway.entity;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import it.interop.eucert.gateway.enums.CertificateType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@CompoundIndexes({
    @CompoundIndex(name = "signer_information_kid_1", def = "{'kid' : 1}"),
    @CompoundIndex(name = "signer_information_id_1", def = "{'id' : 1}")
})
@Document(collection = "signer_information")
public class SignerInformationEntity implements Serializable {
	private static final long serialVersionUID = 5989282342501802070L;

	@Id
    private String id;

    @Field(name="id")
    private Long index;
    
    @Field(name="kid")
    private String kid;
    
    @Field(name = "country")
    private String country;

    @Field(name = "thumbprint")
    private String thumbprint;

    @Field(name = "raw_data")
    private String rawData;

    @Field(name = "signature")
    private String signature;

    @Field(name = "certificate_type")
    private CertificateType certificateType;
    
    @CreatedDate
    @Field(name="created_at")
    private Date createdAt;

    @Field("verified_sign")
	private boolean verifiedSign;

	@Field("revoked")
	private boolean revoked;

	@Field("revoked_date")
	private Date revokedDate;

    @Field(name="batch_tag")
    private String downloadBatchTag;
    
    @Field(name="batch_tag_revoke")
    private String revokedBatchTag;

}
