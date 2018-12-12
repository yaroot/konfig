organization in Global := "com.quadas"
name in Global := "konfig"
scalaVersion in Global := "2.12.8"
crossScalaVersions in Global := Seq(scalaVersion.value)

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
  scalacOptions := Seq(
    "-unchecked",
    "-deprecation",
    "-encoding",
    "utf8",
    "-Ypartial-unification",
    "-feature",
    "-language:higherKinds"
  ),
  scalafmtOnCompile := true
)

libraryDependencies in Global ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.14.0" % "test",
  "org.scalatest"  %% "scalatest"  % "3.0.5"  % "test"
)

lazy val coreDependencies = Seq(
  libraryDependencies ++= Seq(
    "com.typesafe"  % "config"     % "1.3.3",
    "com.chuusai"   %% "shapeless" % "2.3.3",
    "org.typelevel" %% "cats-core" % "1.5.0",
    "org.typelevel" %% "kittens"   % "1.2.0"
  )
)

lazy val twitterUtilDependencies = Seq(
  libraryDependencies ++= Seq(
    "com.twitter" %% "util-core" % "18.12.0"
  )
)

lazy val refinedDependencies = Seq(
  libraryDependencies ++= Seq(
    "eu.timepit" %% "refined"            % "0.9.3",
    "eu.timepit" %% "refined-scalacheck" % "0.9.3" % Test
  )
)

lazy val wartSettings = Seq(
  wartremoverErrors := Warts.allBut()
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
