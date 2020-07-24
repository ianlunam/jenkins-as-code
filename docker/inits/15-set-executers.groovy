// Set number of executers on master
//
// http://<host>/computer/(master)/configure

import jenkins.model.Jenkins

instance = Jenkins.getInstance()

instance.setNumExecutors(7)
instance.setNodes(instance.getNodes())
instance.save()
