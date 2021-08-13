FROM ubuntu:20.04

LABEL desc="Docker container for building and testing plugin-utilities"

# Install OpenJDK11
RUN apt-get update && apt-get install -y openjdk-11-jdk

# Install Git
RUN apt -y install git-all

# Download all mock projects
RUN git clone https://github.com/JetBrains-Research/plugin-utilies-mock-data /mock-data
# Save the path to mock projects
RUN export JAVA_MOCK_PROJECTS=/mock-data/java_mock_projects

WORKDIR repo
