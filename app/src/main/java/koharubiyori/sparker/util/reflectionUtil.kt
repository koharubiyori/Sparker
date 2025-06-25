//import androidx.navigation.compose.ComposeNavigator
//import kotlin.reflect.KClass
//import kotlin.reflect.KProperty
//import kotlin.reflect.KProperty1
//import kotlin.reflect.full.memberProperties
//import kotlin.reflect.jvm.isAccessible
//
//object ReflectionUtil {
//
//}
//
//inline fun <reified T : Any> Any.readSelfSecretProperty(name: String): T {
//  return this::class.memberProperties.first { it.name == name }.let {
//    it.isAccessible = true
//    (it as KProperty1<Any, T>).get(this)
//  }
//}