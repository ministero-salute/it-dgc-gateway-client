package it.interop.dgc.gateway.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import it.interop.dgc.gateway.entity.RevocationBatchEntity;

@Repository
public class RevocationBatchRepository {
    @Autowired
    private MongoTemplate mongoTemplate;
    
    public RevocationBatchEntity save(RevocationBatchEntity revocationBatchEntity) {
        return mongoTemplate.save(revocationBatchEntity);
    }

}
