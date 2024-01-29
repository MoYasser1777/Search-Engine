package dev.ranker.ranker;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WordsRepository extends MongoRepository<Word, ObjectId> {
    Word findByWord(String word);
    List<Word> findAll();



}
