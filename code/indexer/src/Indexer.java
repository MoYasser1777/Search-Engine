import java.net.MalformedURLException;
import java.util.*;
import java.io.*;
import java.net.URL;
import java.io.File;






import com.mongodb.client.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.tartarus.snowball.ext.PorterStemmer;

import static java.lang.Math.log;


public class Indexer implements Runnable {
    final int NumberOfThreads = 12;
    protected static HashMap<String, HashMap<String, pair>> Inverted_Index;

    protected static List<String> Stop_Words;

    protected static List<String> links;

    private static String[] Html_List;
    private static List<pair4> Html_Links;
    private static String Root_Path;
    private static HashMap<String, HashMap<String, Double>> FileName_WordsScore;
    private static HashMap<String, Double> HTML_Tags;
    private static List<JSONObject> JSON_Inverted_Index;

    //kady
    private static List<JSONObject> webpages; //kady
    //kady

    public void Indexing() throws InterruptedException, IOException {

        Inverted_Index = new HashMap<>();

        webpages = new Vector<>(); //kady

        //initialize scores of each tag (ex: title-->3.0)
        Initialize_Tags();

        //read stopWords from text file
        Read_StopWords();

        Read_Links();

        //Read All html files from RootPath
        Read_Html_Files();
//        for (int mn = 0; mn < links.size(); mn++) {
//            System.out.println(Html_List[mn]);
//        }
        Html_Links = new ArrayList<>();
        for (int mn = 0; mn < links.size(); mn++) {
            pair4 htmlLink = new pair4(Html_List[mn], links.get(mn));
            Html_Links.add(htmlLink);
        }
//        for (int mn = 0; mn < links.size(); mn++) {
//            System.out.println(Html_Links.get(mn).html + " " + Html_Links.get(mn).link);
//        }

        //Define array of threads to make inverted index in parallel
        Thread[] Threads_Arr = new Thread[NumberOfThreads];

        for (int i = 0; i < NumberOfThreads; i++) {
            Threads_Arr[i] = new Thread(new Indexer());
            Threads_Arr[i].setName(String.valueOf(i));
        }
        for (int i = 0; i < NumberOfThreads; i++) {
            Threads_Arr[i].start();
        }
        //Wait all threads to join with final inverted index
        for (int i = 0; i < NumberOfThreads; i++) {
            Threads_Arr[i].join();
        }

        Calc_IDF();
        //convert the inverted index to json files to upload to the databases
        JSON_Inverted_Index = Convert_InvertedIndex_to_JSONfile(Inverted_Index);

//        for (String term : Inverted_Index.keySet()) {
//            System.out.println("Word: " + term);
//            HashMap<String, pair> docMap = Inverted_Index.get(term);
//            for (String doc : docMap.keySet()) {
//                pair docPair = docMap.get(doc);
//                System.out.println("  Doc: " + doc);
//                System.out.println("    tf: " + docPair.tf);
//                System.out.println("    idf: " + docPair.idf);
//                System.out.println("    positions: " + docPair.positions);
//                System.out.println("    score: " + docPair.score);
//                System.out.println("    url: " + docPair.url);
//                System.out.println("    title: " + docPair.title);
//                System.out.println("    description: " + docPair.description);
//                for (int k=0; k<docPair.positions.size(); k++)
//                {
//                    pair2 pair = docPair.positions.get(k);
//                    System.out.println(pair.position + " "+pair.OriginalWord);
//                }
//
//            }
//            System.out.println("-------------------------------------------------------------------");
//        }
//        Phrase_Searching ph=new Phrase_Searching("that Kyren Wilson won the first four frames in all of his snooker matches",Inverted_Index);
//        ArrayList<pair> arr=ph.Phrase_Search();
//        if(!arr.isEmpty())
//            for(int k=0;k<arr.size();k++)
//            System.out.println(arr.get(k).url);
//
//
//        System.out.println(webpages);
        Fill_Database(JSON_Inverted_Index,webpages);
    }

