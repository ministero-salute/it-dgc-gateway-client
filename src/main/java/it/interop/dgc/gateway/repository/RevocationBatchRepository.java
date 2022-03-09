package it.interop.dgc.gateway.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.mongodb.client.result.DeleteResult;

import it.interop.dgc.gateway.entity.RevocationBatchEntity;

@Repository
public class RevocationBatchRepository {
    @Autowired
    private MongoTemplate mongoTemplate;
    
    public RevocationBatchEntity save(RevocationBatchEntity revocationBatchEntity) {
        return mongoTemplate.save(revocationBatchEntity);
    }
  
    public Long remove(
     		RevocationBatchEntity revocationBatchEntity
        ) {
    		Query query = new Query();
    		query.addCriteria(new Criteria("batch_id").is(revocationBatchEntity.getBatch_id()));
            DeleteResult count = mongoTemplate.remove(query, revocationBatchEntity.getClass());
            return count.getDeletedCount();

        }

}
