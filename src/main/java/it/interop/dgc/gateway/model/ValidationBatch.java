package it.interop.dgc.gateway.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import it.interop.dgc.gateway.entity.RevocationBatchEntity.BatchEntry;
import lombok.Data;

@Data
public class ValidationBatch {

	@JsonProperty("country")
	private String country;

	@JsonProperty("expires")
	private String expires;

	@JsonProperty("kid")
	private String kid;

	@JsonProperty("hashType")
	private String hashType;

	@JsonProperty("entries")
	private List<BatchEntry> entries;
	
    @JsonIgnore
    private String rawJson;


}
