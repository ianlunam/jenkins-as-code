// Create users and roles from definition files
//
// http://<host>/securityRealm/

import jenkins.model.*
import hudson.model.User
import hudson.tasks.Mailer
import hudson.security.*
import java.util.*
import com.michelin.cio.hudson.plugins.rolestrategy.*
import java.lang.reflect.*
import groovy.json.JsonSlurper
import com.michelin.cio.hudson.plugins.rolestrategy.RoleBasedAuthorizationStrategy

jenkinsHome = System.getenv('JENKINS_HOME')
usersPath = "${jenkinsHome}/tmp/users"
rolesPath = "${jenkinsHome}/tmp/roles"

def instance = jenkins.model.Jenkins.instance

// For each user, run createUser defined below
new File(usersPath).eachFile {
    userFile ->
        createUser(instance, userFile)
}

// For each role, run createrole defined below
// Get the Authorization Strategy from Jenkins
def authStrategy = instance.getAuthorizationStrategy()
// If it's a role based Strategy (hint: it better be!!)
if(authStrategy instanceof RoleBasedAuthorizationStrategy){
    // Map it to the right class
    RoleBasedAuthorizationStrategy roleAuthStrategy = (RoleBasedAuthorizationStrategy) authStrategy
    // For each role, run createrole defined below
    new File(rolesPath).eachFile {
        roleFile ->
            createRole(roleAuthStrategy, roleFile)
    }
}

def createUser(instance, File userFile) {
    List userNameSplit = userFile.getName().split('\\.')
    userNameSplit.pop()
    String userName = userNameSplit.join('.')
    println "Processing user ${userName}"
    def inputJSON = new JsonSlurper().parseText(userFile.text)

    def existingUser = instance.securityRealm.allUsers.find {it.id == userName}
    if (existingUser == null) {
        try {
            User user = instance.securityRealm.createAccountWithHashedPassword(userName, inputJSON.passwordHash)
            user.addProperty(new Mailer.UserProperty(inputJSON.emailAddress));
            user.setFullName(inputJSON.fullName)
        } catch(Exception ex) {
            println "Exception: ${userName}: ${ex}"
        }
    }
}

def createRole(roleAuthStrategy, File roleFile) {
    List roleNameSplit = roleFile.getName().split('\\.')
    roleNameSplit.pop()
    String roleName = roleNameSplit.join('.')
    println "Processing role ${roleName}"
    def inputJSON = new JsonSlurper().parseText(roleFile.text)

    // Create role
    roleAuthStrategy.doAddRole(RoleBasedAuthorizationStrategy.GLOBAL,
                            roleName,
                            inputJSON.perms.join(","),
                            "true",
                            null)
    // Add users to role
    inputJSON.users.each{
        userName ->
            roleAuthStrategy.doAssignRole(RoleBasedAuthorizationStrategy.GLOBAL,
                            roleName,
                            userName);
    }

}

// import hudson.security.HudsonPrivateSecurityRealm.Details;
// def user = hudson.model.User.current();
// user.addProperty(Details.fromHashedPassword('#jbcrypt:sdfgh'))
