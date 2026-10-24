import input.InputHandler;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Entry point for SeqSpectre.
 *
 * PicoCLI parses the command-line arguments. This class validates the input,
 * detects supported files, and provides a clear status until the readers and
 * QC processing pipeline are connected.
 */
@Command(
        name = "seqspectre",
        description = "Performs quality control on sequencing data.",
        mixinStandardHelpOptions = true,
        version = "SeqSpectre development version"
)
public class Main implements Runnable {

    @Option(
            names = {"-i", "--input"},
            description = "Input file or directory containing sequencing data",
            required = true
    )
    private Path input;

    @Option(
            names = {"-o", "--output"},
            description = "Path for the future QC report (default: ${DEFAULT-VALUE})",
            defaultValue = "results.tsv"
    )
    private Path output;

    @Override
    public void run() {
        System.out.println("=== SeqSpectre QC Tool ===");

        // Check that the input exists and is either a file or a directory.
        if (!Files.exists(input)) {
            throw new IllegalArgumentException("Input path does not exist: " + input);
        }
        if (!Files.isRegularFile(input) && !Files.isDirectory(input)) {
            throw new IllegalArgumentException(
                    "Input must be a regular file or directory: " + input
            );
        }

        // The report file does not need to exist yet. It will be created
        // by OutputWriter once report generation is connected.
        if (Files.exists(output) && Files.isDirectory(output)) {
            throw new IllegalArgumentException(
                    "Output path points to a directory, not a file: " + output
            );
        }

        System.out.println("Input:  " + input);
        System.out.println("Output: " + output);

        try {
            List<Path> files = findSupportedFiles(input);

            if (files.isEmpty()) {
                throw new IllegalArgumentException(
                        "No supported input files were found. Expected FASTQ, SAM, BAM, "
                                + "or a FastQC result (fastqc_data.txt inside a subdirectory)."
                );
            }

            System.out.println();
            System.out.println("Recognized input files: " + files.size());
            System.out.printf("%-35s %s%n", "File", "Detected type");
            System.out.println("----------------------------------------------------");

            for (Path file : files) {
                System.out.printf("%-35s %s%n", file.getFileName(), getFileType(file));
            }

            // The current readers and processing pipeline are not yet connected
            // end-to-end. Do not create an empty report or imply that QC ran.
            System.out.println();
            System.out.println("Input validation and file detection completed.");
            System.out.println(
                    "QC analysis and report generation are not connected yet."
            );
            System.out.println(
                    "The requested report path is reserved for a future output step: "
                            + output
            );

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not inspect input path: " + input + " (" + e.getMessage() + ")",
                    e
            );
        }
    }

    /**
     * Finds supported input files. A single supported file can be supplied,
     * or a directory can be supplied. For a directory, only its direct files
     * are scanned, plus FastQC's fastqc_data.txt in immediate subdirectories.
     */
    private static List<Path> findSupportedFiles(Path input) throws IOException {
        if (Files.isRegularFile(input)) {
            return isSupportedFile(input) ? List.of(input) : List.of();
        }

        // Use the existing InputHandler for directory detection so the CLI
        // follows the file categories already defined in the project.
        InputHandler handler = new InputHandler();
        handler.scanFolder(input.toString());

        java.util.ArrayList<Path> files = new java.util.ArrayList<>();
        files.addAll(handler.getFastqFiles());
        files.addAll(handler.getSamFiles());
        files.addAll(handler.getBamFiles());
        files.addAll(handler.getFastqcFiles());
        return files;
    }

    /**
     * Checks supported file extensions for a single-file input.
     */
    private static boolean isSupportedFile(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".bam")
                || name.endsWith(".sam")
                || name.endsWith(".fastq")
                || name.endsWith(".fq")
                || name.endsWith(".fastq.gz")
                || name.endsWith(".fq.gz")
                || name.endsWith("fastqc_data.txt");
    }

    /**
     * Returns the input type for display in the terminal.
     */
    private static String getFileType(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);

        if (name.endsWith(".bam")) {
            return "BAM";
        }
        if (name.endsWith(".sam")) {
            return "SAM";
        }
        if (name.endsWith(".fastq") || name.endsWith(".fq")
                || name.endsWith(".fastq.gz") || name.endsWith(".fq.gz")) {
            return "FASTQ";
        }
        if (name.equals("fastqc_data.txt")) {
            return "FastQC";
        }
        return "Unknown";
    }

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new Main());

        // Display concise execution errors instead of a Java stack trace.
        commandLine.setExecutionExceptionHandler((exception, cmd, parseResult) -> {
            cmd.getErr().println("ERROR: " + exception.getMessage());
            return 1;
        });

        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }
}
