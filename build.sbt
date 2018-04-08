organization in Global  := "com.quadas"
name in Global          := "konfig"
scalaVersion in Global  := "2.12.4"
crossScalaVersions in Global := Seq("2.11.12", "2.12.4")

lazy val root = project.in(file("."))
  .settings(publish := {})
  .settings(publishLocal := {})
  .aggregate(konfig, `konfig-twitter-util`)

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

  lazy val commonSettings = Seq(
    scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-Ypartial-unification", "-feature", "-language:higherKinds")
  )

libraryDependencies in Global ++= Seq(
  "org.scalacheck"      %%  "scalacheck"    % "1.13.5" % "test",
  "org.scalatest"       %%  "scalatest"     % "3.0.5" % "test"
)

lazy val coreDependencies = Seq(
  libraryDependencies ++= Seq(
    "com.typesafe"        %   "config"        % "1.3.3",
    "com.chuusai"         %%  "shapeless"     % "2.3.3"
  )
)

lazy val twitterUtilDependencies = Seq(
  libraryDependencies ++= Seq(
    "com.twitter"   %% "util-core"  % "18.2.0"
  )
)

lazy val wartSettings = Seq(
  wartremoverErrors := Warts.allBut()
)

lazy val publishSettings = Seq(
  licenses                += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
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
