package pw._0x9.termparser

import better.files._
import java.io.{File => JFile}
import java.nio.charset.Charset

import scopt.OParser

import scala.io.Source

object Main {
  case class CommandLineArgs(
    source: Option[File] = None,
    target: Option[File] = None,
    title: String = "規約",
    indentSize: Int = 4,
    debug: Boolean = false
  )
  def main(args: Array[String]): Unit = {
    val pipeInputAvailable = try { System.in.available() != 0 } catch { case e: Exception => false }
    val argsBuilder = OParser.builder[CommandLineArgs]
    val argsParser = {
      import argsBuilder._
      OParser.sequence(
        programName("term-parser"),
        head("term-parser", "1.0.0", "by LaFr4nc3"),
        help('h', "help")
          .text("このヘルプを表示"),
        opt[Unit]("debug")
          .hidden()
          .action((_, c) => c.copy(debug = true)),
        opt[String]('t', "title")
          .action((x, c) => c.copy(title = x))
          .text("パース結果のHTMLで利用するタイトル"),
        opt[Int]("indent-size")
          .action((x, c) => c.copy(indentSize = x))
          .text("リストのパース時に使うインデントのサイズ (default: 4)"),
        arg[JFile]("<source>")
          .minOccurs(if (pipeInputAvailable) 0 else 1)
          .action((x, c) => c.copy(source = Some(x.toScala)))
          .text("パース対象のファイル (e.g. terms.txt)"),
        arg[JFile]("<target>")
          .optional()
          .action((x, c) => c.copy(target = Some(x.toScala)))
          .text("パース結果の出力ファイル (e.g. terms.html)")
      )
    }
    OParser.parse(argsParser, args, CommandLineArgs()) match {
      case Some(args) =>
        implicit val charset: Charset = Charset.forName("UTF-8")
        val input = args.source match {
          case Some(file) => file.contentAsString
          case None => if (pipeInputAvailable) Source.stdin.getLines.mkString("\n") else sys.exit(3)
        }
        val res = TermParser(input, args.indentSize)
        if (args.debug) Debug.PPrintUnicode.pprintln(res, width = 150, height = 5000)
        res match {
          case Right(term) =>
            val title = s"""<title>${args.title}</title>"""
            val normalizeCSS = """<link href="https://cdn.jsdelivr.net/npm/normalize-css@2.3.1/normalize.css" rel="stylesheet">"""
            val termsCSSContent = Source.fromResource("terms.css").getLines.mkString
            val termsCSS = s"""<style type="text/css">$termsCSSContent</style>"""
            val head = s"""<head>$title$normalizeCSS$termsCSS</head>"""

            val titleInBody = s"""<h1>${args.title}</h1>"""
            val smoothScrollJS = """<script src="https://cdn.jsdelivr.net/gh/cferdinandi/smooth-scroll@15/dist/smooth-scroll.polyfills.min.js"></script>"""
            val smoothScrollConfigJSContent = Source.fromResource("smooth-scroll-config.js").getLines.mkString
            val smoothScrollConfigJS = s"""<script lang="text/javascript">$smoothScrollConfigJSContent</script>"""
            val body = s"""<body>$titleInBody${term.toHTML}$smoothScrollJS$smoothScrollConfigJS</body>"""

            val html = s"""<html lang="ja">$head$body</html>"""
            val outputText = html
            args.target match {
              case Some(file) => file.write(outputText)
              case None => println(outputText)
            }
            sys.exit(0)
          case Left(errorMessage) =>
            println(errorMessage)
            sys.exit(1)
        }
      case None => sys.exit(2)
    }
  }
}
