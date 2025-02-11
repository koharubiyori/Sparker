package koharubiyori.sparker.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import koharubiyori.sparker.R

@Composable
fun EmptyContent(
  message: String = stringResource(R.string.no_data),
  icon: ImageVector,
  height: Dp? = null,
  onClick: (() -> Unit)? = null
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .then(height?.let { Modifier.height(height) } ?: Modifier.fillMaxHeight())
      .then(onClick?.let { Modifier.clickable { it() } } ?: Modifier),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      modifier = Modifier
        .size(80.dp),
      imageVector = icon,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Text(
      text = message,
      style = MaterialTheme.typography.titleLarge,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}