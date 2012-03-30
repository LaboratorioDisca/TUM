import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "TIU Backend"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      "postgresql" % "postgresql" % "9.1-901.jdbc4",
      "com.vividsolutions" % "jts" % "1.11"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      
    )

}
