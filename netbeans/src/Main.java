
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

public class Main {

    /**
     * How many points a there in one inch.
     */
    public static final int POINTS_IN_INCH = 72;
    /**
     * Pdf files extension.
     */
    public static final String PDF_EXTENSION = ".pdf";
    /**
     * Path to directory that will be analyzed.
     */
    private String dir;

    /**
     * Creates a new instance and runs it.
     * @param args Command line arguments.
     * @throws IOException If instance causes exception.
     */
    public static void main(String[] args) throws IOException {
        new Main().run(args);
    }

    /**
     * Generates command line options for apache CLI arguments parser.
     * @return Optins Command line options.
     */
    Options generateOptions() {
        Option dir = OptionBuilder.withArgName("dir").hasArg().withDescription("directory to analyze (no subdirectories will be analyzed)").create("dir");
        Option help = new Option("help", "print help message");

        Options options = new Options();
        options.addOption(dir);
        options.addOption(help);

        return options;
    }

    /**
     * Parses command line arguments.
     * @param options Command line options object.
     * @param args Arguments passed from CLI.
     * @return CommandLine Object that might be queried to get information about passed arguments.
     */
    CommandLine parseCommandLineArguments(Options options, String[] args) {
        CommandLineParser parser = new GnuParser();
        CommandLine line = null;
        try {
            line = parser.parse(options, args);
        } catch (Exception e) {
            System.err.println("Can't parse command line arguments: " + e.getLocalizedMessage());
        }

        return line;
    }

    /**
     * Handles command line arguments (sets needed fields in class,
     * prints help message).
     * @param line Command line object that stores information about passed arguments.
     * @param options Options (for printing help message).
     * @return false if argument "help" was passed (should stop the program), true otherwise.
     */
    boolean handleCommandLineArguments(CommandLine line, Options options) {
        if (line.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("(pdf pages info)", options);
            return false;
        }

        if (line.hasOption("dir")) {
            dir = line.getOptionValue("dir");
        } else {
            dir = ".";
        }

        return true;
    }

    /**
     * Converts points to inches.
     * @param points Value in points.
     * @return Corresponding size in inches.
     */
    double pointsToInches(double points) {
        return points / POINTS_IN_INCH;
    }

    /**
     * Converts size to string.
     * @param size Size to convert
     * @return String that represents size.
     */
    String sizeToStr(Rectangle size) {
        return "" + pointsToInches(size.getWidth()) + " x "
                + pointsToInches(size.getHeight()) + " in "
                + " rot: " + size.getRotation();
    }

    /**
     * Analyzes given file.
     * Prints information about every page in a given pdf file.
     * @param file Pdf file to analyze.
     * @return Map where keys are page formats (string), values are number of
     *     pages in corresponding format.
     * @throws IOException
     */
    Map<String, Integer> analyzeFile(File file) throws IOException {
        Map<String, Integer> result = new HashMap<String, Integer>();

        PdfReader reader = new PdfReader(file.getAbsolutePath());
        int numberOfPages = reader.getNumberOfPages();
        for (int i = 1; i <= numberOfPages; i++) {
            String sizeStr = sizeToStr(reader.getPageSize(i));
            Integer count = result.get(sizeStr);
            count = count == null ? 1 : count + 1;
            result.put(sizeStr, count);
        }

        return result;
    }

    /**
     * Outputs result of analysis for one file.
     * @param results Results of analysis.
     * @param file File being analyzed.
     */
    void outputFileResults(Map<String, Integer> results, File file) {
        System.out.println("**** Results for " + file.getName());
        for (Map.Entry<String, Integer> e : results.entrySet()) {
            System.out.println("\t" + e.getKey() + "\t" + e.getValue());
        }
    }

    void mergeResults(Map<String, Integer> mainResult,
            Map<String, Integer> helperResult) {
        for (Map.Entry<String, Integer> e : helperResult.entrySet()) {
            Integer mainResultValue = mainResult.get(e.getKey());
            if (mainResultValue == null) {
                mainResultValue = 0;
            }

            mainResultValue += e.getValue();
            mainResult.put(e.getKey(), mainResultValue);
        }
    }

    /**
     * Analyzes directory (not recursive).
     * @param dir Directory name
     * @return Map: number of pages by format.
     * @throws IOException
     */
    Map<String, Integer> analyzeDirectory(String dir) throws IOException {
        Map<String, Integer> overallResults = new HashMap<String, Integer>();

        File directory = new File(dir);

        File[] files = directory.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File file, String name) {
                return name.toLowerCase().endsWith(PDF_EXTENSION);
            }
        });

        for (File file : files) {
            Map<String, Integer> fileResults = analyzeFile(file);
            outputFileResults(fileResults, file);
            mergeResults(overallResults, fileResults);
        }

        outputFileResults(overallResults, directory);
        return overallResults;
    }

    /**
     * Parses and handles CLI arguments, then does the job.
     * @param args CLI arguments.
     * @throws IOException
     */
    void run(String[] args) throws IOException {
        Options options = generateOptions();
        CommandLine line = parseCommandLineArguments(options, args);
        // If command line was not successfully parsed
        // or handleCommandLineArguments reported that the program should stop,
        // then return immediately.
        if (line == null || !handleCommandLineArguments(line, options)) {
            return;
        }

        analyzeDirectory(dir);
    }
}
