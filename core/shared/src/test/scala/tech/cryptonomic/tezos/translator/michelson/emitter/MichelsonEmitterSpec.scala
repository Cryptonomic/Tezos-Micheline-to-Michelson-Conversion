package tech.cryptonomic.tezos.translator.michelson.emitter

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import tech.cryptonomic.tezos.translator.michelson.ast._
import tech.cryptonomic.tezos.translator.michelson.emitter.MichelsonEmitter._

class MichelsonEmitterSpec extends AnyFlatSpec with Matchers {

  it should "render single MichelsonType" in {
    Type("contract").emit shouldBe "contract"
  }

  it should "render one-argument MichelsonType" in {
    Type("option", List(Type("address"))).emit shouldBe "(option address)"
  }

  it should "render two-argument MichelsonType" in {
    Type("pair", List(Type("int"), Type("address"))).emit shouldBe "(pair int address)"
  }

  it should "render MichelsonType with int constant" in {
    Type("some", List(IntConstant("12"))).emit shouldBe "(some 12)"
  }

  it should "render MichelsonType with string constant" in {
    Type("some", List(StringConstant("testValue"))).emit shouldBe "(some \"testValue\")"
  }

  it should "render MichelsonType with bytes constant" in {
    Type("some", List(BytesConstant("0500"))).emit shouldBe "(some 0x0500)"
  }

  it should "render MichelsonType with annotation" in {
    val michelsonType = Type(
      prim = "pair",
      args = List(Type("int", annotations = List("%x")), Type("int", annotations = List("%y"))),
      annotations = List(":point")
    )

    michelsonType.emit shouldBe "(pair :point (int %x) (int %y))"
  }

  it should "render complex MichelsonType" in {
    val michelsonType = Type(
      "contract",
      List(Type("or", List(Type("option", List(Type("address"))), Type("int"))))
    )

    michelsonType.emit shouldBe "(contract (or (option address) int))"
  }

  it should "render instruction list with only one simple instruction" in {

    val michelsonCode = Schema(EmptyExpression, EmptyExpression, Code(List(SingleInstruction.simple("CDR"))))

    michelsonCode.emit shouldBe s"""parameter {};
                                   |storage {};
                                   |code { CDR }""".stripMargin
  }

  it should "render MichelsonCode with two simple instructions" in {
    val michelsonCode = Schema(
      EmptyExpression,
      EmptyExpression,
      Code(List(SingleInstruction.simple("CDR"), SingleInstruction.simple("DUP")))
    )

    michelsonCode.emit shouldBe s"""parameter {};
                                   |storage {};
                                   |code { CDR ;
                                   |       DUP }""".stripMargin
  }

  it should "render MichelsonCode with typed instruction" in {
    val michelsonCode =
      Schema(EmptyExpression, EmptyExpression, Code(List(SingleInstruction("NIL", List(Type("operation")), Nil))))

    michelsonCode.emit shouldBe s"""parameter {};
                                   |storage {};
                                   |code { NIL operation }""".stripMargin
  }

  it should "render MichelsonCode with typed instruction with constant" in {
    val michelsonCode = Schema(
      EmptyExpression,
      EmptyExpression,
      Code(List(SingleInstruction("PUSH", List(Type("mutez"), IntConstant("0")), Nil)))
    )

    michelsonCode.emit shouldBe s"""parameter {};
                                   |storage {};
                                   |code { PUSH mutez 0 }""".stripMargin
  }

  it should "render MichelsonCode with instruction sequence" in {
    val michelsonCode = Schema(
      EmptyExpression,
      EmptyExpression,
      Code(
        List(InstructionSequence(List(SingleInstruction.simple("DIP"), SingleInstruction.simple("SWAP"))))
      )
    )

    michelsonCode.emit shouldBe s"""parameter {};
                                   |storage {};
                                   |code { { DIP ; SWAP } }""".stripMargin
  }

  it should "render MichelsonCode with complex instruction" in {
    val code = Code(
      List(
        InstructionSequence(
          List(
            SingleInstruction(
              "DIP",
              List(InstructionSequence(List(SingleInstruction.simple("DUP")))),
              Nil
            )
          )
        )
      )
    )

    val michelsonCode = Schema(
      EmptyExpression,
      EmptyExpression,
      code
    )
    michelsonCode.emit shouldBe s"""parameter {};
                                   |storage {};
                                   |code { { DIP { DUP } } }""".stripMargin
  }

