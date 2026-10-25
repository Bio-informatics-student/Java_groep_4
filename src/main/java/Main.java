
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
        mixinStandardHelpOptions = true
)
public class Main implements Runnable {

    // Path to the input FASTQ file or directory.
    @Option(
            names = {"-i", "--input"},
            description = "Input FASTQ file or folder",
            required = true
    )
    private Path input;

    // Path where the QC results will be written.
    @Option(
            names = {"-o", "--output"},
            description = "Output file (default: ${DEFAULT-VALUE})",
            defaultValue = "results.tsv"
    )
    private Path output;

    // Percentage of each read end to analyse.
    @Option(
            names = "--end-percent",
            description = "Percentage of bases to analyse at each read end (1-50; default: ${DEFAULT-VALUE})",
            defaultValue = "10"
    )
    private double endPercent;

    // Minimum percentage of reads that must pass the quality checks.
    @Option(
            names = "--required-reads-percent",
            description = "Minimum percentage of reads that must pass the quality checks (0-100; default: ${DEFAULT-VALUE})",
            defaultValue = "80"
    )
    private double requiredReadsPercent;

    // Maximum percentage of duplicate reads allowed.
    @Option(
            names = "--max-duplicate-percent",
            description = "Maximum allowed percentage of duplicate reads (0-100; default: ${DEFAULT-VALUE})",
            defaultValue = "100"
    )
    private double maxDuplicatePercent;

    // Minimum Phred quality score required for a base to pass.
    @Option(
            names = "--min-quality-score",
            description = "Minimum Phred quality score for a base to pass (default: ${DEFAULT-VALUE})",
            defaultValue = "30"
    )
    private int minQualityScore;

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

        // Resolve the output path and its parent directory.
        Path absoluteOutput = output.toAbsolutePath();
        Path outputParent = absoluteOutput.getParent();

        // An existing directory cannot be used as the output file.
        if (Files.exists(absoluteOutput) && Files.isDirectory(absoluteOutput)) {
            throw new IllegalArgumentException(
                    "Output path points to a directory, not a file: " + output
            );
        }

        // Create the output directory if it does not exist.
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

        if (minQualityScore < 0 || minQualityScore > 93) {
            throw new IllegalArgumentException(
                    "--min-quality-score must be between 0 and 93."
            );
        }

        System.out.println("Input validated: " + input);
        System.out.println("Output file: " + absoluteOutput);

        System.out.printf(
                Locale.ROOT,
                "Minimum Phred score: Q%d | Read-end region: %.1f%% | "
                        + "Required passing reads: %.1f%% | "
                        + "Maximum duplicates: %.1f%%%n",
                minQualityScore,
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
                    maxDuplicatePercent,
                    minQualityScore
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
            // Display file access and processing errors clearly.
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

        // Show readable error messages instead of full stack traces.
        commandLine.setExecutionExceptionHandler(
                (exception, cmd, parseResult) -> {
                    cmd.getErr().println("ERROR: " + exception.getMessage());
                    return 1;
                }
        );

        // Parse the arguments and run the command.
        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }
}
