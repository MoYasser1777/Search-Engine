package dev.ranker.ranker;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface WebpagesRepository extends MongoRepository<Webpages, ObjectId> {
    public Webpages findByUrl(String url);

}
