# Konfig [![Travis](https://img.shields.io/travis/vpon/konfig.svg?style=flat-square)](https://travis-ci.org/vpon/konfig)

A scala helper library for [typesafe config](https://github.com/typesafehub/config)

```
resolvers += Resolver.bintrayRepo("vpon", "maven")
libraryDependencies += "com.vpon" %% "konfig" % "0.1-M2"
```

_bintray for now, we will publish to jcenter or maven central if enough people use it_

## examples

```javascript
// application.conf
my-app {
    host = "0.0.0.0"
    port = 8080
}
```

```scala
// scala code
case class MyAppConfig(host: String, port: Int)
val config = ConfigFactory.load().read[MyAppConfig]("my-app")
```

more: [example.scala](https://github.com/vpon/konfig/blob/master/src/test/scala/com/example/example.scala), [test case](https://github.com/vpon/konfig/blob/master/src/test/scala/com/vpon/konfig/konfig.scala)


## license

```
Copyright 2016 Vpon, Inc

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
