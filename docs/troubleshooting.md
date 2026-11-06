# Troubleshooting Guide

This guide helps resolve common problems when installing, running or using SeqSpectre.

[Home](../README.md) · [Installation](installation.md) · [User Guide](user-guide.md) · [CLI Commands](commands.md) · [Developer Guide](developer-guide.md) · [Contact & Support](contact.md)

---

## 1. Installation Problems

### Java is not recognized

**Possible cause:** Java is not installed or is not available through the system PATH.

Check:

```bash
java -version
javac -version
```

Install a compatible JDK and reopen the terminal.

The verified development environment uses OpenJDK 26.0.2.1.

### Gradle does not start

Make sure the terminal is opened in the repository root.

Windows:

```powershell
.\gradlew.bat --version
```

macOS/Linux:

```bash
./gradlew --version
```

If permission is denied on macOS/Linux:

```bash
chmod +x gradlew
```

The included Gradle Wrapper uses version 9.7.1.

### Gradle cannot download dependencies

Possible causes:

- No internet connection.
- Firewall restrictions.
- Proxy configuration problems.
- Temporary repository availability issues.

Verify connectivity and retry the build.

### Unable to access JAR file

**Problem:**

```text
Error: Unable to access jarfile
```

**Solution:**

Build the executable JAR:

```powershell
.\gradlew.bat shadowJar
```

Verify that the generated file exists:

```text
build/libs/seqspectre-all.jar
```

## 2. Outdated Application Output

### The Total Reads column is missing

The current application includes a `Total Reads` column.

If it is missing, you may be running an older JAR or reading an older report file.

Rebuild the application:

```powershell
.\gradlew.bat clean shadowJar
```

Then execute:

```powershell
java -jar build\libs\seqspectre-all.jar -i testdata\test.fastq -o updated-results.tsv
```

The current output should include:

```text
Sample
Type
Total Reads
```

as the first three column names.

### Percentages use one decimal place

The current `OutputWriter` formats percentages to two decimal places.

Expected examples:

```text
100.00%
0.00%
43.75%
```

If your output uses `100.0%`, regenerate the report using the latest built application.

The existing `results.tsv` and `test-results.tsv` files committed to GitHub may contain output from an earlier version.

## 3. Command-Line Problems

### Missing required input

A normal analysis requires `-i` or `--input`.

Example:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq
```

### Unknown option

Check the available arguments:

```bash
java -jar build/libs/seqspectre-all.jar --help
```

Quality selection flags such as `--gc` do not require a value.

Numerical options such as `--end-percent` do require a value.

### Paths containing spaces

Enclose paths in double quotation marks.

```powershell
java -jar build\libs\seqspectre-all.jar -i "C:\Sequencing Data\sample.fastq"
```

### Invalid numerical arguments

| Argument | Allowed values |
|---|---|
| `--end-percent` | 1–50 |
| `--required-reads-percent` | 0–100 |
| `--max-duplicate-percent` | 0–100 |
| `--min-quality-score` | Integer 0–93 |
| `--min-length` | Integer ≥1 |

Use a decimal point instead of a comma for fractional percentages.

## 4. Input File Problems

### Input path does not exist

Verify the file location.

The included example is:

```text
testdata/test.fastq
```

Relative paths are resolved from the terminal's working directory.

### No FASTQ files found

Supported extensions:

```text
.fastq
.fq
.fastq.gz
.fq.gz
```

The application does not recursively search nested subdirectories.

SAM, BAM and FastQC inputs are not currently processed by the complete CLI workflow.

### Invalid FASTQ header

Every FASTQ record must begin with a header starting with `@`.

Example:

```text
@read001
```

### Invalid FASTQ separator

The third line must start with `+`.

### Incomplete FASTQ record

FASTQ records must contain four complete lines.

A truncated file may cause an error.

### Sequence and quality lengths differ

Every nucleotide must have one corresponding quality character.

For example:

```text
@read1
ACGTACGT
+
IIIIIIII
```

### Invalid Phred+33 character

The current quality analyser accepts printable ASCII characters with values from 33 through 126.

Verify the FASTQ quality encoding and check for corrupted characters.

### Compressed file cannot be read

Make sure the file is actually gzip-compressed.

Renaming a regular FASTQ file to `.fastq.gz` does not compress its contents.

## 5. Minimum Length Filtering

### All reads were removed

**Problem:**

```text
No reads meet the required minimum length
```

If every read is shorter than the specified threshold, QC processing stops.

For example, the included test file contains reads of length 16.

This works:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --min-length 16
```

