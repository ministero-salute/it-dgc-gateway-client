/*-
 *   Copyright (C) 2020 Presidenza del Consiglio dei Ministri.
 *   Please refer to the AUTHORS file for more information. 
 *   This program is free software: you can redistribute it and/or modify 
 *   it under the terms of the GNU Affero General Public License as 
 *   published by the Free Software Foundation, either version 3 of the
 *   License, or (at your option) any later version.
 *   This program is distributed in the hope that it will be useful, 
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 *   GNU Affero General Public License for more details.
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program. If not, see <https://www.gnu.org/licenses/>.   
 */
package it.interop.dgc.gateway.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import it.interop.dgc.gateway.entity.SignerInformationEntity;

@Repository
public class SignerInformationRepository {

	@Autowired
	private MongoTemplate mongoTemplate;

	public SignerInformationEntity save(SignerInformationEntity signerInformationEntity) {
		return mongoTemplate.save(signerInformationEntity);
	}

	public List<SignerInformationEntity> getSignerInformationToSend() {
		Query query = new Query();
		query.addCriteria(Criteria.where("send_batch_tag").is(null));
//		query.fields().include("_id");
		List<SignerInformationEntity> kets = mongoTemplate.find(query, SignerInformationEntity.class);
		return kets;
	}

	public void setSendBatchTag(SignerInformationEntity signerInformationEntity) {
		Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(signerInformationEntity.getId()));
		Update update = new Update();
		update.set("send_batch_tag", signerInformationEntity.getSendBatchTag());
		mongoTemplate.findAndModify(query, update, SignerInformationEntity.class);
	}
	
	public List<SignerInformationEntity> getSignerInformationToRevoke() {
		Query query = new Query();
		query.addCriteria(Criteria.where("revoked").is(true)).addCriteria(Criteria.where("send_batch_tag").exists(true)).addCriteria(Criteria.where("revoked_batch_tag").is(null));
//		query.fields().include("_id");
		List<SignerInformationEntity> kets = mongoTemplate.find(query, SignerInformationEntity.class);
		return kets;
	}

	public void setRevokeBatchTag(SignerInformationEntity signerInformationEntity) {
		Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(signerInformationEntity.getId()));
		Update update = new Update();
		update.set("revoked_batch_tag", signerInformationEntity.getSendBatchTag()).set("to_revoke", false);
		mongoTemplate.findAndModify(query, update, SignerInformationEntity.class);
	}
	
}
