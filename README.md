# hadoop-utils
Utilities/library related to Hadoop technologies. This library could be useful in 
developing applications on "Analytics PaaS" platform, that needs credentials (client configuration) 
to access to Hadoop cluster.

## Build. 
Run command for compile, package and install in local maven repository.
```
mvn clean package install
```

## How to use it?
Add below dependency to your project pom.xml.:
```xml
<dependency>
  <groupId>org.trustedanalytics</groupId>
  <artifactId>hadoop-utils</artifactId>
  <version>0.4.7</version>
</dependency>
```


### Examples.
Below examples assumes to use:
*  https://github.com/trustedanalytics/hdfs-broker,
*  https://github.com/trustedanalytics/hdfs-broker#kerberos-configuration

Hdfs service instance and kerberos-service have to be bound to application.

Getting initialized Hadoop client Configuration class object.:

```java
Configuration hadoopConf = new org.apache.hadoop.conf.Configuration(true);
ConfigurationHelper helper = ConfigurationHelperImpl.getInstance();
Map<String, String> configParams = helper.getConfigurationFromEnv(ConfigurationLocator.HADOOP);
conf.forEach((key, value) -> hadoopConf.set(key, value));

```

If Hadoop cluster is in Secure Mode (hadoop.security.authentication=kerberos), 
authentication can be done as follows.:

```java
ConfigurationHelper helper = ConfigurationHelperImpl.getInstance();
String kdc = helper.getPropertyFromEnv(PropertyLocator.KRB_KDC);
String realm = helper.getPropertyFromEnv(PropertyLocator.KRB_REALM);
String user = helper.getPropertyFromEnv(PropertyLocator.USER);
String pass = helper.getPropertyFromEnv(PropertyLocator.PASSWORD);
KrbLoginManager loginManager = KrbLoginManagerFactory.getInstance()
                .getKrbLoginManagerInstance(kdc, realm);
loginManager.loginInHadoop(loginManager.loginWithCredentials(user, pass.toCharArray()), conf);
```

For spring application you can use https://github.com/trustedanalytics/hadoop-spring-utils.
