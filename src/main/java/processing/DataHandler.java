package processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataHandler {

    public Map<String, List<String>> dataHandler(List<List<String>> reads) {

        List<String> headers = new ArrayList<>();
        List<String> nucleotides = new ArrayList<>();
        List<String> separators = new ArrayList<>();
        List<String> qualityScores = new ArrayList<>();

        for (List<String> read : reads) {

            String header = read.get(0);
            String nucleotide = read.get(1);
            String separator = read.get(2);
            String qualityScore = read.get(3);

            headers.add(header);
            nucleotides.add(nucleotide);
            separators.add(separator);
            qualityScores.add(qualityScore);
        }

        Map<String, List<String>> categorizedReads = new HashMap<>();

        categorizedReads.put("headers", headers);
        categorizedReads.put("nucleotides", nucleotides);
        categorizedReads.put("separators", separators);
        categorizedReads.put("qualityScores", qualityScores);

        return categorizedReads;
    }
}