    @Override
    public void run() {
        int Thread_Num = Integer.parseInt(Thread.currentThread().getName());

        int start_index = Thread_Num * (Html_List.length / NumberOfThreads);
        int end_index = (Thread_Num + 1) * (Html_List.length / NumberOfThreads);

        //special case if number of files are not divisible by number of thread
        //the last thread takes the rest of files
        if (Thread_Num == NumberOfThreads - 1 && end_index != Html_List.length)
            end_index = Html_List.length;

        //loop on all html files , each thread takes its segment of files
        for (int i = start_index; i < end_index; i++) {

            //Reading the content of html file as string "plain text"
            StringBuilder HtmlFile_As_String = new StringBuilder("");
            Document Parsed_HTML = null;
            try {
                BufferedReader reader = new BufferedReader(new FileReader(Root_Path + Html_List[i]));
                String line = "";
                StringBuilder temp = new StringBuilder("");
                while ((line = reader.readLine()) != null) {
                    temp.append(line);
                }
                reader.close();
                Parsed_HTML = Jsoup.parse(temp.toString());
                HtmlFile_As_String.append(Parsed_HTML.body().text());

            } catch (IOException e) {
            }

            //Array of words in each document
            List<pair3> Words;
            //Extract words from the content of the page
            Words = Extract_Words_from_Page(HtmlFile_As_String.toString());
            //remove stopWords
//            Words.removeAll(Stop_Words);
            Iterator<pair3> iter = Words.iterator();
            while (iter.hasNext()) {
                pair3 word = iter.next();
                for (String stop : Stop_Words) {
                    if (word.Word.equals(stop)) {
                        iter.remove();
                        break;
                    }
                }
            }
            //stemming
            Words = Stemming(Words);

            //Make inverted index for the Words, Each thread makes part of this inverted index from the segment of files it has
            synchronized (Inverted_Index) {
                HashMap<String, pair> Word_Docs;
                Double TF;
                for (int j = 0; j < Words.size(); j++) {
                    if (!Inverted_Index.containsKey(Words.get(j).Word)) {
                        Word_Docs = new HashMap<String, pair>();
                        Inverted_Index.put(Words.get(j).Word, Word_Docs);
                    } else {
                        Word_Docs = Inverted_Index.get(Words.get(j).Word);
                    }
                    if (!Word_Docs.containsKey(Html_List[i])) {
                        TF = 1.0/Words.size();
                        String url = "", title = "";
                        if (Parsed_HTML != null) {
                            url = links.get(i);
                            title = Parsed_HTML.title();
                        }
                        //String temp = url.toString();
                        //System.out.println(temp);
                        Word_Docs.put(Html_List[i], new pair(TF,0.0, List.of(new pair2(j, Words.get(j).OriginalWord)), 0.0, url, title, ""));
                    } else {
                        Word_Docs.get(Html_List[i]).tf+=1.0/Words.size();
                        List<pair2> immutableList = Word_Docs.get(Html_List[i]).positions;
                        List<pair2> mutableList = new ArrayList<>(immutableList);
                        mutableList.add(new pair2(j, Words.get(j).OriginalWord));
                        Word_Docs.get(Html_List[i]).positions = mutableList;
                    }
                }
                //System.out.println(Words.size()+"    Doc:"+Html_List[i]);

                //Add score of each word (maybe used in ranker)
                try {

                    Add_Scores_of_Tags(Parsed_HTML, Html_List[i],links.get(i));
                    for (String filename : FileName_WordsScore.keySet()) {
                        HashMap<String, Double> wordScores = FileName_WordsScore.get(filename);

                        for (String word : wordScores.keySet()) {
                            Double score = wordScores.get(word);

                            if (Inverted_Index.containsKey(word) && Inverted_Index.get(word).containsKey(filename)) {
                                Inverted_Index.get(word).get(filename).score = score;
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                //Add paragraphs of each word
                String text = Parsed_HTML.text();

                //Split the text content into paragraphs
                String[] paragraphs = text.split("\\.");

                //Search each paragraph for occurrences of the query
                PorterStemmer stemmer = new PorterStemmer();
                for (String paragraph : paragraphs) {
                    String[] words = paragraph.split(" ");
                    for (String word : words) {
                        stemmer.setCurrent(word);
                        stemmer.stem();
                        word = stemmer.getCurrent().toLowerCase();
                        if (Inverted_Index.containsKey(word) && Inverted_Index.get(word).containsKey(Html_List[i])) {
                            //Return the paragraph as a search result
                            Inverted_Index.get(word).get(Html_List[i]).description = paragraph;
                        }
                    }
                }
            }
        }

    }


    private static List<JSONObject> Convert_InvertedIndex_to_JSONfile(HashMap<String, HashMap<String, pair>> inverted_index) {
        List<JSONObject> Word_JSONS_List = new Vector<>();

        for (String word : inverted_index.keySet()) {

            JSONObject word_JSON = new JSONObject();
            word_JSON.put("word", word);
            List<JSONObject> Docs_JSON_List = new Vector<>();

            for (String doc : inverted_index.get(word).keySet()) {
                JSONObject Doc_JSON = new JSONObject();
                pair Index = inverted_index.get(word).get(doc);
                Doc_JSON.put("Document", doc);
                Doc_JSON.put("Title", Index.title);
                Doc_JSON.put("URL", Index.url);
                Doc_JSON.put("TF", Index.tf);
                Doc_JSON.put("IDF", Index.idf);
                JSONArray jsonArray = new JSONArray();
                for (pair2 pair : Index.positions) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("position", pair.position);
                    jsonObject.put("originalWord", pair.OriginalWord);
                    jsonArray.add(jsonObject);
                }
                Doc_JSON.put("Indexes",jsonArray);

                Doc_JSON.put("Score", Index.score);
                Doc_JSON.put("Description", Index.description);

                Docs_JSON_List.add(Doc_JSON);
            }
            Word_JSONS_List.add(word_JSON);
            word_JSON.put("documents", Docs_JSON_List);
        }
        return Word_JSONS_List;
    }

    private static void Fill_Database(List<JSONObject> Word_JSONS_List,List<JSONObject> webpages) {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("SearchEngine");
        MongoCollection<org.bson.Document> wordsCollection = database.getCollection("Words");
        MongoCollection<org.bson.Document> webpagesCollection = database.getCollection("webpages"); //kady

        for (int i = 0; i < Word_JSONS_List.size(); i++) {
            org.bson.Document document = org.bson.Document.parse(Word_JSONS_List.get(i).toString());
            String word = document.getString("word");

            // Check if the word already exists in the collection
            org.bson.Document existingDocument = wordsCollection.find(new org.bson.Document("word", word)).first();

            if (existingDocument == null) {
                wordsCollection.insertOne(document);
                //System.out.println("Document inserted: " + document.toJson());
            } else {
//                // Extract the URLs from the existing document
//                List<org.bson.Document> existingDocuments = existingDocument.get("documents", List.class);
//
//                // Check if the URL already exists in the existing URLs
//                String newURL = document.getString("URL");
//                boolean urlExists = false;
//                for (org.bson.Document doc : existingDocuments) {
//                    String existingURL = doc.getString("URL");
//                    if (existingURL.equals(newURL)) {
//                        urlExists = true;
//                        break;
//                    }
//                }
//
//                if (!urlExists) {
//                    // Add the new document to the existing document's "documents" array
//                    existingDocuments.add(document);
//                    wordsCollection.replaceOne(new org.bson.Document("word", word), existingDocument);
//                    System.out.println("Document updated with new URL: " + existingDocument.toJson());
//                } else {
//                    System.out.println("Document with word and URL already exists: " + existingDocument.toJson());
//                }
            }
        }



        /////////////kady
        for (int i = 0; i < webpages.size(); i++) {
            org.bson.Document document = org.bson.Document.parse(webpages.get(i).toString());
            org.bson.Document existingDocument = webpagesCollection.find(new org.bson.Document("url", document.getString("url"))).first();
            if (existingDocument == null) {
                webpagesCollection.insertOne(document);
            }
        }
        /////////////kady
    }

    private static void Initialize_Tags() {
        HTML_Tags = new HashMap<String, Double>();
        HTML_Tags.put("title", 3.0);
        HTML_Tags.put("h1", 0.9);
        HTML_Tags.put("a", 0.8);
        HTML_Tags.put("h2", 0.7);
        HTML_Tags.put("h3", 0.6);
        HTML_Tags.put("h4", 0.5);
        HTML_Tags.put("h5", 0.4);
        HTML_Tags.put("h6", 0.3);
        HTML_Tags.put("p", 0.1);
    }

    private static void Add_Scores_of_Tags(Document html, String fileName,String webpageurl) throws IOException {
        PorterStemmer stemmer = new PorterStemmer();
        Pattern pattern = Pattern.compile("\\w+");
        HashMap<String, Double> temp = new HashMap<>();
        FileName_WordsScore = new HashMap<>();

        /////////////kady
        JSONObject HTML_JSON = new JSONObject();
        HTML_JSON.put("title", html.title());
        HTML_JSON.put("url", webpageurl);
        JSONArray linksJson = new JSONArray();
        Elements links = html.select("a[href]");
        Set<String> uniqueLinks = new HashSet<>();
        for (Element link : links) {
            String url = link.attr("href");
            if (!url.startsWith("http")) { // check if it's a relative URL
                try {
                    URL base = new URL(webpageurl);
                    URL absolute = new URL(base, url); // resolve the relative URL
                    url = absolute.toString();
                } catch (MalformedURLException e) {
                    // handle the exception if the URL is malformed
                }
            }
            if (uniqueLinks.add(url)) { // check if it's a unique link before adding it
                linksJson.add(url);
            }
        }
        HTML_JSON.put("links", linksJson);
        webpages.add(HTML_JSON);
        /////////////kady



        //filtration most important tags
        for (String line : HTML_Tags.keySet()) {
            String HTML_Line = html.select(line).text();

            if (html != null && !HTML_Line.isEmpty()) {
                Matcher matcher = pattern.matcher(HTML_Line.toLowerCase());
                while (matcher.find()) {
                    stemmer.setCurrent(matcher.group());
                    stemmer.stem();
                    HTML_Line = stemmer.getCurrent();

                    if (!temp.containsKey(HTML_Line))
                        temp.put(HTML_Line, HTML_Tags.get(line));
                    else
                        temp.put(HTML_Line, temp.get(HTML_Line) + HTML_Tags.get(line));
                }
            }
        }
        FileName_WordsScore.put(fileName, temp);
    }

    /////////////kady
    private static String extractUrl(Document doc) {
        // Find the first anchor tag in the HTML
        Element anchor = doc.selectFirst("a[href]");

        if (anchor != null) {
            // Extract the URL from the href attribute
            String url = anchor.attr("href");
            return url;
        }

        return "";
    }
    /////////////kady

    public void Read_StopWords() throws IOException {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("Stop_Words.txt"));
            Stop_Words = new Vector<String>();
            String word;
            while ((word = reader.readLine()) != null) {
                Stop_Words.add(word);
            }
        } catch (IOException e) {
        }
    }

    public void Read_Links() throws IOException {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("links.txt"));
            links = new Vector<String>();
            String word;
            while ((word = reader.readLine()) != null) {
                links.add(word);
            }
        } catch (IOException e) {
        }
    }

    public void Read_Html_Files() throws IOException {
        File file = new File("rootFolder");
        Root_Path = "rootFolder//";
        Html_List = file.list();
        if (Html_List != null) {
            Arrays.sort(Html_List, new NumericFilenameComparator());
        }
    }
    private static class NumericFilenameComparator implements Comparator<String> {
        @Override
        public int compare(String s1, String s2) {
            int n1 = extractNumber(s1);
            int n2 = extractNumber(s2);
            return Integer.compare(n1, n2);
        }

        private int extractNumber(String s) {
            String number = s.replaceAll("\\D", "");
            return number.isEmpty() ? 0 : Integer.parseInt(number);
        }
    }

    public List<pair3> Extract_Words_from_Page(String HtmlFile_As_String) {
        List<pair3> Words = new ArrayList<>();

        Pattern pattern = Pattern.compile("\\w+");
        Matcher match = pattern.matcher(HtmlFile_As_String);

        while (match.find()) {
            String word = match.group();
            word=word.replaceAll("[.,'\":?!_\\s]","");
            Words.add(new pair3(word.toLowerCase(),word.toLowerCase()));
        }
        return Words;
    }

    public List<pair3> Stemming(List<pair3> Words) {
        PorterStemmer stemmer = new PorterStemmer();
        for (int j = 0; j < Words.size(); j++) {
            stemmer.setCurrent(Words.get(j).Word);
            stemmer.stem();
            Words.set(j, new pair3(stemmer.getCurrent(),Words.get(j).OriginalWord));
        }
        return Words;
    }
    public void Calc_IDF ()
    {
        for (String term : Inverted_Index.keySet()) {
            HashMap<String, pair> documents = Inverted_Index.get(term);
            int size=documents.size();
            for (String docID : documents.keySet()) {
                documents.get(docID).idf=log(6000.0/size);
            }
            Inverted_Index.put(term, documents);
        }
    }
}