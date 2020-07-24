// Global setting for using MarkUp.
//
// http://<host>/configure#section97

import hudson.markup.RawHtmlMarkupFormatter
import jenkins.model.*

instance = Jenkins.getInstance()

instance.setMarkupFormatter(new RawHtmlMarkupFormatter(true))
instance.save()
