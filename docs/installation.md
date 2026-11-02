# Installation Guide

This guide explains how to download, install, build and run SeqSpectre.

SeqSpectre is a Java command-line application designed for quality control of FASTQ sequencing data.

[Home](../README.md) · [User Guide](user-guide.md) · [CLI Commands](commands.md) · [Developer Guide](developer-guide.md) · [Contact & Support](contact.md)

---

## 1. System Requirements

SeqSpectre requires a Java Development Kit (JDK) and a terminal or command-line environment.

### Verified development environment

SeqSpectre has been developed and successfully executed using the following environment:

| Component | Version |
|---|---|
| Java | OpenJDK 26.0.2.1 |
| Java runtime build | 26.0.2.1+1-7 |
| Gradle | 9.7.1 |
| Gradle Launcher JVM | 26.0.2.1 |
| Gradle Daemon JVM | 26.0.2.1 |
| Operating system | Windows 11 (64-bit) |

**Important:** Java 26.0.2.1 is the verified development version, not necessarily the minimum supported Java version. Compatibility with earlier Java versions or other operating systems has not been independently verified.

### Required software

- **Java Development Kit (JDK)** — Required to compile and run SeqSpectre.
- **Git (recommended)** — Used to clone the repository.
- **Terminal** — PowerShell, Command Prompt, macOS Terminal or a Linux shell.
- **Gradle Wrapper (included)** — Automatically downloads the Gradle version required by the project.

A separate Gradle installation is not required.

An internet connection may be necessary for the first build to download Gradle and other dependencies.

---

## 2. Verify Java Installation

Open a terminal.

On Windows, you can use PowerShell or the terminal integrated into IntelliJ IDEA.

Check the installed Java version:

```bash
java -version
```

The verified development environment displays:

```text
openjdk version "26.0.2.1" 2026-08-18
OpenJDK Runtime Environment (build 26.0.2.1+1-7)
OpenJDK 64-Bit Server VM (build 26.0.2.1+1-7, mixed mode, sharing)
```

Check whether the Java compiler is available:

```bash
javac -version
```

If Java is not installed, install a suitable JDK.

An OpenJDK distribution can be obtained from:

https://adoptium.net/

After installing the JDK, restart the terminal and verify the installation again.

---

## 3. Download SeqSpectre

There are two ways to obtain the project.

### Option A — Clone with Git

This is the recommended method for developers.

Run:

```bash
git clone https://github.com/Bio-informatics-student/Java_groep_4.git
```

Navigate to the project:

```bash
cd Java_groep_4
```

### Option B — Download ZIP

If Git is unavailable:

1. Open the [SeqSpectre repository](https://github.com/Bio-informatics-student/Java_groep_4).
2. Click the green **Code** button.
3. Select **Download ZIP**.
4. Extract the downloaded archive.
5. Open a terminal inside the extracted project directory.

The project directory should contain the following files:

```text
Java_groep_4/
├── README.md
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
├── src/
├── testdata/
└── docs/
```

---

## 4. Verify Gradle

SeqSpectre uses the Gradle Wrapper.

The wrapper configuration ensures that the correct Gradle version is used when building the application.

### Windows

```powershell
.\gradlew.bat --version
```

### macOS/Linux

```bash
./gradlew --version
```

The expected Gradle version is:

```text
Gradle 9.7.1
```

In the verified Windows 11 development environment, Gradle reported:

```text
Gradle 9.7.1

Launcher JVM: 26.0.2.1
Daemon JVM:   OpenJDK 26.0.2.1
OS:           Windows 11
```

The Java version shown by Gradle should be compatible with the application.

**Note:** The Gradle JVM may differ from the Java version displayed by `java -version`, depending on the system configuration.

---

## 5. Build the Application

SeqSpectre uses Gradle for compiling the source code and managing dependencies.

Open a terminal in the root of the project.

### Windows

```powershell
.\gradlew.bat build
```

### macOS/Linux

```bash
./gradlew build
```

If necessary, make the Gradle Wrapper executable:

```bash
chmod +x gradlew
```

Then retry the command.

The build process:

1. Downloads required dependencies if necessary.
2. Compiles the Java source code.
3. Compiles and executes the automated tests.
4. Creates the standard build outputs.

A successful build ends with:

```text
BUILD SUCCESSFUL
```

If the build fails, read the Gradle error message or see the [Troubleshooting Guide](troubleshooting.md).

---

## 6. Create the Executable JAR

SeqSpectre uses the Gradle Shadow plugin to package the application and its runtime dependencies into a single executable JAR.

### Windows

```powershell
.\gradlew.bat shadowJar
```

### macOS/Linux

```bash
./gradlew shadowJar
```

After a successful build, the executable JAR is located at:

```text
build/libs/seqspectre-all.jar
```

This file can be executed using Java.

A successful `shadowJar` build does not automatically execute the full test suite. Run the `test` or `build` task separately when tests need to be verified.

---

## 7. Verify the Installation

To check whether SeqSpectre starts correctly, display the command-line help.

### Windows

```powershell
java -jar build\libs\seqspectre-all.jar --help
```

### macOS/Linux

```bash
java -jar build/libs/seqspectre-all.jar --help
```

The application should display its supported command-line options.

These include:

```text
--input
--output
--end-percent
--required-reads-percent
--max-duplicate-percent
--min-quality-score
--quality
--duplicates
--gc
--passing-reads
--length
--min-length
--help
--version
```

The options `--input` and `--output` also support the short forms `-i` and `-o`.

For a complete description, see the [CLI Command Reference](commands.md).

---

## 8. Run Your First Analysis

The repository includes a test FASTQ file:

```text
testdata/test.fastq
```

This file contains four sequencing reads.

It can be used to verify that SeqSpectre processes FASTQ input and generates QC results.

### Windows

```powershell
java -jar build\libs\seqspectre-all.jar -i testdata\test.fastq
```

### macOS/Linux

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq
```

SeqSpectre will:

1. Read the FASTQ file.
2. Validate its records.
3. Analyse read-end sequencing quality.
4. Calculate duplicate percentage.
5. Calculate GC percentage.
6. Determine minimum and maximum sequence length.
7. Calculate the percentage of passing reads.
8. Determine the final PASS/FAIL status.
9. Display results in the terminal.
10. Save results to a TSV file.

The default output file is:

```text
results.tsv
```

### Verified example results

The following results were obtained from an actual execution using the included example file and default settings:

| Measurement | Result |
|---|---|
| Sample | test.fastq |
| Type | FASTQ |
| Beginning Quality | 100.0% |
| Ending Quality | 100.0% |
| Duplicates | 0.0% |
| GC Percentage | 43.75% |
| Minimum Length | 16 |
| Maximum Length | 16 |
| Passing Reads | 100.0% |
| Final Status | PASS |

The four reads are each 16 bases long.

This example demonstrates that the application can process input and generate output in the verified development environment.

It is not intended to represent a full biological sequencing dataset.

---

## 9. Analyse Your Own FASTQ File

To analyse another file, replace the example path with your own FASTQ path.

Example:

```bash
java -jar build/libs/seqspectre-all.jar -i sample.fastq
```

Compressed FASTQ files are also supported:

```bash
java -jar build/libs/seqspectre-all.jar -i sample.fastq.gz
```

Supported filename extensions:

- `.fastq`
- `.fq`
- `.fastq.gz`
- `.fq.gz`

If a file path contains spaces, enclose the entire path in double quotation marks.

### Windows example

```powershell
java -jar build\libs\seqspectre-all.jar -i "C:\Sequencing Data\sample.fastq"
```

---

## 10. Analyse a Directory

SeqSpectre can analyse multiple FASTQ files located directly inside a directory.

Example:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata
```

Each supported FASTQ file is processed separately.

The results are combined into one TSV report.

**Important:** Nested subdirectories are not recursively searched for FASTQ files.

---

## 11. Set a Custom Output Location

By default, SeqSpectre writes the output to:

```text
results.tsv
```

The output is saved relative to the current working directory.

Use `-o` or `--output` to specify another location.

Example:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq -o reports/qc_results.tsv
```

If the parent directory does not exist, SeqSpectre attempts to create it.

If an output file already exists at the selected location, it will be overwritten.

---

## 12. Run SeqSpectre Through Gradle

During development, SeqSpectre can be run directly using Gradle without manually building a new JAR.

### Windows

```powershell
.\gradlew.bat run --args="-i testdata/test.fastq"
```

### macOS/Linux

```bash
./gradlew run --args="-i testdata/test.fastq"
```

Command-line options can be passed through the `--args` argument.

Example:

```powershell
.\gradlew.bat run --args="-i testdata/test.fastq --quality --duplicates --gc"
```

This is useful when testing changes in IntelliJ IDEA or another development environment.

---

## 13. Run Automated Tests

SeqSpectre includes automated tests using JUnit Jupiter.

### Windows

```powershell
.\gradlew.bat test
```

### macOS/Linux

```bash
./gradlew test
```

After execution, the HTML test report is normally located at:

```text
build/reports/tests/test/index.html
```

The complete automated testing workflow is explained in the [Developer Guide](developer-guide.md).

---

## 14. Troubleshooting

### Java is not recognized

Check the installed Java version:

```bash
java -version
```

If Java is unavailable, install a JDK and verify your PATH configuration.

### Gradle fails to start

Make sure you are in the repository root and that a compatible JDK is installed.

Check the Gradle environment:

```powershell
.\gradlew.bat --version
```

### Unable to access JAR file

Rebuild the application:

```powershell
.\gradlew.bat shadowJar
```

Verify that the following file exists:

```text
build/libs/seqspectre-all.jar
```

### No FASTQ files found

Make sure the input file or directory exists and contains a supported FASTQ file.

### Invalid FASTQ data

Each FASTQ record must contain:

```text
@header
SEQUENCE
+
QUALITY
```

The sequence and quality string must have equal lengths.

### Output file not created

Check that:

- The input was successfully processed.
- The output path is valid.
- The selected directory is writable.
- No processing error occurred.

For additional support, see the [Troubleshooting Guide](troubleshooting.md).

---

## 15. Next Steps

SeqSpectre is now ready for FASTQ quality control analysis.

Continue with:

- [User Guide](user-guide.md) — Learn how to use SeqSpectre.
- [CLI Command Reference](commands.md) — Explore all available options.
- [Developer Guide](developer-guide.md) — Learn how the project is built and maintained.
- [Troubleshooting](troubleshooting.md) — Resolve common problems.
- [Contact & Support](contact.md) — Ask questions or suggest improvements.

---

[Back to SeqSpectre Home](../README.md)