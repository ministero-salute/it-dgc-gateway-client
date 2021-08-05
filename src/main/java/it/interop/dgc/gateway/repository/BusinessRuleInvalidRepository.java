package it.interop.dgc.gateway.repository;

import it.interop.dgc.gateway.entity.BusinessRuleInvalidEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BusinessRuleInvalidRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public BusinessRuleInvalidEntity save(
        BusinessRuleInvalidEntity businessRuleEntity
    ) {
        return mongoTemplate.save(businessRuleEntity);
    }
}
