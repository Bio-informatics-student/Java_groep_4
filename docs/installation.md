# Installation Guide

This guide explains how to download, build, install and run SeqSpectre.

[Home](../README.md) · [User Guide](user-guide.md) · [CLI Commands](commands.md) · [Developer Guide](developer-guide.md) · [Troubleshooting](troubleshooting.md) · [Contact & Support](contact.md)

---

## 1. System Requirements

SeqSpectre requires a Java Development Kit (JDK) and a command-line environment.

### Verified Development Environment

| Component | Tested version |
|---|---|
| Java | OpenJDK 26.0.2.1 |
| Java runtime build | 26.0.2.1+1-7 |
| Gradle | 9.7.1 |
| Gradle Launcher JVM | 26.0.2.1 |
| Gradle Daemon JVM | 26.0.2.1 |
| Operating system | Windows 11 (64-bit) |

Java 26.0.2.1 is the verified development version, not an established minimum supported version.

Compatibility with older Java versions and other operating systems has not been independently verified.

### Required Software

- Java Development Kit (JDK).
- Terminal or command-line environment.
- Git (recommended for cloning).
- Internet connection for downloading dependencies when necessary.

Gradle is included through the Gradle Wrapper and does not need to be installed separately.

## 2. Verify Java

Open a terminal and run:

```bash
java -version
```

The verified environment reported:

```text
openjdk version "26.0.2.1" 2026-08-18
OpenJDK Runtime Environment (build 26.0.2.1+1-7)
OpenJDK 64-Bit Server VM (build 26.0.2.1+1-7, mixed mode, sharing)
```

Also verify the compiler:

```bash
javac -version
```

If Java is not installed, install a suitable JDK distribution such as [Eclipse Temurin](https://adoptium.net/).

Restart the terminal after installation.

## 3. Download the Repository

### Option A: Clone with Git

```bash
git clone https://github.com/Bio-informatics-student/Java_groep_4.git
cd Java_groep_4
```

### Option B: Download ZIP

1. Open the [SeqSpectre repository](https://github.com/Bio-informatics-student/Java_groep_4).
2. Select **Code**.
3. Select **Download ZIP**.
4. Extract the archive.
5. Open a terminal in the extracted project directory.

The project root should contain `build.gradle`, `gradlew`, `gradlew.bat` and `src`.

## 4. Verify Gradle

Windows:

```powershell
.\gradlew.bat --version
```

macOS/Linux:

```bash
./gradlew --version
```

The expected Gradle version is:

```text
Gradle 9.7.1
```

If a permissions error occurs on macOS/Linux:

```bash
chmod +x gradlew
```

## 5. Build the Application

### Windows

```powershell
.\gradlew.bat build
```

### macOS/Linux

```bash
./gradlew build
```

The build compiles the application and executes the configured automated tests.

A successful build normally ends with:

```text
BUILD SUCCESSFUL
```

Initial builds may require an internet connection to download dependencies.

## 6. Create the Executable JAR

SeqSpectre uses the Gradle Shadow plugin to package the application with its runtime dependencies.

### Windows

```powershell
.\gradlew.bat shadowJar
```

### macOS/Linux

```bash
./gradlew shadowJar
```

The generated executable is:

```text
build/libs/seqspectre-all.jar
```

Running the `shadowJar` task alone does not replace running the complete automated test suite.

## 7. Verify the Installation

Display the command-line help.

### Windows

```powershell
java -jar build\libs\seqspectre-all.jar --help
```

### macOS/Linux

```bash
java -jar build/libs/seqspectre-all.jar --help
```

The application should display the supported command-line arguments.

The normal command-line name is currently `qc-tool`.

The internal development-only Easter egg is not listed in normal help.

## 8. Run the Included FASTQ Example

The repository contains:

```text
testdata/test.fastq
```

This file contains four reads of 16 bases each.

### Windows

```powershell
java -jar build\libs\seqspectre-all.jar -i testdata\test.fastq
```

### macOS/Linux

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq
```

SeqSpectre will:

1. Read and validate the FASTQ records.
2. Count the original reads.
3. Determine sequence lengths.
4. Analyse read-end quality.
5. Calculate duplicates and GC percentage.
6. Calculate passing reads.
7. Determine the final status.
8. Print the results to the terminal.
9. Save the results to `results.tsv`.

### Expected Results

Using the current application implementation and default settings:

| Measurement | Result |
|---|---|
| Sample | test.fastq |
| Type | FASTQ |
| Total Reads | 4 |
| Beginning Quality | 100.00% |
| Ending Quality | 100.00% |
| Duplicates | 0.00% |
| GC Percentage | 43.75% |
| Minimum Length | 16 |
| Maximum Length | 16 |
| Passing Reads | 100.00% |
| Final Status | PASS |

These values describe a small functional test dataset, not a full biological sequencing experiment.

If your output still lacks `Total Reads` or uses one decimal place, ensure that you rebuilt the executable JAR from the current source.

## 9. Analyse Your Own Data

Supported FASTQ extensions:

```text
.fastq
.fq
.fastq.gz
.fq.gz
```

Example:

```bash
java -jar build/libs/seqspectre-all.jar -i sample.fastq
```

Compressed file:

```bash
java -jar build/libs/seqspectre-all.jar -i sample.fastq.gz
```

Directory:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata
```

Directory scanning does not include nested subdirectories.

For paths containing spaces, use double quotation marks:

```powershell
java -jar build\libs\seqspectre-all.jar -i "C:\Sequencing Data\sample.fastq"
```

## 10. Specify an Output File

Default:

```text
results.tsv
```

Custom output:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq -o reports/qc_results.tsv
```

The application attempts to create missing parent directories.

Existing output files with the same name are overwritten.

## 11. Run Through Gradle

During development, SeqSpectre can run without manually rebuilding the executable JAR.

### Windows

```powershell
.\gradlew.bat run --args="-i testdata/test.fastq"
```

### macOS/Linux

```bash
./gradlew run --args="-i testdata/test.fastq"
```

Additional options can be supplied:

```powershell
.\gradlew.bat run --args="-i testdata/test.fastq --quality --duplicates --gc"
```

The Gradle `run` task forwards standard input to the application, enabling terminal interaction when an interactive feature is used.

## 12. Run Automated Tests

Windows:

```powershell
.\gradlew.bat test
```

macOS/Linux:

```bash
./gradlew test
```

Gradle normally writes the HTML test report to:

```text
build/reports/tests/test/index.html
```

## 13. Troubleshooting

### Java not recognized

Verify:

```bash
java -version
```

Install a compatible JDK if necessary.

### Gradle cannot start

Check the Java environment and Gradle version:

```powershell
.\gradlew.bat --version
```

### Executable JAR missing

Rebuild:

```powershell
.\gradlew.bat shadowJar
```

### Input file not found

Ensure the file path exists and is relative to the current working directory, or use an absolute path.

### No FASTQ files found

Verify the extension and location of the input files.

### Output file missing

Check the terminal for errors and confirm that the selected output directory is writable.

For detailed solutions, see the [Troubleshooting Guide](troubleshooting.md).

---

## Further Documentation

- [User Guide](user-guide.md)
- [CLI Command Reference](commands.md)
- [Developer Guide](developer-guide.md)
- [Troubleshooting](troubleshooting.md)
- [Contact & Support](contact.md)

[Back to SeqSpectre Home](../README.md)
