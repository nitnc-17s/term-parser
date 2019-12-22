package pw._0x9.termparser

import scala.io.Source

object Main {
  def main(args: Array[String]): Unit = {
    val source = Source.fromFile("terms.txt")
    val input = source.getLines().mkString("\n") + "\n"
    val res = TermParser(input)
    PPrint2.pprint2.pprintln(res, width = 150, height = 5000)
  }
}
