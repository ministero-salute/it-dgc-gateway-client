package it.interop.dgc.gateway.repository;

import it.interop.dgc.gateway.entity.CountryListEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class CountryListRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public CountryListEntity save(CountryListEntity countryListEntity) {
        return mongoTemplate.save(countryListEntity);
    }

    public void deleteAll() {
        mongoTemplate.remove(new Query(), CountryListEntity.class);
    }

    public CountryListEntity getCountries() {
        return mongoTemplate.findOne(new Query(), CountryListEntity.class);
    }
}
