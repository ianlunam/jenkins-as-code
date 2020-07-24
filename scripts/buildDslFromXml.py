#!/usr/bin/env python

import os
import json
import re
import xml.etree.ElementTree as ET

trueFalse = {'true': 'true', 'false': 'false'}

# Given a directory, find all 'config.xml' files under it
# and process them, then write out seed.json for the directory
def processDir(dir):
    # Build list of files
    jobs = []
    for root, directories, filenames in os.walk(dir):
        for filename in filenames:
            if filename == 'config.xml':
                if os.path.isfile(os.path.join(root, filename)):
                    jobs.append(os.path.join(root, filename))

    # Loop through files
    seedData = []
    for job in jobs:
        jobConfig = processJob(job)
        if jobConfig:
            # seedData.append(jobConfig)
            toDSL(job, jobConfig)

def writeWithCR(file, string, spaces=0):
    file.write((" " * spaces) + string + '\n')

def removeSpaces(string):
    string = string.replace(" ", "_")
    return string

def escapeString(string):
    if string == None:
        return(string)

    string = string.encode("string_escape")
    string = string.replace("\"", "\\\"")
    string = string.replace("$", "\$")
    # string = string.replace("\"", "\\\"")
    # string = string.replace("\n, "\\\n")
    # string = re.escape(string)
    # string = json.dumps(string)

    return(string)

