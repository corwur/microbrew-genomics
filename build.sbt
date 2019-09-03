name := "microbrew-genomics"

version := "0.1"

scalaVersion := "2.12.0"

val sparkVersion = "2.4.3"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.4.3",
  "org.apache.spark" %% "spark-sql" % "2.4.3",
  "org.bdgenomics.adam" %% "adam-core-spark2" % "0.28.0",
  "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "2.7.2",
  "org.apache.hadoop" % "hadoop-common" % "2.7.2",
  "com.typesafe.akka" %% "akka-http"   % "10.1.9",
  "com.typesafe.akka" %% "akka-stream" % "2.5.23",
  "io.spray" %%  "spray-json" % "1.3.4"

)
resolvers += Resolver.mavenLocal