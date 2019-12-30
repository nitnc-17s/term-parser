package pw._0x9.termparser

import scala.util.matching.Regex
import scala.util.parsing.combinator._

sealed trait RootContent {
  def toHTML: String
}

case class Term(contents: List[TermContent]) extends RootContent {
  override def toHTML: String = s"""<div class="term">${contents.map(_.toHTML).mkString}</div>"""
}
sealed trait TermContent {
  def toHTML: String
}

case class Chapter(number: Int, title: String, contents: List[ChapterContent]) extends TermContent {
  private def id = s"c$number"

  override def toHTML: String = s"""<div id="$id" class="chapter"><h2>第${number}章 $title</h2>${contents.map(_.toHTML(number)).mkString}</div>"""
}
sealed trait ChapterContent {
  def toHTML(chapter: Int): String
}

case class SupplementaryProvisions(contents: List[SupplementaryProvisionsContent]) extends TermContent {
  private val title = "附則"
  private val number = -1

  private def id = "sp"

  override def toHTML: String = s"""<div id="$id" class="supplementary-provisions"><h2>$title</h2>${contents.map(_.toHTML(number)).mkString}</div>"""
}
sealed trait SupplementaryProvisionsContent {
  def toHTML(chapter: Int): String
}

case class Section(number: Int, title: String, contents: List[SectionContent]) extends ChapterContent {
  private def id(chapter: Int) = s"c${chapter}_s$number"

  override def toHTML(chapter: Int): String = s"""<div id="${id(chapter)}" class="section"><h3>第${number}節 $title</h3>${contents.map(_.toHTML(chapter, number)).mkString}</div>"""
}
sealed trait SectionContent {
  def toHTML(chapter: Int, section: Int): String
}

case class Article(number: Int, title: Option[String], contents: List[ArticleContent]) extends ChapterContent with SectionContent with SupplementaryProvisionsContent {
  private def id(chapter: Int) = if (chapter == -1) s"spa$number" else s"a$number"

  override def toHTML(chapter: Int): String = title match {
    case Some(a) => s"""<div id="${id(chapter)}" class="article"><span class="article-title">($a)</span><h4>第${number}条</h4>${contents.map(_.toHTML(chapter, number)).mkString}</div>"""
    case None => s"""<div id="${id(chapter)}" class="article"><h4>第${number}条</h4>${contents.map(_.toHTML(chapter, number)).mkString}</div>"""
  }

  override def toHTML(chapter: Int, section: Int): String = toHTML(chapter)
}
sealed trait ArticleContent {
  def toHTML(chapter: Int, article: Int): String
}

case class Paragraph(number: Int, contents: List[ParagraphContent]) extends ArticleContent {
  private val paragraphNumberHTML = if (number == 1) "" else s"<h5>$number</h5>"

  private def id(chapter: Int, article: Int) = if (chapter == -1) s"spa${article}_$number" else s"a${article}_p$number"

  override def toHTML(chapter: Int, article: Int): String = s"""<div id="${id(chapter, article)}" class="paragraph">$paragraphNumberHTML${contents.map(_.toHTML(article, number)).mkString}</div>"""
}
sealed trait ParagraphContent {
  def toHTML(article: Int, paragraph: Int): String
}
case class ParagraphText(texts: List[Text]) extends ParagraphContent {
  override def toHTML(article: Int, paragraph: Int): String = s"<p>${texts.map(_.toHTML(article: Int, paragraph: Int)).mkString}</p>"
}

sealed trait Text {
  def toHTML(article: Int, paragraph: Int): String
}
case class ListText(texts: List[Text])
case class PlainText(text: String) extends Text {
  override def toHTML(article: Int, paragraph: Int): String = text
}
case class LinkText(text: String, target: LinkTarget) extends Text {
  override def toHTML(article: Int, paragraph: Int): String = s"""<a data-scroll href="${target.toHTML(article, paragraph)}">$text</a>"""
}

sealed trait LinkTarget {
  def toHTML(article: Int, paragraph: Int): String
}
case class LinkTargetArticle(article: Int) extends LinkTarget {
  override def toHTML(article: Int, paragraph: Int): String = s"#a${this.article}"
}
case class LinkTargetParagraph(article: Option[Int], paragraph: Int) extends LinkTarget {
  override def toHTML(article: Int, paragraph: Int): String = {
    val p = if (this.paragraph == -1) paragraph - 1 else this.paragraph
    this.article match {
      case Some(a) => s"#a${a}_p$p"
      case None => s"#a${article}_p$p"
    }
  }
}

