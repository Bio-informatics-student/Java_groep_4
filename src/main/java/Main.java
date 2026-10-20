
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Main entry point for the sequencing QC tool.
 * PicoCLI parses the command-line arguments provided by the user.
 */
@Command(
        name = "qc-tool",
        description = "Performs quality control on sequencing data.",
        // Automatically provides --help and --version options.
        mixinStandardHelpOptions = true
)
public class Main implements Runnable {

    // Path to the input file or directory provided by the user.
    @Option(
            names = {"-i", "--input"},
            description = "Input file or folder",
            required = true
    )
    private Path input;

    // Path where the QC results will be written.
    // Defaults to results.tsv if the user does not specify an output path.
    @Option(
            names = {"-o", "--output"},
            description = "Output file",
            defaultValue = "results.tsv"
    )
    private Path output;

    /**
     * Executes after PicoCLI has parsed the command-line arguments.
     */
    @Override
    public void run() {
        System.out.println("=== Sequencing QC Tool ===");

        // Check whether the input path exists.
        if (!Files.exists(input)) {
            throw new IllegalArgumentException(
                    "Input path does not exist: " + input
            );
        }

        // Only regular files and directories are accepted as input.
        if (!Files.isRegularFile(input) && !Files.isDirectory(input)) {
            throw new IllegalArgumentException(
                    "Input must be a regular file or a directory: " + input
            );
        }

        // Validate the output path.
        // The output file itself does not need to exist yet.
        Path outputParent = output.toAbsolutePath().getParent();

        // An existing directory cannot be used as the output file.
        if (Files.exists(output) && Files.isDirectory(output)) {
            throw new IllegalArgumentException(
                    "Output path points to a directory, not a file: " + output
            );
        }

        // Create the output directory if it does not exist.
        // This allows the user to specify a new directory for the results.
        try {
            if (outputParent != null) {
                Files.createDirectories(outputParent);
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not create output directory: " + outputParent, e
            );
        }

        System.out.println("Input validated: " + input);
        System.out.println("Output file: " + output);

        // Find input files with supported file extensions.
        try {
            List<Path> inputFiles;

            if (Files.isRegularFile(input)) {
                // When a single file is provided, check that file directly.
                if (isSupportedFile(input)) {
                    inputFiles = List.of(input);
                } else {
                    inputFiles = List.of();
                }
            } else {
                // When a directory is provided, inspect its immediate contents.
                try (Stream<Path> paths = Files.list(input)) {
                    inputFiles = paths
                            .filter(Files::isRegularFile)
                            .filter(Main::isSupportedFile)
                            .toList();
                }
            }

            // Stop if no supported files were found.
            if (inputFiles.isEmpty()) {
                throw new IllegalArgumentException(
                        "No supported input files were found. "
                                + "Expected a supported SAM, BAM, FASTQ or FastQC file."
                );
            }

            // Display how many files were recognized.
            System.out.println("Recognized input files: " + inputFiles.size());

            // Display each recognized file and its detected type.
            for (Path file : inputFiles) {
                String fileType = getFileType(file);

                System.out.println(
                        "Found: " + file.getFileName()
                                + " (type: " + fileType + ")"
                );
            }

            // TODO: Connect the processing pipeline.

            System.out.println(
                    "Input validation completed. QC processing is not connected yet."
            );

        } catch (IOException e) {
            // Convert file access errors into a readable error message.
            throw new IllegalStateException(
                    "Could not read the input directory: " + input, e
            );
        }
    }

    /**
     * Checks whether a file has an extension supported by the tool.
     *
     * @param file the file to check
     * @return true if the file extension is supported
     */
    private static boolean isSupportedFile(Path file) {
        // Use lowercase to make extension checks case-insensitive.
        String name = file.getFileName()
                .toString()
                .toLowerCase(Locale.ROOT);

        return name.endsWith(".bam")
                || name.endsWith(".sam")
                || name.endsWith(".fastq")
                || name.endsWith(".fq")
                || name.endsWith(".fastq.gz")
                || name.endsWith(".fq.gz")
                || name.endsWith("_fastqc.html")
                || name.endsWith("_fastqc.zip");
    }

    /**
     * Determines the file type from its extension.
     *
     * @param file the file to identify
     * @return the detected file type
     */
    private static String getFileType(Path file) {
        String name = file.getFileName()
                .toString()
                .toLowerCase(Locale.ROOT);

        if (name.endsWith(".bam")) {
            return "BAM";
        } else if (name.endsWith(".sam")) {
            return "SAM";
        } else if (name.endsWith(".fastq")
                || name.endsWith(".fq")
                || name.endsWith(".fastq.gz")
                || name.endsWith(".fq.gz")) {
            return "FASTQ";
        } else if (name.endsWith("_fastqc.html")) {
            return "FastQC HTML";
        } else if (name.endsWith("_fastqc.zip")) {
            return "FastQC ZIP";
        }

        return "UNKNOWN ERROR";
    }

    /**
     * Creates the PicoCLI command, executes it and returns the exit code.
     *
     * @param args command-line arguments supplied by the user
     */
    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new Main());

        // Show error messages instead of a full Java stack trace.
        commandLine.setExecutionExceptionHandler(
                (exception, cmd, parseResult) -> {
                    cmd.getErr().println(
                            "ERROR: " + exception.getMessage()
                    );
                    return 1; // A non-zero exit code indicates an error.
                }
        );

        // Parse the arguments and run the command.
        int exitCode = commandLine.execute(args);

        System.exit(exitCode);
    }
}
