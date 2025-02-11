package koharubiyori.sparker.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

// It's best not to set the color here because it will invalidate the LocalContentColor
private val basicTextStyle = TextStyle(

)

private val defaultTypography = Typography()

// Set of Material typography styles to start with
val typography = Typography(
  displayLarge = defaultTypography.displayLarge.merge(basicTextStyle),
  displayMedium = defaultTypography.displayMedium.merge(basicTextStyle),
  displaySmall = defaultTypography.displaySmall.merge(basicTextStyle),
  headlineLarge = defaultTypography.headlineLarge.merge(basicTextStyle),
  headlineMedium = defaultTypography.headlineMedium.merge(basicTextStyle),
  headlineSmall = defaultTypography.headlineSmall.merge(basicTextStyle),
  titleLarge = defaultTypography.titleLarge.merge(basicTextStyle),
  titleMedium = defaultTypography.titleMedium.merge(basicTextStyle),
  titleSmall = defaultTypography.titleSmall.merge(basicTextStyle),
  bodyLarge = defaultTypography.bodyLarge.merge(basicTextStyle),
  bodyMedium = defaultTypography.bodyMedium.merge(basicTextStyle),
  bodySmall = defaultTypography.bodySmall.merge(basicTextStyle),
  labelLarge = defaultTypography.labelLarge.merge(basicTextStyle),
  labelMedium = defaultTypography.labelMedium.merge(basicTextStyle),
  labelSmall = defaultTypography.labelSmall.merge(basicTextStyle)
)