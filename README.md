<div align="center">

<img src="docs/images/seqspectre-logo.jpg" alt="SeqSpectre logo" width="280">

# SeqSpectre

### Sequencing Quality Control

**Analyse sequencing data with confidence.**

A Java command-line application for FASTQ sequencing quality control, sequence analysis and TSV reporting.

![Java](https://img.shields.io/badge/Java-26.0.2.1-007396)
![Gradle](https://img.shields.io/badge/Gradle-9.7.1-02303A)
![FASTQ](https://img.shields.io/badge/FASTQ-Supported-16A6A0)
![Platform](https://img.shields.io/badge/Tested-Windows_11-0078D4)

---

### Documentation

[**Home**](README.md) •
[**Installation**](docs/installation.md) •
[**User Guide**](docs/user-guide.md) •
[**CLI Commands**](docs/commands.md) •
[**Developer Guide**](docs/developer-guide.md) •
[**Contact & Support**](docs/contact.md)

</div>

---

## Welcome to SeqSpectre

SeqSpectre is a Java-based bioinformatics application developed to perform quality control on FASTQ sequencing data.

The application analyses sequencing reads, evaluates base-call quality at both read ends, identifies duplicate sequences and generates tab-separated QC reports.

Users can analyse a single FASTQ file or multiple FASTQ files located within one directory.

### Key Features

- **FASTQ input** — Support for `.fastq`, `.fq`, `.fastq.gz` and `.fq.gz` files.
- **Read-end quality** — Analyse a configurable percentage of bases at the beginning and end of reads.
- **Duplicate detection** — Calculate the percentage of exact duplicate sequences.
- **GC content** — Calculate the percentage of G and C nucleotides.
- **Sequence length** — Report minimum and maximum read lengths.
- **Length filtering** — Exclude reads shorter than a configurable minimum.
- **Read counting** — Report the total number of reads in the original FASTQ file.
- **Configurable thresholds** — Adjust Phred quality, passing-read and duplicate requirements.
- **PASS/FAIL classification** — Evaluate samples against configured QC requirements.
- **TSV reporting** — Display results in the terminal and save them as tab-separated values.
- **Customizable output** — Select individual QC measurements to include in reports.

---

## Quick Start

### Requirements

The application has been executed successfully using the following development environment:

| Component | Tested version |
|---|---|
| Java | OpenJDK 26.0.2.1 |
| Gradle | 9.7.1 |
| Operating system | Windows 11 (64-bit) |
| Gradle Launcher JVM | 26.0.2.1 |
| Gradle Daemon JVM | 26.0.2.1 |

A compatible Java Development Kit is required to build the application.

The Gradle Wrapper is included, so Gradle does not need to be installed separately.

Compatibility with older Java versions or other operating systems has not been established through project-specific testing.

### 1. Download the project

```bash
git clone https://github.com/Bio-informatics-student/Java_groep_4.git
cd Java_groep_4
```

Alternatively, select **Code → Download ZIP** on GitHub and extract the repository.

### 2. Build the application

**Windows:**

```powershell
.\gradlew.bat build
.\gradlew.bat shadowJar
```

**macOS/Linux:**

```bash
./gradlew build
./gradlew shadowJar
```

The executable JAR is generated at:

```text
build/libs/seqspectre-all.jar
```

### 3. Run your first analysis

The repository includes `testdata/test.fastq`, containing four sequencing reads.

**Windows:**

```powershell
java -jar build\libs\seqspectre-all.jar -i testdata\test.fastq
```

**macOS/Linux:**

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq
```

SeqSpectre processes the input and writes the results to `results.tsv`, unless another output path is specified.

For detailed instructions, see the [Installation Guide](docs/installation.md).

---

## Example QC Results

The included example FASTQ file contains four reads of 16 bases each.

With the default analysis settings, the calculated results are:

| QC Measurement | Result |
|---|---|
| Sample | `test.fastq` |
| Type | FASTQ |
| Total Reads | 4 |
| Beginning Quality | 100.00% |
| Ending Quality | 100.00% |
| Duplicates | 0.00% |
| GC Percentage | 43.75% |
| Minimum Length | 16 |
| Maximum Length | 16 |
| Passing Reads | 100.00% |
| Final Status | **PASS** |

The values above reflect the current calculation and output-formatting logic. The committed `results.tsv` files may contain output from an older application version.

**Important:** Four sequencing reads are sufficient for demonstrating the application but are not representative of a complete sequencing experiment.

See the [User Guide](docs/user-guide.md) for an explanation of the results.

---

## Documentation

<details>
<summary><b>📚 Open Documentation Overview</b></summary>

### Getting Started

- [Installation Guide](docs/installation.md) — Requirements, installation, building and running SeqSpectre.
- [User Guide](docs/user-guide.md) — Analysing FASTQ files, configuring quality control and interpreting results.
- [CLI Command Reference](docs/commands.md) — Supported command-line arguments, accepted values and examples.

### Development

- [Developer Guide](docs/developer-guide.md) — Architecture, Java classes, algorithms, dependencies and testing.
- [Troubleshooting Guide](docs/troubleshooting.md) — Common errors and recommended solutions.
- [UML Diagram](SeqSpectre_UML.puml) — Project UML source.

### Support & Feedback

- [Contact & Support](docs/contact.md) — Questions, bug reports and improvement suggestions.
- [GitHub Issues](https://github.com/Bio-informatics-student/Java_groep_4/issues) — Report a problem or request a feature.

</details>

---

## Understanding the QC Analysis

SeqSpectre performs the following analyses:

| Analysis | Description |
|---|---|
| Total Reads | Counts FASTQ records before optional length filtering |
| Beginning Quality | Percentage of selected bases at read beginnings meeting the Phred threshold |
| Ending Quality | Percentage of selected bases at read ends meeting the Phred threshold |
| Duplicates | Percentage of repeated, identical read sequences |
| GC Percentage | Percentage of G and C bases across analysed reads |
| Minimum Length | Shortest analysed read length after optional filtering |
| Maximum Length | Longest original read length |
| Passing Reads | Percentage of analysed reads passing the quality requirements at both ends |

When no minimum length is specified, the minimum and maximum lengths are calculated from the same original dataset.

When `--min-length` is specified, shorter reads are excluded from the subsequent QC calculations. The minimum length is updated using the remaining reads, while the maximum length remains the value calculated from the original file.

The final `PASS` or `FAIL` classification depends on the required passing-read percentage and maximum allowed duplicate percentage.

For detailed mathematical explanations, see the [Developer Guide](docs/developer-guide.md).

---

## Technologies

| Technology | Version | Purpose |
|---|---|---|
| Java / OpenJDK | 26.0.2.1 (tested) | Application development and execution |
| Gradle | 9.7.1 | Build automation |
| PicoCLI | 4.7.7 | Command-line argument parsing |
| JUnit Jupiter | 5.11.4 | Automated testing |
| Gradle Shadow plugin | 9.6.1 | Executable JAR packaging |

The complete dependency configuration is available in `build.gradle`.

---

## Project Structure

```text
Java_groep_4/
├── README.md
├── docs/
│   ├── images/
│   │   └── seqspectre-logo.jpg.jpg
│   ├── installation.md
│   ├── user-guide.md
│   ├── commands.md
│   ├── developer-guide.md
│   ├── troubleshooting.md
│   └── contact.md
├── src/
│   ├── main/java/
│   │   ├── Main.java
│   │   ├── input/
│   │   ├── processing/
│   │   └── output/
│   └── test/java/
│       ├── input/
│       ├── processing/
│       └── output/
├── testdata/
│   └── test.fastq
├── SeqSpectre_UML.puml
├── build.gradle
├── settings.gradle
├── gradle/
├── gradlew
└── gradlew.bat
```

---

## Current Limitations

- The complete analysis pipeline currently supports FASTQ input only.
- SAM, BAM and FastQC reader implementations are not yet available.
- FASTQ directory scanning is not recursive.
- FASTQ records are loaded into memory before analysis.
- Duplicate detection identifies exact sequence matches only.
- GC content and sequence length do not independently determine PASS/FAIL.
- The command-line application is internally named `qc-tool`.
- A formal application release version has not been configured.

---

## Contact & Support

Questions, bugs and improvement suggestions can be submitted using the project's GitHub Issues page.

[Contact & Support Guide](docs/contact.md) · [GitHub Issues](https://github.com/Bio-informatics-student/Java_groep_4/issues)

---

## Development Team

SeqSpectre was developed as an educational software development project within the **Bioinformatics programme at Hanze University of Applied Sciences**.

---

<div align="center">

### SeqSpectre

**Sequencing Quality Control**

*Bioinformatics | Hanze University of Applied Sciences*

[Home](README.md) • [Documentation](#documentation) • [Contact & Support](docs/contact.md)

</div>