case class OrderedList(indentLevel: Int, contents: List[OrderedListContent]) extends ParagraphContent with OrderedListItemContent {
  override def toHTML(article: Int, paragraph: Int): String = s"""<ol class="ordered-list-type-$indentLevel">${contents.map(_.toHTML(article: Int, paragraph: Int)).mkString}</ol>"""
}
sealed trait OrderedListContent {
  def toHTML(article: Int, paragraph: Int): String
}

case class OrderedListItem(number: Int, contents: List[OrderedListItemContent]) extends OrderedListContent {
  override def toHTML(article: Int, paragraph: Int): String = s"""<li value="$number">${contents.map(_.toHTML(article: Int, paragraph: Int)).mkString}</li>"""
}
sealed trait OrderedListItemContent {
  def toHTML(article: Int, paragraph: Int): String
}
case class OrderedListItemText(texts: List[Text]) extends OrderedListItemContent {
  override def toHTML(article: Int, paragraph: Int): String = texts.map(_.toHTML(article: Int, paragraph: Int)).mkString
}

object TermParser extends RegexParsers {
  override val skipWhitespace = false

  var indentSize = 4

  def EOL: Parser[String] = "\n" | "\r\n" | "\r"
  def nonSymbols: Regex = """[^!"#$%&'()*+\-.,/:;<=>?@\[\\\]^_`{|}~\t\n\r\f]+""".r
  /** 並列の接続詞 */
  def parallelConjunctions: Parser[String] = "、" | "及び"

  /** 章 */
  def chapterNumber: Parser[Int] = "第" ~> """\d+""".r <~ "章" ^^ { _.toInt }
  def chapterTitle: Regex = nonSymbols
  def chapterHead: Parser[Int ~ String] = (chapterNumber <~ " ") ~ (chapterTitle <~ rep1(EOL))
  def chapter: Parser[Chapter] = chapterHead ~ (rep1(section) | rep1(article)) ^^ { result => Chapter(result._1._1, result._1._2, result._2) }

  /** 附則 */
  def supplementaryProvisionsHead: Parser[String] = "附則" <~ rep1(EOL)
  def supplementaryProvisions: Parser[SupplementaryProvisions] = supplementaryProvisionsHead ~> rep1(article) ^^ { SupplementaryProvisions(_) }

  /** 節 */
  def sectionNumber: Parser[Int] = "第" ~> """\d+""".r <~ "節" ^^ { _.toInt }
  def sectionTitle: Regex = nonSymbols
  def sectionHead: Parser[Int ~ String] = (sectionNumber <~ " ") ~ (sectionTitle <~ EOL.+)
  def section: Parser[Section] = sectionHead ~ rep1(article) ^^ { result => Section(result._1._1, result._1._2, result._2) }

  /** 条 */
  def articleNumber: Parser[Int] = "第" ~> """\d+""".r <~ "条" ^^ { _.toInt }
  def articleTitle: Parser[String] = "(" ~> nonSymbols <~ ")"
  def articleHead: Parser[Option[String] ~ Int] = opt(articleTitle <~ EOL) ~ (articleNumber <~ EOL)
  def article: Parser[Article] = articleHead ~ rep1(paragraph) ^^ { result => Article(result._1._2, result._1._1, result._2) }

  /** 項 */
  def heads: Parser[Serializable] = articleHead | sectionHead | chapterHead | supplementaryProvisionsHead
  def paragraphNumber: Parser[Int] = """\d+""".r ^^ { result => if (result.isEmpty) 0 else result.toInt }
  def paragraphContent: Parser[ParagraphContent] = orderedList() | (not(heads) ~> text ^^ { ParagraphText })
  def paragraph: Parser[Paragraph] = not(heads) ~> opt(paragraphNumber <~ EOL) ~ rep1sep(paragraphContent, EOL) <~ rep(EOL) ^^ {
    case None ~ contents => Paragraph(1, contents)
    case Some(number) ~ contents => Paragraph(number, contents)
  }

  /** 列記事項 */
  def orderedList(indentLevel: Int = 1): Parser[OrderedList] = rep1(repN(indentLevel * indentSize, " ".r) ~> orderedListItem(indentLevel) <~ opt(EOL)) ^^ { OrderedList(indentLevel, _) }

