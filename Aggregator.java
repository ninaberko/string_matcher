import java.util.ArrayList;
import java.util.HashMap;

public class Aggregator {

    private static Aggregator instance = null;
    private final HashMap<String, ArrayList<Long[]>> allMatches;
    private final Object lock;

    /**
     * Aggregator private constructor.
     * Create matches hashmap with empty values
     */
    private Aggregator() {
        allMatches = new HashMap<>();
        lock = new Object();
    }

    /**
     * Return the single instance of the aggregator if exists,
     * If not create one and return it.
     */
    public static Aggregator getInstance()
    {
        if (instance == null)
        {
            instance = new Aggregator();
        }
        return instance;
    }

    /**
     * Insert keys (the strings to find in the text) to an empty match hashmap.
     * ---> I have a code duplication in this function, I intended to create a MatcherHashMap class
     * that wraps the regular hashmap and add functionality to it like insertKeys and resetKeys
     * but prefer to finish the task early.
     */
    public void insertKeys() {
        for (String str : Main.inputStrings) {
            allMatches.put(str, new ArrayList<>());
        }
    }

    /**
     * Update the results by appending a single results from one matcher to the
     * aggregator results hashmap
     * @param matches the matches hashmap to insert to the aggregator
     * @param currMatcher the matcher that updates now the aggregator (for debug)
     */
    public void updateAggregator(HashMap<String, ArrayList<Long[]>> matches) {
        synchronized(lock) {
            for (String key : matches.keySet()) {
                ArrayList<Long[]> keyResults = matches.get(key);
                if (keyResults.size() != 0) {
                    (allMatches.get(key)).addAll(keyResults); // merge the two arrays
                }
            }
        }
    }

    /**
     * Build an output string that describes one position of a string
     * @param output the StringBuilder object
     * @param pos the positions that are printed
     */
    private void buildPositionOutput(StringBuilder output, Long[] pos) {
        output.append("[");
        output.append("lineOffest=");
        output.append(pos[0]);
        output.append(", ");
        output.append("charOffset=");
        output.append(pos[1]);
        output.append("], ");
    }

    /**
     * Print the final results that were collected from all the matchers.
     */
    public void printResults() {
        for (Object obj : allMatches.keySet()) {
            ArrayList<Long[]> res = allMatches.get(obj);
            StringBuilder output = new StringBuilder();
            output.append(obj);
            output.append(" --> [");

            if (res.size() == 0) {
                output.append("]");
                System.out.println(output);
                continue;
            }

            for (Long[] pos : res) {
                buildPositionOutput(output, pos);
            }

            output.delete(output.length() - 2, output.length());
            output.append("]");
            System.out.println(output);
        }
    }
}
