package koharubiyori.sparker.component.styled

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import koharubiyori.sparker.Globals
import koharubiyori.sparker.compable.StatusBar
import koharubiyori.sparker.compable.StatusBarMode
import koharubiyori.sparker.component.BackButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyledTopAppBar(
  modifier: Modifier = Modifier,
  backgroundColor: Color = MaterialTheme.colorScheme.background,
  contentColor: Color = contentColorFor(backgroundColor),
  statusBarMode: StatusBarMode = StatusBarMode.VISIBLE,
  statusBarSticky: Boolean = false,
  statusBarBackgroundColor: Color = backgroundColor,
  statusBarDarkIcons: Boolean = true,
  title: @Composable () -> Unit,
  navigationIcon: (@Composable () -> Unit) = { BackButton() },
  actions: (@Composable (RowScope.() -> Unit))? = null,
) {
  StatusBar(
    mode = statusBarMode,
//    sticky = statusBarSticky,
//    backgroundColor = Color.Transparent,
    darkIcons = statusBarDarkIcons
  )

  Box {
    TopAppBar(
      modifier = Modifier
        .then(modifier),
      colors = TopAppBarColors(
        containerColor = backgroundColor,
        scrolledContainerColor = backgroundColor,
        titleContentColor = contentColor,
        navigationIconContentColor = contentColor,
        actionIconContentColor = contentColor,
      ),
      title = title,
      navigationIcon = { navigationIcon() },
      actions = {
        actions?.invoke(this)
      },
    )
  }
}

@Composable
fun TopAppBarTitle(
  modifier: Modifier = Modifier,
  text: String,
) {
  Text(
    modifier = modifier,
    text = text,
    overflow = TextOverflow.Ellipsis,
  )
}