def toDSL(job, jobConfig):
    dslFile = os.path.splitext(job)[0]+'.groovy'
    fname = dslFile.split('/')
    print(fname[(len(fname)-2)])
    fname[(len(fname)-2)] = fname[(len(fname)-2)].replace(" ", "_")
    fname[(len(fname)-2)] = fname[(len(fname)-2)].replace("-", "_")
    fname.remove('jobs')
    dslFile = '/'.join(fname)
    dslFile = dslFile.replace("/config", "")
    with open(dslFile, 'w') as outFile:
        if 'folder' in jobConfig:
            writeWithCR(outFile, 'folder("{}")'.format(jobConfig['folder']))
            writeWithCR(outFile, 'buildMonitorView("{}") {{'.format(jobConfig['folder']))
            writeWithCR(outFile, 'recurse(true)', 4)
            writeWithCR(outFile, 'jobs {', 4)
            writeWithCR(outFile, 'regex(".*{}.*")'.format(jobConfig['folder']), 8)
            writeWithCR(outFile, '}', 4)
            writeWithCR(outFile, '}')
        if jobConfig['type'] == 'flow-definition':
            writeWithCR(outFile, 'pipelineJob("{}/{}") {{'.format(jobConfig['folder'], removeSpaces(jobConfig['job'])))
        if jobConfig['type'] == 'project':
            writeWithCR(outFile, 'freeStyleJob("{}/{}") {{'.format(jobConfig['folder'], removeSpaces(jobConfig['job'])))
        if 'description' in jobConfig:
            writeWithCR(outFile, 'description("{}")'.format(jobConfig['description']), 4)
        if 'concurrentBuild' in jobConfig:
            writeWithCR(outFile, 'concurrentBuild(allowConcurrentBuild=false)', 4)
        if 'cron' in jobConfig or 'scm' in jobConfig:
            writeWithCR(outFile, 'triggers {', 4)
            if 'cron' in jobConfig:
                writeWithCR(outFile, 'cron("{}")'.format(jobConfig['cron']), 8)
            if 'scm' in jobConfig:
                writeWithCR(outFile, 'scm("{}")'.format(jobConfig['scm']), 8)
            writeWithCR(outFile, '}', 4)
        if 'buildStep' in jobConfig:
            writeWithCR(outFile, 'steps {', 4)
            for buildStep in jobConfig['buildStep']:
                writeWithCR(outFile, 'shell("{}")'.format(escapeString(buildStep['shellCommand'])), 8)
            writeWithCR(outFile, '}', 4)
        if 'buildBindings' in jobConfig:
            writeWithCR(outFile, 'wrappers {', 4)
            for buildBinding in jobConfig['buildBindings']:
                writeWithCR(outFile, 'credentialsBinding {', 8)
                if buildBinding['type'] == 'com.cloudbees.jenkins.plugins.awscredentials.AmazonWebServicesCredentialsBinding':
                    writeWithCR(outFile, 'amazonWebServicesCredentialsBinding {', 12)
                    writeWithCR(outFile, 'accessKeyVariable("{}")'.format(buildBinding['accessKeyVariable']), 16)
                    writeWithCR(outFile, 'secretKeyVariable("{}")'.format(buildBinding['secretKeyVariable']), 16)
                    writeWithCR(outFile, 'credentialsId("{}")'.format(buildBinding['credentialsId']), 16)
                    writeWithCR(outFile, '}', 12)
                writeWithCR(outFile, '}', 8)
            writeWithCR(outFile, '    }', 4)
        if 'parameters' in jobConfig:
            writeWithCR(outFile, 'parameters {', 4)
            for parameter in jobConfig['parameters']:
                if parameter['type'] == 'hudson.model.StringParameterDefinition':
                    if 'description' in parameter:
                        writeWithCR(outFile, 'stringParam("{}", "{}", "{}")'.format(parameter['name'], parameter['defaultValue'], parameter['description']), 8)
                    else:
                        writeWithCR(outFile, 'stringParam("{}", "{}")'.format(parameter['name'], parameter['defaultValue']), 8)
                if parameter['type'] == 'hudson.model.ChoiceParameterDefinition':
                    if 'description' in parameter:
                        writeWithCR(outFile, 'choiceParam("{}", {}, "{}")'.format(parameter['name'], parameter['choices'], parameter['description']), 8)
                    else:
                        writeWithCR(outFile, 'choiceParam("{}", {})'.format(parameter['name'], parameter['choices']), 8)
                if parameter['type'] == 'hudson.model.BooleanParameterDefinition':
                    if 'description' in parameter:
                        writeWithCR(outFile, 'booleanParam("{}", {}, "{}")'.format(parameter['name'], parameter['defaultValue'], parameter['description']), 8)
                    else:
                        writeWithCR(outFile, 'booleanParam("{}", {})'.format(parameter['name'], parameter['defaultValue']), 8)
                if parameter['type'] == 'hudson.model.PasswordParameterDefinition':
                    if 'description' in parameter:
                        writeWithCR(outFile, 'nonStoredPasswordParam("{}", {}")'.format(parameter['name'], parameter['description']), 8)
                    else:
                        writeWithCR(outFile, 'nonStoredPasswordParam("{}")'.format(parameter['name']), 8)
            writeWithCR(outFile, '}', 4)
        writeWithCR(outFile, 'logRotator(numToKeep = 20)', 4)
        if jobConfig['type'] == 'flow-definition':
            writeWithCR(outFile, 'definition {', 4)
            if 'script' in jobConfig:
                writeWithCR(outFile, 'cps {', 8)
                writeWithCR(outFile, 'script("{}")'.format(escapeString(jobConfig['script'])), 12)
                writeWithCR(outFile, '}', 8)
            if 'scriptPath' in jobConfig or 'github' in jobConfig:
                writeWithCR(outFile, 'cpsScm {', 8)
                if 'scriptPath' in jobConfig:
                    writeWithCR(outFile, 'scriptPath("{}")'.format(jobConfig['scriptPath']), 12)
                if 'github' in jobConfig:
                    writeWithCR(outFile, 'scm {', 12)
                    writeWithCR(outFile, 'git {', 16)
                    writeWithCR(outFile, 'remote {', 20)
                    writeWithCR(outFile, 'url("{}")'.format(jobConfig['github']), 24)
                    writeWithCR(outFile, 'credentials("{}")'.format(jobConfig['credentials']), 24)
                    writeWithCR(outFile, '}', 20)
                    writeWithCR(outFile, 'branch("{}")'.format(jobConfig['branch']), 20)
                    writeWithCR(outFile, '}', 16)
                writeWithCR(outFile, '}', 12)
                writeWithCR(outFile, '}', 8)
            writeWithCR(outFile, '}', 4)
        if jobConfig['type'] == 'project':
            if 'github' in jobConfig:
                writeWithCR(outFile, 'scm {', 4)
                writeWithCR(outFile, 'git {', 8)
                writeWithCR(outFile, 'remote {', 12)
                writeWithCR(outFile, 'github("{}")'.format(jobConfig['github']), 16)
                writeWithCR(outFile, 'credentials("{}")'.format(jobConfig['credentials']), 16)
                writeWithCR(outFile, '}', 12)
                writeWithCR(outFile, 'branch("{}")'.format(escapeString(jobConfig['branch'])), 12)
                writeWithCR(outFile, '}', 8)
                writeWithCR(outFile, '}', 4)

        writeWithCR(outFile, '}')

