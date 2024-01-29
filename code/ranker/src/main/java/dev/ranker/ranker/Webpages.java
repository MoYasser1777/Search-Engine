package dev.ranker.ranker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "webpages")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Webpages {
    @Id
    private ObjectId id;
    private String title;
    private String url;

    private String[] links;
}
