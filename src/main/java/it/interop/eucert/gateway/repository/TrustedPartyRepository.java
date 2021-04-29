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
package it.interop.eucert.gateway.repository;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import it.interop.eucert.gateway.entity.TrustedPartyEntity;

@Repository
public class TrustedPartyRepository {

	@Autowired
	private MongoTemplate mongoTemplate;

	public TrustedPartyEntity save(TrustedPartyEntity trustedPartyEntity) {
		return mongoTemplate.save(trustedPartyEntity);
	}

	public TrustedPartyEntity getByKid(String kid) {
		Query query = new Query();
		query.addCriteria(Criteria.where("kid").is(kid));
		return mongoTemplate.findOne(query, TrustedPartyEntity.class);
	}

	public int setAllTrustedPartyRevoked() {
		int numDoc = 0;
		List<TrustedPartyEntity> trustedPartyList = mongoTemplate.findAll(TrustedPartyEntity.class);
		if (trustedPartyList!=null) {
			numDoc = trustedPartyList.size();
			for (TrustedPartyEntity trustedParty:trustedPartyList) {
				if (!trustedParty.isRevoked()) {
					trustedParty.setRevoked(true);
					trustedParty.setRevokedDate(new Date());
					mongoTemplate.save(trustedParty);
				}
			}
		}
		return numDoc;
	}
	
	
	
}
