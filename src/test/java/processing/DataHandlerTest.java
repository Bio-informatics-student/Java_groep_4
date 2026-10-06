package processing;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataHandlerTest {

    @Test
    void dataHandler() {

        DataHandler handler = new DataHandler();

        List<String> read1 = List.of(
                "@read1",
                "ACTG",
                "+",
                "IIII"
        );

        List<String> read2 = List.of(
                "@read2",
                "GGTA",
                "+",
                "HHHH"
        );

        List<List<String>> reads = List.of(
                read1,
                read2
        );

        Map<String, List<String>> result = handler.dataHandler(reads);

        assertEquals(
                List.of("@read1", "@read2"),
                result.get("headers")
        );

        assertEquals(
                List.of("ACTG", "GGTA"),
                result.get("nucleotides")
        );

        assertEquals(
                List.of("+", "+"),
                result.get("separators")
        );

        assertEquals(
                List.of("IIII", "HHHH"),
                result.get("qualityScores")
        );
    }


        @Test
        void dataHandlerWithEmptyInput() {

            DataHandler handler = new DataHandler();

            List<List<String>> reads = List.of();

            Map<String, List<String>> result = handler.dataHandler(reads);

            assertEquals(List.of(), result.get("headers"));
            assertEquals(List.of(), result.get("nucleotides"));
            assertEquals(List.of(), result.get("separators"));
            assertEquals(List.of(), result.get("qualityScores"));
        }
    }