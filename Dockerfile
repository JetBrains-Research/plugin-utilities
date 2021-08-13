FROM ubuntu:20.04

LABEL desc="Docker container for building and testing plugin-utilities"

# Install OpenJDK11
RUN apt-get update && apt-get install -y openjdk-11-jdk

# Install Git
RUN apt -y install git-all

WORKDIR repo

# Download all mock projects
COPY submodules.sh submodules.sh
RUN bash ./submodules.sh
