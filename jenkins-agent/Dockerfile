FROM jenkins/ssh-agent:latest-jdk11

ENV JENKINS_AGENT_SSH_PUBKEY "SETUP HERE"

RUN apt-get -y update && apt-get upgrade -y && \
    apt-get install -y curl bash unzip && \
    curl -fsSL https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz -o /tmp/apache-maven.tar.gz \
    && tar -zxvf /tmp/apache-maven.tar.gz -C /tmp \
    && mv /tmp/apache-maven-3.9.6 /usr/local/apache-maven \
    && ln -s /usr/local/apache-maven/bin/mvn /usr/local/bin/mvn \
    && rm /tmp/apache-maven.tar.gz