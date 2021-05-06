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
package it.interop.dgc.gateway.dto;

import java.time.ZonedDateTime;

import it.interop.dgc.gateway.enums.CertificateType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrustListItemDto {

    private String kid;
    private ZonedDateTime timestamp;
    private String country;
    private CertificateType certificateType;
    private String thumbprint;
    private String signature;
    private String rawData;
    
    private boolean verifiedSign;

}
