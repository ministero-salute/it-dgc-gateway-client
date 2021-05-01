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
package it.interop.eucert.gateway.mapper;

import java.util.ArrayList;
import java.util.List;

import it.interop.eucert.gateway.dto.SignedCertificateDto;
import it.interop.eucert.gateway.dto.TrustListDto;
import it.interop.eucert.gateway.entity.SignerInformationEntity;
import it.interop.eucert.gateway.entity.SignerInvalidInformationEntity;
import it.interop.eucert.gateway.repository.SignerInformationRepository;
import lombok.Setter;

public class EucertMapper {
	@Setter
	public static String localCountry;

    public static SignerInformationEntity trustListDtoToEntity(TrustListDto trustList) {
    	SignerInformationEntity trustedPartyEntity = null;
    	if (trustList != null) {
    		trustedPartyEntity = new SignerInformationEntity();
    		trustedPartyEntity.setKid(trustList.getKid());
    		trustedPartyEntity.setCountry(trustList.getCountry());
    		trustedPartyEntity.setCertificateType(trustList.getCertificateType());
    		trustedPartyEntity.setThumbprint(trustList.getThumbprint());
    		trustedPartyEntity.setSignature(trustList.getSignature());
    		trustedPartyEntity.setRawData(trustList.getRawData());
    	}
    	return trustedPartyEntity;
    };

    public static SignerInvalidInformationEntity invalidTrustListDtoToEntity(TrustListDto trustList) {
    	SignerInvalidInformationEntity signerInvalidInformationEntity = null;
    	if (trustList != null) {
    		signerInvalidInformationEntity = new SignerInvalidInformationEntity();
    		signerInvalidInformationEntity.setKid(trustList.getKid());
    		signerInvalidInformationEntity.setCountry(trustList.getCountry());
    		signerInvalidInformationEntity.setCertificateType(trustList.getCertificateType());
    		signerInvalidInformationEntity.setThumbprint(trustList.getThumbprint());
    		signerInvalidInformationEntity.setSignature(trustList.getSignature());
    		signerInvalidInformationEntity.setRawData(trustList.getRawData());
    	}
    	return signerInvalidInformationEntity;
    };
    
    public static SignedCertificateDto signerInformationRepositoryToDto(SignerInformationRepository signerInformationRepository) {
    	SignedCertificateDto signedCertificateDto = new SignedCertificateDto();
    	
    	return signedCertificateDto;
    }

}
