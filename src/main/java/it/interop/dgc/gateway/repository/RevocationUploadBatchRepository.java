package it.interop.dgc.gateway.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import it.interop.dgc.gateway.entity.RevocationBatchEntity;
import it.interop.dgc.gateway.entity.RevocationUploadBatchEntity;

@Repository
public class RevocationUploadBatchRepository {
    @Autowired
    private MongoTemplate mongoTemplate;
    
    public RevocationBatchEntity save(RevocationBatchEntity revocationBatchEntity) {
        return mongoTemplate.save(revocationBatchEntity);
    }
  
    public List<RevocationUploadBatchEntity> getRevocationBatchUpload() {
        Query query = new Query();
        query.addCriteria(Criteria.where("batch_tag").is(null));
        List<RevocationUploadBatchEntity> certs = mongoTemplate.find(
            query,
            RevocationUploadBatchEntity.class
        );
        return certs;
        }
    
    public void updateBatchUploadInformation(String batchId, String batchTag, String id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));
        Update update = new Update(); 
        update.set("batch_id", batchId);
        update.set("batch_tag", batchTag);
        mongoTemplate.updateFirst(query, update ,RevocationUploadBatchEntity.class);
        
    	
    }
  }
