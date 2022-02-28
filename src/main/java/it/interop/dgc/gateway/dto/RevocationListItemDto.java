package it.interop.dgc.gateway.dto;

import java.util.Date;
import lombok.Data;

@Data
public class RevocationListItemDto {

	private String batchId;
	private String country;
	private Date date;
	private Boolean deleted;
	
}