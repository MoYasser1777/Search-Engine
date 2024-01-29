package dev.ranker.ranker;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



import java.util.ArrayList;
import java.util.List;

@Service
public class RankerService {
    @Autowired
    private WordsRepository wordsRepository;

    @Autowired
    private WebpagesRepository webpagesRepository;

    @Autowired
    private QueriesRepository queriesRepository;

    public List<DocumentData> getDocumentsByWord(String word) {
        // Query the repository for the Word object with the given word
        System.out.println(word);
        Word wordObj = wordsRepository.findByWord(word);
        System.out.println(wordObj);
        if (wordObj == null) {
            return new ArrayList<>();
        }

        // Extract the DocumentData objects from the Word object
        List<DocumentData> documentDataList = new ArrayList<>();
        for (DocumentData documentData : wordObj.getDocuments()) {
            documentDataList.add(documentData);
        }

        return documentDataList;
    }



    public List<Webpages> getAllWebPages(){
        return webpagesRepository.findAll();
    }

    public List<queries> getAllQueries() { return queriesRepository.findAll(); }

    public String[] getLinksFromUrl(String url) {
        Webpages webpages = webpagesRepository.findByUrl(url);
        if (webpages != null) {
            return webpages.getLinks();
        } else {
            return new String[]{};
        }
    }

    public List<Word> getAllWords() {
        return wordsRepository.findAll();
    }








}
