package pw._0x9.termparser

import scala.util.parsing.combinator._

sealed trait ChapterContent
case class Chapter(number: Int, title: String, contents: List[ChapterContent])

sealed trait SupplementaryProvisionsContent
case class SupplementaryProvisions(contents: List[SupplementaryProvisionsContent]) {
  val title = "附則"
}

sealed trait SectionContent
case class Section(number: Int, title: String, contents: List[SectionContent]) extends ChapterContent

sealed trait ArticleContent
case class Article(number: Int, title: Option[String], contents: List[ArticleContent]) extends ChapterContent with SectionContent with SupplementaryProvisionsContent

sealed trait ParagraphContent
case class Paragraph(number: Int, contents: List[ParagraphContent]) extends ArticleContent
case class ParagraphText(text: String) extends ParagraphContent

sealed trait OrderedListItemContent
case class OrderedListItem(indentTimes: Int, number: Int, contents: List[OrderedListItemContent]) extends ParagraphContent
case class OrderedListItemText(text: String) extends OrderedListItemContent

object TermParser extends RegexParsers {
  override val skipWhitespace = false

  def eol = "\r".? <~ "\n"
  def notSymbol = """[^!"#$%&'()*+\-.,/:;<=>?@\[\\\]^_`{|}~\t\n\r\f]+""".r

  def chapterNumber = "第" ~> """\d+""".r <~ "章" ^^ { _.toInt }
  def chapterTitle = notSymbol
  def chapterHead = (chapterNumber <~ " ") ~ (chapterTitle <~ eol.+)
  def chapter = chapterHead ~ (section.+ | article.+) ^^ { result => Chapter(result._1._1, result._1._2, result._2) }

  def supplementaryProvisions = "附則" ~> article.+ ^^ { result => SupplementaryProvisions(result) }

  def sectionNumber = "第" ~> """\d+""".r <~ "節" ^^ { _.toInt }
  def sectionTitle = notSymbol
  def sectionHead = (sectionNumber <~ " ") ~ (sectionTitle <~ eol.+)
  def section = sectionHead ~ article.+ ^^ { result => Section(result._1._1, result._1._2, result._2) }

  def articleNumber = "第" ~> """\d+""".r <~ "条" ^^ { _.toInt }
  def articleTitle = "(" ~> notSymbol <~ ")"
  def articleHead = (articleTitle <~ eol).? ~ (articleNumber <~ eol)
  def article = articleHead ~ paragraph.+ ^^ { result => Article(result._1._2, result._1._1, result._2) }

  def paragraphNumber = """\d+""".r ^^ { result => if (result.isEmpty) 0 else result.toInt }
  def paragraphContent = listItem | (".+".r ^^ { result => ParagraphText(result) })
  def paragraph = not(chapterHead | sectionHead | articleHead) ~> (paragraphNumber <~ eol).? ~ repsep(paragraphContent, eol) <~ eol.+ ^^ {
    case None~contents => Paragraph(1, contents)
    case Some(number)~contents => Paragraph(number, contents)
  }

  def listItemNumber = "([一二三四五六七八九〇]|[イロハニホヘト])+".r ^^ { result => {
    val numericCharacter = Map(
      "一" -> 1,
      "二" -> 2,
      "三" -> 3,
      "四" -> 4,
      "五" -> 5,
      "六" -> 6,
      "七" -> 7,
      "八" -> 8,
      "九" -> 9,
      "十" -> 10,
      "イ" -> 1,
      "ロ" -> 2,
      "ハ" -> 3,
      "ニ" -> 4,
      "ホ" -> 5,
      "ヘ" -> 6,
      "ト" -> 7
    )
    numericCharacter.getOrElse(result, 0)
  } }
  def listItemContent: TermParser.Parser[OrderedListItemContent] = ".+".r ^^ { result => OrderedListItemText(result) }
  def listItem = """ {4}""".r.+ ~ (listItemNumber <~ " ") ~ listItemContent ^^ {
    result => OrderedListItem(result._1._1.length, result._1._2, List(result._2))
  }

  def term = eol.? ~> (supplementaryProvisions | chapter).+

  def parse(input: String) = parseAll(term, input)
  def apply(input: String) = parse(input) match {
    case Success(data, next) => Right(data)
    case NoSuccess(errorMessage, next) => Left(s"$errorMessage on line ${next.pos.line} on column ${next.pos.column}")
  }
}
