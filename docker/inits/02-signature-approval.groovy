// Pre-approve some method signatures used in scripts
//
// http://<host>/scriptApproval/

import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval

signatures = [
    "method hudson.model.AbstractItem setDescription java.lang.String",
    "method hudson.model.ItemGroup getItem java.lang.String",
    "method hudson.model.Run getPreviousSuccessfulBuild",
    "method java.io.File exists",
    "method org.jenkinsci.plugins.workflow.support.steps.build.RunWrapper getRawBuild",
    "new com.cwctravel.hudson.plugins.extended_choice_parameter.ExtendedChoiceParameterDefinition java.lang.String java.lang.String java.lang.String java.lang.String java.lang.String java.lang.String java.lang.String java.lang.String java.lang.String java.lang.String java.lang.String java.lang.String java.lang.String java.lang.String java.lang.String java.lang.String java.lang.String java.lang.String java.lang.String java.lang.String java.lang.String java.lang.String java.lang.String java.lang.String java.lang.String java.lang.String boolean boolean int java.lang.String java.lang.String",
    "new java.io.File java.lang.String",
    "staticMethod hudson.model.Hudson getInstance",
    "staticMethod jenkins.model.Jenkins getInstance",
    "staticMethod org.codehaus.groovy.runtime.DefaultGroovyMethods replaceAll java.lang.String java.util.regex.Pattern java.lang.String",
    "staticMethod org.mindrot.jbcrypt.BCrypt gensalt",
    "staticMethod org.mindrot.jbcrypt.BCrypt hashpw java.lang.String java.lang.String"
]

ScriptApproval sa = ScriptApproval.get();

signatures.each{
    signature ->
        sa.approveSignature(signature)
}

sa.save()
