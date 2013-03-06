name := "topicmodel-eval"

version := "1.0-SNAPSHOT"

organization := "edu.utexas"

scalaVersion := "2.10.0"

crossPaths := false

retrieveManaged := true

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "Scala tools" at "https://oss.sonatype.org/content/groups/scala-tools"

resolvers ++= Seq(
  "clojars" at "http://clojars.org/repo/",
  "clojure-releases" at "http://build.clojure.org/releases"
)

libraryDependencies += "cc.mallet" % "mallet" % "2.0.7"

// Experimental dependencies
libraryDependencies ++= Seq(
  "org.scalanlp" %% "breeze-math" % "0.2-SNAPSHOT",   
  "org.scalanlp" %% "breeze-learn" % "0.2-SNAPSHOT"
)

