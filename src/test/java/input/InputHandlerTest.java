package input;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class InputHandlerTest {

    /*** TempDir creates a temporary map for the tests */
    @TempDir
    Path tempDir;

    /*** IOException handles errors and makes the test fail when it occurs.
     * It could occur with reading or writing a file. */
    @Test
    void recognizesFastqFiles() throws IOException {
        Files.createFile(tempDir.resolve("sample1.fastq"));
        Files.createFile(tempDir.resolve("sample2.fastq.gz"));

        InputHandler inputHandler = new InputHandler();

        inputHandler.readFolder(tempDir.toString());

        assertEquals(2, inputHandler.getFastqFiles().size());
    }

    @Test
    void recognizesSamFiles() throws IOException {
        Files.createFile(tempDir.resolve("sample.sam"));

        InputHandler inputHandler = new InputHandler();

        inputHandler.readFolder(tempDir.toString());

        assertEquals(1, inputHandler.getSamFiles().size());

    }

    @Test
    void recognizesBamFiles() throws IOException {
        Files.createFile(tempDir.resolve("sample.bam"));

        InputHandler inputHandler = new InputHandler();

        inputHandler.readFolder(tempDir.toString());

        assertEquals(1, inputHandler.getBamFiles().size());

    }

    @Test
    void recognizeMixedFiles() throws IOException {
        Files.createFile(tempDir.resolve("sample1.fastq"));
        Files.createFile(tempDir.resolve("sample2.sam"));
        Files.createFile(tempDir.resolve("sample3.bam"));

        InputHandler inputHandler = new InputHandler();

        inputHandler.readFolder(tempDir.toString());

        assertEquals(1, inputHandler.getFastqFiles().size());
        assertEquals(1, inputHandler.getSamFiles().size());
        assertEquals(1, inputHandler.getBamFiles().size());
    }
}
