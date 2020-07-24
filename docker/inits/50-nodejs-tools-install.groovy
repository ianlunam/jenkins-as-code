// Jenkins Global Tool Configuration, NodeJS Installs
//
// http://<host>/configureTools/

import jenkins.model.*
import hudson.model.*
import jenkins.plugins.nodejs.tools.*
import hudson.tools.*

def inst = Jenkins.getInstance()

def desc = inst.getDescriptor("jenkins.plugins.nodejs.tools.NodeJSInstallation")

def versions = [
    "nodejs893": [version: "8.9.3", npmPackages: 'yarn ng'],
    "nodejs10120": [version: "10.12.0", npmPackages: 'yarn ng'],
    "nodejs10160": [version: "10.16.0", npmPackages: 'yarn ng']
]
def installations = [];

for (v in versions) {
    def installer = new NodeJSInstaller(v.value.version, v.value.npmPackages, 0)
    def installerProps = new InstallSourceProperty([installer])
    def installation = new NodeJSInstallation(v.key, "", [installerProps])
    installations.push(installation)
}

desc.setInstallations(installations.toArray(new NodeJSInstallation[0]))

desc.save()
