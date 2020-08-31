# Tezos-Micheline-to-Michelson-Conversion
Scala library to covert Tezos Micheline representation into Michelson. 

Cross-compiled to Javascript and published to NPM as [tezos-micheline-to-michelson-conversion](https://www.npmjs.com/package/tezos-micheline-to-michelson-conversion) package.

## How to use (Scala)

Add the dependency into your `build.sbt`:

```scala
libraryDependencies += "tech.cryptonomic" %% "tezos-micheline-to-michelson-conversion" % "0.1.0"
```

And then you can run the conversion the following way:

```scala
import tech.cryptonomic.tezos.translator.michelson.MichelineToMichelson
import tech.cryptonomic.tezos.translator.michelson.ast.Schema

val micheline: String = "..."

val michelson: Either[Throwable, String] = MichelineToMichelson.convert[Schema](micheline)

```

If you want to have access to parsed Micheline AST before emitting Michelson, use `Parser`:
```scala
import tech.cryptonomic.tezos.translator.michelson.parser.Parser
import tech.cryptonomic.tezos.translator.michelson.ast.Schema
import tech.cryptonomic.tezos.translator.michelson.emitter.MichelsonEmitter._

val micheline: String = "..."

val ast: Either[Throwable, Schema] = Parser.parse[Schema](micheline)
// produces Michelson code string
ast.map(_.emit)
```

## How to use (NPM)

```
npm install tezos-micheline-to-michelson-conversion
```

```javascript
var translator = require("tezos-micheline-to-michelson-conversion").Translator
var input = "..."
translator.translate(input)
```

## Publishing (for maintainers)

To be able to publish a new version, you need
 - to be logged in under NPM account, that has maintainer access to the repo (`npm login`)
 - to have sonatype credentials specified in `~/.sbt/1.0/sonatype.sbt`, for example:
```
credentials += Credentials(
  "Sonatype Nexus Repository Manager",
  "oss.sonatype.org",
  "login",
  "password"
)
```

When these are setup, you can launch interactive release process with `sbt release`, which will prompt you for a release version number and eventually publish both maven jars and npm package.
