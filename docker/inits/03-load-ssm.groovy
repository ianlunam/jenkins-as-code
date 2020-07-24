// Load Credentials from AWS SSM (See Readme)
//
// http://<host>/credentials/

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersByPathRequest;
import com.cloudbees.jenkins.plugins.awscredentials.AWSCredentialsImpl;
import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey;
import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey.DirectEntryPrivateKeySource;
import com.cloudbees.plugins.credentials.domains.*;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import hudson.util.Secret;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;

region = 'us-east-1'
if (System.getenv("AWS_ACCESS_KEY")) {
    basicCreds = new BasicAWSCredentials(System.getenv("AWS_ACCESS_KEY"), System.getenv("AWS_SECRET_KEY"))
    credentials = new AWSStaticCredentialsProvider(basicCreds)
} else {
    credentials = InstanceProfileCredentialsProvider.getInstance()
}
simpleSystemsManagementClient = (AWSSimpleSystemsManagement)((AWSSimpleSystemsManagementClientBuilder)((AWSSimpleSystemsManagementClientBuilder) AWSSimpleSystemsManagementClientBuilder.standard().withCredentials(credentials)).withRegion(region)).build();

protected List < Parameter > getParametersFromSSM(String parameterKey) {
    parameterRequest = new GetParametersByPathRequest();
    parameterRequest.withPath(parameterKey).setWithDecryption(Boolean.valueOf(true));
    parameterResult = simpleSystemsManagementClient.getParametersByPath(parameterRequest);
    return parameterResult.getParameters();
}

protected Parameter getParameterFromSSMByName(String parameterKey) {
    parameterRequest = new GetParameterRequest();
    parameterRequest.withName(parameterKey).setWithDecryption(Boolean.valueOf(true));
    parameterResult = simpleSystemsManagementClient.getParameter(parameterRequest);
    return parameterResult.getParameter();
}

protected String getParameterValue(String parameterKey) {
    param = getParameterFromSSMByName(parameterKey)
    return param.value
}

// Load username:password creds from SSM
// File name is used as cred name and description
// SSM Property contents should be 'username:password'
pairs = getParametersFromSSM('/jenkins/secret_pairs')
pairs.each {
    Parameter pair ->
        pair_password = pair.value.split(':').last()
        pair_username = pair.value.split(':').first()
        pair_desc = pair.name.split('/').last()
        pair_name = pair.name.split('/').last()
        cred = (Credentials) new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL,
            pair_name,
            pair_desc,
            pair_username,
            pair_password)
        SystemCredentialsProvider.getInstance().getStore().addCredentials(Domain.global(), cred)
}

// Load accessKey:secretKey creds from SSM
// File name is used as cred name and description
// SSM Property contents should be 'accessKey:secretKey'
keys = getParametersFromSSM('/jenkins/secret_keys')
keys.each {
    Parameter key ->
        key_secret_key = key.value.split(':').last()
        key_access_key = key.value.split(':').first()
        key_desc = key.name.split('/').last()
        key_name = key.name.split('/').last()
        cred = (Credentials) new AWSCredentialsImpl(CredentialsScope.GLOBAL,
            key_name,
            key_access_key,
            key_secret_key,
            key_desc)
        SystemCredentialsProvider.getInstance().getStore().addCredentials(Domain.global(), cred)
}

// Load secret string creds from SSM
// File name is used as cred name and description
// SSM Property contents should just be 'secret'
strings = getParametersFromSSM('/jenkins/secret_strings')
strings.each {
    Parameter string ->
        secret_string = new Secret(string.value)
        secret_desc = string.name.split('/').last()
        secret_name = string.name.split('/').last()
        cred = (Credentials) new StringCredentialsImpl(CredentialsScope.GLOBAL,
            secret_name,
            secret_desc,
            secret_string)
        SystemCredentialsProvider.getInstance().getStore().addCredentials(Domain.global(), cred)
}

// Load certificate creds from SSM
// File name minus suffix is used as cred name and description
// SSM Properties are threefold: <name>.pem <name>.user <name>.pass
// <name>.pem contains the certificate
// <name>.user contains the username
// <name>.pass contains the passphrase
certs = getParametersFromSSM('/jenkins/secret_certs')
certs.each {
    Parameter cert ->
        cert_name = cert.name.split('/').last()
        file_suffix = cert_name.split(/\./).last()
        if (file_suffix == 'pem') {
            cert_source = cert.value
            cert_prefix = cert_name.split(/\./).first()
            cert_username = getParameterValue("/jenkins/secret_certs/${cert_prefix}.user")
            cert_passphrase = getParameterValue("/jenkins/secret_certs/${cert_prefix}.pass")
            cert_desc = cert_name
            certificate = new DirectEntryPrivateKeySource(cert_source)
            cred = (Credentials) new BasicSSHUserPrivateKey(CredentialsScope.GLOBAL,
                cert_name,
                cert_username,
                certificate,
                cert_passphrase,
                cert_desc)
            SystemCredentialsProvider.getInstance().getStore().addCredentials(Domain.global(), cred)
        }
}
