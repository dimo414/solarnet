package dev.martianzoo.tfm.repl

import org.jline.utils.AttributedStringBuilder
import org.jline.utils.AttributedStyle

enum class TfmColors(val hexString: String) {
  MEGACREDIT("f4d400"),
  STEEL("c8621e"),
  TITANIUM("777777"),
  PLANT("6dd248"),
  ENERGY("b23bcb"),
  HEAT("ef4320"),
  LAND_AREA("f68e5a"),
  WATER_AREA("a4dcf9"),
  OCEAN("0e3f68"),
  GREENERY("6dd248"),
  SPECIAL("a87a58"),
  TR("eb8f56"),
  ;

  fun color(string: String): String? {
    val r = hexString.substring(0, 2).toInt(16)
    val g = hexString.substring(2, 4).toInt(16)
    val b = hexString.substring(4, 6).toInt(16)
    return AttributedStringBuilder()
        .style(AttributedStyle.DEFAULT.foreground(r, g, b))
        .append(string)
        .style(AttributedStyle.DEFAULT)
        .toAnsi()
  }


}
