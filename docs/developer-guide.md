# Developer Guide

This guide describes the current implementation of SeqSpectre and explains how to build, test, maintain and extend the application.

[Home](../README.md) · [Installation](installation.md) · [User Guide](user-guide.md) · [CLI Commands](commands.md) · [Troubleshooting](troubleshooting.md)

## 1. Technology stack

SeqSpectre is developed in Java and uses the following technologies:

| Technology | Version | Purpose |
|---|---|---|
| Java | Compatible JDK | Application programming language |
| Gradle | 9.7.1 | Build automation |
| PicoCLI | 4.7.7 | Command-line argument parsing |
| Shadow plugin | 9.6.1 | Executable JAR creation |
| JUnit Jupiter | 5.11.4 | Automated unit testing |

The application uses the Gradle `java` and `application` plugins.

The main application class is configured in `build.gradle`:

```groovy
application {
    mainClass = 'Main'
}
```

The repository includes the Gradle Wrapper, so a separate Gradle installation is not required.

## 2. Project structure

The source code is divided into three main packages: `input`, `processing` and `output`.

```text
Java_groep_4/
├── README.md
├── build.gradle
├── settings.gradle
├── gradle.properties
├── gradlew
├── gradlew.bat
├── SeqSpectre_UML.puml
├── docs/
├── testdata/
└── src/
    ├── main/java/
    │   ├── Main.java
    │   ├── input/
    │   │   ├── InputController.java
    │   │   ├── InputHandler.java
    │   │   ├── FastQReader.java
    │   │   ├── SamReader.java
    │   │   ├── BamReader.java
    │   │   └── FastQCReader.java
    │   ├── processing/
    │   │   ├── FastqQCProcessor.java
    │   │   ├── AnalyseReadQuality.java
    │   │   ├── QualityResult.java
    │   │   ├── DuplicateCalculator.java
    │   │   ├── GCCalculator.java
    │   │   ├── SequenceLengthAnalyzer.java
    │   │   ├── DataHandler.java
    │   │   └── InvalidReadException.java
    └── output/
        ├── QCResult.java
        └── OutputWriter.java
```

Automated tests are located in `src/test/java/`, divided into the same packages.

A PlantUML diagram is available in [SeqSpectre_UML.puml](../SeqSpectre_UML.puml).

## 3. Application architecture

SeqSpectre follows a processing pipeline in which each component has a specific responsibility.

```text
Command-line arguments
         |
         v
       Main
         |
         v
  InputController
         |
         v
    InputHandler
         |
         v
     FastQReader
         |
         v
  FastqQCProcessor
         |
         +---------------------+
         |          |          |
         v          v          v
   Read Quality   Duplicates   GC
         |
         v
  Sequence Length
         |
         v
      QCResult
         |
         v
    OutputWriter
         |
         +-------------+
         |             |
         v             v
      Terminal      TSV File
```

The pipeline begins in `Main.java`, where PicoCLI parses and validates the command-line options.

`InputController` identifies the FASTQ input files and passes them to `FastqQCProcessor`.

The processor coordinates the individual analyses and creates a `QCResult` object for each sample.

Finally, `OutputWriter` displays the results and writes them to a TSV file.

## 4. Classes and responsibilities

### `Main`

Located in:

```text
src/main/java/Main.java
```

Responsibilities:

- Define command-line arguments using PicoCLI.
- Validate user input and numerical thresholds.
- Validate input and output paths.
- Create output directories.
- Start the processing pipeline.
- Select output columns.
- Handle application errors.

### `InputController`

Located in:

```text
src/main/java/input/InputController.java
```

Coordinates input file discovery and processing.

The class accepts either an individual FASTQ file or a directory and passes the detected files to `FastqQCProcessor`.

### `InputHandler`

Located in:

```text
src/main/java/input/InputHandler.java
```

Scans an input directory and categorizes recognized files.

The current detection implementation recognizes FASTQ, SAM, BAM and FastQC data.

Only FASTQ files are currently connected to the complete QC pipeline.

