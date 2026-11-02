# Developer Guide

This guide explains the architecture, implementation, development environment and maintenance workflow of SeqSpectre.

It is intended for developers who want to understand, test, maintain or extend the application.

[Home](../README.md) · [Installation](installation.md) · [User Guide](user-guide.md) · [CLI Commands](commands.md) · [Troubleshooting](troubleshooting.md) · [Contact & Support](contact.md)

---

## 1. Development Environment

SeqSpectre is developed in Java and uses Gradle for building and testing the application.

### Verified development environment

The following environment was used to develop and execute SeqSpectre successfully:

| Component | Version |
|---|---|
| Java | OpenJDK 26.0.2.1 |
| Java runtime build | 26.0.2.1+1-7 |
| Gradle | 9.7.1 |
| Groovy | 4.0.32 |
| Kotlin | 2.4.0 |
| Ant | 1.10.17 |
| Operating system | Windows 11 (64-bit) |
| Gradle Launcher JVM | 26.0.2.1 |
| Gradle Daemon JVM | 26.0.2.1 |

The configured Java installation in the verified development environment was:

```text
C:\Users\janin\.jdks\openjdk-26.0.2.1
```

This is a machine-specific development path and is not required on other systems.

**Compatibility note:** The verified development environment uses Java 26.0.2.1. This does not establish the minimum required JDK version. Compatibility with older Java versions should be verified through additional testing.

### Build dependencies

The following dependencies and plugins are declared in `build.gradle`:

| Dependency / Plugin | Version | Purpose |
|---|---|---|
| Java plugin | Gradle built-in | Compiling Java applications |
| Application plugin | Gradle built-in | Running the application |
| PicoCLI | 4.7.7 | Command-line argument handling |
| JUnit Jupiter | 5.11.4 | Automated unit testing |
| JUnit Platform Launcher | Managed through dependency configuration | JUnit test execution |
| Shadow plugin | 9.6.1 | Creating an executable JAR |

All required external dependencies are obtained through the configured Gradle repositories.

---

## 2. Project Structure

SeqSpectre separates input handling, data processing and output generation.

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
│   ├── images/
│   │   └── seqspectre-logo.jpg
│   ├── installation.md
│   ├── user-guide.md
│   ├── commands.md
│   ├── developer-guide.md
│   ├── troubleshooting.md
│   └── contact.md
├── testdata/
│   └── test.fastq
└── src/
    ├── main/
    │   └── java/
    │       ├── Main.java
    │       ├── input/
    │       │   ├── InputController.java
    │       │   ├── InputHandler.java
    │       │   ├── FastQReader.java
    │       │   ├── SamReader.java
    │       │   ├── BamReader.java
    │       │   └── FastQCReader.java
    │       ├── processing/
    │       │   ├── FastqQCProcessor.java
    │       │   ├── AnalyseReadQuality.java
    │       │   ├── QualityResult.java
    │       │   ├── DuplicateCalculator.java
    │       │   ├── GCCalculator.java
    │       │   ├── SequenceLengthAnalyzer.java
    │       │   ├── DataHandler.java
    │       │   └── InvalidReadException.java
    │       └── output/
    │           ├── QCResult.java
    │           └── OutputWriter.java
    └── test/
        └── java/
            ├── input/
            ├── processing/
            └── output/
```

The application uses a modular structure that allows individual analysis components to be tested separately.

The project's UML representation is available in [SeqSpectre_UML.puml](../SeqSpectre_UML.puml).

---

## 3. Application Architecture

SeqSpectre follows a processing pipeline consisting of input discovery, FASTQ parsing, quality analysis and output generation.

### Processing workflow

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
          +----------------------+
          |          |           |
          v          v           v
    Read Quality  Duplicates    GC
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
          +---------------+
          |               |
          v               v
       Terminal        TSV File
```

The processing pipeline is coordinated by `Main`, `InputController` and `FastqQCProcessor`.

Each FASTQ file produces a separate `QCResult` object.

The result objects are passed to `OutputWriter`, which generates both terminal and file output.

---

## 4. Main Application Classes

### `Main`

Located in:

```text
src/main/java/Main.java
```

This class is the command-line entry point.

