package it.interop.dgc.gateway.dto;

import lombok.Data;

@Data
public class RevocationBatchDto {
	private String batchId;

	private String signedCms;

}
