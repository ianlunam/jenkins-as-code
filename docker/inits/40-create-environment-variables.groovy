// Add system environment variables
//
// http://<host>/configure#section102

import jenkins.model.Jenkins
import hudson.slaves.EnvironmentVariablesNodeProperty

instance = Jenkins.getInstance()

globalNodeProperties = instance.getGlobalNodeProperties()
envVarsNodePropertyList = globalNodeProperties.getAll(hudson.slaves.EnvironmentVariablesNodeProperty.class)

newEnvVarsNodeProperty = null
envVars = null

if ( envVarsNodePropertyList == null || envVarsNodePropertyList.size() == 0 ) {
    newEnvVarsNodeProperty = new hudson.slaves.EnvironmentVariablesNodeProperty();
    globalNodeProperties.add(newEnvVarsNodeProperty)
    envVars = newEnvVarsNodeProperty.getEnvVars()
} else {
    envVars = envVarsNodePropertyList.get(0).getEnvVars()
}

envVars.put("ECR_REPO", "XXXXXXXXXXX.dkr.ecr.us-east-1.amazonaws.com")
instance.save()
