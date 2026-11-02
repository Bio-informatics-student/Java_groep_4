# Troubleshooting Guide

This guide helps resolve common problems when installing, running or using SeqSpectre.

[Home](../README.md) · [Installation](installation.md) · [User Guide](user-guide.md) · [CLI Commands](commands.md) · [Developer Guide](developer-guide.md)

## 1. Installation problems

### Java is not recognized

**Problem**

The terminal displays:

```text
'java' is not recognized as an internal or external command
```

Or:

```text
command not found: java
```

**Possible cause**

Java is not installed or is not available through the system's PATH environment variable.

**Solution**

Check the Java installation:

```bash
java -version
```

Check the Java compiler:

```bash
javac -version
```

If these commands fail, install a compatible JDK and reopen your terminal.

If using IntelliJ IDEA, also verify that the Gradle JVM is configured correctly.

### Gradle Wrapper does not start

**Windows**

Make sure you are in the root directory of the project.

Run:

```powershell
.\gradlew.bat build
```

**macOS/Linux**

If the terminal displays a permission error, execute:

```bash
chmod +x gradlew
```

Then:

```bash
./gradlew build
```

### Gradle cannot download dependencies

**Possible causes**

- No internet connection.
- Firewall restrictions.
- Proxy configuration problems.
- Temporary unavailability of a dependency repository.

**Solution**

Verify your internet connection and retry the build.

The first build may need to download Gradle and the application's dependencies.

### Incompatible Java version

Check which Java version is installed:

```bash
java -version
```

Check which JVM Gradle uses:

```powershell
.\gradlew.bat --version
```

Use a JDK compatible with the Gradle version included in the repository.

### Unable to access JAR file

**Problem**

```text
Error: Unable to access jarfile
```

**Possible cause**

The executable JAR does not exist or the specified path is incorrect.

**Solution**

Build the executable JAR:

```powershell
.\gradlew.bat shadowJar
```

Check whether this file exists:

```text
build/libs/seqspectre-all.jar
```

Run the application again.

## 2. Command-line problems

### Missing input argument

**Problem**

SeqSpectre reports that the required input option is missing.

**Solution**

Supply a FASTQ file or directory using `-i` or `--input`.

Example:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq
```

### Unknown command-line option

**Possible cause**

The argument is misspelled or is not supported.

**Solution**

Display all available options:

```bash
java -jar build/libs/seqspectre-all.jar --help
```

Check the spelling of each argument.

For example:

```text
--end-percent 20
```

requires a numerical value.

However:

```text
--quality
```

is a flag and does not require a value.

### File paths containing spaces

Paths containing spaces must be placed between double quotation marks.

Windows example:

```powershell
java -jar build\libs\seqspectre-all.jar -i "C:\Sequencing data\test.fastq"
```

### Invalid numerical settings

SeqSpectre validates the allowed ranges for numerical arguments.

| Option | Valid range |
|---|---|
| `--end-percent` | 1–50 |
| `--required-reads-percent` | 0–100 |
| `--max-duplicate-percent` | 0–100 |
| `--min-quality-score` | Integer 0–93 |
| `--min-length` | Integer of at least 1 |

For fractional percentages, use a decimal point:

```text
--end-percent 12.5
```

Do not use:

```text
--end-percent 12,5
```

Invalid values are rejected.

## 3. Input file problems

### Input path does not exist

**Problem**

```text
Input path does not exist
```

**Possible cause**

The specified file or directory cannot be found.

**Solution**

Check the spelling and location of the input path.

When starting the program from the repository root, the example FASTQ file is:

```text
testdata/test.fastq
```

### No FASTQ files found

**Problem**

```text
No FASTQ files found in:
```

**Possible causes**

- The directory is empty.
- The directory contains no supported FASTQ files.
- The FASTQ files are located inside nested subdirectories.
- An unsupported file format was supplied.

**Solution**

Make sure the directory directly contains at least one supported file.

Supported extensions:

```text
.fastq
.fq
.fastq.gz
.fq.gz
```

SAM, BAM and FastQC files are not currently processed by the command-line workflow.

### Invalid FASTQ header

**Problem**

```text
Invalid FASTQ header
```

**Possible cause**

A FASTQ record does not start with `@`.

**Solution**

Check the first line of the affected record.

Example:

```text
@read1
```

### Invalid FASTQ separator

**Problem**

```text
Invalid FASTQ separator
```

**Possible cause**

The third line of a FASTQ record does not start with `+`.

**Solution**

Check the FASTQ structure:

```text
@read1
ACGTACGT
+
IIIIIIII
```

### Incomplete FASTQ record

**Problem**

```text
Incomplete FASTQ record
```

**Possible cause**

The file ends before a complete four-line FASTQ record has been read.

**Solution**

Check whether the file was truncated or contains missing lines.

Every record must contain exactly four lines.

### Sequence and quality lengths differ

**Problem**

```text
Sequence and quality lengths differ
```

**Possible cause**

The nucleotide sequence and corresponding quality string contain different numbers of characters.

**Solution**

Check the second and fourth lines of the FASTQ record.

Every base must have one corresponding quality character.

### Invalid Phred+33 character

**Problem**

```text
Invalid Phred+33 character
```

**Possible cause**

A quality string contains a character outside the accepted Phred+33 range.

**Solution**

Verify that the FASTQ file uses Phred+33 encoding and has not been corrupted.

The accepted quality character range is printable ASCII 33–126.

### FASTQ file is empty

**Problem**

```text
FASTQ file is empty
```

**Solution**

Supply a FASTQ file containing at least one complete record.

### Compressed FASTQ cannot be processed

**Possible cause**

The file is not a valid gzip-compressed FASTQ file, even though its filename ends in `.gz`.

**Solution**

Check that the file is correctly compressed and not damaged.

Renaming a regular FASTQ file to `.fastq.gz` does not compress the file.

## 4. Filtering problems

### No reads meet the required minimum length

**Problem**

```text
No reads meet the required minimum length
```

**Possible cause**

All reads are shorter than the value supplied using `--min-length`.

**Example**

The repository's `test.fastq` contains four reads of 16 bases each.

This command works:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --min-length 16
```

