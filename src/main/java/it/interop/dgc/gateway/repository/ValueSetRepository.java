package it.interop.dgc.gateway.repository;

import it.interop.dgc.gateway.entity.ValueSetEntity;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class ValueSetRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public ValueSetEntity save(ValueSetEntity valueSetEntity) {
        return mongoTemplate.save(valueSetEntity);
    }

    public void saveAll(List<ValueSetEntity> valueSetEntityList) {
        for (ValueSetEntity valueSetEntity : valueSetEntityList) {
            mongoTemplate.save(valueSetEntity);
        }
    }

    public void deleteAll() {
        mongoTemplate.remove(new Query(), ValueSetEntity.class);
    }
}
