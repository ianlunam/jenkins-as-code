// I'm not even sure what this does.
// Something to do with publishing to S3
//
// See https://wiki.jenkins.io/display/JENKINS/S3+Plugin

import hudson.plugins.s3.*
import jenkins.model.*

instance = Jenkins.getInstance()
S3Profile profile = new S3Profile(null, null, 'need something here', false, 60, "5", "5", "5", "5", false);

publisher = hudson.plugins.s3.S3BucketPublisher
des = new hudson.plugins.s3.S3BucketPublisher.DescriptorImpl();
s3Plugin = instance.getDescriptor(S3BucketPublisher.class);

new_profiles = [];
des.getProfiles().each {
    if (it.name != "") {
        new_profiles.push(it)
    }
}
new_profiles.push(profile)

s3Plugin.replaceProfiles(new_profiles)
