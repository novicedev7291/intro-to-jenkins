FROM 4oh4/jenkins-docker

USER root

RUN apt-get update && apt-get install -y ruby curl

ENV JAVA_OPTS -Djenkins.install.runSetupWizard=false \
    -Djava.awt.headless=true \
    -Dhudson.model.ParametersAction.keepUndefinedParameters=true

USER jenkins

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

COPY noosyn_seed_job.groovy /usr/share/jenkins/noosyn_seed_job.groovy
COPY noosyn_init.groovy /usr/share/jenkins/ref/init.groovy