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

import it.interop.dgc.gateway.dto.TrustListItemDto;
import it.interop.dgc.gateway.enums.CertificateType;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
public class DgcLogInfo {

    @Field(name = "kid")
    private String kid;

    @Field("country")
    private String country;

    @Field(name = "certificate_type")
    private CertificateType certificateType;

    @Field("verified_sign")
    private boolean verifiedSign;

    @Field("already_exists")
    private boolean alreadyExists;

    public DgcLogInfo() {}

    public DgcLogInfo(TrustListItemDto trustListItemDto) {
        this.kid = trustListItemDto.getKid();
        this.country = trustListItemDto.getCountry();
        this.certificateType = trustListItemDto.getCertificateType();
        this.verifiedSign = trustListItemDto.isVerifiedSign();
    }

    public DgcLogInfo(
        SignerUploadInformationEntity signerUploadInformationEntity
    ) {
        this.certificateType =
            signerUploadInformationEntity.getCertificateType();
    }
}
