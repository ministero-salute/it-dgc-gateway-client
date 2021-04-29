package it.interop.eucert.gateway.mapper;

import java.util.ArrayList;
import java.util.List;

import it.interop.eucert.gateway.dto.SignedCertificateDto;
import it.interop.eucert.gateway.dto.TrustListDto;
import it.interop.eucert.gateway.entity.TrustedPartyEntity;
import it.interop.eucert.gateway.repository.SignerInformationRepository;
import lombok.Setter;

public class DgcMapper {
	@Setter
	public static String localCountry;

    public static TrustedPartyEntity trustListDtoToEntity(TrustListDto trustList) {
    	TrustedPartyEntity trustedPartyEntity = null;
    	if (trustList != null) {
    		trustedPartyEntity = new TrustedPartyEntity();
    		trustedPartyEntity.setKid(trustList.getKid());
    		trustedPartyEntity.setCountry(trustList.getCountry());
    		trustedPartyEntity.setCertificateType(trustList.getCertificateType());
    		trustedPartyEntity.setThumbprint(trustList.getThumbprint());
    		trustedPartyEntity.setSignature(trustList.getSignature());
    		trustedPartyEntity.setRawData(trustList.getRawData());
    	}
    	return trustedPartyEntity;
    };

    public static List<TrustedPartyEntity> trustListToTrustListDto(List<TrustListDto> trustList) {
    	List<TrustedPartyEntity> trustedPartyEntityList = null;
    	if (trustList != null) {
    		trustedPartyEntityList = new ArrayList<TrustedPartyEntity>();
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
