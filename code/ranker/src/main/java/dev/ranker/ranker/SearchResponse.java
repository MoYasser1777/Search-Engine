package dev.ranker.ranker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResponse {
    private List<SearchResults> results;
    private double time;

    public List<SearchResults> getResults() {
        return results;
    }

    public void setResults(List<SearchResults> results) {
        this.results = results;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }
}

