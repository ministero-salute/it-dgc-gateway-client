package it.interop.dgc.gateway.entity;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Data
@Document(collection = "revocation_batch_upload_information")
public class RevocationUploadBatchEntity  implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6223359373416744077L;

	@Id
    private String id;
	
	@Field(name = "country")
    private String country;
	
    @Field(name = "expires")
    private String expires;
    
    @Field(name = "kid")
    private String kid;
    
    @Field(name = "hash_type")
    private String hashType;
	
	@Field(name = "entries")
	private List<BatchEntry> entries;
	
	@Field(name = "raw_data")
    private String rawData;
	
	@Field(name = "batch_tag")
	private String batchTag;	
	
	@Field(name = "batch_id")
	private String batchId;


	@Data
	public static class BatchEntry {

		String hash;

	}
	
	
}
