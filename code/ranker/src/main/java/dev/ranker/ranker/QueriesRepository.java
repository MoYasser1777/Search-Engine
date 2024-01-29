package dev.ranker.ranker;



import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QueriesRepository extends MongoRepository<queries, ObjectId> {
    public List<queries> findAll();

}

