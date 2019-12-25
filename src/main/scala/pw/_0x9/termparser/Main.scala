package pw._0x9.termparser

import better.files._
import java.io.{File => JFile}
import java.nio.charset.{Charset, StandardCharsets}

import com.typesafe.config.ConfigFactory
import org.fusesource.scalate._
import scopt.OParser

import scala.io.Source

object Main {
  case class CommandLineArgs(
    source: Option[File] = None,
    target: Option[File] = None,
    title: String = "規約",
    indentSize: Int = 4,
    useStd: Boolean = false,
    debug: Boolean = false
  )
  def main(args: Array[String]): Unit = {
    val conf = ConfigFactory.load
    val versionString = try { conf.getString("version") } catch { case _: Exception => "" }
    val pipeInputAvailable = try { System.in.available() != 0 } catch { case _: Exception => false }
    val argsBuilder = OParser.builder[CommandLineArgs]
    val argsParser = {
      import argsBuilder._
      OParser.sequence(
        programName("term-parser"),
        head("term-parser", versionString, "by LaFr4nc3"),
        version('v', "version"),
        help('h', "help")
          .text("このヘルプを表示"),
        opt[Unit]("debug")
          .hidden()
          .action((_, c) => c.copy(debug = true)),
        opt[Unit]('S', "use-std")
          .action((_, c) => c.copy(useStd = true))
          .text("標準入出力を使用する"),
        opt[String]('t', "title")
          .action((x, c) => c.copy(title = x))
          .text("パース結果のHTMLで利用するタイトル"),
        opt[Int]("indent-size")
          .action((x, c) => c.copy(indentSize = x))
          .text("リストのパース時に使うインデントのサイズ (default: 4)"),
        arg[JFile]("<source>")
          .optional()
          .action((x, c) => c.copy(source = Some(x.toScala)))
          .text("パース対象のファイル (default: terms.txt)"),
        arg[JFile]("<target>")
          .optional()
          .action((x, c) => c.copy(target = Some(x.toScala)))
          .text("パース結果の出力ファイル (default: terms.html)"),
        checkConfig(
          c => if (!c.useStd) {
            if (c.source.isEmpty) {
              failure("<source>を指定してください")
            } else if (!c.source.get.isRegularFile)
              failure("<source>はファイルを指定してください")
            else {
              success
            }
          } else if (pipeInputAvailable) {
            success
          } else {
            failure("標準入力がありません")
          }
        )
      )
    }
    OParser.parse(argsParser, args, CommandLineArgs()) match {
      case Some(args) =>
        implicit val charset: Charset = StandardCharsets.UTF_8
        val input = if (args.useStd) Source.stdin.getLines.mkString("\n") else args.source.get.contentAsString

        val res = TermParser(input, args.indentSize)

        if (args.debug) Debug.PPrintUnicode.pprintln(res, width = 150, height = 5000)

        res match {
          case Right(term) =>
            val engine = new TemplateEngine
            val bindings = Map(
              "title" -> args.title,
              "cdn_styles" -> List(
                "https://cdn.jsdelivr.net/npm/normalize-css@2.3.1/normalize.css",
                "https://gist.githack.com/LaFr4nc3/8c20a737bbe5baad44d2e4d964086d96/raw/5be708bf904584480cbb6501d76a9a2cf3e25c23/terms.css"
              ),
              "cdn_scripts" -> List(
                "https://cdn.jsdelivr.net/gh/cferdinandi/smooth-scroll@15/dist/smooth-scroll.polyfills.min.js",
                "https://gist.githack.com/LaFr4nc3/8c20a737bbe5baad44d2e4d964086d96/raw/5be708bf904584480cbb6501d76a9a2cf3e25c23/terms.js"
              ),
              "body" -> term.toHTML
            )
            val templateUri = File(getClass.getResource("/mustache/index.mustache")).pathAsString
            val outputText = engine.layout(templateUri, bindings)
            if (args.useStd) {
              println(outputText)
            } else {
              args.target match {
                case Some(file) => file.write(outputText)
                case None =>
                  val source = args.source.get
                  source.parent.createChild(source.nameWithoutExtension + ".html").write(outputText)
              }
            }
            sys.exit(0)
          case Left(errorMessage) =>
            System.err.println(errorMessage)
            sys.exit(1)
        }
      case None => sys.exit(2)
    }
  }
}
