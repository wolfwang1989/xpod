import play.Project._

name := "computer-database"

version := "1.0"

resolvers += (
  "Local Maven Repository" at "file:///"+Path.userHome.absolutePath+"/.m2/repository"
  )

libraryDependencies ++= Seq(javaJdbc, javaEbean)

libraryDependencies ++= Seq("org.apache.hbase" %  "hbase-client" % "0.98.3-hadoop2",
  "org.apache.hadoop" %  "hadoop-common" % "2.2.0",
  "org.apache.hbase" %  "hbase-common" % "0.98.3-hadoop2",
  "joda-time" %  "joda-time" % "2.3"
)

playJavaSettings
