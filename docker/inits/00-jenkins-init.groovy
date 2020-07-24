// Simple script to read environment variables and set jenkins parameters
// Environment Variables:
//     JENKINS_EMAIL -> used to set admin email address
//     JENKINS_URL -> used to set jenkins url
//
// http://<host>/manage

import jenkins.model.JenkinsLocationConfiguration

def jenkinsLocationConfiguration = JenkinsLocationConfiguration.get()

def env = System.getenv()

if (env.JENKINS_EMAIL) {
    jenkinsLocationConfiguration.setAdminAddress(env.JENKINS_EMAIL)
} else {
    jenkinsLocationConfiguration.setAdminAddress('nobody@example.com')
}

if (env.JENKINS_URL) {
    jenkinsLocationConfiguration.setUrl(env.JENKINS_URL)
} else {
    jenkinsLocationConfiguration.setUrl('http://localhost:8080/')
}

jenkinsLocationConfiguration.save()
