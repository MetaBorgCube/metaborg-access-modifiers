# AML

Please answer the following questions concisely, either with bullet lists or short paragraphs.

Title of the submitted paper: Defining Name Accessibility using Scope Graphs
ECOOP submission number for the paper: 11

## Quick-start guide (Kick-the-tires phase)

The artifact can be tested using the following commands.

```bash
gunzip -c aml-docker-image.tar.gz | docker load
docker run aml:v1 ./test aml/test/test.spt
```

This should give an output roughly similar to

```
Starting a Gradle Daemon, 1 incompatible and 1 stopped Daemons could not be reused, use --status for details
> Task :aml:compileAdapterProject UP-TO-DATE
> Task :aml:compileLanguage UP-TO-DATE
> Task :aml:compileLanguageProject UP-TO-DATE
> Task :aml:compileJava UP-TO-DATE
> Task :aml:processResources UP-TO-DATE
> Task :aml:classes UP-TO-DATE
> Task :aml:compileTestJava NO-SOURCE
> Task :aml:jar UP-TO-DATE
> Task :aml:processTestResources NO-SOURCE
> Task :aml:testClasses UP-TO-DATE
> Task :aml.test.runner:compileJava UP-TO-DATE
> Task :aml.test.runner:processResources NO-SOURCE
> Task :aml.test.runner:classes UP-TO-DATE
> Task :aml.test.runner:jar UP-TO-DATE

> Task :aml:runSpt
TestSuite test
java##/home/myuser/aml/test/test.spt:
1 tests
parse some program : PASS
java##/home/myuser/aml/test/test.spt:

Deprecated Gradle features were used in this build, making it incompatible with Gradle 8.0.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

See https://docs.gradle.org/7.6.2/userguide/command_line_interface.html#sec:command_line_warnings

BUILD SUCCESSFUL in 22s
9 actionable tasks: 1 executed, 8 up-to-date
The build is running offline. A build scan will not be published at this time, but it can be published if you run the buildScanPublishPrevious task in the next build.
```

Important is to validate that the test passes:
```
> Task :aml:runSpt
TestSuite test
java##/home/myuser/aml/test/test.spt:
1 tests
parse some program : PASS
java##/home/myuser/aml/test/test.spt:
```

and the build indicates it passed properly:

```
BUILD SUCCESSFUL in 22s
```

### Tests

To validate all tests pass, the following command can be used:

```
docker run aml:v1 ./test aml/test
```

This can take up to 30 minutes.
Again, the output should indicate that the build succeeds.


## Overview: What does the artifact comprise?

Please list for each distinct component of your artifact:

This code artifact contains:
- the specification presented in the paper
- a Spoofax language definition that makes it executable
- tests that validate the specification

It is an archive that contains
- sources
- a compressed docker image that includes pre-built spoofax dependencies and a aml language implementation, reference compilers and test scripts.

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

Assuming the image is already imported with tag `aml:v1`, the tests of the individual subsections can be reproduced as follows:

### 10.2: Reference compilers

We validate our specification using of three reference compilers.
These test can be invoked as follows.
Note that console output of these test can include failure messages.
This is expected, as we do have 'negative tests', i.e., tests that validate that programs that reference compilers reject, are also rejected by our specification.
The `BUILD SUCCESSFUL` or `BUILD FAILED` messages at the end of the command output are indicative of the test result.

*Java.* Compatibility with the java compiler can tested as follows:

```bash
docker run aml:v1 ./test aml/test/compatibility/java
```

*CSharp.* Compatibility with the C# compiler can tested as follows:

```bash
docker run aml:v1 ./test aml/test/compatibility/csharp
```

*Rust.* Compatibility with the Rust compiler can tested as follows:

```bash
docker run aml:v1 ./test aml/test/compatibility/rust
```


### 10.3: Own tests

Tests that validate agains manually written expectations can be executed using:

```bash
docker run aml:v1 ./test aml/test/self
```

### 10.4: Code Completion

Tests that validate agains manually written expectations can be executed using:

```bash
docker run aml:v1 ./test aml/test/codecompletion
```
