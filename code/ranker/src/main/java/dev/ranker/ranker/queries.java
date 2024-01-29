package dev.ranker.ranker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
@Document(collection = "queries")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class queries {

    @Id
    private ObjectId id;

    private String query;
}
