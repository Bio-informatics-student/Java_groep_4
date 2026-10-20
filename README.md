# SeqSpectre

SeqSpectre is a Java project for quality control (QC) of sequencing data. The project aims to inspect sequencing reads, assess sequence quality, detect duplicate sequences, and present QC results in a readable format.

**Development status:** Work in progress. Several components are implemented independently, but the complete input-to-report workflow is not yet operational.

## Current implementation

### Input file detection

The `InputHandler` scans a selected directory and categorizes supported file types:

| Input | Recognized files |
| --- | --- |
| FASTQ | `.fastq`, `.fq`, `.fastq.gz`, `.fq.gz` |
| SAM | `.sam` |
| BAM | `.bam` |
| FastQC | `fastqc_data.txt` inside a direct subdirectory |

File-extension matching is case-insensitive. Unsupported files are ignored, and an invalid input directory raises an `IllegalArgumentException`. File recognition is implemented; parsing of these file types is not yet implemented in the reader classes.

### FASTQ record validation

`DataHandler` validates FASTQ reads supplied as lists of four strings in the order header, nucleotide sequence, separator, and quality string. It checks that:

- A read contains exactly four elements.
- The header begins with `@`.
- The sequence contains one or more uppercase `A`, `C`, `G`, or `T` characters.
- The separator is exactly `+`.
- The sequence and quality string have equal lengths.

Valid entries are separated into lists named `headers`, `nucleotides`, `separators`, and `qualityScores`. Invalid entries can either raise `InvalidReadException` or be skipped, depending on the `skipInvalidReads` argument.

### Read-end quality analysis

`AnalyseReadQuality` analyses a user-selected percentage of bases independently at the start and end of an individual FASTQ read. The permitted selection is **1â€“50% per end**, with the number of bases rounded upwards.

The analysis decodes **Phred+33** quality strings and calculates, for each end:

- Mean, median, and minimum Phred quality score.
- Percentage of bases at or above Q20 and Q30.
- Mean estimated base-call error probability.
- Quality classification: `GOOD`, `MODERATE`, or `POOR`.

The project's current classification thresholds are:

| Classification | Rule |
| --- | --- |
| `GOOD` | At least 90% of selected bases have Q30 or higher. |
| `MODERATE` | Fewer than 90% reach Q30, but at least 90% reach Q20. |
| `POOR` | Fewer than 90% reach Q20. |

These are SeqSpectre-specific classification rules, not official FastQC thresholds. Analysis results are stored in `QualityResult` objects, with separate statistics for both ends.

### Duplicate sequence calculation

`DuplicateCalculator` calculates the percentage of repeated sequences using a `HashSet` to count unique read strings:

```text
duplicate percentage = (total reads - unique reads) / total reads Ã— 100
```

For example, `A`, `A`, `B`, `C` produces **25% duplicates**. The calculation is available as a standalone method; it is not yet connected to a complete file-processing workflow.

### QC result formatting

The `output` package provides `QCResult` for storing per-sample reporting fields and `OutputWriter` for:

- Printing tab-separated results to the terminal.
- Writing tab-separated results to a text file.
- Showing missing numeric QC values as `N/A`.

The current report columns are `Sample`, `Type`, `Begin kwaliteit`, `Eind kwaliteit`, `Duplicaten`, and `Eindstatus`.

## Project structure

```text
src/
  main/java/
  input/       # File detection, input coordination, reader placeholders
  processing/  # FASTQ validation, read-end quality and duplicates
  output/      # QC result objects and text output
test/java/
   input/
   processing/
   output/
```

## Technologies

- Java
- Gradle 9.7.1 (Gradle Wrapper)
- JUnit Jupiter 5.11.4 for unit tests

## Build and testing

The project includes the Gradle Wrapper and uses JUnit 5. Once the current compilation issues are resolved, tests can be executed from the repository root with:

**Windows:**

```powershell
.\gradlew.bat test
```

**macOS / Linux:**

```bash
./gradlew test
```

Unit test classes are present for input handling, quality analysis, duplicate calculation, and output formatting. Their successful execution has not been verified here.

## Known limitations

- `FastQReader`, `SamReader`, `BamReader`, and `FastQCReader` currently return `null` rather than parsed input.
- `InputController` currently refers to `DataHandler` and `List` without the required imports, and calls a `handleFastqData` method that is not present in the current `DataHandler`. Its FASTQ reader usage also does not match the current reader return type. As a result, the current source does not compile as one complete project.
- The input, processing, and output components have not yet been integrated into an end-to-end application.
- No application entry point or finished command-line interface is present on the inspected `main` branch.

## Installation

## Usage

## Input formats and examples

## Complete analysis workflow

## Output examples

## Future development

## Contributors

## License
