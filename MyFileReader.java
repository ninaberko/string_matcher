import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;


public class MyFileReader extends Thread{
    private final LinkedBlockingQueue<String[]> textQueue;
    private final String fileName;

    public MyFileReader(LinkedBlockingQueue<String[]> queue, String fileName) {
        super("File Reader");
        this.textQueue = queue;
        this.fileName = fileName;
    }

    /**
     * Create an object of BufferReader for the given file, and check for IO exception
     * @return
     */
    private BufferedReader createBufferReader() {
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new java.io.FileReader(this.fileName));
        }
        catch (IOException e) {
            System.err.println(Main.ERROR_MSG + e.getMessage());
            System.exit(1);
        }

        return reader;
    }

    /**
     * For each matcher thread insert a terminate signal - an array with one
     * member - null, so that when a thread will take this signal it will know
     * to stop the running
     */
    private void insertTerminationSignal() {
        final int cores = Runtime.getRuntime().availableProcessors();

        try {
            for (int i = 0; i < cores; i++) {
                String[] terminateSignal = new String[1];
                terminateSignal[0] = null;
                textQueue.put(terminateSignal);
            }
        } catch (InterruptedException e) {
            System.err.println(Main.ERROR_MSG + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * The method that running the fileReader, create BufferReader object,
     * iterate the file's lines and insert them as chunk to the queue
     */
    @Override
    public void run() {
        BufferedReader reader = createBufferReader();
        int chunkCounter = 0; // calculate the number of chunk in order to calculate the line offset

        try {
            String currLine;
            int linesPointer = 1;
            String[] currChunk = new String[Main.MAX_LINES_IN_CHUNK + 1]; // plus 1 is for saving the line index
            // (to easily calculate the offset)
            currChunk[0] = "1"; // saves the current line index

            while ((currLine = reader.readLine()) != null) {

                currChunk[linesPointer] = currLine;
                linesPointer ++;

                // when the chunk is full, it is inserted to the queue
                if (linesPointer == Main.MAX_LINES_IN_CHUNK + 1) {
                    textQueue.put(currChunk);
                    chunkCounter ++;
                    currChunk = new String[Main.MAX_LINES_IN_CHUNK + 1];
                    currChunk[0] = String.valueOf(Main.MAX_LINES_IN_CHUNK * chunkCounter + 1);
                    linesPointer = 1;
                }
            }

            // for the last lines
            textQueue.put(currChunk);
            // notify the matchers to stop taking from the queue
            insertTerminationSignal();

        } catch (InterruptedException|IOException e) {
            insertTerminationSignal();
            System.err.println(Main.ERROR_MSG + e.getMessage());
            System.exit(1);
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                insertTerminationSignal();
                System.err.println(Main.ERROR_MSG + e.getMessage());
                System.exit(1);
            }
        }
    }

}

