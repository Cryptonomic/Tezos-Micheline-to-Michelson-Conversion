package tech.cryptonomic.tezos.translator.michelson.parser

import org.scalatest._
import org.scalatest.matchers.should.Matchers
import tech.cryptonomic.tezos.translator.michelson.ast._
import Parser._

class JsonParserSpec extends FlatSpec with Matchers {

  it should "parse one-argument MichelsonType" in {
    val json = """{"prim": "contract"}"""

    parse[Expression](json) should equal(Right(Type("contract")))
  }

  it should "parse two-argument MichelsonType" in {
    val json =
      """{
        |  "prim": "pair",
        |  "args": [
        |    {
        |      "prim": "int"
        |    },
        |    {
        |      "prim": "address"
        |    }
        |  ]
        |}""".stripMargin

    parse[Expression](json) should equal(
      Right(Type("pair", List(Type("int"), Type("address"))))
    )
  }

  it should "parse complex MichelsonType" in {
    val json =
      """{
        |  "prim": "contract",
        |  "args": [
        |    {
        |      "prim": "or",
        |      "args": [
        |        {
        |          "prim": "option",
        |          "args": [
        |            {
        |              "prim": "address"
        |            }
        |          ]
        |        },
        |        {
        |          "prim": "int"
        |        }
        |      ]
        |    }
        |  ]
        |}""".stripMargin

    parse[Expression](json) should equal(
      Right(
        Type(
          "contract",
          List(
            Type("or", List(Type("option", List(Type("address"))), Type("int")))
          )
        )
      )
    )
  }

  it should "parse MichelsonType with annotation" in {
    val json =
      """{
        |  "prim": "int",
        |  "annots": [
        |    ":p"
        |  ]
        |}""".stripMargin

    parse[Expression](json) should equal(Right(Type(prim = "int", annotations = List(":p"))))
  }

  it should "parse MichelsonInstruction with only one simple instruction" in {
    val json = """{"prim": "DUP"}"""

    parse[Instruction](json) should equal(Right(SingleInstruction.simple("DUP")))
  }

  it should "parse MichelsonInstructionSequence" in {
    val json = """[{"prim": "CDR"}, {"prim": "DUP"}]"""

    parse[Instruction](json) should equal(
      Right(InstructionSequence(List(SingleInstruction.simple("CDR"), SingleInstruction.simple("DUP"))))
    )
  }

  it should "parse typed MichelsonInstruction" in {
    val json = """{"prim": "NIL", "args": [{"prim": "operation"}]}"""

    parse[Instruction](json) should equal(
      Right(SingleInstruction("NIL", List(Type("operation")), Nil))
    )
  }

  it should "parse MichelsonInstruction with annotation" in {
    val json = """{"prim": "CAR", "annots": ["@pointcolor"]}"""

    parse[Instruction](json) should equal(
      Right(SingleInstruction("CAR", Nil, List("@pointcolor")))
    )
  }

