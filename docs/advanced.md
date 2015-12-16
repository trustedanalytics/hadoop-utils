### More generic examples.
If you need to get configuration for particular service, get configurations list for 
all bounded instances of service given type or get value for concrete parameter,
you can use AppConfiguration interface. 
##### Getting initialized HDFS client Configuration class object:

```java
//Read application config from environment
AppConfiguration helper = Configurations.newInstanceFromEnv();

//Select configuration for service type HDFS_TYPE. We assume that there is only one 
//service instance of that type. 
Configuration configuration = helper.getServiceConfig(ServiceType.HDFS_TYPE).asHadoopConfiguration();

//Getting configuration property (i.e.: HDFS_URI)
Optional<String> hdfsUri = helper.getServiceConfig(ServiceType.HDFS_TYPE).getProperty(Property.HDFS_URI);
```

If Hadoop cluster is in Secure Mode (hadoop.security.authentication=kerberos), 
authentication can be done as follows.:

```java
//Read application config from environment
AppConfiguration helper = Configurations.newInstanceFromEnv();

//Select configuration for service instance named "kerberos-service"
ServiceInstanceConfiguration krbConf = helper.getServiceConfig("kerberos-service");
ServiceInstanceConfiguration hdfsConf = helper.getServiceConfig(ServiceType.HDFS_TYPE);

//Getting config properties values 
String kdc = krbConf.getProperty(Property.KRB_KDC).get();
String realm = krbConf.getProperty(Property.KRB_REALM).get();
String user = krbConf.getProperty(Property.USER).get();
String pass = krbConf.getProperty(Property.PASSWORD).get();

//Login in hadoop for authorized operating on HDFS
KrbLoginManager loginManager = KrbLoginManagerFactory.getInstance()
                .getKrbLoginManagerInstance(kdc, realm);
loginManager.loginInHadoop(loginManager.loginWithCredentials(user, pass.toCharArray()), 
                           hdfsConf.asHadoopConfiguration());
```
Above example assumes that we have user provided service "kerberos-service" bound to application.

##### Getting zip configuration

Example:

```
AppConfiguration helper = Configurations.newInstanceFromEnv();
Optional<String> zipFile =
    helper.getServiceConfig("hbase-instance1").getProperty(Property.HADOOP_ZIP);
```


##### Getting hdfs FileSystem.
```java
FileSystem = FileSystem.get(new URI(hdfsConf.getProperty(Property.HDFS_URI).get()),
                            hdfsConf.asHadoopConfiguration(),
                            user);
```

##### Getting connection to Hbase.
Hbase service instance has to be bound to application.

```java
ConfigurationHelper helper = AppConfiguration helper = Configurations.newInstanceFromEnv();
ServiceInstanceConfiguration hbaseConf = helper.getServiceConfig(ServiceType.HBASE_TYPE);

Subject subject = loginManager.loginWithCredentials(user, pass.toCharArray());
Configuration hadoopConf= hbaseConf.asHadoopConfiguration();
loginManager.loginInHadoop(subject, hadoopConf);

Configuration conf = HBaseConfiguration.create(hadoopConf);
User user = UserProvider.instantiate(conf)
            .create(UserGroupInformation.getUGIFromSubject(subject));
            
Connection connection = ConnectionFactory.createConnection(conf, user);
```
For spring application you can use https://github.com/trustedanalytics/hadoop-spring-utils.