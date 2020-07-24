// Configure seed job (See scripts/seed_function.groovy)
//
// http://<host>/job/SeedJob/

import jenkins.model.*
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval

def jobName = "SeedJob"

// def git_url = System.getenv('SEEDJOB_GIT')
def git_url = 'https://github.com/ianlunam/jenkins-jobs-as-code.git'
def git_auth = 'github-user'
def git_branch = 'master'

scm = """\
  <scm class="hudson.plugins.git.GitSCM">
    <configVersion>2</configVersion>
    <userRemoteConfigs>
      <hudson.plugins.git.UserRemoteConfig>
        <url><![CDATA[${git_url}]]></url>
        <credentialsId>${git_auth}</credentialsId>
      </hudson.plugins.git.UserRemoteConfig>
    </userRemoteConfigs>
    <branches>
      <hudson.plugins.git.BranchSpec>
        <name>${git_branch}</name>
      </hudson.plugins.git.BranchSpec>
    </branches>
    <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
    <submoduleCfg class="list"/>
    <extensions/>
  </scm>
"""

def configXml = """\
  <?xml version='1.0' encoding='UTF-8'?>
  <project>
    <actions/>
    <description>Create Jenkins jobs from DSL groovy files</description>
    <keepDependencies>false</keepDependencies>
    <properties/>
    ${scm}
    <canRoam>true</canRoam>
    <disabled>false</disabled>
    <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
    <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
    <triggers>
      <hudson.triggers.TimerTrigger>
        <spec>H 9 * * 6</spec>
      </hudson.triggers.TimerTrigger>
    </triggers>
    <concurrentBuild>false</concurrentBuild>
    <builders>
      <javaposse.jobdsl.plugin.ExecuteDslScripts plugin="job-dsl@1.37">
        <targets>jobs/**/*.groovy</targets>
        <usingScriptText>false</usingScriptText>
        <ignoreExisting>false</ignoreExisting>
        <failOnSeedCollision>true</failOnSeedCollision>
        <removedJobAction>DELETE</removedJobAction>
        <removedViewAction>DELETE</removedViewAction>
        <lookupStrategy>JENKINS_ROOT</lookupStrategy>
        <additionalClasspath></additionalClasspath>
      </javaposse.jobdsl.plugin.ExecuteDslScripts>
    </builders>
    <publishers/>
    <buildWrappers/>
  </project>
""".stripIndent()

if (!Jenkins.instance.getItem(jobName)) {
    def xmlStream = new ByteArrayInputStream( configXml.getBytes() )
    try {
        def seedJob = Jenkins.instance.createProjectFromXML(jobName, xmlStream)
    } catch (ex) {
        println "ERROR: ${ex}"
        println configXml.stripIndent()
    }
}
