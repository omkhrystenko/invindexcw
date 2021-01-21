package kw;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        IndexBuilder[] threads;
        ArrayList<Path> files = new ArrayList<>();
        int numOfThread;

        System.out.print("Enter the number of threads: ");
        numOfThread = new Scanner(System.in).nextInt();

        threads = new IndexBuilder[numOfThread];
        IndexBuilder.initIndex(numOfThread);

        createFilesList(files);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numOfThread; i++) {
            threads[i] = new IndexBuilder(files);
        }
        for (int i = 0; i < numOfThread; i++) {
            threads[i].join();
        }
        
        long allTime = System.currentTimeMillis() - startTime;

        System.out.println(allTime);
        searchIndexWord();
    }

    private static void searchIndexWord(){
        String searchWord = "";
        while (!searchWord.equals("programmexit")){
            System.out.print("Enter word for search: ");
            searchWord = new Scanner(System.in).next();
            for (Path path : IndexBuilder.getWordData(searchWord.toLowerCase())){
                System.out.println(path);
            }
        }
    }

    private static void createFilesList(Collection<Path> collection) {
        try (Stream<Path> writer = Files.walk(Paths.get("resources"))) {
            writer.filter(Files::isRegularFile).collect(Collectors.toCollection(() -> collection));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}