
<div align="center">

# 🧬 SeqSpectre

### Sequencing Quality Control

**Analyse sequencing data with confidence.**

A Java-based command-line application for analysing FASTQ sequencing data, evaluating read quality and generating QC reports.

![Java](https://img.shields.io/badge/Java-Programming-007396)
![Gradle](https://img.shields.io/badge/Gradle-Build-02303A)
![FASTQ](https://img.shields.io/badge/FASTQ-Supported-16A6A0)

---

### Documentation

[**Home**](README.md) •
[**Installation**](docs/installation.md) •
[**User Guide**](docs/user-guide.md) •
[**CLI Commands**](docs/commands.md) •
[**Developer Guide**](docs/developer-guide.md)

</div>

---

## Welcome to SeqSpectre

SeqSpectre is a bioinformatics tool designed to perform quality control on sequencing reads.

### Features

- FASTQ and compressed FASTQ input
- Read-end Phred quality analysis
- Duplicate sequence detection
- GC percentage calculation
- Sequence length analysis and filtering
- Configurable QC thresholds
- PASS/FAIL classification
- TSV report generation

## Quick Start

Build the application:

```bash
./gradlew shadowJar
```

Run a QC analysis:

```bash
java -jar build/libs/seqspectre-all.jar -i sample.fastq
```

For Windows, use `.\gradlew.bat shadowJar` to build the application.

## Documentation

<details>
<summary><b>📚 Open documentation overview</b></summary>

### Getting Started

- [Installation Instructions](docs/installation.md)
- [User Manual](docs/user-guide.md)
- [Command Reference](docs/commands.md)

### Development

- [Developer Guide](docs/developer-guide.md)
- [Troubleshooting](docs/troubleshooting.md)

</details>

---

<div align="center">

**SeqSpectre — Bioinformatics | Hanze University of Applied Sciences**

</div>
