
import input.InputController;
import output.OutputWriter;
import output.QCResult;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

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
            description = "Input FASTQ file or folder",
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

    // Percentage of bases to analyse at each read end.
    @Option(
            names = "--end-percent",
            description = "Percentage of bases to analyse at each read end (1-50)",
            defaultValue = "10"
    )
    private double endPercent;

    // Minimum percentage of reads that must pass the quality checks.
    @Option(
            names = "--required-reads-percent",
            description = "Minimum percentage of reads that must pass the quality checks (0-100)",
            defaultValue = "80"
    )
    private double requiredReadsPercent;

    // Maximum percentage of duplicate reads allowed for a sample to pass.
    @Option(
            names = "--max-duplicate-percent",
            description = "Maximum allowed percentage of duplicate reads (0-100)",
            defaultValue = "100"
    )
    private double maxDuplicatePercent;

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
        Path absoluteOutput = output.toAbsolutePath();
        Path outputParent = absoluteOutput.getParent();

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

        // Validate the quality and duplicate thresholds.
        if (!Double.isFinite(endPercent)
                || endPercent < 1 || endPercent > 50) {
            throw new IllegalArgumentException(
                    "--end-percent must be between 1 and 50."
            );
        }

        if (!Double.isFinite(requiredReadsPercent)
                || requiredReadsPercent < 0 || requiredReadsPercent > 100) {
            throw new IllegalArgumentException(
                    "--required-reads-percent must be between 0 and 100."
            );
        }

        if (!Double.isFinite(maxDuplicatePercent)
                || maxDuplicatePercent < 0 || maxDuplicatePercent > 100) {
            throw new IllegalArgumentException(
                    "--max-duplicate-percent must be between 0 and 100."
            );
        }

        System.out.println("Input validated: " + input);
        System.out.println("Output file: " + absoluteOutput);

        System.out.printf(
                Locale.ROOT,
                "Read-end region: %.1f%% | Required passing reads: %.1f%% | Maximum duplicates: %.1f%%%n",
                endPercent,
                requiredReadsPercent,
                maxDuplicatePercent
        );

        // Run the FASTQ processing pipeline.
        try {
            InputController controller = new InputController();

            List<QCResult> results = controller.handleInput(
                    input,
                    endPercent,
                    requiredReadsPercent,
                    maxDuplicatePercent
            );

            // Display the QC results in the terminal.
            OutputWriter writer = new OutputWriter();
            writer.printToTerminal(results);

            // Write the QC results to the output file.
            writer.writeToFile(results, absoluteOutput);

            System.out.println();
            System.out.println(
                    "QC processing completed. Results written to: "
                            + absoluteOutput
            );

        } catch (IOException e) {
            // Convert file access and processing errors into a readable message.
            throw new IllegalStateException(
                    "Could not process input: " + input + ". "
                            + e.getMessage(),
                    e
            );
        }
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
