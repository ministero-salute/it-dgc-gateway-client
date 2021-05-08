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
package it.interop.dgc.gateway.entity;

import org.springframework.data.mongodb.core.mapping.Field;

import it.interop.dgc.gateway.dto.TrustListItemDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class DgcLogInfo {

    @Field(name="kid")
    private String kid;
    
	@Field("country")
	private String country;

	@Field("verified_sign")
	private boolean verifiedSign;
	
	@Field("already_exists")
	private boolean alreadyExists;

	public DgcLogInfo(TrustListItemDto trustListItemDto) {
		this.kid = trustListItemDto.getKid();
		this.country = trustListItemDto.getCountry();
		this.verifiedSign = trustListItemDto.isVerifiedSign();
	}
	
}
