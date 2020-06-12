organization in Global := "com.quadas"
name in Global := "konfig"
scalaVersion in Global := "2.12.11"
crossScalaVersions in Global := Seq("2.12.11", "2.13.2")

lazy val root = project
  .in(file("."))
  .settings(publish := {})
  .settings(publishLocal := {})
  .aggregate(konfig, `konfig-twitter-util`, `konfig-refined`)

lazy val konfig = project
  .settings(name := "konfig")
  .settings(commonSettings)
  .settings(coreDependencies)
  .settings(wartSettings)
  .settings(publishSettings)

lazy val `konfig-twitter-util` = project
  .settings(name := "konfig-twitter-util")
  .settings(commonSettings)
  .settings(twitterUtilDependencies)
  .settings(wartSettings)
  .settings(publishSettings)
  .dependsOn(konfig)

lazy val `konfig-refined` = project
  .settings(name := "konfig-refined")
  .settings(commonSettings)
  .settings(refinedDependencies)
  .settings(wartSettings)
  .settings(publishSettings)
  .dependsOn(konfig)

lazy val commonSettings = Seq(
  scalafmtOnCompile := true
)

libraryDependencies in Global ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.14.3" % "test",
  "org.scalatest"  %% "scalatest"  % "3.1.2"  % "test"
)

lazy val coreDependencies = Seq(
  libraryDependencies ++= Seq(
    "com.typesafe"  % "config"     % "1.4.0",
    "com.chuusai"   %% "shapeless" % "2.3.3",
    "org.typelevel" %% "cats-core" % "2.1.1",
    "org.typelevel" %% "kittens"   % "2.1.0"
  )
)

lazy val twitterUtilDependencies = Seq(
  libraryDependencies ++= Seq(
    "com.twitter" %% "util-core" % "20.5.0"
  )
)

lazy val refinedDependencies = Seq(
  libraryDependencies ++= Seq(
    "eu.timepit" %% "refined"            % "0.9.14",
    "eu.timepit" %% "refined-scalacheck" % "0.9.14" % Test
  )
)

lazy val wartSettings = Seq(
  //wartremoverErrors := Warts.allBut()
)

lazy val publishSettings = Seq(
  licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
  // bintrayRepository       := "maven",
  // bintrayOrganization     := Some("quadas"),
  // bintrayReleaseOnPublish := false,
  pomExtra := {
    <url>https://github.com/quadas/konfig</url>
      <scm>
        <connection>scm:git:https://github.com/quadas/konfig.git</connection>
        <developerConnection>scm:git:https://github.com/quadas/konfig.git</developerConnection>
        <url>https://github.com/quadas/konfig</url>
      </scm>
      <developers>
        <developer>
          <name>Yan Su</name>
          <email>yan.su@quadas.com</email>
        </developer>
      </developers>
  }
)
