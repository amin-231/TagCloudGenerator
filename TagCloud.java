import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

/**
 * Program to convert file with Glossary to an HTML Page output.
 *
 * @author Dhairya Amin
 */
public final class TagCloud {

    /**
     * Default constructor--private to prevent instantiation.
     */
    private TagCloud() {
    }

    final static String SEPARATORS = " \t\n\r,-.!?[]';:/()<>@#$%^&*/+=/\"\\{}_";

    /**
     * Outputs the index file using output stream.
     *
     * @param inputFileName
     *            input file
     * @param numberOfWords
     *            top words given by user
     * @param out
     *            output stream
     * @ensures outputs inputFileName.html file by reading input file
     */
    private static void outputIndex(String inputFileName, int numberOfWords,
            PrintWriter out) {

        out.print("<!DOCTYPE html>\r\n" + "<html lang=\"en\"> \r\n"
                + "<head>\r\n" + "<meta charset=\"UTF-8\">\r\n" + "<head>\r\n"
                + "<title>");
        out.print("Top " + numberOfWords + " words in " + inputFileName
                + "</title>\r\n");
        out.print(
                "<link href=\"http://web.cse.ohio-state.edu/software/2231/web-sw2/assignments/projects/tag-cloud-generator/data/tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">\r\n"
                        + "<link href=\"tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">\r\n");

        out.print("</head>\r\n" + "<body>\r\n" + "<h2>Top " + numberOfWords
                + " words in " + inputFileName + "</h2>\r\n" + "<hr>\r\n");
        out.print("<div class=\"cdiv\">\r\n" + "<p class=\"cbox\">\r\n");

    }

    /**
     * Outputs the top sorted entries using output stream with appropriate
     * CloudTag.
     *
     * @param inputFileName
     *            input file
     * @param numberOfWords
     *            top words given by user
     * @param out
     *            output stream
     * @requires [file named inputFileName exists but is not open]
     * @ensures outputs inputFileName.html file by reading input file
     */
    private static void outputSortedEntriesWithCouldTag(String inputFileName,
            int numberOfWords, PrintWriter out) {

        Comparator<Map.Entry<String, Integer>> orderString = new MapEntryStringLT();
        Queue<Map.Entry<String, Integer>> wordsAndCounts = new PriorityQueue<>(
                orderString);
        int[] minAndMax = { 0, 0 };
        minAndMax = getSortedWordsAndWordCount(inputFileName, numberOfWords,
                wordsAndCounts);

        double min = minAndMax[0];
        double max = minAndMax[1];
        double rangeBy38 = (max - min) / 38;
        for (Map.Entry<String, Integer> i : wordsAndCounts) {
            boolean condition = false;
            int x = 1;
            while (x < 37 && !condition) {
                condition = i.getValue() <= (min + (x * rangeBy38));
                if (condition) {
                    out.print("<span style=\"cursor:default\" class=\"f"
                            + (x + 10) + "\" title=\"count: " + i.getValue()
                            + "\">" + i.getKey() + "</span>\r\n");
                }
                x++;
            }
            if (!condition) {
                out.print("<span style=\"cursor:default\" class=\"f" + (x + 11)
                        + "\" title=\"count: " + i.getValue() + "\">"
                        + i.getKey() + "</span>\r\n");
            }
        }
    }

    /**
     * Read text from input file and add words and their counts in
     * SortingMachine.
     *
     * @param inputFileName
     *            input file
     * @param numberOfWords
     *            top words given by user
     * @param wordsAndCounts
     *            to sorted words and their counts as Map.Pair
     * @update wordsAndCounts
     * @return minimum and maximum counts.
     * @requires [file named inputFileName exists but is not open]
     * @ensures <pre>
     * wordsAndCounts contains key value pair of top numberOfWords words and their counts from input file
     * </pre>
     */
    private static int[] getSortedWordsAndWordCount(String inputFileName,
            int numberOfWords,
            Queue<Map.Entry<String, Integer>> wordsAndCounts) {

        Map<String, Integer> wordsCount = new HashMap<>();
        String str;
        try (BufferedReader in = new BufferedReader(
                new FileReader(inputFileName))) {
            while ((str = in.readLine()) != null) {
                int position = 0;
                while (position < str.length()) {
                    String token = nextWordOrSeparator(str, position);
                    if (!SEPARATORS.contains(String.valueOf(token.charAt(0)))) {
                        token = token.toLowerCase();
                        if (!wordsCount.containsKey(token)) {
                            wordsCount.put(token, 1);
                        } else {
                            int count = wordsCount.remove(token) + 1;
                            wordsCount.put(token, count);
                        }
                    }
                    position += token.length();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error in opening or closing a file");
        }

        Comparator<Map.Entry<String, Integer>> orderInteger = new MapEntryIntegerGT();
        Queue<Map.Entry<String, Integer>> integerSort = new PriorityQueue<>(
                orderInteger);
        Set<Map.Entry<String, Integer>> s = wordsCount.entrySet();
        for (Map.Entry<String, Integer> i : s) {
            integerSort.add(i);
        }
        int[] minAndMax = { 0, 0 };

        for (int i = 0; i < numberOfWords && integerSort.size() > 0; i++) {
            Map.Entry<String, Integer> m = integerSort.remove();
            wordsAndCounts.add(m);
            if (i == 0) {
                minAndMax[1] = m.getValue();
            } else {
                minAndMax[0] = m.getValue();
            }
        }
        return minAndMax;
    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code separators}) or "separator string" (maximal length string of
     * characters in {@code separators}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @param separators
     *            the {@code Set} of separator characters
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection separators = {}
     * then
     *   entries(nextWordOrSeparator) intersection separators = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection separators /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of separators  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of separators)
     * </pre>
     */
    private static String nextWordOrSeparator(String text, int position) {
        assert text != null : "Violation of: text is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        String resultingWordOrSeparator = "";
        char tempCharacter = text.charAt(position);
        int i = position;
        boolean foundSeparator = SEPARATORS
                .contains(String.valueOf(tempCharacter));
        while (i < text.length() && foundSeparator == SEPARATORS
                .contains(String.valueOf(text.charAt(i)))) {
            i++;
        }
        resultingWordOrSeparator = text.substring(position, i);
        return resultingWordOrSeparator;
    }

    /**
     * private Comparator classes for sorting.
     */

    private static class MapEntryStringLT
            implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> o1,
                Map.Entry<String, Integer> o2) {
            if (o1.equals(o2)) {
                return 0;
            }
            return (o1.getKey()).compareTo(o2.getKey());
        }
    }

    private static class MapEntryIntegerGT
            implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> o1,
                Map.Entry<String, Integer> o2) {
            return (o2.getValue()).compareTo(o1.getValue());
        }
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("Input file location: ");
        String inputFileName = in.nextLine();
        System.out.println("Output file location: ");
        String outputFileName = in.nextLine();

        int count = 100; //-1;

        try (PrintWriter out = new PrintWriter(
                new BufferedWriter(new FileWriter(outputFileName)))) {
            outputIndex(inputFileName, count, out);
            outputSortedEntriesWithCouldTag(inputFileName, count, out);
            out.print("</p>\r\n" + "</div>\r\n" + "</body>\r\n" + "</html>");
        } catch (IOException e) {
            e.printStackTrace();
        }
        in.close();
    }
}