It uses PicoCLI to define and parse application arguments.

Responsibilities:

- Define CLI options.
- Validate numerical parameters.
- Validate input and output paths.
- Create missing output directories.
- Start the processing pipeline.
- Configure the displayed report columns.
- Handle execution errors.

The current PicoCLI command name is:

```text
qc-tool
```

The application is distributed under the name SeqSpectre.

### `InputController`

Located in:

```text
src/main/java/input/InputController.java
```

Responsible for coordinating FASTQ file discovery and processing.

It accepts a single FASTQ file or a directory containing supported FASTQ files.

For each detected file, `FastqQCProcessor` is called.

### `InputHandler`

Located in:

```text
src/main/java/input/InputHandler.java
```

Responsible for scanning directories and recognizing input formats.

The current detection implementation recognizes:

- FASTQ
- SAM
- BAM
- FastQC output containing `fastqc_data.txt` in a direct subdirectory

Only FASTQ files are currently processed by the complete CLI workflow.

### `FastQReader`

Located in:

```text
src/main/java/input/FastQReader.java
```

Responsible for reading FASTQ records.

Supported input includes:

- `.fastq`
- `.fq`
- `.fastq.gz`
- `.fq.gz`

Each record consists of:

```text
Header
Sequence
Separator
Quality string
```

The reader validates the basic structure and returns the records for further processing.

### `FastqQCProcessor`

Located in:

```text
src/main/java/processing/FastqQCProcessor.java
```

This class coordinates the quality control calculations.

It connects:

- FASTQ reading.
- Read-end quality analysis.
- Duplicate calculation.
- GC calculation.
- Sequence length analysis.
- Optional length filtering.
- Passing-read percentage calculation.
- Final PASS/FAIL classification.

The results are stored in an `output.QCResult` object.

### `AnalyseReadQuality`

Located in:

```text
src/main/java/processing/AnalyseReadQuality.java
```

Responsible for analysing Phred+33 quality information.

For each read end, it calculates:

- Mean Phred score.
- Median Phred score.
- Minimum Phred score.
- Percentage of bases at Q20 or higher.
- Percentage of bases at Q30 or higher.
- Mean estimated base-call error probability.
- Quality classification.

The possible read-end classifications are:

```text
GOOD
MODERATE
POOR
```

These classifications use SeqSpectre-specific thresholds and are not official FastQC classifications.

### `DuplicateCalculator`

Located in:

```text
src/main/java/processing/DuplicateCalculator.java
```

Uses a `HashSet` to determine the number of unique nucleotide sequences.

Duplicates are identified using exact sequence equality.

### `GCCalculator`

Located in:

```text
src/main/java/processing/GCCalculator.java
```

Calculates the GC percentage across all sequences provided to the class.

G and C bases are counted case-insensitively.

### `SequenceLengthAnalyzer`

Located in:

```text
src/main/java/processing/SequenceLengthAnalyzer.java
```

Provides methods for:

- Determining minimum read length.
- Determining maximum read length.
- Calculating the percentage of reads below a selected length.
- Filtering reads based on minimum length.

### `QCResult`

Located in:

```text
src/main/java/output/QCResult.java
```

Stores the aggregated QC results for one sequencing sample.

This is different from `processing.QualityResult`, which stores detailed quality-analysis results for an individual read.

### `OutputWriter`

Located in:

```text
src/main/java/output/OutputWriter.java
```

Responsible for:

- Formatting QC measurements.
- Selecting output columns.
- Printing results to the terminal.
- Writing results to a TSV file.

### Additional components

`DataHandler` and `InvalidReadException` provide validation-related functionality.

`SamReader`, `BamReader` and `FastQCReader` are currently placeholders and do not implement complete parsing.

---

## 5. Important Algorithms and Calculations

### Read-end selection

SeqSpectre analyses a configurable percentage of bases at each read end.

The number of selected bases is:

```text
ceil(read length × percentage / 100)
```

For example:

```text
Read length: 16
End percentage: 10%

ceil(16 × 0.10) = 2
```

Two bases are analysed at each end.

### Phred quality

SeqSpectre uses Phred+33 encoding.

The numerical quality score is calculated as:

```text
Phred score = ASCII value - 33
```

The estimated error probability is:

```text
P(error) = 10^(-Q/10)
```

### Passing-read calculation

A read passes when both ends individually satisfy the quality requirement.

For each end, at least 90% of the selected bases must meet the configured minimum Phred score.

The overall passing-read percentage is:

```text
Passing Reads =
(passing reads / analysed reads) × 100
```

### Duplicate calculation

The duplicate calculator counts unique read sequences using a `HashSet`.

```text
Duplicate percentage =
(total reads - unique reads) / total reads × 100
```

For example:

```text
ACGT
ACGT
TGCA
CCCC
```

There are four reads and three unique sequences.

```text
Duplicate percentage = (4 - 3) / 4 × 100 = 25%
```

### GC calculation

GC percentage is determined by counting guanine and cytosine bases.

```text
GC percentage =
(number of G and C bases / total sequence characters) × 100
```

### Minimum length filtering

When `--min-length` is supplied:

1. The minimum and maximum lengths are calculated from the original reads.
2. The percentage of reads below the threshold is calculated.
3. Reads shorter than the threshold are removed.
4. Subsequent QC calculations use the remaining reads.

### Final status

The final result is:

```text
PASS
```

when both conditions are satisfied:

```text
passingReadsPercent >= requiredPassingReadsPercent

AND

duplicatePercent <= maximumDuplicatePercent
```

Otherwise the result is:

```text
FAIL
```

---

## 6. Gradle Configuration

SeqSpectre uses:

```text
build.gradle
```

for application configuration and dependency management.

The current plugins are:

```groovy
plugins {
    id 'java'
    id 'application'
    id 'com.gradleup.shadow' version '9.6.1'
}
```

Dependencies are obtained from Maven Central.

The application entry point is configured using:

```groovy
application {
    mainClass = 'Main'
}
```

The Shadow plugin is configured to generate:

```text
seqspectre-all.jar
```

This JAR contains SeqSpectre and its runtime dependencies.

The Gradle Wrapper version is 9.7.1.

### Verifying the build environment

Windows:

```powershell
.\gradlew.bat --version
```

macOS/Linux:

```bash
./gradlew --version
```

Developers should verify that Gradle uses a compatible Java installation.

---

## 7. Running the Application During Development

Developers can run SeqSpectre directly through Gradle.

### Windows

```powershell
.\gradlew.bat run --args="-i testdata/test.fastq"
```

### macOS/Linux

```bash
./gradlew run --args="-i testdata/test.fastq"
```

Additional CLI arguments can be included.

Example:

```powershell
.\gradlew.bat run --args="-i testdata/test.fastq --quality --duplicates --gc"
```

Running through Gradle is useful during development because the application does not have to be manually repackaged after every code change.

---

## 8. Building an Executable JAR

### Windows

```powershell
.\gradlew.bat shadowJar
```

### macOS/Linux

```bash
./gradlew shadowJar
```

The resulting file is:

```text
build/libs/seqspectre-all.jar
```

