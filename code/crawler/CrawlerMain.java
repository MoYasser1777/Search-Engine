package engine;



import com.mongodb.client.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter;
import org.bson.Document;
import com.mongodb.client.MongoCursor;
import org.json.simple.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CrawlerMain {



    private static void checkFolder(){
        String directoryPath = "../html"; // Replace with the actual directory path

        Path path = Paths.get(directoryPath);

        // Check if the directory exists
        if (Files.exists(path)) {
            System.out.println("Directory already exists.");
        } else {
            try {
                // Create the directory
                Files.createDirectory(path);
                System.out.println("Directory created successfully.");
            } catch (Exception e) {
                System.err.println("Failed to create directory: " + e.getMessage());
            }
        }
    }
    public void Read_Links() throws IOException {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database= mongoClient.getDatabase("SearchEngine");
        MongoCollection<org.bson.Document> visited= database.getCollection("visited");
        MongoCollection<org.bson.Document> index= database.getCollection("visited");

        org.bson.Document endQuery = new org.bson.Document("end", new org.bson.Document("$exists", true));
        MongoCursor<org.bson.Document> cursor = index.find(endQuery).iterator();
        if (!cursor.hasNext()) {
            System.out.println("couldn't find count");
            return ;
        }
        org.bson.Document startDoc = cursor.next();
        int cnt = Integer.parseInt(startDoc.get("cnt").toString());// getting the start idx
        FindIterable<org.bson.Document> iterable = visited.find();
        Vector<String> links = new Vector<String >(cnt);
        for (Document doc:
             iterable) {
            links.set(Integer.parseInt(doc.get("index").toString()),doc.get("link").toString());
        }
        for (String s:
             links) {
            System.out.println(s);
        }
    }
    public static Boolean saveLinks(String [] crawlerLinks){
        String file = "../html/links.txt";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i < crawlerLinks.length; i++) {
                writer.write(crawlerLinks[i]+"\n");
            }
            writer.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    private static void fillHashSet(MongoCollection collection,HashSet<String>set,String key){
        FindIterable<Document> documents = collection.find();
        MongoCursor<Document> cursor = documents.iterator();
        while (cursor.hasNext()) {
            Document document = cursor.next();
            set.add((String)document.get(key));
//            System.out.println(key + document.get(key));
        }
    }

    private static void init(HashSet<String> visited, HashSet<String> bad, HashSet<String> compact){
        MongoDatabase database;
        MongoCollection<Document> visitedCollection;
        MongoCollection<org.bson.Document> badCollection;
        MongoCollection<org.bson.Document> compactCollection;
        MongoCollection<org.bson.Document> queueCollection;

        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        database = mongoClient.getDatabase("SearchEngine");
        visitedCollection=database.getCollection("visited");
        badCollection=database.getCollection("bad");
        compactCollection=database.getCollection("compact");
        queueCollection=database.getCollection("queue");


        MongoCollection<org.bson.Document> index=database.getCollection("index");

        Document filter = new Document("start", 3);
        Document query = new Document("start", new Document("$exists", true));
        MongoCursor<Document> cursor = index.find(query).iterator();
        Document document = cursor.next();
        System.out.println(document.get("cnt"));
        Crawler.start=Integer.parseInt(document.get("start").toString());
        Crawler.end=Integer.parseInt(document.get("end").toString());
        Crawler.cnt=Integer.parseInt(document.get("cnt").toString());

        Crawler.compactCollection=compactCollection;
        Crawler.visitedCollection=visitedCollection;
        Crawler.badCollection=badCollection;
        Crawler.indexCollection=index;
        Crawler.queueCollection=queueCollection;

       fillHashSet(visitedCollection,visited,"link");
       fillHashSet(badCollection,bad,"link");
       fillHashSet(compactCollection,compact,"str");

       Crawler.visited=visited;
       Crawler.compact=compact;
       Crawler.bad=bad;


    }
    public static void main(String[] args) throws InterruptedException {
        checkFolder();
        LinkedList<String> linkQ=new LinkedList<>();
        HashSet<String> visited = new HashSet<String>();
        HashSet<String> bad = new HashSet<String>();
        HashSet<String> compact = new HashSet<String>();
        String [] crawlerLinks=new String[Crawler.NUM_OF_PAGES];
        init(visited,bad,compact);

//        try {
//            File Links = new File("links.txt");
//            Scanner reader = new Scanner(Links);
//            while (reader.hasNextLine()) {
//                linkQ.add(reader.nextLine());
//            }
//        } catch (FileNotFoundException e) {
//            System.out.println("error in reading initial links\nprogram will terminate");
//            return;
//        }

        int cnt = 0, numThread = 0;

        Scanner scan = new Scanner(System.in);
        numThread = 8;
        Thread[] threads = new Thread[numThread];
        System.out.println("starting");
        long startTime = System.nanoTime();
        for (int i = 0; i < numThread; i++) {
            threads[i] = new Thread(new Crawler());
            threads[i].setName(Integer.toString(i+1));
            threads[i].start();
        }
        for (int i = 0; i < numThread; i++) {
            threads[i].join();
        }
        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;
        System.out.println("elapsed time is "+elapsedTime/1e9);
        System.out.println("visited size is  "+visited.size());
//        for (String s:
//             visited) {
//            System.out.println(s);
//        }
//        for (String s:
//                crawlerLinks) {
//            System.out.println(s);
//        }
    }

}
