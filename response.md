Dear reviewers,

That you for the (mainly positive) kick-the-tires feedback.
We respond to the following points:
1. The output of the `./test aml/test` command (Review A)
2. The artifact size (Review A)
3. The artifact anonymity (Review A)
4. Support for ARM-chipsets

When relevant, we mention the updates to the artifact that incorporate the feedback.
At the end of the response, we explain where to find the update artifact, and how to use it.

## 1. Running `./test aml/test`

Strictly speaking, this command was not intended to be part of the kick-the-tires phase,
although we admit the readme suggested so.
We appreciate the effort put in running it, and sharing your impression on the output.

The listed Rust/C# error messages are indeed expected, and do not indicate test failures,
as indeed our later remark in the readme indicates.

The reduced runtime is most likely due to a better machine/more resources allocated to docker.

To improve our artifact in this regard, we:
- Moved the actual `./test aml/test` command to a later section in the readme.
- Silenced the C#/Rust compiler errors if the test expects them.
  Failing tests or other logger configurations still show these.
- Added a message printed at the end of the test execution
  that warns that error messages from the reference compiler can occur,
  but those do not necessarily indicate test failure.
- Expanded the explanation in the readme about these failures.
- We updated the expected runtime to 10-30 minutes.


## 2. Artifact Size

The artifact needed the gradle/maven dependencies to run/validate the gradle build.
To reduce the artifact size, we changed the `./test` script to use the shadowed jar directly, instead of via gradle.
However, this changed the script outputs.
We updated the readme to explain the new script output.

In addition, we included the sources and cached artifacts at the top-level as well, indead with the idea of using it for further development (using graphical environments).
Because we do not officially support that, and artifacts can be exported out of the docker image anyway, we dropped the top-level includes.

This reduced the (compressed) artifact size to roughly 2.5GB.


## 3. Artifact Anonymity

This was an oversight in our `.dockerignore`, which should be fixed in the current version.
Thank you for notifying.


## 4. ARM Support

We are happy to hear that the artifact works to a large extent on Apple M1.
Our remark that "arm chipsets (e.g. Apple M1/M2/M3) are not supported" was meant to convey that we do not _guarantee_ that it works, although it might (and apparently does).
We added a "Requirements" section to the readme that states this explicitly.


## Updated Artifact

We uploaded an updated version of the artifact:
[https://doi.org/10.5281/zenodo.10636271](https://doi.org/10.5281/zenodo.10636271)
(a new version of the previous one).
In this artifact, the tag of the container changed to `aml:v2`.
All commands in the readme incorporate this update.