Run the application with:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq
```

A clean build can be performed using:

```powershell
.\gradlew.bat clean build shadowJar
```

This removes previous build outputs, compiles the project, executes tests and creates the executable JAR.

---

## 9. Automated Testing

SeqSpectre uses JUnit Jupiter 5.11.4.

Tests are organized into the following packages:

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

Windows:

```powershell
.\gradlew.bat test
```

macOS/Linux:

```bash
./gradlew test
```

### Run a specific test class

Example:

```powershell
.\gradlew.bat test --tests processing.DuplicateCalculatorTest
```

### View test results

After running the tests, Gradle normally generates an HTML report:

```text
build/reports/tests/test/index.html
```

### Recommended test coverage

Developers should test:

- Valid and invalid FASTQ records.
- Empty FASTQ files.
- Compressed FASTQ input.
- Incorrect file paths.
- Numerical boundary values.
- Correct quality calculations.
- Duplicate detection.
- GC calculation.
- Sequence length filtering.
- PASS/FAIL classification.
- Output formatting.
- Multiple file processing.

---

## 10. Verified Functional Test

The included test file is:

```text
testdata/test.fastq
```

It contains four FASTQ records, each consisting of 16 nucleotides.

A successful analysis was performed using:

```powershell
java -jar build\libs\seqspectre-all.jar -i testdata\test.fastq
```

The generated `results.tsv` contained:

| Measurement | Value |
|---|---|
| Beginning Quality | 100.0% |
| Ending Quality | 100.0% |
| Duplicates | 0.0% |
| GC Percentage | 43.75% |
| Minimum Length | 16 |
| Maximum Length | 16 |
| Passing Reads | 100.0% |
| Final Status | PASS |

This confirms successful execution of the standard command-line workflow in the verified development environment.

It does not independently establish that all automated tests or all possible input cases pass.

---

## 11. Adding New QC Functionality

New calculations should preferably be implemented in a separate class under:

```text
src/main/java/processing/
```

Recommended development procedure:

1. Identify the required analysis.
2. Create the processing class or method.
3. Write unit tests for the calculation.
4. Handle invalid or missing input.
5. Connect the calculation to `FastqQCProcessor`.
6. Extend `output.QCResult` if the result must be stored.
7. Extend `OutputWriter` if the result must be displayed.
8. Add a command-line argument in `Main` if the analysis must be configurable.
9. Update the relevant documentation.
10. Run automated and manual tests.

### Adding command-line options

PicoCLI command-line arguments are declared in `Main.java`.

Example:

```java
@Option(
    names = "--example",
    description = "Example command-line argument"
)
private boolean example;
```

This is an illustrative example. `--example` is not a supported argument in the current application.

New arguments should have descriptive names, clear documentation and appropriate validation.

---

## 12. Git Development Workflow

A recommended workflow is to implement changes in separate Git branches.

### Create a new branch

```bash
git checkout -b feature/new-analysis
```

### Check modified files

```bash
git status
```

### Run automated tests

Windows:

```powershell
.\gradlew.bat test
```

macOS/Linux:

```bash
./gradlew test
```

### Stage changes

```bash
git add .
```

### Commit

```bash
git commit -m "Add new QC functionality"
```

### Push the branch

```bash
git push -u origin feature/new-analysis
```

Changes should be reviewed before merging them into the main branch.

Avoid committing:

- Temporary build outputs.
- Generated local result files.
- Large sequencing datasets that are not needed for testing.
- Confidential or sensitive sequencing information.
- Environment-specific configuration containing private paths or credentials.

---

## 13. Repository Access and Contributions

SeqSpectre is hosted on GitHub:

https://github.com/Bio-informatics-student/Java_groep_4

The repository can be viewed and downloaded publicly.

Direct changes to the original repository require the necessary GitHub permissions.

Developers should use separate branches and pull requests when proposing changes.

Repository administrators can configure branch protection or repository rulesets to restrict direct changes to the main branch.

Public source-code availability does not automatically establish an open-source license or permission to redistribute modified versions.

For questions or feature suggestions, see the [Contact & Support Guide](contact.md).

---

## 14. Current Limitations

The current version of SeqSpectre has the following limitations:

- FASTQ is the only format connected to the complete QC processing pipeline.
- SAM, BAM and FastQC reader classes are not yet implemented.
- Directory scanning is not recursive.
- FASTQ records are loaded into memory.
- Duplicate detection uses exact nucleotide sequence equality.
- GC percentage and sequence length do not independently affect PASS/FAIL.
- The PicoCLI command is currently named `qc-tool`.
- An explicit application release version is not configured in the PicoCLI annotation.
- Java compatibility has been verified in the supplied Java 26.0.2.1 development environment, but the minimum compatible Java version has not been established.

Potential future improvements include:

- Support for additional sequencing file formats.
- Streaming FASTQ processing for large datasets.
- Additional QC measurements.
- More configurable PASS/FAIL criteria.
- Improved reporting formats.
- Performance optimization.
- Further compatibility testing across Java versions and operating systems.

---

## 15. Further Documentation

- [Installation Guide](installation.md)
- [User Guide](user-guide.md)
- [CLI Command Reference](commands.md)
- [Troubleshooting Guide](troubleshooting.md)
- [Contact & Support](contact.md)

---

[Back to SeqSpectre Home](../README.md)