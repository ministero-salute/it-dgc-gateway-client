package it.interop.dgc.gateway.dto;

import java.util.Date;

import lombok.Data;

@Data
public class RevocationBatchListItemDto {

	    private String batchId;

	    private Date date;
	    
	    private String country;

	    private Boolean deleted;
}
