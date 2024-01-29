package dev.ranker.ranker;

import java.util.*;

public class PageRank {
    private Map<String, Integer> popularityIds;
    private Map<String, Double> pageRank;


    private Map<String, Set<String>> links;

    public PageRank(Map<String, Integer> popularityIds, Map<String, Set<String>> links) {
        this.popularityIds = popularityIds;
        this.links = links;
    }

    public Map<String, Double> calculatePageRank() {
        // Initialize the PageRank values
        pageRank = new HashMap<String, Double>();

        for (String url : popularityIds.keySet()) {
            pageRank.put(url, 1.0);
        }

        // Perform PageRank iterations
        int numIterations = 10; // Change this value as needed
        double dampingFactor = 0.85; // Change this value as needed
        for (int i = 0; i < numIterations; i++) {
            Map<String, Double> newPageRank = new HashMap<String, Double>();
            double totalRank = 0.0;
            for (String url : popularityIds.keySet()) {
                double rank = 0.0;
                for (String link : popularityIds.keySet()) {
                    if (link.equals(url)) continue;
                    if (hasLink(link, url)) {
                        rank += pageRank.get(link) / getNumOutgoingLinks(link);
                    }
                }
                rank = (1 - dampingFactor) + dampingFactor * rank;
                newPageRank.put(url, rank);
                totalRank += rank;
            }
            // Normalize the PageRank values
            for (String url : popularityIds.keySet()) {
                double rank = newPageRank.get(url) / totalRank;
                pageRank.put(url, rank);
            }
        }

        return pageRank;
    }

    private boolean hasLink(String fromUrl, String toUrl) {
        // Assume that the links between URLs are represented as a Map<String, Set<String>> object named links
        Set<String> outgoingLinks = links.get(fromUrl);
        return outgoingLinks != null && outgoingLinks.contains(toUrl);
    }

    private int getNumOutgoingLinks(String url) {
        // Assume that the links between URLs are represented as a Map<String, Set<String>> object named links
        Set<String> outgoingLinks = links.get(url);
        return outgoingLinks != null ? outgoingLinks.size() : 0;
    }


}

    // In the main class


