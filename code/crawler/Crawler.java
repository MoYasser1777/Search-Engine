package engine;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.DateCodec;
import org.json.simple.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.module.FindException;
import java.sql.Time;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class Crawler implements Runnable {
    public static final int NUM_OF_PAGES = 100;
    private static  Boolean addLinks = true;
    public static int cnt=0;
    public static int start=0;
    public static int end=0;
    int c = 0;
    static HashSet<String> visited;
    static HashSet<String> bad;
    static HashSet<String> compact;
    static String[] crawlerLinks;
    MongoClient mongoClient;
    MongoDatabase database;
    static MongoCollection<org.bson.Document> visitedCollection;
    static MongoCollection<org.bson.Document> badCollection;
    static MongoCollection<org.bson.Document> compactCollection;
    static MongoCollection<org.bson.Document> queueCollection;
    static MongoCollection<org.bson.Document> indexCollection;

    Queue<String> linkQ;
    String name;

    Crawler() {
    }



    public void run() {
//            String link = popQueue();
//
//            getLinks(getDocument(link));
//            int inc=incrementCnt();
//            saveFile(getDocument(link),link,utils.hashString(getDocument(link).html()),inc);
        while (true) {
            long startTime = System.nanoTime();
            String link = popQueue();
            System.out.println("poping took "+(System.nanoTime()-startTime)/1e9);
            if (link == null) {
                System.out.println("error in retrieving link from database terminating");
                return;
            }
            startTime = System.nanoTime();
            Document doc = getDocument(link);
            System.out.println("getting document took "+(System.nanoTime()-startTime)/1e9);
            if (doc == null) {
                System.out.println("this link document is not available "+link);
                continue;
            }

            String hash = utils.hashString(doc.html());
            if (hash == null) continue;
            //checking if it was already visited, or we already have the same html
            startTime = System.nanoTime();
            if (!check(link, hash)) {
                System.out.println("bad link is "+ link);
                continue;
            }
            System.out.println("checking took "+ (System.nanoTime()-startTime)/1e9);
            startTime = System.nanoTime();
            int inc = incrementCnt();
            System.out.println("incrementing took"+(System.nanoTime()-startTime)/1e9);
            if (inc == -1) {
                visited.remove(link);
                return;
            }
            startTime = System.nanoTime();
            if (!saveFile(doc, link,hash, inc))
                System.out.println("error in saving");
            System.out.println("saving file took" +(System.nanoTime()-startTime)/1e9);
            startTime = System.nanoTime();
            if(addLinks)
                getLinks(doc);
            System.out.println("adding links took "+(System.nanoTime()-startTime)/1e9);
            System.out.println("\n");
        }
    }




    private Document getDocument(String Link) {
        try {
            Connection connect = Jsoup.connect(Link);
            Document doc = connect.get();
            if (connect.response().statusCode() != 200) {
//                System.out.println("bad Connection in link" + Link);
                return null;
            }
//            System.out.println("good connection in link" + Link);
            return doc;
        } catch (IOException e) {
            return null;
        }

    }
    public boolean getLinks(Document doc) {
        System.out.println("number of links is "+doc.select("a[href]").size());
        int cnt=0;
        for (Element link : doc.select("a[href]")) {
            if(!addLinks||cnt>50)
                break;
            String new_link = utils.normalizeURL(link.absUrl("href"));
            if (new_link == null) continue;
            pushQueue(new_link);
            cnt++;
//            System.out.println("cnt is  "+ cnt+"  link is added by thread "+Thread.currentThread().getName()+" "+new_link);
        }
        return true;
    }



    public boolean saveFile(Document doc, String link,String hash, int num) {
        String file = null;
        file = "../html/" + num + ".html";

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(doc.html());
            writer.close();
        } catch (IOException e) {
            return false;
        }

        JSONObject linkJSON = new JSONObject();
        linkJSON.put("link", link);
        linkJSON.put("index", num);
        visitedCollection.insertOne(org.bson.Document.parse(linkJSON.toString()));
        JSONObject hashJSON = new JSONObject();
        hashJSON.put("hash",hash);
        compactCollection.insertOne(org.bson.Document.parse(hashJSON.toString()));
        return true;
    }

    public synchronized static int incrementCnt() {
        org.bson.Document cntQuery = new org.bson.Document("cnt", new org.bson.Document("$exists", true));
        MongoCursor<org.bson.Document> cursor = indexCollection.find(cntQuery).iterator();
        if (!cursor.hasNext()) {
            System.out.println("no cursor");
            return -1;
        }
        org.bson.Document cntDoc = cursor.next();
        int cnt = Integer.parseInt(cntDoc.get("cnt").toString());// getting the start idx
        if(cnt>NUM_OF_PAGES)
            return -1;
        org.bson.Document update = new org.bson.Document("$set", new org.bson.Document("cnt", cnt + 1));
        indexCollection.updateOne(new org.bson.Document(), update);
        System.out.println("cnt now is "+(cnt));
        return cnt;

    }


    private synchronized static int getStartIdx() {

            org.bson.Document update = new org.bson.Document("$set", new org.bson.Document("start", start + 1));
            indexCollection.updateOne(new org.bson.Document(), update);
            System.out.println("i am thread " + Thread.currentThread().getName() + " " + start);
            start++;
            return start-1;
    }

    private String popQueue() {

        int startIdx = getStartIdx();
        if (startIdx == -1) {
            return null;
        }

        org.bson.Document idxQuery = new org.bson.Document("index", startIdx);
        org.bson.Document linkDoc = queueCollection.findOneAndDelete(idxQuery);
        if (linkDoc == null) {
            System.out.println("error in finding link"+startIdx);
            return null;
        }

        return linkDoc.get("link").toString();
    }

    private synchronized static int getEndIdx() {
        if(end-start>5000)
        {
            return -1;
        }
        org.bson.Document update = new org.bson.Document("$set", new org.bson.Document("end", end + 1));
        indexCollection.updateOne(new org.bson.Document(), update);
        end=end+1;
        return end;
    }

    private Boolean pushQueue(String link) {

        int endIdx = getEndIdx();
        if (endIdx == -1) {
            return false;
        }
        JSONObject json = new JSONObject();
        json.put("index", endIdx);
        json.put("link", link);

        queueCollection.insertOne(org.bson.Document.parse(json.toString())); // inserting the new link in queue

        return true;
    }

    private synchronized static boolean check(String link, String hash) {
        if (visited.contains(link) || bad.contains(link))
            return false;
        if (compact.contains(hash)) {
            bad.add(link);
            JSONObject js = new JSONObject();
            js.put("link", link);
            badCollection.insertOne(org.bson.Document.parse(js.toString()));
            return false;
        }

        visited.add(link);
        compact.add(hash);
        return true;
    }




}
