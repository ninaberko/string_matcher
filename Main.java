import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {

    private static final int NUM_OF_ARGS = 2;
    public static final int MAX_LINES_IN_CHUNK = 1000;
    public static final String ERROR_MSG = "Error: ";
    private static final String INVALID_INPUT = "The number of arguments is not correct";

    public static String[] inputStrings;

    /**
     * Validate the program's arguments - check that there are 2 arguments
     * @param args the program's arguments
     */
    private static void validateInput(String[] args) {
        if (args.length != NUM_OF_ARGS) {
            System.err.println(ERROR_MSG + INVALID_INPUT);
            System.exit(1);
        }
    }

    /**
     * Run the FileReader thread and the Matchers and run them.
     * After all the matchers are done running, call aggregate
     * to print the results.
     * @param fileName the file name (including the path)
     * @param textQueue the queue that the text chunks are inserted to
     */
    private static void findMatches(String fileName, LinkedBlockingQueue<String[]> textQueue) {

        MyFileReader reader = new MyFileReader(textQueue, fileName);
        reader.start();

        final int cores = Runtime.getRuntime().availableProcessors(); // num of threads
        ArrayList<Matcher> matchers = new ArrayList<>(cores);
        // Create the matchers
        for (int i = 0; i < cores; i++) {
            Matcher matcher = new Matcher(textQueue, i);
            matchers.add(matcher);
        }

        // Start the matchers
        for (Matcher m : matchers) {
            m.start();
        }

        // waiting for all the threads to be done
        for (Matcher m : matchers) {
            try {
                m.join();
            }
            catch (InterruptedException e){
                System.err.println(ERROR_MSG + e.getMessage());
            }
        }
    }


    public static void main(String[] args) {
        validateInput(args);
        inputStrings = args[1].split(",", 0);
        LinkedBlockingQueue<String[]> textQueue = new LinkedBlockingQueue<>();

        // initialize the aggregator's hashmap
        Aggregator aggregator = Aggregator.getInstance();
        aggregator.insertKeys();

        findMatches(args[0], textQueue);

        // print the results
        aggregator.printResults();
    }
}


