# Developer Guide

This guide documents the architecture, implementation, development environment, testing procedures and extension workflow of SeqSpectre.

It is intended for developers who want to understand, maintain or extend the application.

[Home](../README.md) · [Installation](installation.md) · [User Guide](user-guide.md) · [CLI Commands](commands.md) · [Troubleshooting](troubleshooting.md) · [Contact & Support](contact.md)

---

## 1. Development Environment

SeqSpectre is a Java application built using Gradle.

The application has been developed and executed successfully in the following environment:

| Component | Tested version |
|---|---|
| Java | OpenJDK 26.0.2.1 |
| Runtime build | 26.0.2.1+1-7 |
| Gradle | 9.7.1 |
| Groovy | 4.0.32 |
| Kotlin | 2.4.0 |
| Ant | 1.10.17 |
| Operating system | Windows 11 (64-bit) |
| Gradle Launcher JVM | 26.0.2.1 |
| Gradle Daemon JVM | 26.0.2.1 |

Java 26.0.2.1 is the verified development version. The minimum compatible Java version has not been established through testing.

### Dependencies

| Dependency or plugin | Version | Purpose |
|---|---|---|
| Gradle Java plugin | Built-in | Java compilation |
| Gradle Application plugin | Built-in | Application execution |
| PicoCLI | 4.7.7 | Command-line parsing |
| JUnit Jupiter | 5.11.4 | Unit testing |
| JUnit Platform Launcher | Managed by Gradle | Test execution |
| Gradle Shadow plugin | 9.6.1 | Executable JAR creation |

Dependencies are obtained using Maven Central.

## 2. Project Structure

```text
Java_groep_4/
├── README.md
├── build.gradle
├── settings.gradle
├── gradle.properties
├── gradlew
├── gradlew.bat
├── gradle/
├── SeqSpectre_UML.puml
├── testdata/
│   └── test.fastq
├── docs/
│   ├── images/
│   │   └── seqspectre-logo.jpg.jpg
│   ├── installation.md
│   ├── user-guide.md
│   ├── commands.md
│   ├── developer-guide.md
│   ├── troubleshooting.md
│   └── contact.md
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
    │   └── output/
    │       ├── QCResult.java
    │       └── OutputWriter.java
    └── test/java/
        ├── input/
        ├── processing/
        └── output/
```

The source packages separate input handling, data processing and report generation.

The UML source is available in [SeqSpectre_UML.puml](../SeqSpectre_UML.puml). Developers should keep the diagram synchronized with the current implementation.

## 3. Application Architecture

The primary data flow is:

```text
CLI Arguments
     |
     v
    Main
     |
     v
InputController
     |
     +--> InputHandler
     |
     v
FastqQCProcessor
     |
     +--> FastQReader
     |
     +--> SequenceLengthAnalyzer
     |
     +--> AnalyseReadQuality
     |
     +--> DuplicateCalculator
     |
     +--> GCCalculator
     |
     v
output.QCResult
     |
     v
OutputWriter
     |
     +--> Terminal
     |
     +--> TSV File
```

A complete FASTQ analysis consists of:

1. Parsing and validating command-line arguments.
2. Finding the selected FASTQ files.
3. Reading and validating FASTQ records.
4. Counting original reads and determining original lengths.
5. Applying optional minimum-length filtering.
6. Calculating read-end quality measurements.
7. Calculating duplicates and GC percentage.
8. Calculating the percentage of passing reads.
9. Determining the overall PASS/FAIL status.
10. Generating terminal and TSV output.

## 4. Class Responsibilities

### `Main.java`

The application entry point.

Responsibilities:

- Define PicoCLI arguments.
- Validate command-line values.
- Verify input and output paths.
- Create missing output directories.
- Start the input-processing pipeline.
- Configure report column selection.
- Handle execution exceptions.
- Handle the optional hidden interactive Easter egg.

The PicoCLI command name is currently:

```text
qc-tool
```

The product name is SeqSpectre.

### `InputController.java`

Coordinates the processing of a single FASTQ file or a directory containing FASTQ files.

It uses `InputHandler` for directory scanning and calls `FastqQCProcessor` for every selected FASTQ file.

### `InputHandler.java`

Scans a directory and categorizes supported file types.

It currently detects:

- FASTQ files.
- SAM files.
- BAM files.
- FastQC `fastqc_data.txt` files inside direct subdirectories.

Only FASTQ files are currently connected to the complete QC pipeline.

### `FastQReader.java`

Reads FASTQ records and returns a list of four-element string arrays.

Each array contains:

```text
record[0] = Header
record[1] = Sequence
record[2] = Separator
record[3] = Quality string
```

The reader supports gzip-compressed input and validates the basic FASTQ structure.

### `FastqQCProcessor.java`

Coordinates the main QC workflow.

It performs:

- Original read counting.
- Original sequence length calculations.
- Optional minimum length filtering.
- Quality calculations for both read ends.
- Duplicate detection.
- GC percentage calculation.
- Passing-read percentage calculation.
- PASS/FAIL classification.
- Creation of an `output.QCResult` object.

