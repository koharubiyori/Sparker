import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.node.Ref
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import koharubiyori.sparker.component.commonDialog.CommonAlertDialog
import koharubiyori.sparker.component.commonDialog.CommonAlertDialogRef
import koharubiyori.sparker.Globals
import koharubiyori.sparker.compable.LifecycleEventEffect
import koharubiyori.sparker.component.commonDialog.CommonRadioDialog
import koharubiyori.sparker.component.commonDialog.CommonRadioDialogRef
import koharubiyori.sparker.initialization.onComposeCreated
import koharubiyori.sparker.util.DeviceStateCenter
import koharubiyori.sparker.util.SimpleNetworkStateObserver

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun OnComposeWillCreate(
  content: @Composable (NavHostController) -> Unit
) {
  val navController = rememberNavController()
  val simpleNetworkStateObserver = remember { SimpleNetworkStateObserver() }

  val commonAlertDialogRef = remember { Ref<CommonAlertDialogRef>() }
  val commonRadioDialogRef = remember { Ref<CommonRadioDialogRef>() }

  Globals.navController = navController

  DisposableEffect(true) {
    Globals.commonAlertDialog = commonAlertDialogRef.value!!
    Globals.commonRadioDialog = commonRadioDialogRef.value!!

    onComposeCreated()
    onDispose { simpleNetworkStateObserver.unregister() }
  }

  LifecycleEventEffect(
    onPause = { DeviceStateCenter.autoRefresh = false },
    onResume = { DeviceStateCenter.autoRefresh = true }
  )

  simpleNetworkStateObserver.Provider {
    content(navController)
  }

  CommonAlertDialog(ref = commonAlertDialogRef)
  CommonRadioDialog(ref = commonRadioDialogRef)
}