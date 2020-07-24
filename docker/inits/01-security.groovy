// Setup Jenkins admin user and password
// Write out admin password to file
// Set Jenkins authorisation strategy
// Whitelist slave to master control
//
// http://<host>/securityRealm/user/admin/configure

import jenkins.model.*
import hudson.security.*
import hudson.tasks.Mailer
import java.io.FileWriter
import org.apache.commons.lang.RandomStringUtils
import jenkins.security.s2m.AdminWhitelistRule
import com.michelin.cio.hudson.plugins.rolestrategy.RoleBasedAuthorizationStrategy

def instance = Jenkins.getInstance()

String charset = (('a'..'z') + ('A'..'Z') + ('0'..'9')).join()
String adminUsername = 'admin'
String adminEmail = 'jenkins-admin@example.com'
String adminPassword = RandomStringUtils.random(16, charset.toCharArray())

// Store admin password
String jenkinsHome = System.getenv('JENKINS_HOME')
FileWriter adminPasswordFile = new FileWriter("${jenkinsHome}/secrets/initialAdminPassword")
adminPasswordFile.write(adminPassword)
adminPasswordFile.close()

// AWS_ACCESS_KEY should only be set if we are running a local copy
if (System.getenv("AWS_ACCESS_KEY")) {
    println "adminPassword: ${adminPassword}"
}

def hudsonRealm = new HudsonPrivateSecurityRealm(false)
adminUser = hudsonRealm.createAccount(adminUsername, adminPassword)
adminUser.addProperty(new Mailer.UserProperty(adminEmail))
instance.setSecurityRealm(hudsonRealm)

def strategy = new RoleBasedAuthorizationStrategy()
instance.setAuthorizationStrategy(strategy)
instance.save()

// Enable userContent to include scripts and CSS
System.setProperty("hudson.model.DirectoryBrowserSupport.CSP", "")

Jenkins.instance.getInjector().getInstance(AdminWhitelistRule.class).setMasterKillSwitch(false)
