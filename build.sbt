name := "term-parser"

version := "1.0.0"

scalaVersion := "2.13.1"

libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"
libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.29"
libraryDependencies += "com.github.pathikrit" %% "better-files" % "3.8.0"
libraryDependencies += "com.github.scopt" %% "scopt" % "4.0.0-RC2"
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.2.0"
libraryDependencies += "org.ccil.cowan.tagsoup" % "tagsoup" % "1.2.1"
libraryDependencies += "com.lihaoyi" %% "pprint" % "0.5.6"

assemblyJarName in assembly := { s"${name.value}-${version.value}.jar" }
