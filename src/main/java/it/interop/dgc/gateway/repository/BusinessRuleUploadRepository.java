package it.interop.dgc.gateway.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import it.interop.dgc.gateway.entity.BusinessRuleUploadEntity;

@Repository
public class BusinessRuleUploadRepository {
	
	@Autowired
	private MongoTemplate mongoTemplate;

	public BusinessRuleUploadEntity save(BusinessRuleUploadEntity businessRuleUploadEntity) {
		return mongoTemplate.save(businessRuleUploadEntity);
	}

	public List<BusinessRuleUploadEntity> getSignerInformationToSend() {
		Query query = new Query();
		query.addCriteria(Criteria.where("batch_tag").is(null));
		List<BusinessRuleUploadEntity> certs = mongoTemplate.find(query, BusinessRuleUploadEntity.class);
		return certs;
	}

	public List<BusinessRuleUploadEntity> getSignerInformationToRevoke() {
		Query query = new Query();
		query.addCriteria(Criteria.where("revoked").is(true))
		.addCriteria(Criteria.where("batch_tag").exists(true))
		.addCriteria(Criteria.where("batch_tag_revoke").is(null));
		List<BusinessRuleUploadEntity> certs = mongoTemplate.find(query, BusinessRuleUploadEntity.class);
		return certs;
	}

}
