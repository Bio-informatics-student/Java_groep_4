
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;

@Command(
        name = "qc-tool",
        description = "Performs quality control on sequencing data.",
        mixinStandardHelpOptions = true
)
public class Main implements Runnable {

    @Option(
            names = {"-i", "--input"},
            description = "Input file or folder",
            required = true
    )
    private Path input;

    @Option(
            names = {"-o", "--output"},
            description = "Output file",
            defaultValue = "results.txt"
    )
    private Path output;

    @Override
    public void run() {
        System.out.println("Input: " + input);
        System.out.println("Output: " + output);

        // Later: call InputController here.
    }

    public static void main(String[] args) {
        int exitCode = new picocli.CommandLine(new Main())
                .execute(args);

        System.exit(exitCode);
    }
}
