package dev.ranker.ranker;

import java.util.List;


public class pair {
    public Double tf;
    public Double idf;
    public List<pair2> positions;
    public Double score;
    public String url;
    public String title;
    public String description;

    public pair(Double tf,Double idf, List<pair2> positions, Double tag, String url, String title, String description) {
        this.tf = tf;
        this.idf=idf;
        this.positions = positions;
        this.score = tag;
        this.url = url;
        this.title = title;
        this.description = description;
    }

}