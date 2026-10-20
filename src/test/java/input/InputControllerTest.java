package input;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class InputControllerTest {

    @TempDir
    Path tempDir;

    /**
     * Tests whether the controller can handle a folder containing
     * multiple supported input file types
     */
    @Test
    void handlesMixedInputFiles() throws IOException {
        Files.createFile(tempDir.resolve("sample.fastq"));
        Files.createFile(tempDir.resolve("sample.bam"));
        Files.createFile(tempDir.resolve("sample.sam"));

        InputController inputController = new InputController();

        assertDoesNotThrow(() ->
                inputController.handleInput(tempDir.toString())
        );
    }
}
