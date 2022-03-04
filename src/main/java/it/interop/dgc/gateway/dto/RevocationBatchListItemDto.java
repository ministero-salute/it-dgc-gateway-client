package it.interop.dgc.gateway.dto;

import java.util.Date;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class RevocationBatchListItemDto {

	    private String batchId;

	    private Date date;
	    
	    private String country;

	    private Boolean deleted;
}