The current processor passes `records.size()` to the report object as the original total-read count.

### `AnalyseReadQuality.java`

Analyses Phred+33 quality strings.

For each read end, it calculates:

- Mean Phred score.
- Median Phred score.
- Minimum Phred score.
- Percentage Q20 or higher.
- Percentage Q30 or higher.
- Mean estimated error probability.
- GOOD, MODERATE or POOR classification.

The detailed values are stored in `processing.QualityResult`.

### `DuplicateCalculator.java`

Calculates duplicate percentage using a `HashSet`.

Exact sequence matches are treated as duplicates.

The calculation is applied to sequences remaining after optional length filtering.

### `GCCalculator.java`

Calculates GC percentage by counting G and C characters across the selected sequences.

The implementation counts G and C case-insensitively.

### `SequenceLengthAnalyzer.java`

Provides methods for:

- Finding minimum sequence length.
- Finding maximum sequence length.
- Calculating the percentage of reads below a minimum length.
- Filtering reads shorter than a selected threshold.

### `output.QCResult.java`

Stores the aggregated results of one sample.

The current fields include:

```text
sample
fileType
totalReads
beginningQuality
endQuality
duplicates
gcPercentage
minimumSequenceLength
maximumSequenceLength
belowMinimumPercent
passingReadsPercent
status
```

The class also retains a constructor without the `totalReads` argument for compatibility with existing callers.

### `OutputWriter.java`

Formats `QCResult` objects and generates terminal and TSV reports.

The output always includes:

```text
Sample
Type
Total Reads
Final Status
```

Additional columns depend on the selected output flags.

Percentages are formatted using:

```java
String.format(Locale.ROOT, "%.2f%%", value);
```

This produces values such as:

```text
100.00%
43.75%
0.00%
```

Null percentage values are displayed as `N/A`.

### Additional Classes

`DataHandler` and `InvalidReadException` provide additional validation-related functionality.

`SamReader`, `BamReader` and `FastQCReader` are currently placeholders without complete parsing implementations.

## 5. QC Algorithms

### 5.1 Read-End Selection

The number of bases analysed at each read end is:

```text
ceil(readLength × endPercentage / 100)
```

The allowed end percentage is 1–50.

Both regions are analysed independently.

Due to upward rounding, very short reads may have overlapping selected regions.

### 5.2 Phred Quality

The FASTQ quality score is decoded using Phred+33:

```text
Q = ASCII character value - 33
```

Estimated sequencing error probability:

```text
P(error) = 10^(-Q/10)
```

A read passes if at least 90% of the selected bases at both ends independently meet the configured minimum quality score.

### 5.3 Passing Reads

```text
Passing Reads (%) =
Number of passing analysed reads
/
Total analysed reads
× 100
```

The denominator excludes reads removed by optional minimum-length filtering.

### 5.4 Duplicate Detection

```text
Duplicates (%) =
(Total analysed reads - Unique analysed sequences)
/
Total analysed reads
× 100
```

The implementation uses a `HashSet` to count unique sequences.

### 5.5 GC Calculation

```text
GC (%) =
Number of G and C characters
/
Total number of sequence characters
× 100
```

The calculation is performed using reads remaining after optional filtering.

### 5.6 Sequence Length Filtering

The sequence length workflow is important because different report values describe different datasets.

The processor initially calculates minimum and maximum read lengths using the original records.

If `--min-length` is provided:

1. The percentage of original reads below the threshold is calculated.
2. Reads shorter than the threshold are removed.
3. Processing stops if no reads remain.
4. The minimum sequence length is recalculated using the remaining sequences.
5. The maximum sequence length retains the original value.

For example:

```text
Original lengths:
50, 80, 100, 150

Minimum required length:
90

Remaining lengths:
100, 150

Reported minimum:
100

Reported maximum:
150

Total Reads:
4
```

The QC percentages are calculated using only the remaining reads.

### 5.7 Final Status

A sample receives PASS if:

```text
passingReadsPercent >= requiredPassingReadsPercent

AND

duplicatePercent <= maximumDuplicatePercent
```

Otherwise, it receives FAIL.

GC percentage and sequence length do not independently determine final status.

## 6. Gradle Configuration

The project uses these Gradle plugins:

```groovy
plugins {
    id 'java'
    id 'application'
    id 'com.gradleup.shadow' version '9.6.1'
}
```

The application entry point is configured as:

```groovy
application {
    mainClass = 'Main'
}
```

The Shadow plugin creates:

```text
build/libs/seqspectre-all.jar
```

The test task uses JUnit Platform:

```groovy
test {
    useJUnitPlatform()
}
```

### Interactive Input Support

The current `build.gradle` includes:

```groovy
tasks.named('run', JavaExec) {
    standardInput = System.in
}
```

This forwards terminal input to applications started through Gradle.

It is relevant to the optional interactive functionality in `Main.java`.

