[![Build Status](https://travis-ci.org/trustedanalytics/hadoop-utils.svg?branch=master)](https://travis-ci.org/trustedanalytics/hadoop-utils)
[![Dependency Status](https://www.versioneye.com/user/projects/57236849ba37ce004309f323/badge.svg?style=flat)](https://www.versioneye.com/user/projects/57236849ba37ce004309f323)

# hadoop-utils
Library of utilities, related to Hadoop and Cloud Foundry. This library is intended for 
TAP (link) applications developers. It supports Cloud Foundry brokers and facilitates 
access to Hadoop (kerberized) clusters.

## Prerequisites 
*  maven 3.x
*  java 8
 
## How to use it?
### Build it first 

```
mvn clean install
```

...and add following dependency to your pom.xml :

```xml
<dependency>
  <groupId>org.trustedanalytics</groupId>
  <artifactId>hadoop-utils</artifactId>
  <version>0.5.4</version>
</dependency>
```

### Hdfs usage example
Following example assumes that instances of hdfs service and kerberos service are bound to the app.

More about those services can be found here : 
*  https://github.com/trustedanalytics/hdfs-broker,
*  https://github.com/trustedanalytics/hdfs-broker#kerberos-configuration

#### 1) Use case with one configured service account (headless account).

##### Getting hdfs file system object
```java
  FileSystem hdfsFs = Hdfs.newInstance().createFileSystem();
```

Or, if all you need is a configuration, you can get it like this : 

```java
  Configuration hdfsConf = Hdfs.newInstance().createConfig();
```
 
### HBase usage example
Following example assumes that instances of hbase service and kerberos service are bound to the app.

More about those services can be found here : 
*  https://github.com/trustedanalytics/hbase-broker,
*  https://github.com/trustedanalytics/hbase-broker#kerberos-configuration


##### Getting hbase connection

```java
 Connection hbaseConn = Hbase.newInstance().createConnection();
```

Or, if all you need is a configuration, you can get it like this :

```java
 Configuration hbaseConf = Hbase.newInstance().createConfig();
```

### Hive usage example
Following example assumes that instances of hive service and kerberos service are bound to the app.

More about those services can be found here :
*  https://github.com/trustedanalytics/hive-broker,
*  https://github.com/trustedanalytics/hive-broker#kerberos-configuration


##### Getting hive connection

```java
 Connection hiveConn = Hive.newInstance().createConnection();
```

Or, if all you need is a configuration, you can get it like this :

```java
 Configuration hiveConf = Hive.newInstance().createConfig();
```

## More advanced examples
There is a lot more. Check out the javadocs. For more advanced examples, see [advanced](docs/advanced.md).  
