package dev.ranker.ranker;

import org.tartarus.snowball.ext.PorterStemmer;

import java.util.*;

public class Phrase_Searching {
    private static HashMap<String, pair> Unique_Docs;
    private static String[] Words;
    private static String[] Query;
    private static ArrayList<pair> Docs_With_Phrase;
    protected static HashMap<String, HashMap<String, pair>> Inverted_Index;
    protected static HashMap<String, pair> Docs_pair = new HashMap<>();

    public Phrase_Searching(String Phrase, HashMap<String, HashMap<String, pair>> inverted_Index) {
        String[] stopWords = {"i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", "yours", "yourself", "yourselves", "he", "him", "his", "himself", "she", "her", "hers", "herself", "it", "its", "itself", "they", "them", "their", "theirs", "themselves", "what", "which", "who", "whom", "this", "that", "these", "those", "am", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "having", "do", "does", "did", "doing", "a", "an", "the", "and", "but", "if", "or", "because", "as", "until", "while", "of", "at", "by", "for", "with", "about", "against", "between", "into", "through", "during", "before", "after", "above", "below", "to", "from", "up", "down", "in", "out", "on", "off", "over", "under", "again", "further", "then", "once", "here", "there", "when", "where", "why", "how", "all", "any", "both", "each", "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very", "s", "t", "can", "will", "just", "don", "should", "now"};
        Inverted_Index = inverted_Index;
        Unique_Docs = new HashMap<>();  //docs with all words
        Docs_With_Phrase = new ArrayList<>();
        Words = Phrase.split("\\s+"); // split the search phrase into individual words
        Query = Phrase.split("\\s+");

        HashMap<String, Integer> Docs = new HashMap<>();

        for (int i = 0; i < Words.length; i++) {
            Words[i] = Words[i].toLowerCase();
            Words[i]=Words[i].replaceAll("[.,'\":?!\\s]","");
        }
        for (int i = 0; i < Query.length; i++) {
            Query[i] = Query[i].toLowerCase();
            Query[i]=Query[i].replaceAll("[.,'\":?!\\s]","");
        }

        for (int i = 0; i < Words.length; i++) {
            for (int j = 0; j < stopWords.length; j++) {
                if (Words[i].equals(stopWords[j])) {
                    for (int k = i; k < Words.length - 1; k++) {
                        Words[k] = Words[k + 1];
                    }
                    Words = Arrays.copyOf(Words, Words.length - 1);
                    i--;
                    break;
                }
            }
        }

        for (int i = 0; i < Query.length; i++) {
            for (int j = 0; j < stopWords.length; j++) {
                if (Query[i].equals(stopWords[j])) {
                    for (int k = i; k < Query.length - 1; k++) {
                        Query[k] = Query[k + 1];
                    }
                    Query = Arrays.copyOf(Query, Query.length - 1);
                    i--;
                    break;
                }
            }
        }

        PorterStemmer stemmer = new PorterStemmer();
        for (int i = 0; i < Words.length; i++) {
            stemmer.setCurrent(Words[i]);
            stemmer.stem();
            Words[i] = stemmer.getCurrent();
        }

        for (String word : Words) {
            if (Inverted_Index.containsKey(word)) {
                HashMap<String, pair> WordIndex = Inverted_Index.get(word);
                for (Map.Entry<String, pair> entry : WordIndex.entrySet()) {
                    if (!Docs.containsKey(entry.getKey())) {
                        Docs.put(entry.getKey(), 1);
                        Docs_pair.put(entry.getKey(), entry.getValue());
                    } else
                        Docs.put(entry.getKey(), Docs.get(entry.getKey()) + 1);
                }
            }
        }

        for (Map.Entry<String, Integer> entry : Docs.entrySet()) {
            if (entry.getValue() == Words.length)
                Unique_Docs.put(entry.getKey(), Docs_pair.get(entry.getKey()));
        }
        for (int mm = 0; mm < Words.length; mm++) {
            System.out.println(Words[mm]);
        }

        for (int mm = 0; mm < Query.length; mm++) {
            System.out.println(Query[mm]);
        }
    }


    public ArrayList<pair> Phrase_Search() {
        for (String i : Unique_Docs.keySet()) {
            List<List<Integer>> Positions = new ArrayList<>();
            List<List<String>> RealWords = new ArrayList<>();
            // Initialize Positions and RealWords with empty lists
            for (int j = 0; j < Words.length; j++) {
                Positions.add(new ArrayList<Integer>());
                RealWords.add(new ArrayList<String>());
            }
            int m = 0;
            for (String word : Words) {
                if (Inverted_Index.containsKey(word)) {
                    pair p = Inverted_Index.get(word).get(i);
                    for (int k=0;k<p.positions.size();k++) {
                        Positions.get(m).add(p.positions.get(k).position);
                        RealWords.get(m).add(p.positions.get(k).originalWord);
                    }
                    m++;
                }
            }
            for (int f=0;f<RealWords.size();f++)
            {
                for(int q=0;q<RealWords.get(f).size();q++)
                    System.out.println(RealWords.get(f).get(q)+" ---> "+Positions.get(f).get(q));
            }


            int qcounter;
            for (int z = 0; z < Positions.get(0).size(); z++)   //loop on positions of first word
            {
                // check if the subsequent words in the search phrase occur immediately after the first word in the document
                qcounter = 0;
                System.out.println(RealWords.get(0).get(z) + " -> " + Positions.get(0).get(z) + "  :" + Query[qcounter]);
                if (Query[qcounter].equals(RealWords.get(0).get(z))) {
                    qcounter++;
                    boolean match = true;
                    for (int j = 1; j < Positions.size(); j++) {    //loop for the words
                        boolean found = false;
                        for (int k = 0; k < Positions.get(j).size(); k++) {    //loop for positions of the words
                            System.out.println(RealWords.get(j).get(k) + " -> " + Positions.get(j).get(k) + "  :" + Query[qcounter]);
                            if (Positions.get(j).get(k) == Positions.get(0).get(z) + j && Query[qcounter].equals(RealWords.get(j).get(k))) {
                                qcounter++;
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        Docs_With_Phrase.add(Unique_Docs.get(i));
                        break;
                    }
                }
            }
        }
        return Docs_With_Phrase;
    }
}