  it should "render MichelsonInstruction with empty embedded instruction" in {
    val michelsonInstruction = InstructionSequence(
      List(
        SingleInstruction(
          "IF_NONE",
          List(
            InstructionSequence(
              List(
                InstructionSequence(
                  List(SingleInstruction.simple("UNIT"), SingleInstruction.simple("FAILWITH"))
                )
              )
            ),
            EmptyInstruction
          ),
          Nil
        )
      )
    )

    michelsonInstruction.emit shouldBe "{ IF_NONE { { UNIT ; FAILWITH } } {} }"
  }

  it should "render MichelsonInstruction IF" in {
    val michelsonInstruction =
      SingleInstruction(
        "IF",
        List(
          InstructionSequence(
            List(
              SingleInstruction(
                "PUSH",
                List(Type("string"), StringConstant("The image bid contract is now closed")),
                Nil
              ),
              SingleInstruction.simple("FAILWITH")
            )
          ),
          InstructionSequence(List(SingleInstruction.simple("UNIT")))
        ),
        Nil
      )

    michelsonInstruction.emit shouldBe
      """IF { PUSH string "The image bid contract is now closed" ;
        |     FAILWITH }
        |   { UNIT }""".stripMargin
  }

  it should "render MichelsonInstruction with embedded IF-s" in {
    val michelsonInstruction =
      SingleInstruction(
        "IF",
        List(
          InstructionSequence(
            List(
              SingleInstruction(
                "PUSH",
                List(Type("string"), StringConstant("The image bid contract is now closed")),
                Nil
              ),
              SingleInstruction.simple("FAILWITH")
            )
          ),
          InstructionSequence(
            List(
              SingleInstruction(
                "IF",
                List(
                  InstructionSequence(
                    List(
                      SingleInstruction(
                        "PUSH",
                        List(Type("string"), StringConstant("The image bid contract is now closed")),
                        Nil
                      ),
                      SingleInstruction.simple("FAILWITH")
                    )
                  ),
                  InstructionSequence(List(SingleInstruction.simple("UNIT")))
                ),
                Nil
              )
            )
          )
        ),
        Nil
      )

    michelsonInstruction.emit shouldBe
      """IF { PUSH string "The image bid contract is now closed" ;
        |     FAILWITH }
        |   { IF { PUSH string "The image bid contract is now closed" ;
        |          FAILWITH }
        |        { UNIT } }""".stripMargin
  }

  it should "render empty MichelsonSchema" in {
    Schema.empty.emit shouldBe ""
  }

  it should "render MichelsonInstruction with an annotation" in {
    val michelsonInstruction = SingleInstruction("CAR", Nil, annotations = List("@pointcolor"))

    michelsonInstruction.emit shouldBe "CAR @pointcolor"
  }

  it should "render complex MichelsonCode" in {
    val expr = Code(
      List(
        SingleInstruction.simple("CDR"),
        SingleInstruction.simple("DUP"),
        SingleInstruction("NIL", List(Type("operation")), Nil),
        InstructionSequence(
          List(
            SingleInstruction(
              "DIP",
              List(
                InstructionSequence(
                  List(
                    SingleInstruction(
                      "DIP",
                      List(InstructionSequence(List(SingleInstruction.simple("DUP")))),
                      Nil
                    ),
                    SingleInstruction.simple("SWAP")
                  )
                )
              ),
              Nil
            ),
            SingleInstruction.simple("SWAP")
          )
        ),
        InstructionSequence(
          List(
            SingleInstruction(
              "DIP",
              List(
                InstructionSequence(
                  List(
                    SingleInstruction(
                      "DIP",
                      List(InstructionSequence(List(SingleInstruction.simple("DUP")))),
                      Nil
                    ),
                    SingleInstruction("NIL", List(Type("operation")), Nil)
                  )
                )
              ),
              Nil
            ),
            SingleInstruction.simple("SWAP")
          )
        )
      )
    )

    val michelsonCode = Schema(
      EmptyExpression,
      EmptyExpression,
      expr
    )
    michelsonCode.emit shouldBe s"""parameter {};
                                   |storage {};
                                   |code { CDR ;
                                   |       DUP ;
                                   |       NIL operation ;
                                   |       { DIP { DIP { DUP } ; SWAP } ; SWAP } ;
                                   |       { DIP { DIP { DUP } ; NIL operation } ; SWAP } }""".stripMargin
  }
}
