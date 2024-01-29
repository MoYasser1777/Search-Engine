package dev.ranker.ranker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentData {
    private double TF;

    private double IDF;

    private double Score;
    private String Description;

    private String Title;

    private String Document;
    private List<pair2> Indexes;
    private String URL;


}
