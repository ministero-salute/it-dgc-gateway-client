package it.interop.dgc.gateway.dto;

import java.util.Date;

import lombok.Data;

@Data
public class ValidationBatchDto {
	
	private String version;
	private Date validFrom;
	private Date validTo;
	private String cms;

	private boolean verifiedSign;

}
