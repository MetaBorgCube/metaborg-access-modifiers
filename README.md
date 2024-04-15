# AML

Please answer the following questions concisely, either with bullet lists or short paragraphs.

Title of the submitted paper: Defining Name Accessibility using Scope Graphs
ECOOP submission number for the paper: 11

## Quick-start guide (Kick-the-tires phase)

After unzipping, the artifact can be tested using the following commands.

```bash
gunzip -c aml-docker-image.tar.gz | docker load
docker run aml:v2 ./test aml/test/test.spt
```

This should give an output roughly similar to

```
Executing test runner with command 'runTestSuite' on file/path '/home/myuser/aml/test/test.spt'.
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
TestSuite test
java##/home/myuser/aml/test/test.spt:
1 tests
parse some program : PASS
java##/home/myuser/aml/test/test.spt:

Command exited with exit code 0
This indicates that all executed tests passed.
```

Important is to validate that the test passes:
```
TestSuite test
java##/home/myuser/aml/test/test.spt:
1 tests
parse some program : PASS
java##/home/myuser/aml/test/test.spt:
```

and the build indicates it passed properly:

```
Command exited with exit code 0
This indicates that all executed tests passed.
```

## Requirements

- CPU: x86, Intel Core i7/i9 (virtualization )
- Memory: ~6GB RAM
- Disk: 32GB Hard Drive
- Software: zip, gzip, docker

Running on an ARM chips (such as Apple Silicon), possibly with virtualization software and or the `--platform linux/amd64` argument to the `docker run` commands, may work, but is not tested nor officially supported by the artifact authors.

## Overview: What does the artifact comprise?

Please list for each distinct component of your artifact:

This code artifact contains:
- the specification presented in the paper
- a Spoofax language definition that makes it executable
- tests that validate the specification

It is an archive that contains
- sources
- a compressed docker image that includes pre-built spoofax dependencies and an aml language implementation, reference compilers and test scripts.

The aml language is included in:
- `./aml` (artifact)
- `/home/myuser/aml` (docker image)

The specification is included in the `src/statics/access` directory, relative to the `aml` root.
Each of the files corresponds (rougly) to a figure in the paper:
- `base.stx`: base definitions
- `public.stx`: Figure 7, `A-Pub` and `AP-Pub` rules.
- `auxiliary.stx`: Figure 9
- `internal.stx`: Figure 10
- `private.stx`: Figure 14
- `protected.stx`: Figure 15
- `private-protected.stx`: Figure 17
- `protected-internal.stx`: Figure 17
-	`path.stx`: Figure 18

## For authors claiming an available badge

We want to publish the artifact on Zenodo, with Apache License 2.0.

## For authors claiming a functional or reusable badge

Assuming the image is already imported with tag `aml:v2`, the tests of the individual subsections can be reproduced as follows:

### Running All Tests

To validate all tests pass, the following command can be used:

```
docker run aml:v2 ./test
```

This can take 10-30 minutes, depending on the specifications of the host machine, and the resources allocated to the docker container.

A failing test suite is indicated as follows:
```
mb.spoofax.cli.SpoofaxCliException: Command 'Run SPT tests' failed (see messages above).
	at mb.spoofax.cli.CommandRunner.call(CommandRunner.java:108)
	at picocli.CommandLine.executeUserObject(CommandLine.java:1933)
	at picocli.CommandLine.access$1100(CommandLine.java:145)
	at picocli.CommandLine$RunLast.executeUserObjectOfLastSubcommandWithSameParent(CommandLine.java:2332)
	at picocli.CommandLine$RunLast.handle(CommandLine.java:2326)
	at picocli.CommandLine$RunLast.handle(CommandLine.java:2291)
	at picocli.CommandLine$AbstractParseResultHandler.execute(CommandLine.java:2159)
	at picocli.CommandLine.execute(CommandLine.java:2058)
	at mb.spoofax.cli.SpoofaxCli.run(SpoofaxCli.java:63)
	at mb.spoofax.cli.SpoofaxCli.run(SpoofaxCli.java:45)
	at mb.aml.test.runner.AMLSptTestRunner.main(AMLSptTestRunner.java:100)

Command exited with non-zero exit code: 1.
This indicates that one or more tests failed.
Please consult the test output above for more information.
```
and a non-zero exit code of the script.

*Compiler Errors.* Note that console output of these test can include failure messages (although they are suppressed by default).
This is expected, as we do have 'negative tests', i.e., tests that validate that programs that reference compilers reject, are also rejected by our specification.
Thus, these error messages can indicate _expected compilation errors_ (i.e., compilation errors that are expected by the test).

*Self-Contained.* The artifact is self-contained;
i.e., all docker commands can be executed without network access (using `--network none`).


### 10.2: Reference compilers

We validate our specification using of three reference compilers.
These test can be invoked as follows.

*Java.* Compatibility with the java compiler can tested as follows:

```bash
docker run aml:v2 ./test aml/test/compatibility/java
```

*CSharp.* Compatibility with the C# compiler can tested as follows:

```bash
docker run aml:v2 ./test aml/test/compatibility/csharp
```

*Rust.* Compatibility with the Rust compiler can tested as follows:

```bash
docker run aml:v2 ./test aml/test/compatibility/rust
```


### 10.3: Own tests

Tests that validate agains manually written expectations can be executed using:

```bash
docker run aml:v2 ./test aml/test/self
```

### 10.4: Code Completion

Tests that validate agains manually written expectations can be executed using:

```bash
docker run aml:v2 ./test aml/test/codecompletion
```
