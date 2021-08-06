/*
 *  Copyright (C) 2021 Ministero della Salute and all other contributors.
 *  Please refer to the AUTHORS file for more information.
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package it.interop.dgc.gateway.repository;

import it.interop.dgc.gateway.entity.BusinessRuleEntity;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class BusinessRuleRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public BusinessRuleEntity save(BusinessRuleEntity businessRuleEntity) {
        return mongoTemplate.save(businessRuleEntity);
    }

    public BusinessRuleEntity getByCountryAndHash(
        String country,
        String hash,
        String batchTag
    ) {
        Query query = new Query();
        query
            .addCriteria(Criteria.where("country").is(country))
            .addCriteria(Criteria.where("hash").is(hash))
            .addCriteria(Criteria.where("batch_tag_revoke").is(batchTag));
        return mongoTemplate.findOne(query, BusinessRuleEntity.class);
    }

    public int setAllBusinessRuleRevoked(String revokedBatchTag) {
        int numDoc = 0;
        List<BusinessRuleEntity> businessRuleEntityList = mongoTemplate.findAll(
            BusinessRuleEntity.class
        );
        if (businessRuleEntityList != null) {
            for (BusinessRuleEntity businessRuleEntity : businessRuleEntityList) {
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
