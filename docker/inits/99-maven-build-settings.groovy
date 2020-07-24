// Just a couple of random settings for maven builds
//
// http://<host>/configure#section101

import hudson.markup.RawHtmlMarkupFormatter
import jenkins.model.*

instance = Jenkins.getInstance()

instance.setQuietPeriod(5)
instance.setScmCheckoutRetryCount(0)

instance.save()
