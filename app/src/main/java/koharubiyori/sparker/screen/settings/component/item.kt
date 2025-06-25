package koharubiyori.sparker.screen.settings.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import koharubiyori.sparker.util.BorderSide
import koharubiyori.sparker.util.sideBorder

@Composable
fun SettingsScreenItem(
  title: String,
  titleStyle: TextStyle = TextStyle(),
  subtext: String? = null,
  compact: Boolean = false,
  onClick: (() -> Unit)? = null,
  rightContent: (@Composable () -> Unit)? = null,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick?.invoke() }
      .padding(vertical = 5.dp)
      .height(if (compact) 40.dp else 60.dp)
      .padding(
        horizontal = 20.dp,
      ),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(
      modifier = Modifier
        .weight(1f),
    ) {
      Text(
        text = title,
        style = titleStyle,
        fontSize = 16.sp
      )
      if (subtext != null) {
        Text(
          modifier = Modifier
            .padding(top = 3.dp),
          text = subtext,
          fontSize = 14.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          lineHeight = 16.sp
        )
      }
    }

    Box(
      modifier = Modifier
        .padding(start = 20.dp)
    ) {
      rightContent?.invoke()
    }
  }
}