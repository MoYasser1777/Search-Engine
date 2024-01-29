package dev.ranker.ranker;

import java.util.Comparator;

public class SearchResultsComparator implements Comparator<SearchResults> {

    private double wRelevance;
    private double wTag;
    private double wFactor;

    public SearchResultsComparator(double wRelevance, double wTag, double wFactor) {
        this.wRelevance = wRelevance;
        this.wTag = wTag;
        this.wFactor = wFactor;
    }

    @Override
    public int compare(SearchResults o1, SearchResults o2) {
        double score1 = o1.getPopularity() * (wRelevance * o1.getRelevance() + wTag * o1.getTagImportance()) * wFactor;
        double score2 = o2.getPopularity() * (wRelevance * o2.getRelevance() + wTag * o2.getTagImportance()) * wFactor;
        o1.setRanking(score1);
        o2.setRanking(score2);
        return Double.compare(score2, score1);
    }
}



