package dev.ranker.ranker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "Words")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Word {
    @Id
    private ObjectId id;

   private List<DocumentData> documents;
    private String word;

}

