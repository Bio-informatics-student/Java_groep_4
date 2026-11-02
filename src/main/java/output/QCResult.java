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
    private final Double gcPercentage;
    private final Integer minimumSequenceLength;
    private final Integer maximumSequenceLength;
    private final Double belowMinimumPercent;
    private final String status;

    /**
     * Creates a QCResult containing the results of one sample.
     *
     * @param sample the name of the sample
     * @param fileType the type of input file
     * @param beginningQuality the quality percentage at the beginning of the reads
     * @param endQuality the quality percentage at the end of the reads
     * @param duplicates the percentage of duplicate reads
     * @param gcPercentage the GC percentage of the reads
     * @param minimumSequenceLength the shortest read length
     * @param maximumSequenceLength the longest read length
     * @param belowMinimumPercent the percentage of reads below the required minimum length
     * @param status the final PASS or FAIL status
     */
    public QCResult(
            String sample,
            String fileType,
            Double beginningQuality,
            Double endQuality,
            Double duplicates,
            Double gcPercentage,
            Integer minimumSequenceLength,
            Integer maximumSequenceLength,
            Double belowMinimumPercent,
            String status
    ) {
        this.sample = sample;
        this.fileType = fileType;
        this.beginningQuality = beginningQuality;
        this.endQuality = endQuality;
        this.duplicates = duplicates;
        this.gcPercentage = gcPercentage;
        this.minimumSequenceLength = minimumSequenceLength;
        this.maximumSequenceLength = maximumSequenceLength;
        this.belowMinimumPercent = belowMinimumPercent;
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

    public Double getGcPercentage() {
        return gcPercentage;
    }

    public Integer getMinimumSequenceLength() {
        return minimumSequenceLength;
    }
    public Integer getMaximumSequenceLength() {
        return maximumSequenceLength;
    }

    public Double getBelowMinimumPercent() {
        return belowMinimumPercent;
    }

    public String getStatus() {
        return status;
    }
}