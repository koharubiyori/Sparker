//package koharubiyori.sparker.compable
//
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.SideEffect
//import androidx.compose.runtime.remember
//import koharubiyori.sparker.util.InitRef
//
//@Composable
//fun DoSideEffect(
//  effect: () -> Unit
//) {
//  val doneFlag = remember { InitRef(false) }
//
//  if (!doneFlag.value) {
//    effect()
//    doneFlag.value = true
//  }
//
//  SideEffect(effect)
//}