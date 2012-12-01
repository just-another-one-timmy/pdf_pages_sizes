
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

public class Main {

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
     * Analyzes given file.
     * Prints information about every page in a given pdf file.
     * @param file Pdf file to analyze.
     * @throws IOException
     */
    void analyzeFile(File file) throws IOException {
        System.out.println("Analyzing " + file.getName());
        PdfReader reader = new PdfReader(file.getAbsolutePath());
        int numberOfPages = reader.getNumberOfPages();
        for (int i = 1; i <= numberOfPages; i++) {
            Rectangle size = reader.getPageSize(i);
            System.out.println("\tPage " + i + ": " + size.getWidth() / 72
                    + " x " + size.getHeight() / 72 + " in");
        }
    }

    /**
     * Analyzes directory (not recursive).
     * @param dir Directory name.
     * @throws IOException
     */
    void analyzeDirectory(String dir) throws IOException {
        System.out.println("Analyzing directory " + dir);
        File directory = new File(dir);

        File[] files = directory.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File file, String name) {
                return name.toLowerCase().endsWith(PDF_EXTENSION);
            }
        });

        for (File file : files) {
            analyzeFile(file);
        }
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
