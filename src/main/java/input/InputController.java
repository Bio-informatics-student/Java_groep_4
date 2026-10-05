package input;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Controls the flow of input files after they have been categorized.
 * The inputcontroller sends each supported file type to the correct reader.
 */

public class InputController {

    private final InputHandler inputHandler;
    private final FastQReader fastQReader;
    private final SamReader samReader;
    private final BamReader bamReader;
    private final FastQCReader fastQCReader;


    /**
     * Creates an InputController with the required input handler and readers.
     */
    public InputController() {
        this.inputHandler = new InputHandler();
        this.fastQReader = new FastQReader();
        this.samReader = new SamReader();
        this.bamReader = new BamReader();
        this.fastQCReader = new FastQCReader();
    }


    /**
     * Scans the selected folder and sends each detected file to the correct reader.
     *
     * @param folderPath path to the folder selected by the user
     * @throws IOException if an error occurs while accessing or reading input files
     */
    public void handleInput(String folderPath) throws IOException {
        inputHandler.scanFolder(folderPath);

        for (Path file : inputHandler.getFastqFiles()) {
            fastQReader.read(file);
        }

        for (Path file : inputHandler.getSamFiles()) {
            samReader.read(file);
        }

        for (Path file : inputHandler.getBamFiles()) {
            bamReader.read(file);
        }

        for (Path file : inputHandler.getFastqcFiles()) {
            fastQCReader.read(file);
        }
    }
}