# Build config from config.xml
def processJob(job):
    # Parse XML, ignore folders
    tree = ET.parse(job)
    root = tree.getroot()
    if 'com.cloudbees.hudson.plugins.folder.Folder' == root.tag:
        return None

    print("Processing {}".format(job))

    # Extract job name and folder
    jobName = job.split('/')
    folder = None
    if len(jobName) > 3:
        folder = jobName[-4]
    jobName = jobName[-2]

    # Add folder if included in path
    jobConfig = {'job': jobName,
                 'type': root.tag}
    if folder:
        jobConfig['folder'] = folder

    # Loop through roots looking for anything of interet
    for child in root:
        # Description, obvs.
        if 'description' == child.tag:
            jobConfig['description'] = child.text

        # Job properties
        elif 'properties' == child.tag:
            for property in child:
                # Log rotation
                if 'jenkins.model.BuildDiscarderProperty' == property.tag:
                    for strategy in property:
                        rotate = {}
                        for attribute in strategy:
                            if int(attribute.text) > 0:
                                if 'artifactNumToKeep' == attribute.tag and attribute.text == '20':
                                    continue
                                if 'numToKeep' == attribute.tag and attribute.text == '20':
                                    continue
                                rotate[attribute.tag] = int(attribute.text)
                        if len(rotate) > 0:
                            jobConfig['logRotator'] = rotate

                # Job parameters
                if 'hudson.model.ParametersDefinitionProperty' == property.tag:
                    jobConfig['parameters'] = []
                    for parameterDefinition in property:
                        for parameters in parameterDefinition:
                            param = {'type': parameters.tag}
                            for parameter in parameters:
                                # Specific to choices dropdowns
                                if 'choices' == parameter.tag:
                                    for array in parameter:
                                        values = []
                                        for value in array:
                                            values.append(value.text)
                                    param[parameter.tag] = values
                                # Superfulous
                                elif 'trim' == parameter.tag:
                                    pass
                                # Default values, can be boolean or text
                                elif 'defaultValue' == parameter.tag:
                                    if 'hudson.model.BooleanParameterDefinition' == parameters.tag:
                                        param[parameter.tag] = trueFalse[parameter.text]
                                    else:
                                        param[parameter.tag] = parameter.text
                                # Everything else
                                else:
                                    param[parameter.tag] = parameter.text
                            # Add it to the job
                            jobConfig['parameters'].append(param)

                if 'org.jenkinsci.plugins.workflow.job.properties.DisableConcurrentBuildsJobProperty' == property.tag:
                    jobConfig['concurrentBuild'] = True

                # Triggers
                if 'org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty' == property.tag:
                    for properties in property:
                        for triggers in properties:
                            for param in triggers:
                                if 'spec' == param.tag:
                                    if 'hudson.triggers.TimerTrigger' == triggers.tag:
                                        jobConfig['cron'] = param.text
                                    if 'hudson.triggers.SCMTrigger' == triggers.tag:
                                        jobConfig['scm'] = param.text

        elif 'definition' == child.tag:
            for definition in child:
                # Path for script to execute. Defaults to Jenkinsfile
                if 'script' == definition.tag:
                    jobConfig['script'] = definition.text
                if 'scriptPath' == definition.tag:
                    jobConfig['scriptPath'] = definition.text
                # Source code manager.
                if 'scm' == definition.tag:
                    for line in definition:
                        # Branch to checkout
                        if 'branches' == line.tag:
                            for spec in line:
                                for name in spec:
                                    jobConfig['branch'] = name.text
                        # Remote configs
                        if 'userRemoteConfigs' == line.tag:
                            for userConfigs in line:
                                for userConfig in userConfigs:
                                    if 'credentialsId' == userConfig.tag:
                                        jobConfig['credentials'] = userConfig.text
                                    if 'url' == userConfig.tag:
                                        # Extract repo name from url
                                        jobConfig['github'] = userConfig.text
        elif 'disabled' == child.tag and child.text == 'true':
            jobConfig['disabled'] = trueFalse[child.text]
        elif 'blockBuildWhenDownstreamBuilding' == child.tag:
            jobConfig['blockOnDownstreamProjects'] = trueFalse[child.text]
        elif 'blockBuildWhenUpstreamBuilding' == child.tag:
            jobConfig['blockOnUpstreamProjects'] = trueFalse[child.text]
        elif 'keepDependencies' == child.tag and child.text == 'true':
            jobConfig['keepDependencies'] = trueFalse[child.text]
        elif 'builders' == child.tag:
            buildStep = []
            for line in child:
                if 'hudson.tasks.Shell' == line.tag:
                    for command in line:
                        buildStep.append({'type': line.tag, 'shellCommand': command.text})
            jobConfig['buildStep'] = buildStep
        elif 'buildWrappers' == child.tag:
            buildBindings = []
            for line in child:
                for bind in line:
                    for inner in bind:
                        buildBinding = {'type': inner.tag}
                        for binding in inner:
                            print("ding: {}".format(binding.tag))
                            buildBinding[binding.tag] = binding.text
                        buildBindings.append(buildBinding)
            jobConfig['buildBindings'] = buildBindings
        elif 'scm' == child.tag:
            for line in child:
                # Branch to checkout
                if 'branches' == line.tag:
                    for spec in line:
                        for name in spec:
                            jobConfig['branch'] = name.text
                # Remote configs
                if 'userRemoteConfigs' == line.tag:
                    for userConfigs in line:
                        for userConfig in userConfigs:
                            if 'credentialsId' == userConfig.tag:
                                jobConfig['credentials'] = userConfig.text
                            if 'url' == userConfig.tag:
                                # Extract repo name from url
                                jobConfig['github'] = userConfig.text
        else:
            print("Unprocessed tag: {}".format(child.tag))
    # Return config for the job
    return(jobConfig)

# Code starts execution here.
listOfFiles = os.listdir(".")
for l in listOfFiles:
    if os.path.isdir(l):
        processDir(l)
