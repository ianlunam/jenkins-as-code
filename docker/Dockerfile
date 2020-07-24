#   Use the current Long Term Support copy of Jenkins
FROM jenkins/jenkins:lts

#####################
# As Root
#####################
USER root

# Get everything up to date, esp. security patches, etc.
RUN apt-get update && apt-get -y upgrade

# A few things needed around the place
RUN apt-get -y install apt-transport-https ca-certificates curl software-properties-common gnupg2 python3-pip python-pip dnsutils jq uuid-runtime zip

# Add docker repo and install
RUN curl -fsSL https://download.docker.com/linux/debian/gpg | apt-key add -
RUN add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/debian $(lsb_release -cs) stable"
RUN apt-get update && apt-get -y install docker-ce
RUN apt -y autoremove

# Add AWS CLI
RUN pip3 install awscli virtualenv --upgrade

# Allow Jenkins to sudo without password. Needed to use Docker host's Docker
RUN echo "jenkins ALL=NOPASSWD: ALL" >> /etc/sudoers

#####################
# As Jenkins
#####################
USER jenkins

# Prevent startup wizard
ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"

# Install init scripts
COPY --chown=jenkins inits/*.groovy /usr/share/jenkins/ref/init.groovy.d/

# Configure plugins
# TODO: Lots of these may not be needed
COPY --chown=jenkins configs/*.xml /var/jenkins_home/

# Put user and role definition files somewhere Jenkins can get at them, for init scripts
RUN mkdir -p /var/jenkins_home/tmp/users
RUN mkdir -p /var/jenkins_home/tmp/roles
COPY --chown=jenkins users/* /var/jenkins_home/tmp/users/
COPY --chown=jenkins roles/* /var/jenkins_home/tmp/roles/

# Install all required plugins
COPY plugins.txt /usr/share/jenkins/ref/
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt
