package dev.ranker.ranker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResults {
    private String url;
    private String queryParagraph;
    private String title;
    private double popularity;
    private double relevance;

    private double tagImportance;

    private double ranking;
    

}
