package pw._0x9.termparser

import pprint.{Tree, Util}

import scala.annotation.switch

object PPrint2 {
  val pprint2 = pprint.copy(
    additionalHandlers = {
      case x: String =>
        if (x.exists(c => c == '\n' || c == '\r')) Tree.Literal("\"\"\"" + x + "\"\"\"")
        else Tree.Literal(Util2.literalize(x))
    }
  )
}

object Util2 {
  def escapeChar(c: Char,
                 sb: StringBuilder,
                 unicode: Boolean = false) = (c: @switch) match {
    case '"' => sb.append("\\\"")
    case '\\' => sb.append("\\\\")
    case '\b' => sb.append("\\b")
    case '\f' => sb.append("\\f")
    case '\n' => sb.append("\\n")
    case '\r' => sb.append("\\r")
    case '\t' => sb.append("\\t")
    case c =>
      if (c < ' ' || (c > '~' && unicode)) sb.append("\\u%04x" format c.toInt)
      else sb.append(c)
  }


  /**
   * Convert a string to a C&P-able literal. Basically
   * copied verbatim from the uPickle source code.
   */
  def literalize(s: IndexedSeq[Char], unicode: Boolean = false) = {
    val sb = new StringBuilder
    sb.append('"')
    var i = 0
    val len = s.length
    while (i < len) {
      Util.escapeChar(s(i), sb, unicode)
      i += 1
    }
    sb.append('"')

    sb.result()
  }
}
