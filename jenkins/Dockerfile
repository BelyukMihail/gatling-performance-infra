FROM jenkins/jenkins:lts-jdk17

ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false -Dhudson.model.DirectoryBrowserSupport.CSP=''"
COPY security.groovy /usr/share/jenkins/ref/init.groovy.d/security.groovy
COPY configure-markup-formatter.groovy /usr/share/jenkins/ref/init.groovy.d/configure-markup-formatter.groovy
COPY jobs/ /jobs/
COPY agents/ /agents/
COPY run.sh /run.sh
COPY plugins.txt /usr/share/jenkins/ref/

USER root
RUN jenkins-plugin-cli -f /usr/share/jenkins/ref/plugins.txt && \
apt-get -y update && \
apt-get -y install sudo && \
apt-get -y install jq && \
sudo usermod -aG staff jenkins && \
echo "jenkins ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers
ENV PATH "$PATH:/usr/local/bin/dind/"

ENTRYPOINT ["bash","-x","/run.sh"]
