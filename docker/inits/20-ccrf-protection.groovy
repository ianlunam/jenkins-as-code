// Configure cross site request forgery protection
//
// http://<host>/configureSecurity/  CSRF Protection

import hudson.security.csrf.DefaultCrumbIssuer
import jenkins.model.Jenkins

def instance = Jenkins.instance
instance.setCrumbIssuer(new DefaultCrumbIssuer(false))
instance.save()
