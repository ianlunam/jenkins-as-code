// Disable warning about queue authenticator settings
// These are just annoying warning the developer thought should always be there
//
// http://<host>/manage

import jenkins.security.QueueItemAuthenticatorMonitor
import jenkins.model.Jenkins

def instance = Jenkins.getInstance()

QueueItemAuthenticatorMonitor qiam = new QueueItemAuthenticatorMonitor()

qiam.disable(true)
instance.save()