  /** 号 */
  def orderedListItemNumber(indentLevel: Int): Parser[Int] = indentLevel match {
    case 1 => "[一二三四五六七八九十百千]+".r ^^ {
      result => {
        KanjiNumberParser.parse(result).getOrElse(0)
      }
    }
    /* イロハ順のニを漢数字にしている誤記があるためそれに対応 */
    case 2 => "([イロハニホヘトチリヌルヲワカヨタレソツネナラムウヰノオクヤマケフコエテアサキユメミシヱヒモセスン]|二)".r ^^ {
      result => {
        if (result == "二") 4
        else {
          val iroha = "イロハニホヘトチリヌルヲワカヨタレソツネナラムウヰノオクヤマケフコエテアサキユメミシヱヒモセスン".split("").zipWithIndex.toMap
          iroha.getOrElse(result, -1) + 1
        }
      }
    }
  }
  def orderedListItemText(indentLevel: Int): Parser[OrderedListItemText] = not(repN(indentLevel * indentSize, " ".r)) ~> text ^^ { OrderedListItemText }
  def orderedListItemContent(indentLevel: Int): Parser[List[OrderedListItemContent]] = rep1sep(orderedList(indentLevel + 1) | orderedListItemText(indentLevel), EOL) <~ opt(EOL)
  def orderedListItem(indentLevel: Int): Parser[OrderedListItem] = (orderedListItemNumber(indentLevel) <~ " ".r) ~ orderedListItemContent(indentLevel) ^^ {
    result => OrderedListItem(result._1, result._2)
  }

  /** 本文 */
  def plainText: Parser[PlainText] = rep1(not(linkText) ~> ".".r) ^^ { result => PlainText(result.mkString) }
  def anotherArticleParagraph: Parser[LinkText] = ("第" ~> """\d+""".r <~ "条") ~ ("第" ~> """\d+""".r <~ "項") ^^ {
    result => LinkText(s"第${result._1.toInt}条第${result._2.toInt}項", LinkTargetParagraph(Some(result._1.toInt), result._2.toInt))
  }
  def anotherArticleParagraphs: Parser[ListText] = ("第" ~> """\d+""".r <~ "条") ~ ("第" ~> """\d+""".r <~ "項") ~ rep1(parallelConjunctions ~ ("第" ~> """\d+""".r <~ "項")) ^^ {
    result => ListText(List(LinkText(s"第${result._1._1.toInt}条第${result._1._2.toInt}項", LinkTargetParagraph(Some(result._1._1.toInt), result._1._2.toInt))) ++ result._2.flatMap(x => {
      List(PlainText(x._1), LinkText(s"第${x._2.toInt}項", LinkTargetParagraph(Some(result._1._1.toInt), x._2.toInt)))
    }))
  }
  def sameArticleParagraph: Parser[LinkText] = "第" ~> """\d+""".r <~ "項" ^^ { result => LinkText(s"第${result.toInt}項", LinkTargetParagraph(None, result.toInt)) }
  def precedingParagraph: Parser[LinkText] = "前項" ^^ { _ => LinkText("前項", LinkTargetParagraph(None, -1)) }
  def anotherArticle: Parser[LinkText] = "第" ~> """\d+""".r <~ "条" ^^ { result => LinkText(s"第${result.toInt}条", LinkTargetArticle(result.toInt)) }
  def linkTexts: Parser[ListText] = anotherArticleParagraphs
  def linkText: Parser[LinkText] = anotherArticleParagraph | sameArticleParagraph | precedingParagraph | anotherArticle
  def text: Parser[List[Text]] = rep1(linkTexts | linkText | plainText) ^^ { result => result.flatMap({
    case ListText(x) => x
    case x: Text => List(x)
  }) }

  /** 規約 */
  def term: Parser[Term] = rep(EOL) ~> rep1(supplementaryProvisions | chapter) ^^ { Term(_) }

  def parse(input: String): ParseResult[Term] = parseAll(term, input)
  def apply(input: String, indentSize: Int = 4): Either[String, Term] = {
    this.indentSize = indentSize
    parse(input) match {
      case Success(data, _) => Right(data)
      case NoSuccess(errorMessage, next) => Left(s"$errorMessage on line ${next.pos.line} on column ${next.pos.column}")
    }
  }
}
