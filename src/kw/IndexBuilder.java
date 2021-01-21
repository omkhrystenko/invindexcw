package kw;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class IndexBuilder extends Thread{
    private static ArrayList<Path> filesList;
    private static AtomicInteger docFile = new AtomicInteger(0);
    private static ConcurrentHashMap<String, List<Integer>> invIndex;

    IndexBuilder(ArrayList<Path> filesList) {
        this.filesList = filesList;
        start();
    }

    @Override
    public void run() {
        int docId;
        List<String> readFile;
        Path docPath;


        HashMap<String, List<Integer>> threadLevelIndex = new HashMap<>();

        while ((docId = docFile.getAndIncrement()) < filesList.size()) {
            try {
                docPath = filesList.get(docId);
                readFile = Files.readAllLines(docPath);

                for (String docLine : readFile) {
                    docLine = processLine(docLine);
                    addStringToThreadIndex(docLine, docId, threadLevelIndex);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        combineThreadIndexes(threadLevelIndex);
    }


   public static void initIndex(int numOfThreads) {
            invIndex = new ConcurrentHashMap<>(50,50, numOfThreads);
    }

    private String processLine(String str) {
        String result;

        result = str.replaceAll("(^[\\W_]+)|([\\W_]+$)", "")
                .replaceAll("[\\W_]", " ")
                .replaceAll("\\s+", " ");

        return result.toLowerCase();
    }


    private void addStringToThreadIndex(String str, int docId, HashMap<String, List<Integer>> map) {
        List<Integer> documentList;
        String[] words = str.split(" ");

        for (String word : words) {

            documentList = map.get(word);

            if (documentList == null) {
                documentList = new ArrayList<>();
                map.put(word, documentList);
            }

            if (!documentList.contains(docId)) {
                documentList.add(docId);
            }
        }
    }

    private void combineThreadIndexes(HashMap<String, List<Integer>> threadLevelIndex) {
        List<Integer> documentsList;

        for (Map.Entry<String, List<Integer>> entry : threadLevelIndex.entrySet()) {

            documentsList = invIndex.get(entry.getKey());

            if (documentsList == null) {
                documentsList = Collections.synchronizedList(new ArrayList<>(entry.getValue()));
            } else {
                documentsList.addAll(entry.getValue());
            }

            invIndex.put(entry.getKey(), documentsList);
            documentsList.sort(Integer::compareTo);
        }
    }

    public static ArrayList<Path> getWordData(String word){
        List<Integer> idList = invIndex.get(word);
        ArrayList<Path> pathList = new ArrayList<>();
        if(!(idList == null)) {
            for (Integer id : idList) {
                Path path = filesList.get(id);
                pathList.add(path);
            }
        }else pathList.add(Paths.get("This word was not available in files"));

            return pathList;
    }
}
