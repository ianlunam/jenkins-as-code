import jenkins.model.Jenkins
import jenkins.model.JenkinsLocationConfiguration

def jenkinsLocationConfiguration = JenkinsLocationConfiguration.get()

def List<String> splitted = jenkinsLocationConfiguration.jenkinsUrl.split("/")
hostname = splitted[2]
splitted = hostname.split("\\.")
splitted.remove(0)
domainname = splitted.join(".")

println(domainname)
