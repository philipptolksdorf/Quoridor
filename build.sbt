val scala3Version = "3.2.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "Quoridor",
    version := "0.10.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.14",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.14" % "test",
    libraryDependencies += "com.google.inject" % "guice" % "5.1.0",
    libraryDependencies += "net.codingwell" %% "scala-guice" % "5.1.0" cross CrossVersion.for3Use2_13,
    libraryDependencies ++= {
      lazy val osName = System.getProperty("os.name") match {
        case n if n.startsWith("Linux") => "linux"
        case n if n.startsWith("Mac") => "mac-aarch64"
        case n if n.startsWith("Windows") => "win"
        case _ => throw new Exception("Unknown platform!")
      }
      Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
        .map(m => "org.openjfx" % s"javafx-$m" % "19" classifier osName)
    },
    libraryDependencies += "org.scalafx" %% "scalafx" % "19.0.0-R30")
