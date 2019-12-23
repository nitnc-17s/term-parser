package pw._0x9.termparser

import scalaz.Scalaz._
import scala.util.parsing.combinator._

object KanjiNumberParser extends RegexParsers {
  def one   = "一" ^^ { _ => 1 }
  def two   = "二" ^^ { _ => 2 }
  def three = "三" ^^ { _ => 3 }
  def four  = "四" ^^ { _ => 4 }
  def five  = "五" ^^ { _ => 5 }
  def six   = "六" ^^ { _ => 6 }
  def seven = "七" ^^ { _ => 7 }
  def eight = "八" ^^ { _ => 8 }
  def nine  = "九" ^^ { _ => 9 }

  def oneDigit =
    one | two | three | four | five | six | seven | eight | nine

  def ju    = opt(oneDigit) <~ "十" ^^ { n => (n | 1) * 10 }
  def hyaku = opt(oneDigit) <~ "百" ^^ { n => (n | 1) * 100 }
  def sen   = opt(oneDigit) <~ "千" ^^ { n => (n | 1) * 1000 }

  def threeDigits = opt(sen) ~ opt(hyaku) ~ opt(ju) ~ opt(oneDigit) ^^ {
    case a ~ b ~ c ~ d => ~ (a |+| b |+| c |+| d)
  }

  def kanjiNumber = opt(threeDigits) ^^ { x => x.getOrElse(0) }

  def parse(in: String) = parseAll(kanjiNumber, in)
}
