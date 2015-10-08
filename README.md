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
  <version>0.5.0</version>
</dependency>
```


### Examples.
Below examples assumes to use:
*  https://github.com/trustedanalytics/hdfs-broker,
*  https://github.com/trustedanalytics/hdfs-broker#kerberos-configuration

Hdfs service instance and kerberos-service have to be bound to application.

#### 1) Use case with one configured service account.
##### 1.1) How to get hdfs configuration (one hdfs service bound)?
```
  Configuration hdfsConf = Hdfs.newInstance().createConfig();
```
 
##### 1.2) How to get hdfs file system object (one hdfs service bound)?
```
  FileSystem hdfsFs = Hdfs.newInstance().createFileSystem();
```

##### 1.3) How to get hdfs configuration for given service instance name?
```
  Configuration hdfsConf = Hdfs.newInstance("hdfs-instance").createConfig();
```

##### 1.4) How to get file system object  for given service instance name?
```
  FileSystem hdfsFs = Hdfs.newInstance("hdfs-instance").createFileSystem();
```

##### 1.5) How to get hbase client configuration (one hbase service bound)?
```
 Configuration hbaseConf = Hbase.newInstance().createConfig();
```

##### 1.6) How to get hbase connection (one hbase service bound)?
```
 Connection hbaseConn = Hbase.newInstance().createConnection();
```

##### 1.7) How to get hbase configuration for given service instance name?
```
  Configuration hbaseConf = Hbase.newInstance("hbase-instance").createConfig();
```

##### 1.8) How to get hbase connection for given service instance name?
```
  Connection hbaseConn = Hbase.newInstance("hbase-instance").createConnection();
```


#### 2) Use case with user identity from oauth (not yet implemented).
##### 2.1) How to get hdfs configuration (one hdfs service bound)?
```
  JwtToken jwtToken;
  ...
  Configuration hdfsConf = Hdfs.newInstance().createConfig(jwtToken);
```

##### 2.2) How to get hdfs file system object (one hdfs service bound)?
```
 JwtToken jwtToken;
 ...
 FileSystem hdfsFs = Hdfs.newInstance().createFileSystem(jwtToken);
```
 
##### 2.3) How to get hdfs configuration for given service instance name?
```
 JwtToken jwtToken;
 ...
 Configuration hdfsConf = Hdfs.newInstance("hdfs-instance").createConfig(jwtToken);
```

##### 2.4) How to get file system object for given service instance name?
```
 JwtToken jwtToken;
 ...
 FileSystem hdfsFs = Hdfs.newInstance("hdfs-instance").createFileSystem(jwtToken);
```

##### 2.5) How to get hbase configuration (one hbase service bound)?
```
  JwtToken jwtToken;
  ...
  Configuration hbaseConf = Hbase.newInstance().createConfig(jwtToken);
```

##### 2.6) How to get hbase connection (one hbase service bound)?
```
  JwtToken jwtToken;
  ...
  Connection hbaseConn = Hbase.newInstance().createConnection(jwtToken);
```

##### 2.7) How to get hbase configuration for given service instance name?
```
  JwtToken jwtToken;
  ...
  Configuration hbaseConf = Hbase.newInstance("hbase-instance").createConfig(jwtToken);
```

##### 2.8) How to get file system object for given service instance name?
```
  JwtToken jwtToken;
  ...
  Connection hbaseConn = Hbase.newInstance("hbase-instance").createConnection(jwtToken);
```

### More generic examples.
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