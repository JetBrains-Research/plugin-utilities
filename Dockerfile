FROM ubuntu:20.04

LABEL desc="Docker container for building and testing plugin-utilities"

# Install OpenJDK11
RUN apt-get update && apt-get install -y openjdk-17-jdk

# Install Git and Curl and Unzip
RUN apt -y install git-all curl unzip

# Download all mock projects
RUN git clone https://github.com/JetBrains-Research/plugin-utilies-mock-data.git /mock-data
# Save the path to mock projects
ENV JAVA_MOCK_PROJECTS /mock-data/java_mock_projects

# Download and install Android SDK
## Set the paths
ENV ANDROID_HOME      /opt/android-sdk
ENV ANDROID_SDK_HOME  ${ANDROID_HOME}
ENV ANDROID_SDK_ROOT  ${ANDROID_HOME}
ENV ANDROID_SDK       ${ANDROID_HOME}
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

WORKDIR repo

ENTRYPOINT ["./gradlew"]
CMD ["build"]
