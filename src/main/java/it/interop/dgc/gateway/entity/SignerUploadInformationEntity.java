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
package it.interop.dgc.gateway.entity;

import it.interop.dgc.gateway.enums.CertificateType;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "signer_upload_information")
public class SignerUploadInformationEntity implements Serializable {

    private static final long serialVersionUID = 3303522990191884259L;

    @Id
    private String id;

    @Field(name = "raw_data")
    private String rawData;

    @Field(name = "certificate_type")
    private CertificateType certificateType;

    @CreatedDate
    @Field(name = "created_at")
    private Date createdAt;

    @Field("revoked")
    private boolean revoked;

    @Field("revoked_date")
    private Date revokedDate;

    @Field(name = "batch_tag")
    private String uploadBatchTag;

    @Field(name = "batch_tag_revoke")
    private String revokedBatchTag;
}
