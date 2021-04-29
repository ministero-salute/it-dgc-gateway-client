package it.interop.dgc.gateway.entity;

import lombok.Data;

@Data
public class DgcLogInfo {
	private Integer numDocFlusso = 0;
	private Integer numNewDoc = 0;
	private Integer numInvalidDoc = 0;
	private Integer numOldDoc = 0;
	private Integer numRevokedDoc = 0;
	private Integer numTotDoc = 0;
	
}
