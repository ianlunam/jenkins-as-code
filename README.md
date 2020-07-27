# Jenkins Infrastructure Configuration as Code

Build a fully functioning Jenkins instance without manual intervention:

- All system configuration handled by init files (see: `docker/inits/*.groovy`)
- Jobs populated from seed jobs pulled regularly from BitBucket (see: *Creating or changing Jobs* below)
- Users and roles defined in code (see: *Creating or changing users and roles* below)
- Credentials populated from AWS SSM (see: *Creating or changing Credentials* below)

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

##### What things you need installed:

- docker

##### Information you need at hand:

- An AWS access key and secret key for the AWS account containing the SSM secrets.

### Installing and Running

* Check this repo out with the right branch
* cd into the docker directory
* Replace all the `XXXXXXX`'s in the `env-file` with valid AWS creds
* Run:
```
    ./rebuild.sh
```

### Errors

If docker complains is can't build it due to space issues, run it again with a `-d`
```
    ./rebuild.sh -d
```
This cleans out old containers, images and volumes before doing the build as per above.

This is worth trying for any other errors as a lot of them could be space related.

## Testing

On the same machine, go to http://localhost:8080, Normal usernames and passwords will work. The automatically generated admin password is visible in the output from the above rebuild command. Look for a line like so:
```
    INFO: Executing /var/jenkins_home/init.groovy.d/01-security.groovy
    adminPassword: BzYodEV5DMrQtARQ
```
Please note that the password is regenerated every time, so will never be the same twice. It will also be stored on the docker container filesystem at `/var/jenkins_home/secrets/initialAdminPassword`.

Also note: The admin password will not be visible in the output for non-local deployments.

## Creating or changing Jobs

All jobs are defined in Jenkins DSL code stored in seed.json files in directories under `/jobs/`.

See [Job DSL Plugin](https://jenkinsci.github.io/job-dsl-plugin/) for details.

**NOTE** Jobs are updated when the `SeedJob` job in the root of the Jenkins server is run. This happens automatically every 10 minutes, or can be run manually by an administrator.

**NOTE** Any changed made to jobs locally on Jenkins will be overwritten the next time the seed job runs. Please update via this repo.

## Creating or changing users and roles

All users and roles are defined in json code stored in json files under `docker/users/` and `docker/roles/` respectively.

**NOTE** All users and roles are loaded at jenkins startup time from the information stored in the Jenkins image.

User files are named using the format `<username>.json` where `<username>` is the user's login and contains:

- "fullName": The user's full name
- "emailAddress": The user's email address
- "passwordHash": The user's hashed password (see *Password Encryption* below)

Role files are named using the format `<role>.json` where `<role>` is the name of the role and contains:

- "users": A list of text usernames to be attached to the role (the `<username>` from `<username>.json` above)
- "perms": A list of text permissions to be applied to the role

(see: `docker/users/*`, `docker/roles/*` and `docker/inits/05-create_users.groovy`)

## Creating or changing Credentials

**NOTE** All credentials are loaded from AWS System Manager Parameters at jenkins startup time.

We have 4 types of Credentials which are loaded from AWS System Manager Parameters all of which are stored as SecureString parameters using account default KMS key.

- Secret Pairs: A username and password pair
    - Stored as `/jenkins/secret_pairs/<name>`
    - Format: <username>:<password>
- Secret Keys: An AWS access key and secret key pair
    - Stored as `/jenkins/secret_keys/<name>`
    - Format: <access-key>:<secret-key>
- Secret Strings: A single secret string
    - Stored as `/jenkins/secret_strings/<name>`
    - Format: <secret>
- Secret Certificates: A PEM file with its username and passphrase
    - Stored as:
        - `/jenkins/secret_certs/<name>.pem`
        - `/jenkins/secret_certs/<name>.user`
        - `/jenkins/secret_certs/<name>.pass`
    - Format: Each file contains the appropriate component

(see: `docker/inits/03-load-ssm.groovy`)

## Password Encryption

Passwords stored in the user definition files need to be encrypted using a one-way encryption supported by Jenkins. A method has been supplied in the job `Jenkins/password-encrypt`. Running this job with the any password will return in the output the [jbcrypt](https://www.mindrot.org/projects/jBCrypt/) encrypted string to cut and paste to the user definition file.

The encrypted string will look something like:
```
    #jbcrypt:$2a$10$wtSQbx/JDAo/jIaq9nDzvuZpe5KrJEjtP2MONWqI6WwkmKHgdnJ/G
```

## Deployment

On the running jenkins, either locally or previously deployed, there are a set of jobs in the `Jenkins` directory with names starting with `jenkins-` to build the docker image, create the ECS cluster for it, and deploying the image to the cluster.

`jenkins-build` only takes one parameter: the branch of this repo to build from.

`jenkins-create-ecs-cluster` takes a series of parameters, most of which *should* be left as the defaults, except maybe the `infraBranch`.

`jenkins-deploy` also takes a series of parameters, most of which *should* be left as the defaults, except `dockerVersion` which should be set to the version generated by the `jenkins-build` job (see the line starting `Built Tag: ` at the end of its output), and maybe the `infraBranch`.

**NOTE** Running `jenkins-deploy` will result in an outage of approximately one minute (maybe two ...).

Please note: The admin password will not be visible in the output for non-local deployments. Instead it will only be stored on the docker container filesystem at `/var/jenkins_home/secrets/initialAdminPassword`.

## Refresh seed jobs from an existing Jenkins

To refresh the contents of the seed.json files, collect all job config.xml files from an existing Jenkins server. On the Jenkins server:
```
    cd $JENKINS_HOME/jobs
    find . -name config.xml -exec tar -rvf /tmp/configs.tar {} \;
```
Copy the file `/tmp/configs.tar` from the jenkins server to a temporary directory on your machine, untar the file, and run the supplied helper script:
```
    mkdir /tmp/configs
    cd /tmp/configs
    tar xvf <pathtofile>/configs.tar
    <pathtorepo>/scripts/buildDslFromXml.py
```
Then copy the groovy DSL files into the repo.
```
    find . -name '*.groovy' -exec cp {} <pathtorepo>/jobs/{} \;
```
You can then commit and push the changes

## TODO:

- Weed out unneeded files from docker/configs
- Move BAP SSH config to SSM (if needed)
- Move user and role processing to seed jobs (see: `scripts/create_users.fails`)
- Move user and role definitions to SSM or BitBucket

## Built With

-   Lots of googling.

## Authors

-   **Ian Lunam**
