FROM ubuntu:20.04 as base

ENV ANDROID_HOME      /opt/android-sdk
ENV ANDROID_SDK_HOME  ${ANDROID_HOME}
ENV ANDROID_SDK_ROOT  ${ANDROID_HOME}
ENV ANDROID_SDK       ${ANDROID_HOME}

ENV MOCK_DATA             /mock-data
ENV JAVA_MOCK_PROJECTS    ${MOCK_DATA}/java_mock_projects
ENV JAVA_GITHUB_PROJECTS  /github-data/java

# Install OpenJDK11
RUN apt-get update && apt-get install -y openjdk-11-jdk
# Install Git
RUN apt -y install git-all

FROM base as builder

# Install Curl and Unzip
RUN apt -y install curl unzip

# Download all mock projects
RUN git clone https://github.com/JetBrains-Research/plugin-utilies-mock-data $MOCK_DATA

# Download Github projects
WORKDIR repo
COPY . .
RUN ./gradlew downloadRepos \
  -Prepos="/repo/plugin-utilities-plugin/src/test/resources/javaGithubRepositories.yaml"  \
  -Poutput="$JAVA_GITHUB_PROJECTS"

# Download and install Android SDK
## First install Android command-tools from https://developer.android.com/studio#command-tools
RUN curl https://dl.google.com/android/repository/commandlinetools-linux-7302050_latest.zip --output /cmdtools.zip
RUN unzip /cmdtools.zip && rm /cmdtools.zip
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools/latest
RUN mv cmdline-tools/* ${ANDROID_HOME}/cmdline-tools/latest
ENV PATH "${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin"
## Accept all licences
RUN yes | sdkmanager --licenses
## Then install Android SDK components
RUN sdkmanager "platform-tools" "platforms;android-28"

FROM base

LABEL desc="Docker container for building and testing plugin-utilities"

# Copy all test projects
COPY --from=builder ${MOCK_DATA} ${MOCK_DATA}
COPY --from=builder ${JAVA_GITHUB_PROJECTS} ${JAVA_GITHUB_PROJECTS}

# Copy Android SDK
COPY --from=builder ${ANDROID_HOME} ${ANDROID_HOME}
ENV PATH "${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin"

WORKDIR repo

ENTRYPOINT ["./gradlew"]
CMD ["build"]
