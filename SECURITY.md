# Security Policy

## Project Status

This is an **academic project** for the Planning and Automated Reasoning course at the University of Verona. The project has been completed and submitted as of January 2026.

## Purpose

This solver is designed for **educational and research purposes only**. It implements decision procedures for satisfiability checking in formal verification contexts.

## Supported Versions

| Version | Status | Support |
| ------- | ------ | ------- |
| v3.0.0  | ✅ Complete | Final submission version |
| v2.0.0  | 📦 Archive | Previous milestone |
| v1.0.0  | 📦 Archive | Initial release |

## Reporting a Vulnerability

As this is an academic project that has been completed and submitted, we are not actively maintaining or patching vulnerabilities. However, if you discover a critical security issue, you can:

1. **Open an Issue**: Create a GitHub issue describing the vulnerability
2. **Contact**: Email the author at jacopo.parretti@studenti.univr.it

### What to Include

When reporting a vulnerability, please include:

- **Description**: Clear description of the vulnerability
- **Impact**: Potential security impact
- **Reproduction**: Steps to reproduce the issue
- **Environment**: Java version, OS, and other relevant details

## Security Considerations

### Input Validation

The solver accepts user-provided input in two formats:
- Custom format (text-based literals)
- SMT-LIB 2.0 format

**Known Limitations:**
- No input size limits enforced (could lead to memory exhaustion on very large inputs)
- No protection against malformed input causing excessive computation
- Not designed for production use or untrusted input

### Recommended Usage

- ✅ **DO**: Use for educational purposes and formal verification research
- ✅ **DO**: Test with trusted input files
- ✅ **DO**: Run with appropriate JVM memory limits (`-Xmx`)
- ❌ **DON'T**: Use in production systems without thorough security review
- ❌ **DON'T**: Process untrusted input without sandboxing
- ❌ **DON'T**: Expose as a web service without proper authentication and rate limiting

## Dependencies

This project uses Maven for dependency management. All dependencies are pulled from Maven Central:

- **JUnit 5**: Testing framework (test scope only)
- No runtime dependencies outside Java Standard Library

### Keeping Dependencies Updated

To check for dependency updates:

```bash
mvn versions:display-dependency-updates
```

## Academic Integrity

This project was developed as original coursework for academic credit. It should not be copied or submitted as coursework by other students. See [LICENSE](LICENSE) for usage terms.

## Disclaimer

This software is provided "as is" without warranty of any kind. The author assumes no liability for any damages resulting from the use of this software.

---

**Last Updated:** January 5, 2026
**Project Status:** Academic Assignment - Completed