### `FastQReader`

Located in:

```text
src/main/java/input/FastQReader.java
```

Reads FASTQ records and supports both regular and gzip-compressed files.

Each FASTQ record is represented using four strings:

```text
Header
Sequence
Separator
Quality
```

The reader checks the structure of each record and rejects incomplete or invalid input.

### `FastqQCProcessor`

Located in:

```text
src/main/java/processing/FastqQCProcessor.java
```

Coordinates the QC calculations for a FASTQ file.

Responsibilities:

- Read FASTQ records.
- Calculate original sequence lengths.
- Apply optional length filtering.
- Analyse read-end quality.
- Calculate duplicate percentage.
- Calculate GC percentage.
- Calculate passing-read percentage.
- Determine final PASS/FAIL status.
- Create a report-level QC result.

### `AnalyseReadQuality`

Located in:

```text
src/main/java/processing/AnalyseReadQuality.java
```

Analyses quality values at both ends of an individual read.

It decodes Phred+33 encoded quality strings and calculates:

- Mean Phred score.
- Median Phred score.
- Minimum Phred score.
- Percentage of bases with Q20 or higher.
- Percentage of bases with Q30 or higher.
- Mean estimated sequencing error probability.
- GOOD, MODERATE or POOR classification.

These detailed results are stored in `processing.QualityResult`.

### `DuplicateCalculator`

Located in:

```text
src/main/java/processing/DuplicateCalculator.java
```

Calculates duplicate percentage using a `HashSet`.

Duplicate detection is based on exact equality of nucleotide sequences.

### `GCCalculator`

Located in:

```text
src/main/java/processing/GCCalculator.java
```

Calculates the percentage of guanine and cytosine across the analysed sequences.

### `SequenceLengthAnalyzer`

Located in:

```text
src/main/java/processing/SequenceLengthAnalyzer.java
```

Provides methods for:

- Determining minimum sequence length.
- Determining maximum sequence length.
- Calculating the percentage of reads below a minimum length.
- Filtering records shorter than a specified length.

### `QCResult`

Located in:

```text
src/main/java/output/QCResult.java
```

Stores the aggregated QC results for a sample, including the final PASS/FAIL status.

This differs from `processing.QualityResult`, which stores detailed information for an individual read.

### `OutputWriter`

Located in:

```text
src/main/java/output/OutputWriter.java
```

Responsible for formatting QC results and writing them to the terminal and a TSV file.

The included columns depend on the user's display options.

### Additional components

`DataHandler` and `InvalidReadException` provide additional validation-related functionality.

`SamReader`, `BamReader` and `FastQCReader` are currently placeholders and do not yet implement file parsing.

## 5. Important calculations

### Read-end selection

The number of bases selected at each end is:

```text
ceil(sequence length × end percentage / 100)
```

For example, a 16-base read with an end percentage of 10% results in two selected bases at each end.

The beginning and ending regions are analysed separately.

### Phred scores

FASTQ quality values use Phred+33 encoding.

The numerical Phred score is calculated using:

```text
Phred score = ASCII value - 33
```

The estimated probability of an incorrect base call is:

```text
P(error) = 10^(-Q/10)
```

A read passes its quality check if at least 90% of the analysed bases at both ends individually meet the selected Phred threshold.

### Duplicate percentage

```text
Duplicate percentage =
(total reads - unique sequences) / total reads × 100
```

The calculation uses exact sequence matches.

### GC percentage

```text
GC percentage =
(number of G and C bases / total number of sequence characters) × 100
```

The current implementation counts G and C case-insensitively.

### Sequence length filtering

The minimum and maximum read lengths are calculated before optional length filtering.

After filtering, the remaining reads are used for quality, duplicate, GC and passing-read calculations.

### Final sample status

```text
PASS if:
passingReadsPercent >= requiredPassingReadsPercent
AND
duplicatePercent <= maximumDuplicatePercent
```

Otherwise:

```text
FAIL
```

GC percentage and read length do not independently determine PASS or FAIL.

## 6. Building the application

