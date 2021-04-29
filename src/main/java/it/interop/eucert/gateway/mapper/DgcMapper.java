package it.interop.eucert.gateway.mapper;

import java.util.ArrayList;
import java.util.List;

import it.interop.eucert.gateway.dto.SignedCertificateDto;
import it.interop.eucert.gateway.dto.TrustListDto;
import it.interop.eucert.gateway.entity.SignerInformationEntity;
import it.interop.eucert.gateway.entity.SignerInvalidInformationEntity;
import it.interop.eucert.gateway.repository.SignerInformationRepository;
import lombok.Setter;

public class DgcMapper {
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

    public static SignerInvalidInformationEntity trustListDtoInvalidToEntity(TrustListDto trustList) {
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

    
    
    public static List<SignerInformationEntity> trustListToTrustListDto(List<TrustListDto> trustList) {
    	List<SignerInformationEntity> trustedPartyEntityList = null;
    	if (trustList != null) {
    		trustedPartyEntityList = new ArrayList<SignerInformationEntity>();
    		for (TrustListDto trustListDto:trustList) {
    			trustedPartyEntityList.add(trustListDtoToEntity(trustListDto));
    		}
    	}
    	return null;
    };
    
    
    public static SignedCertificateDto signerInformationRepositoryToDto(SignerInformationRepository signerInformationRepository) {
    	SignedCertificateDto signedCertificateDto = new SignedCertificateDto();
    	
    	return signedCertificateDto;
    }

}
