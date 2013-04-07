name := "topicmodel-eval"

version := "1.0-SNAPSHOT"

organization := "edu.utexas"

scalaVersion := "2.10.1"

crossPaths := false

retrieveManaged := true

libraryDependencies ++= Seq(
  "cc.mallet" % "mallet" % "2.0.7",
  "org.ccil.cowan.tagsoup" % "tagsoup" % "1.2.1",
  "org.apache.commons" % "commons-vfs2" % "2.0",
  "org.rogach" %% "scallop" % "0.8.1",
  "org.scalanlp" %% "breeze-math" % "0.2",
  "org.scalanlp" %% "breeze-learn" % "0.2"
)
