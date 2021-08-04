package it.interop.dgc.gateway.repository;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import it.interop.dgc.gateway.entity.BusinessRuleEntity;

@Repository
public class BusinessRuleRepository {

	@Autowired
	private MongoTemplate mongoTemplate;

	public BusinessRuleEntity save(BusinessRuleEntity businessRuleEntity) {
		return mongoTemplate.save(businessRuleEntity);
	}

	
	public BusinessRuleEntity getByCountryAndHash(String country, String hash, String batchTag) {
		Query query = new Query();
		query.addCriteria(Criteria.where("country").is(country))
			.addCriteria(Criteria.where("hash").is(hash))
			.addCriteria(Criteria.where("batch_tag_revoke").is(batchTag));
		return mongoTemplate.findOne(query, BusinessRuleEntity.class);
	}

	public int setAllBusinessRuleRevoked(String revokedBatchTag) {
		int numDoc = 0;
		List<BusinessRuleEntity> businessRuleEntityList = mongoTemplate.findAll(BusinessRuleEntity.class);
		if (businessRuleEntityList!=null) {
			for (BusinessRuleEntity businessRuleEntity:businessRuleEntityList) {
				if (!businessRuleEntity.isRevoked()) {
					businessRuleEntity.setRevoked(true);
					businessRuleEntity.setRevokedDate(new Date());
					businessRuleEntity.setRevokedBatchTag(revokedBatchTag);
					mongoTemplate.save(businessRuleEntity);
					numDoc++;
				}
			}
		}
		return numDoc;
	}

}