From the root of the repository, execute the following commands.

### Windows

Compile and test:

```powershell
.\gradlew.bat build
```

Run all tests:

```powershell
.\gradlew.bat test
```

Create an executable JAR:

```powershell
.\gradlew.bat shadowJar
```

Run the application directly through Gradle:

```powershell
.\gradlew.bat run --args="-i testdata/test.fastq"
```

### macOS/Linux

```bash
./gradlew build
./gradlew test
./gradlew shadowJar
./gradlew run --args="-i testdata/test.fastq"
```

The executable JAR is generated at:

```text
build/libs/seqspectre-all.jar
```

## 7. Automated testing

SeqSpectre uses JUnit Jupiter for unit testing.

Tests are organized under:

```text
src/test/java/input/
src/test/java/processing/
src/test/java/output/
```

Current test classes include:

- `FastQReaderTest`
- `InputControllerTest`
- `InputHandlerTest`
- `AnalyseReadQualityTest`
- `DataHandlerTest`
- `DuplicateCalculatorTest`
- `GCCalculatorTest`
- `SequenceLengthAnalyzerTest`
- `QCResultTest`
- `OutputWriterTest`

Run all tests:

```powershell
.\gradlew.bat test
```

Run an individual test class:

```powershell
.\gradlew.bat test --tests processing.DuplicateCalculatorTest
```

After execution, the HTML test report is normally available at:

```text
build/reports/tests/test/index.html
```

Recommended test cases include:

- Valid FASTQ records.
- Incomplete FASTQ records.
- Invalid sequence and quality lengths.
- Invalid Phred characters.
- Compressed FASTQ input.
- Invalid file paths.
- Boundary values for quality thresholds.
- Duplicate and unique sequences.
- Minimum length filtering.
- PASS/FAIL boundary conditions.
- Multiple input files.
- Output column combinations.

## 8. Adding new functionality

When implementing a new QC analysis:

1. Create a focused processing class or method.
2. Implement the calculation.
3. Add unit tests covering normal and boundary cases.
4. Connect the calculation to `FastqQCProcessor`.
5. Store the result in `output.QCResult` if necessary.
6. Extend `OutputWriter` to display the result.
7. Add a command-line option to `Main` if the feature must be configurable.
8. Update the relevant documentation.
9. Run automated tests and manually verify the output.

### Adding command-line options

PicoCLI options are declared in `Main.java`.

Example:

```java
@Option(
    names = "--example",
    description = "Description of the option"
)
private boolean example;
```

This example demonstrates the annotation structure. `--example` is not a currently supported SeqSpectre argument.

New options should be validated before processing and passed to the appropriate classes.

## 9. Git development workflow

Developers should preferably work on separate feature branches.

Example:

```bash
git checkout -b feature/new-analysis
```

After implementing the feature:

```bash
git status
```

Run tests:

```bash
./gradlew test
```

Stage changed files:

```bash
git add .
```

Commit:

```bash
git commit -m "Add new QC functionality"
```

Push the feature branch:

```bash
git push -u origin feature/new-analysis
```

The changes can then be reviewed and merged into the main branch.

Avoid committing temporary output files, build artifacts and sensitive sequencing data.

## 10. Known limitations

The current version has several limitations:

- The complete processing pipeline supports FASTQ input only.
- SAM, BAM and FastQC reader classes are not implemented.
- Directory scanning is not recursive.
- Duplicate detection is based on exact sequence equality.
- FASTQ records are loaded into memory, which can require substantial memory for large datasets.
- The command-line name is currently `qc-tool`, although the project is called SeqSpectre.
- The application version is not explicitly configured in the PicoCLI command annotation.

Potential future improvements include streaming FASTQ input, additional file formats, extended QC analyses and configurable status requirements.

## 11. Further documentation

- [Installation Guide](installation.md)
- [User Guide](user-guide.md)
- [CLI Command Reference](commands.md)
- [Troubleshooting Guide](troubleshooting.md)

---

[Back to SeqSpectre Home](../README.md)
