#!/bin/sh

JENKINS_USER=${JENKINS_ADMIN_LOGIN:-admin};
JENKINS_PASSWORD=${JENKINS_ADMIN_PASSWORD:-admin};
JENKINS_HOST="localhost";
JENKINS_PORT="8080";
SERVER="http://$JENKINS_HOST:$JENKINS_PORT"
USER=$JENKINS_USER:$JENKINS_PASSWORD
JENKINS_API="http://$JENKINS_USER:$JENKINS_PASSWORD@$JENKINS_HOST:$JENKINS_PORT";
JENKINS_URL_CONFIG=${JENKINS_URL_CONFIG:-"http:\\/\\/127.0.0.1:8080\\/"};
JENKINS_GRAFANA_URL="127.0.0.1:3000";

JENKINS_CLI_PATH="/usr/share/jenkins/ref/jenkins-cli.jar";

bash -x /usr/local/bin/jenkins.sh &

echo "Waiting Jenkins to start"
sleep 8

# Proceed with other commands here
echo "Continue with other commands..."

echo "Downloading jenkins-cli.jar..."
curl -o $JENKINS_CLI_PATH $SERVER/jnlpJars/jenkins-cli.jar
sleep 3

java -jar $JENKINS_CLI_PATH -s $JENKINS_API version
while [ $? -ne 0 ]; do
  echo -n "."
  echo "TRYING AGAIN to download jenkins-cli.jar..."
  curl -o $JENKINS_CLI_PATH $SERVER/jnlpJars/jenkins-cli.jar
  sleep 3
  java -jar $JENKINS_CLI_PATH -s $JENKINS_API version
done
echo " "

echo "Move security.groovy to init.groovy.d/"
cp /usr/share/jenkins/ref/init.groovy.d/security.groovy /var/jenkins_home/init.groovy.d/security.groovy

echo "configure-markup-formatter.groovy to init.groovy.d/"
cp /usr/share/jenkins/ref/init.groovy.d/configure-markup-formatter.groovy /var/jenkins_home/init.groovy.d/configure-markup-formatter.groovy

echo "Trying to import jobs to Jenkins"

for job in `ls -1 /jobs/*.xml`; do
  JOB_NAME=$(basename ${job} .xml)
  java -jar $JENKINS_CLI_PATH -s $JENKINS_API create-job $JOB_NAME < ${job}
done

echo "Trying to create agent in Jenkins"

for agent in `ls -1 /agents/*.xml`; do
  AGENT_NAME=$(basename ${agent} .xml)
  java -jar $JENKINS_CLI_PATH -s $JENKINS_API create-node $AGENT_NAME < ${agent}
done

wait $!