package input;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This test class checks whether supported input files are recognized correctly, mixed file types can be handled,
 * FastQC output is detected, unsupported files are ignored and invalid folders cause an exception.
 */


public class InputHandlerTest {

    /* TempDir creates a temporary directory to create temporary test files */
    @TempDir
    Path tempDir;

    /* IOException can occur when reading or writing files.
    If it occurs during the test, the test will fail immediately. */
    @Test
    void recognizesFastqFiles() throws IOException {
        Files.createFile(tempDir.resolve("sample1.fastq"));
        Files.createFile(tempDir.resolve("sample2.fastq.gz"));

        InputHandler inputHandler = new InputHandler();

        inputHandler.scanFolder(tempDir.toString());

        assertEquals(2, inputHandler.getFastqFiles().size());
    }

    @Test
    void recognizesSamFiles() throws IOException {
        Files.createFile(tempDir.resolve("sample.sam"));

        InputHandler inputHandler = new InputHandler();

        inputHandler.scanFolder(tempDir.toString());

        assertEquals(1, inputHandler.getSamFiles().size());

    }

    @Test
    void recognizesBamFiles() throws IOException {
        Files.createFile(tempDir.resolve("sample.bam"));

        InputHandler inputHandler = new InputHandler();

        inputHandler.scanFolder(tempDir.toString());

        assertEquals(1, inputHandler.getBamFiles().size());

    }

    @Test
    void recognizesMixedFiles() throws IOException {
        Files.createFile(tempDir.resolve("sample1.fastq"));
        Files.createFile(tempDir.resolve("sample2.sam"));
        Files.createFile(tempDir.resolve("sample3.bam"));

        InputHandler inputHandler = new InputHandler();

        inputHandler.scanFolder(tempDir.toString());

        assertEquals(1, inputHandler.getFastqFiles().size());
        assertEquals(1, inputHandler.getSamFiles().size());
        assertEquals(1, inputHandler.getBamFiles().size());
    }

    @Test
    void recognizesFastQcFolder() throws IOException {
        Path fastqcFolder = Files.createDirectory(
                tempDir.resolve("sample_fastqc")
        );

        Files.createFile(
                fastqcFolder.resolve("fastqc_data.txt")
        );

        InputHandler inputHandler = new InputHandler();

        inputHandler.scanFolder(tempDir.toString());

        assertEquals(1, inputHandler.getFastqcFiles().size());
    }

    @Test
    void ignoresUnsupportedFiles() throws IOException {
        Files.createFile(tempDir.resolve("notes.txt"));
        Files.createFile(tempDir.resolve("image.jpg"));

        InputHandler inputHandler = new InputHandler();

        inputHandler.scanFolder(tempDir.toString());

        assertEquals(0, inputHandler.getFastqFiles().size());
        assertEquals(0, inputHandler.getSamFiles().size());
        assertEquals(0, inputHandler.getBamFiles().size());
        assertEquals(0, inputHandler.getFastqcFiles().size());
    }

    @Test
    void throwsExceptionForInvalidFolder() {
        InputHandler inputHandler = new InputHandler();

        assertThrows(
                IllegalArgumentException.class,
                () -> inputHandler.scanFolder("Folder does not exist")
        );
    }
}