## 7. Building and Running

### Windows

Build and test:

```powershell
.\gradlew.bat build
```

Run automated tests:

```powershell
.\gradlew.bat test
```

Create executable JAR:

```powershell
.\gradlew.bat shadowJar
```

Run directly through Gradle:

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

The generated JAR can be executed using:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq
```

A full clean build can be performed with:

```powershell
.\gradlew.bat clean build shadowJar
```

## 8. Automated Testing

SeqSpectre uses JUnit Jupiter 5.11.4.

Tests are organized in:

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

### Run all tests

```powershell
.\gradlew.bat test
```

### Run one test class

```powershell
.\gradlew.bat test --tests processing.DuplicateCalculatorTest
```

### View test results

Gradle normally generates an HTML test report at:

```text
build/reports/tests/test/index.html
```

### Recommended additional testing

Developers should verify:

- Valid and invalid FASTQ structure.
- Compressed FASTQ input.
- Empty and incomplete files.
- Exact duplicate sequences.
- Quality threshold boundaries.
- Percentages rounded to two decimal places.
- Total Reads before filtering.
- Minimum Length after filtering.
- Maximum Length after filtering.
- Length filtering that removes every read.
- Output column combinations.
- PASS/FAIL decisions.
- Multiple-file processing.
- Interactive standard input.

The existence of a test class does not by itself establish that every current implementation path is covered or that the latest test run passed.

## 9. Example Dataset

The included file is:

```text
testdata/test.fastq
```

It contains four reads of 16 bases each.

The current processing code produces the following expected values using default settings:

| Measurement | Value |
|---|---|
| Total Reads | 4 |
| Beginning Quality | 100.00% |
| Ending Quality | 100.00% |
| Duplicates | 0.00% |
| GC Percentage | 43.75% |
| Minimum Length | 16 |
| Maximum Length | 16 |
| Passing Reads | 100.00% |
| Final Status | PASS |

The tracked `results.tsv` file may contain values generated using an earlier output format.

Regenerate the file to validate the current report format.

## 10. Adding New Functionality

When implementing a new analysis:

1. Create a focused processing class or method.
2. Add unit tests for the expected calculation.
3. Handle invalid input and boundary conditions.
4. Integrate the calculation into `FastqQCProcessor`.
5. Extend `output.QCResult` if a new value must be stored.
6. Extend `OutputWriter` if the value must be displayed.
7. Add a PicoCLI option in `Main.java` when user configuration is required.
8. Update the command reference and user guide.
9. Run automated tests.
10. Execute a manual end-to-end analysis.

### Adding a CLI Option

PicoCLI options are defined using the `@Option` annotation.

Example:

```java
@Option(
    names = "--example",
    description = "Example command-line argument"
)
private boolean example;
```

This is an illustrative example and not an existing SeqSpectre option.

## 11. Interactive Developer Option

The application includes a hidden command-line flag:

```text
--bart-mode
```

This option is declared in `Main.java` with:

```java
@Option(
    names = "--bart-mode",
    hidden = true
)
private boolean bartMode;
```

It is intentionally omitted from the normal command-line help.

After successful QC processing and report writing, the option activates an interactive prompt.

The program reads input using `Scanner(System.in)`.

The Gradle `standardInput = System.in` configuration allows this functionality to be used when running through Gradle from an interactive terminal.

This feature does not affect QC calculations and is not part of the normal user workflow.

## 12. Git Development Workflow

Create a feature branch:

```bash
git checkout -b feature/new-analysis
```

Check the current working tree:

```bash
git status
```

Run tests:

```powershell
.\gradlew.bat test
```

Stage changes:

```bash
git add .
```

Commit:

```bash
git commit -m "Add new QC functionality"
```

Push the branch:

```bash
git push -u origin feature/new-analysis
```

Changes should be reviewed before merging into the main branch.

Do not commit confidential sequencing datasets, credentials or unnecessary build artifacts.

## 13. Repository Access and Contributions

The source code is hosted at:

https://github.com/Bio-informatics-student/Java_groep_4

The repository is publicly readable and downloadable.

Only accounts with the appropriate GitHub permissions can modify the original repository directly.

Users can report problems and propose enhancements through GitHub Issues.

The availability of source code does not automatically grant an open-source license. No license file is currently included in the repository.

## 14. Known Limitations

- Complete input processing currently supports FASTQ only.
- SAM, BAM and FastQC reader implementations are incomplete.
- FASTQ directory scanning is not recursive.
- FASTQ records are loaded into memory.
- Duplicate detection uses exact nucleotide sequence equality.
- GC percentage and length measurements do not independently determine PASS/FAIL.
- The command-line name remains `qc-tool`.
- A formal release version has not been configured.
- Compatibility testing across Java versions and operating systems is incomplete.
- The project UML should be checked after architectural changes.

Potential future improvements include streaming FASTQ processing, support for additional formats and extended QC reporting.

---

[Back to SeqSpectre Home](../README.md)
