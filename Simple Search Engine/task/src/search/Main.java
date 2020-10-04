package search;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

class Finder {

    private FindingStrategy strategy;

    public Finder(FindingStrategy strategy) {
        this.strategy = strategy;
    }

    public List<String> find(String key, Storage dataBase) {
        return strategy.find(key, dataBase);
    }
}

interface FindingStrategy {

    List<String> find(String key, Storage dataBase);

}

class FindAll implements FindingStrategy {
    public List<String> find(String key, Storage dataBase) {
        List<String> result = new ArrayList<>();
        Map<String, Set<Integer>> invertedIndex = dataBase.getInvertedIndex();
        String[] words = key.toLowerCase().split("\\s+");
        if (!invertedIndex.containsKey(words[0])) {
            return result;
        }
        Set<Integer> matchLines = invertedIndex.get(words[0]);
        for (int i = 1; i < words.length; i++) {
            if (!invertedIndex.containsKey(words[i])) {
                return result;
            }
            matchLines.retainAll(invertedIndex.get(words[i]));
        }
        for (Integer i : matchLines) {
            result.add(dataBase.getLineAtIndex(i));
        }
        return result;
    }
}

class FindAny implements FindingStrategy {
    public List<String> find(String key, Storage dataBase) {
        List<String> result = new ArrayList<>();
        Map<String, Set<Integer>> invertedIndex = dataBase.getInvertedIndex();
        String[] words = key.toLowerCase().split("\\s+");
        if (!invertedIndex.containsKey(words[0])) {
            return result;
        }
        Set<Integer> matchLines = invertedIndex.get(words[0]);
        for (int i = 1; i < words.length; i++) {
            if (invertedIndex.containsKey(words[i])) {
                matchLines.addAll(invertedIndex.get(words[i]));
            }
        }
        for (Integer i : matchLines) {
            result.add(dataBase.getLineAtIndex(i));
        }
        return result;
    }
}

class FindNone implements FindingStrategy {
    public List<String> find(String key, Storage dataBase) {
        List<String> result = new ArrayList<>();
        Map<String, Set<Integer>> invertedIndex = dataBase.getInvertedIndex();
        String[] words = key.toLowerCase().split("\\s+");
        if (!invertedIndex.containsKey(words[0])) {
            return result;
        }
        Set<Integer> matchLines = new TreeSet<>();
        for (int i = 0; i < dataBase.getContent().size(); i++) {
            matchLines.add(i);
        }
        for (int i = 0; i < words.length; i++) {
            if (invertedIndex.containsKey(words[i])) {
                matchLines.removeAll(invertedIndex.get(words[i]));
            }
        }
        for (Integer i : matchLines) {
            result.add(dataBase.getLineAtIndex(i));
        }
        return result;
    }
}


class Storage {

    private List<String> content;
    private Map<String, Set<Integer>> invertedIndex;

    Storage (File file) {
        int lineNumber = 0;
        content = new ArrayList<>();
        invertedIndex = new TreeMap<>();
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                content.add(line);
                Scanner scanner2 = new Scanner(line);
                while (scanner2.hasNext()) {
                    String key = scanner2.next().toLowerCase();
                    if (invertedIndex.containsKey(key)) {
                        Set<Integer> tmp = invertedIndex.get(key);
                        tmp.add(lineNumber);
                        invertedIndex.put(key, tmp);
                    } else {
                        Set<Integer> tmp = new TreeSet<>();
                        tmp.add(lineNumber);
                        invertedIndex.put(key, tmp);
                    }
                }
                lineNumber++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public String getLineAtIndex(int index) {
        return content.get(index);
    }

    public List<String>  getContent() {
        return content;
    }
    public Map<String, Set<Integer>> getInvertedIndex() {
        return invertedIndex;
    }
}

public class Main {

    public static File getFile(String[] args) {

        String fileName = null;
        File file = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--data")) {
                fileName = args[i + 1];
                break;
            }
        }
        if (fileName != null) {
            file = new File(fileName);
        }
        return file;
    }

    public static void query2(Scanner scanner, List<String> input, Map<String, Set<Integer>> readStore) {
        System.out.println("Enter a name or email to search all suitable people.");
        String key = scanner.nextLine();
        if (readStore.containsKey(key)) {
            System.out.println(readStore.get(key).size() + "persons found");
            for (Integer nLine : readStore.get(key)) {
                System.out.println(input.get(nLine));
            }
        } else {
                System.out.println("No matching people found.");
        }
    }

    public static void printList(List<String> input) {
        System.out.println("=== List of people ===");
        for (String str : input) {
            System.out.println(str);
        }
    }

    public static void doSearch(Scanner scanner, Storage dataBase) {
        Finder finder = null;
        String strategyType;
        boolean rightChoose = false;
        List<String> result;
        while (!rightChoose) {
            System.out.println("Select a matching strategy: ALL, ANY, NONE");
            strategyType = scanner.nextLine();
            switch (strategyType.toLowerCase()) {
                case "any":
                    finder = new Finder(new FindAny());
                    rightChoose = true;
                    break;
                case "none":
                    finder = new Finder(new FindNone());
                    rightChoose = true;
                    break;
                case "all":
                    finder = new Finder(new FindAll());
                    rightChoose = true;
                    break;
                default:
                    System.out.println("Unknown strategy, try again");
            }
        }
        result = finder.find(scanner.nextLine(), dataBase);
        if (result.size() == 0) {
            System.out.println("No people found");
        } else {
            System.out.println(result.size() + " persons found:");
            printList(result);
        }
    }

    public static void printInexes(Map<String, Set<Integer>> invertedIndex) {
        System.out.println(invertedIndex);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        File file = getFile(args);
        if (file == null) {
            System.out.println("Error file open");
            System.exit(-1);
        }
        Storage dataBase = new Storage(file);
        String menuItem;
        while (true) {
            System.out.println("=== Menu ===\n" +
                    "1. Find a person\n" +
                    "2. Print all people\n" +
                    "0. Exit");
            menuItem = scanner.nextLine();
            switch (menuItem) {
                case "0":
                    System.out.println("Bye");
                    System.exit(0);
                case "1":
                    doSearch(scanner, dataBase);
                    break;
                case "2":
                    printList(dataBase.getContent());
                    break;
                case "3":
                    printInexes(dataBase.getInvertedIndex());
                    break;
                default:
                    System.out.println("Incorrect option! Try again.");
            }
        }
    }
}
