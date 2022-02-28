package it.interop.dgc.gateway.dto;

import java.util.Date;
import java.util.List;
import it.interop.dgc.gateway.dto.RevocationBatchListItemDto;
import lombok.Data;

@Data
public class RevocationItemDto {


    private Boolean more;

    private List<RevocationBatchListItemDto> batches;

	public Boolean getMore() {
		return more;
	}

	public void setMore(Boolean more) {
		this.more = more;
	}

	public List<RevocationBatchListItemDto> getBatches() {
		return batches;
	}

	public void setBatches(List<RevocationBatchListItemDto> batches) {
		this.batches = batches;
	}

    

}