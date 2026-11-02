# Installation Guide

This guide explains how to download, build and run SeqSpectre on your computer.

SeqSpectre is a Java command-line application for quality control of FASTQ sequencing data.

## 1. System requirements

Before installing SeqSpectre, make sure your computer meets the following requirements.

| Software | Requirement | Purpose |
|---|---|---|
| Java Development Kit (JDK) | Java 17 or newer, compatible with the included Gradle version | Compile and run the application |
| Git | Recommended | Download the repository using Git |
| Gradle | Included (Gradle Wrapper 9.7.1) | Build the application |
| Operating system | Windows, macOS or Linux | Run the application |

**Note:** A separate Gradle installation is not required. SeqSpectre includes the Gradle Wrapper, which automatically downloads the required Gradle version.

## 2. Verify Java installation

Open a terminal.

**Windows:** Open PowerShell or Command Prompt.

**macOS/Linux:** Open Terminal.

Check whether Java is installed:

```bash
java -version
```

Check whether the Java compiler is available:

```bash
javac -version
```

Both commands should display version information.

If Java is not installed, download a suitable JDK from [Eclipse Temurin](https://adoptium.net/) and follow the installation instructions for your operating system.

After installation, reopen your terminal and verify the installation again.

## 3. Download SeqSpectre

### Option A: Clone using Git (recommended)

Open a terminal and enter:

```bash
git clone https://github.com/Bio-informatics-student/Java_groep_4.git
```

Navigate to the project directory:

```bash
cd Java_groep_4
```

### Option B: Download ZIP

If you do not have Git installed:

1. Open the [SeqSpectre GitHub repository](https://github.com/Bio-informatics-student/Java_groep_4).
2. Click the green **Code** button.
3. Select **Download ZIP**.
4. Extract the ZIP file.
5. Open a terminal in the extracted project directory.

Make sure your current directory contains the following files:

```text
Java_groep_4/
├── build.gradle
├── gradlew
├── gradlew.bat
├── settings.gradle
└── src/
```

## 4. Build SeqSpectre

SeqSpectre uses Gradle to compile the Java application and manage dependencies.

### Windows

From the project directory, run:

```powershell
.\gradlew.bat build
```

### macOS/Linux

Run:

```bash
./gradlew build
```

If the Gradle Wrapper does not have execution permission, run:

```bash
chmod +x gradlew
```

Then retry the build command.

Gradle will download the required dependencies when necessary, compile the application and execute the automated tests.

A successful build ends with a message similar to:

```text
BUILD SUCCESSFUL
```

An internet connection may be required for the initial build to download dependencies.

## 5. Create the executable JAR

To create a standalone executable JAR containing the required application dependencies, use the Gradle Shadow plugin.

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

The JAR contains SeqSpectre and its required runtime dependencies.

## 6. Verify the installation

Check whether the application can start by displaying its command-line help.

### Windows

```powershell
java -jar build\libs\seqspectre-all.jar --help
```

### macOS/Linux

```bash
java -jar build/libs/seqspectre-all.jar --help
```

The application should display its available command-line options.

If the help information is displayed, the application has been built and can be started.

## 7. Run your first analysis

SeqSpectre requires a FASTQ input file or a directory containing FASTQ files.

The repository contains a `testdata` directory that can be used for development and testing.

Run the application using:

### Windows

```powershell
java -jar build\libs\seqspectre-all.jar -i testdata\test.fastq
```

### macOS/Linux

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq
```

### Example FASTQ file

The repository includes a small example FASTQ file named `test.fastq`, located in the `testdata` directory.

This file contains **4 sequencing reads** and can be used to verify that SeqSpectre processes FASTQ input correctly.

Running SeqSpectre with this file will:

1. Read the four FASTQ records.
2. Perform the configured quality control analyses.
3. Display the QC results in the terminal.
4. Generate a `results.tsv` output file.

This example is intended as a basic functional test, not as a representative biological dataset.

SeqSpectre searches for supported FASTQ files directly inside the specified directory.

When processing succeeds, the application displays QC results in the terminal and writes them to:

```text
results.tsv
```

You can also analyse your own FASTQ file:

```bash
java -jar build/libs/seqspectre-all.jar -i sample.fastq
```

Replace `sample.fastq` with the path to your FASTQ file.

If a file or directory path contains spaces, enclose the path in double quotation marks.

Example:

```bash
java -jar build/libs/seqspectre-all.jar -i "my sequencing data/sample.fastq"
```

## 8. Specify an output file

By default, SeqSpectre writes the results to `results.tsv` in the current working directory.

To specify another output location, use `-o` or `--output`.

Example:

```bash
java -jar build/libs/seqspectre-all.jar -i sample.fastq -o output/qc_results.tsv
```

If the output directory does not exist, SeqSpectre attempts to create it automatically.

Existing output files with the same path are overwritten.

## 9. Run SeqSpectre without building a JAR

Developers can run SeqSpectre directly through Gradle.

### Windows

```powershell
.\gradlew.bat run --args="-i testdata"
```

### macOS/Linux

```bash
./gradlew run --args="-i testdata"
```

This is useful during development because Gradle compiles and starts the application directly.

Additional command-line options can be included inside `--args`.

Example:

```powershell
.\gradlew.bat run --args="-i testdata --quality --duplicates --gc"
```

## 10. Troubleshooting installation

### Java is not recognized

If the terminal displays a message such as:

```text
'java' is not recognized as an internal or external command
```

Java may not be installed or the Java executable may not be available through the system's `PATH` environment variable.

Install a compatible JDK and verify the Java installation.

### Permission denied when running Gradle

On macOS/Linux, execute:

```bash
chmod +x gradlew
```

Then run the Gradle command again.

### Unable to access JAR file

If Java reports:

```text
Error: Unable to access jarfile
```

Check whether the JAR exists in:

```text
build/libs/seqspectre-all.jar
```

If it does not exist, run the `shadowJar` task again.

### No FASTQ files found

Make sure the input path contains at least one supported FASTQ file.

Supported extensions:

- `.fastq`
- `.fq`
- `.fastq.gz`
- `.fq.gz`

SeqSpectre does not recursively scan nested directories for FASTQ files.

### Build fails

Make sure:

- a compatible JDK is installed;
- the terminal is opened in the project directory;
- an internet connection is available if dependencies need to be downloaded;
- the full Gradle error message is checked to identify the failing task.

For more information, see the [Troubleshooting Guide](troubleshooting.md).

---

## Next steps

SeqSpectre is now ready to be used.

Continue with the following documentation:

- [User Guide](user-guide.md) — Learn how to analyse sequencing data.
- [CLI Commands](commands.md) — View all command-line options and allowed values.
- [Developer Guide](developer-guide.md) — Learn about the project structure and development workflow.

[Back to SeqSpectre Home](../README.md)