  it should "parse complex MichelsonInstruction" in {
    val json = """[{"prim": "DIP", "args": [[{"prim": "DUP"}]]}]"""

    parse[Instruction](json) should equal(
      Right(
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
  }

  it should "parse MichelsonInstruction typed with int data" in {
    val json =
      """[{
        |  "prim": "PUSH",
        |  "args": [
        |    {
        |      "prim": "mutez"
        |    },
        |    {
        |      "int": "0"
        |    }
        |  ]
        |}]""".stripMargin

    parse[Instruction](json) should equal(
      Right(
        InstructionSequence(
          List(SingleInstruction("PUSH", List(Type("mutez"), IntConstant("0")), Nil))
        )
      )
    )
  }

  it should "parse MichelsonInstruction typed with string data" in {
    val json =
      """[{
        |  "prim": "PUSH",
        |  "args": [
        |    {
        |      "prim": "mutez"
        |    },
        |    {
        |      "string": "0"
        |    }
        |  ]
        |}]""".stripMargin

    parse[Instruction](json) should equal(
      Right(
        InstructionSequence(
          List(SingleInstruction("PUSH", List(Type("mutez"), StringConstant("0")), Nil))
        )
      )
    )
  }

  it should "parse MichelsonInstruction typed with bytes data" in {
    val json =
      """{
        |  "prim": "PUSH",
        |  "args": [
        |    {
        |      "prim": "bytes"
        |    },
        |    {
        |      "bytes": "0500"
        |    }
        |  ]
        |}""".stripMargin

    parse[Instruction](json) should equal(
      Right(SingleInstruction("PUSH", List(Type("bytes"), BytesConstant("0500")), Nil))
    )
  }

  it should "parse double embedded MichelsonInstruction" in {
    val json =
      """[
        |  {
        |    "prim": "IF_NONE",
        |    "args": [
        |      [
        |        [
        |          {
        |            "prim": "UNIT"
        |          },
        |          {
        |            "prim": "FAILWITH"
        |          }
        |        ]
        |      ]
        |    ]
        |  }
        |]""".stripMargin

    parse[Instruction](json) should equal(
      Right(
        InstructionSequence(
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
                )
              ),
              Nil
            )
          )
        )
      )
    )
  }

  it should "parse empty MichelsonInstruction" in {
    val json =
      """[
        |  {
        |    "prim": "IF_NONE",
        |    "args": [
        |      [],
        |      [
        |        [
        |          {
        |            "prim": "UNIT"
        |          },
        |          {
        |            "prim": "FAILWITH"
        |          }
        |        ]
        |      ],
        |      []
        |    ]
        |  }
        |]""".stripMargin

    parse[Instruction](json) should equal(
      Right(
        InstructionSequence(
          List(
            SingleInstruction(
              "IF_NONE",
              List(
                EmptyInstruction,
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
      )
    )
  }

  it should "parse empty MichelsonInstruction when it appears alone" in {
    val json =
      """[
        |  {
        |    "prim": "IF_NONE",
        |    "args": [
        |      []
        |    ]
        |  }
        |]""".stripMargin

    parse[Instruction](json) should equal(
      Right(
        InstructionSequence(List(SingleInstruction("IF_NONE", List(EmptyInstruction), Nil)))
      )
    )
  }

  it should "parse empty MichelsonExpression" in {
    val json =
      """{
        |  "prim": "Pair",
        |  "args": [
        |    {
        |      "int": "0"
        |    },
        |    []
        |  ]
        |}""".stripMargin

    parse[Expression](json) should equal(
      Right(Type("Pair", List(IntConstant("0"), EmptyInstruction), Nil))
    )
  }

  it should "parse LAMBDA MichelsonInstruction" in {
    val json =
      """{
        |  "prim": "LAMBDA",
        |  "args": [
        |    {
        |      "prim": "address"
        |    },
        |    {
        |      "prim": "contract",
        |      "args": [
        |        {
        |          "prim": "unit"
        |        }
        |      ]
        |    },
        |    [
        |      {
        |        "prim": "DUP"
        |      }
        |    ]
        |  ]
        |}""".stripMargin

    parse[Instruction](json) should equal(
      Right(
        SingleInstruction(
          "LAMBDA",
          List(
            Type("address"),
            Type("contract", List(Type("unit"))),
            InstructionSequence(List(SingleInstruction.simple("DUP")))
          ),
          Nil
        )
      )
    )
  }

  it should "parse MichelsonExpression with both MichelsonExpression and MichelsonInstruction as arguments" in {
    val json =
      """{
        |  "prim": "Pair",
        |  "args": [
        |    [
        |      {
        |        "prim": "Elt",
        |        "args": [
        |          {
        |            "int": "0"
        |          }
        |        ]
        |      }
        |    ],
        |    {
        |      "string": "Author: Teckhua Chiang, Company: Cryptonomic"
        |    }
        |  ]
        |}
        |""".stripMargin

    parse[Expression](json) should equal(
      Right(
        Type(
          "Pair",
          List(
            InstructionSequence(List(SingleInstruction("Elt", List(IntConstant("0")), Nil))),
            StringConstant("Author: Teckhua Chiang, Company: Cryptonomic")
          ),
          List()
        )
      )
    )
  }

  it should "convert simplest json to MichelsonSchema" in {

    val json =
      """[
        |  {
        |    "prim": "parameter",
        |    "args": [
        |      {
        |        "prim": "int"
        |      }
        |    ]
        |  },
        |  {
        |    "prim": "storage",
        |    "args": [
        |      {
        |        "prim": "int"
        |      }
        |    ]
        |  },
        |  {
        |    "prim": "code",
        |    "args": [
        |      [
        |        {
        |          "prim": "DUP"
        |        }
        |      ]
        |    ]
        |  }
        |]""".stripMargin

    parse[Schema](json) should equal(
      Right(
        Schema(
          Type("int"),
          Type("int"),
          Code(List(SingleInstruction.simple("DUP")))
        )
      )
    )
  }

  it should "parse MichelsonCode" in {
    val json = """[{"prim": "DUP"}]"""

    parse[Code](json) should equal(Right(Code(List(SingleInstruction.simple("DUP")))))
  }

  it should "give meaningful error in case of json without parameter section" in {
    val json = """[{"prim": "storage", "args": []}]"""

    parse[Schema](json) should equal(Left(ParserError("No expression parameter found")))
  }

  it should "give meaningful error in case of json without code section" in {
    val json =
      """[{"prim": "parameter", "args": [{"prim": "unit"}]}, {"prim": "storage", "args": [{"prim": "unit"}]}]"""

    parse[Schema](json) should equal(Left(ParserError("No code section found")))
  }

  it should "parse empty schema" in {
    val json = """[]"""

    parse[Schema](json) should equal(Right(Schema.empty))
  }

  it should "convert complex json to MichelsonSchema" in {

    val json =
      """[
        |  {
        |    "prim": "parameter",
        |    "args": [
        |      {
        |        "prim": "unit"
        |      }
        |    ]
        |  },
        |  {
        |    "prim": "storage",
        |    "args": [
        |      {
        |        "prim": "contract",
        |        "args": [
        |          {
        |            "prim": "or",
        |            "args": [
        |              {
        |                "prim": "option",
        |                "args": [
        |                  {
        |                    "prim": "address"
        |                  }
        |                ]
        |              },
        |              {
        |                "prim": "or",
        |                "args": [
        |                  {
        |                    "prim": "pair",
        |                    "args": [
        |                      {
        |                        "prim": "option",
        |                        "args": [
        |                          {
        |                            "prim": "address"
        |                          }
        |                        ]
        |                      },
        |                      {
        |                        "prim": "option",
        |                        "args": [
        |                          {
        |                            "prim": "mutez"
        |                          }
        |                        ]
        |                      }
        |                    ]
        |                  },
        |                  {
        |                    "prim": "or",
        |                    "args": [
        |                      {
        |                        "prim": "mutez"
        |                      },
        |                      {
        |                        "prim": "or",
        |                        "args": [
        |                          {
        |                            "prim": "pair",
        |                            "args": [
        |                              {
        |                                "prim": "option",
        |                                "args": [
        |                                  {
        |                                    "prim": "address"
        |                                  }
        |                                ]
        |                              },
        |                              {
        |                                "prim": "option",
        |                                "args": [
        |                                  {
        |                                    "prim": "mutez"
        |                                  }
        |                                ]
        |                              }
        |                            ]
        |                          },
        |                          {
        |                            "prim": "address"
        |                          }
        |                        ]
        |                      }
        |                    ]
        |                  }
        |                ]
        |              }
        |            ]
        |          }
        |        ]
        |      }
        |    ]
        |  },
        |  {
        |    "prim": "code",
        |    "args": [
        |      [
        |        {
        |          "prim": "CDR"
        |        },
        |        {
        |          "prim": "DUP"
        |        },
        |        {
        |          "prim": "NIL",
        |          "args": [
        |            {
        |              "prim": "operation"
        |            }
        |          ]
        |        },
        |        [
        |          {
        |            "prim": "DIP",
        |            "args": [
        |              [
        |                {
        |                  "prim": "DIP",
        |                  "args": [
        |                    [
        |                      {
        |                        "prim": "DUP"
        |                      }
        |                    ]
        |                  ]
        |                },
        |                {
        |                  "prim": "SWAP"
        |                }
        |              ]
        |            ]
        |          },
        |          {
        |            "prim": "SWAP"
        |          },
        |          {
        |            "prim": "NIL",
        |            "args": [
        |              {
        |                "prim": "operation"
        |              }
        |            ]
        |          }
        |        ]
        |      ]
        |    ]
        |  }
        |]""".stripMargin

    parse[Schema](json) should equal(
      Right(
        Schema(
          Type("unit"),
          Type(
            "contract",
            List(
              Type(
                "or",
                List(
                  Type("option", List(Type("address"))),
                  Type(
                    "or",
                    List(
                      Type(
                        "pair",
                        List(
                          Type("option", List(Type("address"))),
                          Type("option", List(Type("mutez")))
                        )
                      ),
                      Type(
                        "or",
                        List(
                          Type("mutez"),
                          Type(
                            "or",
                            List(
                              Type(
                                "pair",
                                List(
                                  Type("option", List(Type("address"))),
                                  Type("option", List(Type("mutez")))
                                )
                              ),
                              Type("address")
                            )
                          )
                        )
                      )
                    )
                  )
                )
              )
            )
          ),
          Code(
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
                  SingleInstruction.simple("SWAP"),
                  SingleInstruction("NIL", List(Type("operation")), Nil)
                )
              )
            )
          )
        )
      )
    )
  }
}
