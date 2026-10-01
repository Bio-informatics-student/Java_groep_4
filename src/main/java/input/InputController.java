package input;

import java.io.IOException;
import java.nio.file.Path;

public class InputController {

    private final InputHandler inputHandler;
    private final FastQReader fastQReader;
    private final SamReader samReader;
    private final BamReader bamReader;
    private final FastQCReader fastQCReader;

    public InputController() {
        this.inputHandler = new InputHandler();
        this.fastQReader = new FastQReader();
        this.samReader = new SamReader();
        this.bamReader = new BamReader();
        this.fastQCReader = new FastQCReader();
    }

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
