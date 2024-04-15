# Temurin OpenJDK
FROM eclipse-temurin:11.0.20.1_1-jdk
USER root
RUN update-alternatives --install /usr/bin/java java ${JAVA_HOME}/bin/java 1



# Metadata
ARG BUILD_DATE
LABEL org.label-schema.schema-version="1.0"
LABEL org.label-schema.build-date=$BUILD_DATE
LABEL org.label-schema.name="AML Environment"
LABEL org.label-schema.description="An environment to build and run the AML language."

# Install packages
RUN apt-get update \
 && apt-get install --no-install-recommends -y \
    curl \
    wget \
    unzip \
    tar \
    bash \
    procps \
    nano \
    gcc \
    gcc-multilib \
    git \
    dotnet-sdk-7.0\
 && dotnet --version \
 && rm -rf /var/lib/apt/lists/*

# Install Gradle
ENV GRADLE_HOME=/opt/gradle
ARG GRADLE_VERSION=7.6.2
ARG GRADLE_DOWNLOAD_SHA256=a01b6587e15fe7ed120a0ee299c25982a1eee045abd6a9dd5e216b2f628ef9ac
ARG GRADLE_URL=https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip
RUN set -o errexit -o nounset \
 && echo "Downloading Gradle" \
 && wget --no-verbose --output-document=gradle.zip "${GRADLE_URL}" \
    \
 && echo "Checking download hash" \
 && echo "${GRADLE_DOWNLOAD_SHA256} *gradle.zip" | sha256sum --check - \
    \
 && echo "Installing Gradle" \
 && unzip gradle.zip \
 && rm gradle.zip \
 && mv "gradle-${GRADLE_VERSION}" "${GRADLE_HOME}/" \
 && ln --symbolic "${GRADLE_HOME}/bin/gradle" /usr/bin/gradle \
    \
 && echo "Testing Gradle installation" \
 && gradle --version



# Create non-privileged user
ARG GNAME=myuser
ARG GID=901
ARG UNAME=myuser
ARG UID=901
ARG UHOME=/home/${UNAME}
ENV GRADLE_USER_HOME=${UHOME}/.gradle
RUN set -o errexit -o nounset \
 && groupadd --gid ${GID} ${GNAME} \
 && useradd --create-home --home "${UHOME}" --uid ${UID} --gid ${GID} "${UNAME}"

USER ${UNAME}
WORKDIR ${UHOME}

# Install Rust compiler
RUN curl --proto '=https' --tlsv1.3 https://sh.rustup.rs -sSf | bash -s -- -y
ENV PATH="${UHOME}/.cargo/bin:${PATH}"

# Make AML and all require resources to build it available in image
COPY --chown=${UNAME} . ${UHOME}

# Build AML test runner
RUN cd aml \
 && gradle build \
 && cd ../aml.test.runner \
 && gradle shadowJar \
 && cd .. \
 && cp aml.test.runner/build/libs/aml.test.runner-all.jar aml/aml-test-runner.jar \
 && ./test aml/test/test.spt \
 && echo "Build succeeded" \
 && rm -r .gradle/ \
 && rm -r .m2/

# Run
CMD [ "/bin/bash" ]
