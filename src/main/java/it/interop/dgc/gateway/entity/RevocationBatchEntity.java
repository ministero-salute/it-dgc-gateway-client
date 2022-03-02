package it.interop.dgc.gateway.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Data
@Document(collection = "revocation_batch")
public class RevocationBatchEntity  implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6223359373416744077L;

	@Id
    private String id;
	
	@Indexed(unique=true)
    private String batch_id;
	
	@Field(name = "entries")
	private List<BatchEntry> entries;
	
	@CreatedDate
    @Field(name = "created_at")
    private Date createdAt;
	
    @Field(name = "expires")
    private String expires;

	@Field(name = "raw_data")
    private String rawData;

	@Data
	public static class BatchEntry {

		String hash;

	}
	
	
}