This command removes all four reads:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --min-length 100
```

**Solution**

Choose a lower minimum length or supply a dataset containing longer reads.

## 5. Understanding unexpected results

### Why does my sample receive FAIL?

The final status depends on two requirements:

```text
Passing Reads >= required-reads-percent
AND
Duplicates <= max-duplicate-percent
```

If either condition is not met, the sample receives `FAIL`.

A sample may have high average quality values but still fail if too few individual reads pass both end-quality requirements.

### Why are Beginning Quality and Passing Reads different?

`Beginning Quality` describes the percentage of selected bases at the beginning of reads that meet the quality threshold.

`Passing Reads` describes the percentage of complete reads that pass the required quality checks at both ends.

These percentages represent different measurements and do not necessarily have the same value.

### Why do sequence lengths remain unchanged after filtering?

Minimum and maximum sequence lengths are calculated before the optional minimum-length filter is applied.

The original lengths are retained in the report.

Other QC measurements are calculated using the reads remaining after filtering.

### Why are some columns missing?

If one or more display flags are provided, SeqSpectre shows only the selected measurement columns.

For example:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --gc
```

shows GC percentage alongside the mandatory sample, type and final status columns.

To display all available measurements, do not specify any of the five display flags.

### Why is the percentage below minimum length missing?

The `% Reads Below Minimum Length` column is displayed only when a minimum length is specified and length statistics are included in the report.

Example:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --min-length 12 --length
```

## 6. Output problems

### Output path points to a directory

**Problem**

```text
Output path points to a directory, not a file
```

**Solution**

Specify an output filename rather than only a directory.

Example:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq -o reports/results.tsv
```

### Could not create output directory

**Possible cause**

The application does not have permission to create the selected directory.

**Solution**

Choose an output location where you have write permission.

Try placing the output inside your project directory.

### Cannot find results.tsv

By default, SeqSpectre writes `results.tsv` to the current working directory.

This is not necessarily the directory containing the FASTQ input.

Specify an explicit output path when necessary:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq -o reports/results.tsv
```

### Previous results have disappeared

SeqSpectre overwrites an existing output file when the same path is used.

To preserve previous reports, provide a different output filename for each analysis.

## 7. Diagnostic checklist

Before reporting a problem, verify the following:

1. Java is installed and recognized by the terminal.
2. The terminal is opened in the correct directory.
3. The application has been compiled successfully.
4. The executable JAR exists.
5. The input path is valid.
6. The FASTQ file has a supported extension.
7. The FASTQ records contain valid sequence and quality information.
8. Numerical arguments are within their permitted ranges.
9. The output directory is writable.
10. The problem can be reproduced using `testdata/test.fastq`.

## 8. Reporting issues

If a problem cannot be resolved, it can be reported through the project's GitHub repository.

[SeqSpectre GitHub Repository](https://github.com/Bio-informatics-student/Java_groep_4)

When reporting a problem, include:

- Operating system.
- Java version.
- Command used to run SeqSpectre.
- Relevant error message.
- Expected behaviour.
- Actual behaviour.
- Steps required to reproduce the problem.

If possible, include a small example FASTQ file containing non-sensitive test data.

Do not publish confidential or identifiable sequencing data.

---

## Further documentation

- [Installation Guide](installation.md)
- [User Guide](user-guide.md)
- [CLI Command Reference](commands.md)
- [Developer Guide](developer-guide.md)

[Back to SeqSpectre Home](../README.md)
