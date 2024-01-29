package dev.ranker.ranker;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tartarus.snowball.ext.PorterStemmer;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;


@RestController
@RequestMapping("/")
public class RankerController {
    @Autowired
    private RankerService rankerService;

    public String[] stopWords = {"i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", "yours", "yourself", "yourselves", "he", "him", "his", "himself", "she", "her", "hers", "herself", "it", "its", "itself", "they", "them", "their", "theirs", "themselves", "what", "which", "who", "whom", "this", "that", "these", "those", "am", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "having", "do", "does", "did", "doing", "a", "an", "the", "and", "but", "if", "or", "because", "as", "until", "while", "of", "at", "by", "for", "with", "about", "against", "between", "into", "through", "during", "before", "after", "above", "below", "to", "from", "up", "down", "in", "out", "on", "off", "over", "under", "again", "further", "then", "once", "here", "there", "when", "where", "why", "how", "all", "any", "both", "each", "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very", "s", "t", "can", "will", "just", "don", "should", "now"};

    @CrossOrigin(origins = "*")
    @GetMapping
    public ResponseEntity<SearchResponse> getAllWords(@RequestParam("q") String query) {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("SearchEngine");
        MongoCollection<Document> queries = database.getCollection("queries");

        // Check if the query already exists in the collection
        Document existingQuery = queries.find(eq("query", query)).first();
        if (existingQuery != null) {
            System.out.println("Query already exists: " + query);
        }else {
            Document documentQuery = new Document("query", query);
            try {
                queries.insertOne(documentQuery);
                System.out.println("Unique string added successfully.");
            } catch (MongoException e) {
                System.out.println("Error adding unique string: " + e.getMessage());
            }

        }
        long startTime = System.currentTimeMillis();



        //new ResponseEntity<>(rankerService.getAllWebPages(), HttpStatus.OK); test returning all webpages
        //query = "Confid properli stack year govern addit complet offer path successfulli mostli visual volum rang beginn pythonologi happen newslett grow ad sure pass impact AI version cours click whose 0 fold 2 3 4 5 6 7 domain 8 9 Codecademy retriev object chapter role prove syllabu icon least delet polici search see releas term measur machin TypeScript behavior solidifi hand pathschoos c set mini learn J abl right versatil p sale r answer meet partner u affili thank imag interview showcasi featur contribut DL advic got parti aggreg brochur beauti contact legal tip ea info Swift practic rev test need count April2023 check list consent els Andrew respect take resolut month success Plone final activ PyCon virtual Radschinski origin CIO 2012 2011 back compani univers content hone Kelvin vol software rate alert skill scope client end promot live modifi sold brilliant custom go 2023 perform advert monitor howev home forum 2019 print 2018 condit PDF 2016 form 2015 certif 2014 2013 well syntax Feedburn foundat strictli career select challeng catalog DevOps visibl Armin output variabl identifi action block text join decis Kenni write flow order creat cybersecur Python period io made byte understand anonym worldwid manag system field million even experi driven doc John issu begin dog enrol booklet docsfind ES6 skip articl local mention eight YouTube world file top mai peer KB share map Mar avail product creator robust question 2nd menu comfort Chri PHP exampl relev return ll USA arrow chariti Montreal subject pleas sign screen feel motiv directli second download conveni find function logo commun stock traffic new read level todai";
        //query = "I AM HAPPILY MARRYING THIS AND PLAYER PLAYING PAYING FOR HER SERVICE";
        Map<String, SearchResults> resultMap = new HashMap<>();
        Map<String, Integer> popularityIds = new HashMap<>();
        Map<String, Set<String>> outgoingLinksMap = new HashMap<>();
        Map<String, Double> inversePopularityIds = new HashMap<>();


        String[] words = query.split(" ");

        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].toLowerCase();
        }

        for (int i = 0; i < words.length; i++) {
            for (int j = 0; j < stopWords.length; j++) {
                if (words[i].equals(stopWords[j])) {
                    for (int k = i; k < words.length - 1; k++) {
                        words[k] = words[k + 1];
                    }
                    words = Arrays.copyOf(words, words.length - 1);
                    i--;
                    break;
                }
            }
        }

        PorterStemmer stemmer = new PorterStemmer();
        for (int i = 0; i < words.length; i++) {
            stemmer.setCurrent(words[i]);
            stemmer.stem();
            words[i] = stemmer.getCurrent();
        }

        int totalNumberofDocuments = 5440; //should be 6000 when allam make crawler work
        for (String word : words) {
            List<DocumentData> wordDocuments = rankerService.getDocumentsByWord(word);
            System.out.println(wordDocuments);
            int numDocumentsPerWord = wordDocuments.size();
            for (DocumentData document : wordDocuments) {
                String url = document.getURL();
                if (resultMap.containsKey(url)) {
                    SearchResults result = resultMap.get(url);
                    result.setRelevance(result.getRelevance() + (document.getTF()*document.getIDF()));
                    result.setTagImportance(result.getTagImportance() + document.getScore());
                } else {
                    SearchResults result = new SearchResults();
                    result.setUrl(url);
                    result.setQueryParagraph(document.getDescription());
                    result.setTitle(document.getTitle());
                    result.setPopularity(1);
                    result.setRelevance(document.getTF()*document.getIDF());
                    result.setTagImportance(document.getScore());
                    resultMap.put(url, result);
                    //////////////////url
                    if (!popularityIds.containsKey(url)) {
                        popularityIds.put(url, popularityIds.size() + 1);
                    }

                    String links[] = rankerService.getLinksFromUrl(url);
                    Set<String> linksFromUrl = new HashSet<>();

                    for (String link : links) {
                        linksFromUrl.add(link);
                        if (!popularityIds.containsKey(link)) {
                            popularityIds.put(link, popularityIds.size() + 1);
                        }
                    }
                    outgoingLinksMap.put(url, linksFromUrl);



                }
            }
        }



        //return new ResponseEntity<>(results, HttpStatus.OK);
        PageRank pageRank = new PageRank(popularityIds, outgoingLinksMap);
        inversePopularityIds = pageRank.calculatePageRank();
        for (String url : resultMap.keySet()) {
            SearchResults result = resultMap.get(url);
            result.setPopularity(inversePopularityIds.get(url));
        }
        List<SearchResults> results = new ArrayList<>(resultMap.values());

        double wRelevance = 0.8; // example values for the weights
        double wTag = 0.2;
        double wFactor = 1.0;

        Comparator<SearchResults> comparator = new SearchResultsComparator(wRelevance, wTag, wFactor);

        results.sort(comparator); // sort the List in descending order based on the Ranking Score

        // set the ranking field for each SearchResults object in the sorted List
        for (SearchResults result : results) {
            result.setRanking(result.getPopularity() * (wRelevance * result.getRelevance() + wTag * result.getTagImportance()) * wFactor);
        }

        for (String url : inversePopularityIds.keySet()) {
            System.out.println(url + ": " + inversePopularityIds.get(url));
        }
        long endTime = System.currentTimeMillis();
        double elapsedTime = (endTime - startTime) / 1000.0;
        SearchResponse response = new SearchResponse(results, elapsedTime);


        System.out.println("Elapsed time: " + elapsedTime + " seconds");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @CrossOrigin(origins = "*")
    @GetMapping("/phrase")
    public ResponseEntity<SearchResponse> searchForPhrase(@RequestParam("q") String query) {
        long startTime = System.currentTimeMillis();
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("SearchEngine");
        MongoCollection<Document> queries = database.getCollection("queries");

        // Check if the query already exists in the collection
        Document existingQuery = queries.find(eq("query", query)).first();
        if (existingQuery != null) {
            System.out.println("Query already exists: " + query);
        }else {
            Document documentQuery = new Document("query", query);
            try {
                queries.insertOne(documentQuery);
                System.out.println("Unique string added successfully.");
            } catch (MongoException e) {
                System.out.println("Error adding unique string: " + e.getMessage());
            }

        }




        // Implement phrase searching logic here
        //System.out.println(rankerService.getAllWords());
        List<Word> Inverted_Index_DB = rankerService.getAllWords();


        HashMap<String,HashMap<String,pair>> Inverted_Index = new HashMap<>();
        for (Word word : Inverted_Index_DB) {
            // do something with the word object
            // for example, print the word and its document data
            HashMap<String,pair> temp = new HashMap<>();
            //Double tf, List<Integer> positions, Double tag, String url, String title, String description
            for (DocumentData documentData : word.getDocuments()) {
                temp.put(documentData.getDocument(),new pair(documentData.getTF(),documentData.getIDF(),documentData.getIndexes(),documentData.getScore(),documentData.getURL(),documentData.getTitle(),documentData.getDescription()));
            }
            Inverted_Index.put(word.getWord(),temp);
        }

        Phrase_Searching ps = new Phrase_Searching(query,Inverted_Index);
        List<pair> hamada = ps.Phrase_Search();
        Map<String, SearchResults> resultMap = new HashMap<>();
        Map<String, Integer> popularityIds = new HashMap<>();
        Map<String, Set<String>> outgoingLinksMap = new HashMap<>();
        Map<String, Double> inversePopularityIds = new HashMap<>();
        for (pair p : hamada) {
            System.out.println("tf: " + p.tf);
            System.out.println("positions: " + p.positions);
            System.out.println("score: " + p.score);
            System.out.println("url: " + p.url);
            System.out.println("title: " + p.title);
            System.out.println("description: " + p.description);
            //////////////////////////////////
            String url = p.url;
            if (resultMap.containsKey(url)) {
                SearchResults result = resultMap.get(url);
                result.setRelevance(result.getRelevance() + (p.tf*p.idf));
                result.setTagImportance(result.getTagImportance() + p.score);
            } else {
                SearchResults result = new SearchResults();
                result.setUrl(url);
                result.setQueryParagraph(p.description);
                result.setTitle(p.title);
                result.setPopularity(1);
                result.setRelevance(p.tf*p.idf);
                result.setTagImportance(p.score);
                resultMap.put(url, result);
                //////////////////url
                if (!popularityIds.containsKey(url)) {
                    popularityIds.put(url, popularityIds.size() + 1);
                }

                String links[] = rankerService.getLinksFromUrl(url);
                Set<String> linksFromUrl = new HashSet<>();

                for (String link : links) {
                    linksFromUrl.add(link);
                    if (!popularityIds.containsKey(link)) {
                        popularityIds.put(link, popularityIds.size() + 1);
                    }
                }
                outgoingLinksMap.put(url, linksFromUrl);



            }
        }

        PageRank pageRank = new PageRank(popularityIds, outgoingLinksMap);
        inversePopularityIds = pageRank.calculatePageRank();
        for (String url : resultMap.keySet()) {
            SearchResults result = resultMap.get(url);
            result.setPopularity(inversePopularityIds.get(url));
        }
        List<SearchResults> results = new ArrayList<>(resultMap.values());

        double wRelevance = 0.8; // example values for the weights
        double wTag = 0.2;
        double wFactor = 1.0;

        Comparator<SearchResults> comparator = new SearchResultsComparator(wRelevance, wTag, wFactor);

        results.sort(comparator); // sort the List in descending order based on the Ranking Score

        // set the ranking field for each SearchResults object in the sorted List
        for (SearchResults result : results) {
            result.setRanking(result.getPopularity() * (wRelevance * result.getRelevance() + wTag * result.getTagImportance()) * wFactor);
        }



        long endTime = System.currentTimeMillis();
        double elapsedTime = (endTime - startTime) / 1000.0;
        SearchResponse response = new SearchResponse(results, elapsedTime);
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @CrossOrigin(origins = "*")
    @GetMapping("/autocomplete")
    public ResponseEntity<List<String>> autocomplete() {
        List<queries> queriesList = rankerService.getAllQueries();
        List<String> queryStrings = new ArrayList<>();

        for (queries query : queriesList) {
            queryStrings.add(query.getQuery());
        }

        return new ResponseEntity<>(queryStrings, HttpStatus.OK);
    }
}

