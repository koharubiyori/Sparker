package koharubiyori.sparker.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import kotlin.math.roundToInt

fun Color.adjustByHSL(saturation: Float, lightness: Float): Color {
  val hsl = FloatArray(3)
  ColorUtils.colorToHSL(this.toArgb(), hsl)
  return Color.hsl(hsl[0], saturation, lightness)
}