This removes every read:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --min-length 100
```

Choose a lower threshold or use another dataset.

### Total Reads does not decrease after filtering

This is expected.

`Total Reads` counts the original FASTQ records before filtering.

The filtered records are used for subsequent QC calculations, but the original count remains available for reporting.

### Minimum Length changes after filtering

This is also expected.

The current processor recalculates `Minimum Length` using the reads that remain after optional length filtering.

Example:

```text
Original lengths:
50, 80, 100, 150

--min-length 90

Reported Minimum Length:
100
```

### Maximum Length does not change after filtering

The current implementation retains the original maximum read length.

Because the filter only removes reads shorter than the specified minimum, the original maximum will also remain among the retained reads whenever filtering succeeds.

### Percentage below minimum is missing

Use both `--min-length` and `--length`:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --min-length 12 --length
```

The report then includes `% Reads Below Minimum Length`.

## 6. Understanding QC Results

### Why did my sample receive FAIL?

A sample receives PASS only when:

```text
Passing Reads >= required-reads-percent

AND

Duplicates <= max-duplicate-percent
```

If either requirement is not satisfied, the final status is FAIL.

### Why are Beginning Quality and Passing Reads different?

`Beginning Quality` describes the percentage of selected bases at the beginnings of reads meeting the Phred threshold.

`Passing Reads` describes the percentage of complete analysed reads meeting the requirements at both read ends.

These are different measurements and can have different values.

### Why do I see fewer columns?

The output selection flags determine which measurement columns are included.

For example:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --gc
```

displays:

```text
Sample
Type
Total Reads
GC Percentage
Final Status
```

To display all measurements, omit the selection flags.

### Why is GC percentage not changing PASS/FAIL?

GC percentage is currently descriptive.

The final sample status depends on passing-read and duplicate thresholds only.

## 7. Output Problems

### Output path points to a directory

Use a filename rather than an existing directory.

Correct:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq -o reports/results.tsv
```

### Output directory cannot be created

Choose a location where you have write permission.

### Cannot find results.tsv

The default report is written relative to the current working directory.

Specify an explicit output path to avoid confusion:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq -o reports/results.tsv
```

### Previous results disappeared

SeqSpectre overwrites an existing output file with the same path.

Use separate filenames to preserve previous analyses.

## 8. Interactive Terminal Behaviour

The application includes a hidden developer-only option:

```text
--bart-mode
```

When this option is enabled, SeqSpectre asks an additional question after successful QC processing.

If the application appears to be awaiting user input after completing the analysis, check whether this option was supplied.

When running through Gradle, standard input is forwarded to the application using the configured `run` task.

The hidden option is not part of normal FASTQ quality control and does not change the generated QC report.

## 9. Diagnostic Checklist

Before reporting an issue, verify that:

1. Java is installed and recognized by the terminal.
2. The terminal is opened in the project directory.
3. Gradle uses a compatible JDK.
4. The latest JAR has been built.
5. The input path exists.
6. The FASTQ extension is supported.
7. The FASTQ records contain valid data.
8. Numerical arguments are within the allowed ranges.
9. The minimum-length filter leaves at least one read.
10. The output directory is writable.
11. The error can be reproduced with a small test dataset.

## 10. Reporting Issues

Report problems through the [SeqSpectre GitHub Issues page](https://github.com/Bio-informatics-student/Java_groep_4/issues).

Include:

- Operating system.
- Java version.
- Exact command used.
- Input format.
- Expected behaviour.
- Actual behaviour.
- Relevant error message.
- Steps required to reproduce the issue.

Do not upload confidential or personally identifiable sequencing data to public issues.

For more information, see the [Contact & Support Guide](contact.md).

---

[Back to SeqSpectre Home](../README.md)
