
package input;

import java.io.IOException;

/**
 * Coordinates input discovery.
 *
 * The input readers and processing classes are being integrated.
 */
public class InputController {

    private final InputHandler inputHandler;

    public InputController() {
        this.inputHandler = new InputHandler();
    }

    /**
     * Scans a folder and categorizes the supported input files.
     *
     * @param folderPath path to the folder selected by the user
     * @throws IOException if the folder cannot be read
     */
    public void handleInput(String folderPath) throws IOException {
        inputHandler.scanFolder(folderPath);

        // TODO: reading and QC processing pipeline connection.
    }
}
