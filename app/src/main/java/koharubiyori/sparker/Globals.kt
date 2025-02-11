package koharubiyori.sparker

import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.navigation.NavHostController
import koharubiyori.sparker.component.commonDialog.CommonAlertDialogRef
import koharubiyori.sparker.component.commonDialog.CommonRadioDialogRef

// 专门存放全局延后赋值的变量
@SuppressLint("StaticFieldLeak")
object Globals {
  lateinit var activity: ComponentActivity
  lateinit var context: Context
  lateinit var navController: NavHostController
  lateinit var commonAlertDialog: CommonAlertDialogRef
  lateinit var commonRadioDialog: CommonRadioDialogRef
}