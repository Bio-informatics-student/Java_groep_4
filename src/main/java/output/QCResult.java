package output;

/**
 * QCResult stores the QC results for one sequencing sample.
 */
public class QCResult {

    private final String sample;
    private final String fileType;
    private final Double beginningQuality;
    private final Double endQuality;
    private final Double duplicates;
    private final String status;

    /**
     * Creates a QCResult containing the results of one sample.
     *
     * @param sample the name of the sample
     * @param fileType the type of input file
     * @param beginningQuality the quality percentage at the beginning of the reads
     * @param endQuality the quality percentage at the end of the reads
     * @param duplicates the percentage of duplicate reads
     * @param status the final PASS or FAIL status
     */
    public QCResult(
            String sample,
            String fileType,
            Double beginningQuality,
            Double endQuality,
            Double duplicates,
            String status
    ) {
        this.sample = sample;
        this.fileType = fileType;
        this.beginningQuality = beginningQuality;
        this.endQuality = endQuality;
        this.duplicates = duplicates;
        this.status = status;
    }

    public String getSample() {
        return sample;
    }

    public String getFileType() {
        return fileType;
    }

    public Double getBeginningQuality() {
        return beginningQuality;
    }

    public Double getEndQuality() {
        return endQuality;
    }

    public Double getDuplicates() {
        return duplicates;
    }

    public String getStatus() {
        return status;
    }
}