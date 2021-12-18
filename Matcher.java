import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class Matcher extends Thread {

    private final LinkedBlockingQueue<String[]> textQueue;
    private final HashMap<String, ArrayList<Long[]>> matches;
    private static Aggregator aggregator;

    /**
     * Matcher constructor
     * @param textQueue the queue that holds the file chunks that the matcher is checking
     * @param i the number of the matcher (for debug)
     */
    public Matcher(LinkedBlockingQueue<String[]> textQueue, int i) {
        super("Matcher " + i);
        this.textQueue = textQueue;
        this.matches = new HashMap<>();
        aggregator = Aggregator.getInstance();
        insertKeys();
    }

    /**
     * Insert keys (the strings to find in the text) to an empty match hashmap.
     * ---> I have a code duplication in this function, I intended to create a MatcherHashMap class
     * that wraps the regular hashmap and add functionality to it like insertKeys and resetKeys
     * but prefer to finish the task early.
     */
    private void insertKeys() {
        for (String str : Main.inputStrings) {
            matches.put(str, new ArrayList<>());
        }
    }

    /**
     * Reset the values of the keys in the hash map (to an empty array of positions)
     */
    private void resetKeys() {
        for (String key : matches.keySet()) {
            matches.get(key).clear();
        }
    }

    /**
     * The method that the runs when the thread is starting.
     * Iterate the chunk lines and check for matches,
     * If an input string is found, insert the information about the positions
     * to the matches hashmap.
     */
    @Override
    public void run() {
        while(true) {
            try {
                if (!textQueue.isEmpty()) {
                    String[] text = textQueue.take();

                    // encounter a terminate signal from the file reader
                    if (text[0] == null) {
                        break;
                    }

                    long chunkStartLine = Long.parseLong(text[0]);
                    resetKeys();
                    long lineOffset = 0;

                    for (int i = 1; i < text.length; i ++) {
                        String line = text[i];

                        // If the line is empty increase the line counter and move to the next line
                        if (line == null) {
                            lineOffset += 1;
                            continue;
                        }

                        // Check for each input string its occurrences in the current line
                        for (String key : matches.keySet()) {
                            int fromIndex = 0;
                            int keyPosition = line.indexOf(key, fromIndex);

                            // the word is founded in the line
                            while (keyPosition != -1) {
                                Long[] info = {chunkStartLine + lineOffset, (long) keyPosition + 1};
                                // update the hashmap with the new position that was found
                                (matches.get(key)).add(info);
                                // continue to check for more occurrences in the line
                                fromIndex = key.length() + keyPosition;
                                keyPosition = line.indexOf(key, fromIndex);
                            }
                        }

                        lineOffset += 1;
                    }
                    aggregator.updateAggregator(matches);
                }
            } catch (InterruptedException e) {
                System.err.println(Main.ERROR_MSG + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
