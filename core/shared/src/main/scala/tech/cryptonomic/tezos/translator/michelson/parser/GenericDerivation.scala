package tech.cryptonomic.tezos.translator.michelson.parser

import cats.syntax.functor._
import io.circe._
import io.circe.generic.auto._

object GenericDerivation {

  /** Provides deconding to an Either value, without needing a discrimination "tag" in the json source */
  implicit def decodeUntaggedEither[A, B](
    implicit leftDecoder: Decoder[A],
    rightDecoder: Decoder[B]
  ): Decoder[Either[A, B]] =
    leftDecoder.map(Left.apply) or rightDecoder.map(Right.apply)

  implicit val decodeSection: Decoder[JsonSection] =
    List[Decoder[JsonSection]](
      Decoder[JsonCodeSection]
        .ensure(_.prim == "code", "No code section found")
        .widen,
      Decoder[JsonExpressionSection].widen
    ).reduceLeft(_ or _)

  implicit val decodeExpression: Decoder[JsonExpression] =
    Decoder[JsonType].widen

  val decodeInstructionSequence: Decoder[JsonInstructionSequence] =
    _.as[List[JsonInstruction]].map(JsonInstructionSequence)

  implicit val decodeInstruction: Decoder[JsonInstruction] =
    List[Decoder[JsonInstruction]](
      decodeInstructionSequence.widen,
      Decoder[JsonSimpleInstruction].widen,
      Decoder[JsonIntConstant].widen,
      Decoder[JsonStringConstant].widen,
      Decoder[JsonBytesConstant].widen
    ).reduceLeft(_ or _)

}
