package koharubiyori.sparker.util

import android.os.Bundle
import androidx.navigation.*
import koharubiyori.sparker.util.RouteArguments.Companion.getRouteArgumentFields
import java.lang.reflect.Field

// Screens will receive a Bundle containing route arguments when navigation occurs. This function convert it into an instance of entity class.
inline fun <reified T : RouteArguments> Bundle.toRouteArguments(): T {
  val entityRef = T::class.java
  val entityConstructor = entityRef.getConstructor()
  val entityInstance = entityConstructor.newInstance()
  val argumentFields = entityRef.getRouteArgumentFields()
  val argumentNameMapToInPoolId = mutableMapOf<String, String>()

  argumentFields.forEach {
    it.isAccessible = true
    val argumentKey = this.getString(it.name) as String
    val value = RouteArgumentsPool.get(argumentKey)
    it.set(entityInstance, value)
    argumentNameMapToInPoolId[it.name] = argumentKey
  }

  val poolIdsField = RouteArguments::class.java.getDeclaredField("argumentNameMapToInPoolId")
  poolIdsField.isAccessible = true
  poolIdsField.set(entityInstance, argumentNameMapToInPoolId.toMap())

  return entityInstance
}

abstract class RouteArguments {
  private var argumentNameMapToInPoolId = emptyMap<String, String>()
  private fun createArgumentQueryStr(): String {
    if (argumentNameMapToInPoolId.isEmpty()) {
      argumentNameMapToInPoolId = this::class.java.getRouteArgumentFields()
        .map {
          it.isAccessible = true
          val name = it.name
          val value = RouteArgumentsPool.putIn(it.get(this))

          name to value
        }
        .associate { it }
    }

    return argumentNameMapToInPoolId.entries.joinToString("&") { "${it.key}=${it.value}" }
  }

  fun createRouteNameWithArguments(): String {
    val routeName = this::class.java.declaredAnnotations.filterIsInstance<RouteName>()[0].name
    return "${routeName}?${createArgumentQueryStr()}"
  }

  // This must be called in ViewModel.onCleared to clear data in the arguments pool and prevent memory leaks
  fun removeReferencesFromArgumentPool() {
    argumentNameMapToInPoolId.values.forEach { RouteArgumentsPool.delete(it) }
  }

  companion object {
    private fun Class<out RouteArguments>.checkRouteNameAnnotation() {
      if (!this.isAnnotationPresent(RouteName::class.java)) {
        error("An implementation class of RouteArguments must have a @RouteName annotation")
      }
    }

    // Obtains arguments from implementation classes
    fun Class<out RouteArguments>.getRouteArgumentFields(): List<Field> {
      // Filters out the property named $stable in declaredFields of derived classes
      return this.declaredFields.filter { it.name != "\$stable" }
    }

    // Add markers to append arguments to the end of route names
    val Class<out RouteArguments>.formattedRouteName: String get() {
      this.checkRouteNameAnnotation()
      val routeName = this.declaredAnnotations.filterIsInstance<RouteName>()[0].name
      val argumentFields = this.getRouteArgumentFields()
      val argStr = argumentFields.joinToString("&") { "${it.name}={${it.name}}" }
      return "$routeName?$argStr"
    }

    // Route arguments
    val Class<out RouteArguments>.formattedArguments: List<NamedNavArgument> get() {
      this.checkRouteNameAnnotation()
      val argumentFields = this.getRouteArgumentFields()
      return argumentFields.map { RouteArgumentConfig(it.name).toNavArgument() }
    }
  }
}

// Configs for route arguments
class RouteArgumentConfig(
  val name: String,
  val type: NavType<*> = NavType.StringType,
  val nullable: Boolean = false,
  /*
    关于!nullable时默认值为空字符串，不知道navArgument是什么个设计，nullable如果为true，defaultValue就必须设置，
    按常理来想应该是可空才应该设置默认值的。因为这里已经将NavType.StringType, nullable = false作为默认值了，
    所以defaultValue也之好设置默认值为空字符串了。
    另外要注意：除了NavType.StringType，其他类型的默认值都不能为null
  */
  val defaultValue: Any? = if (nullable) null else ""
) {
  fun toNavArgument() = navArgument(name) {
    this.type = this@RouteArgumentConfig.type
    this.nullable = this@RouteArgumentConfig.nullable
    this.defaultValue = this@RouteArgumentConfig.defaultValue
  }
}

// Annotate with route name
@Target(AnnotationTarget.CLASS)
annotation class RouteName(val name: String)

// For saving non-serializable objects to be sent across pages
object RouteArgumentsPool {
  private var incrementId = 0
  private val pool = mutableMapOf<String, Any?>()

  fun putIn(value: Any?): String {
    val id = (++incrementId).toString()
    pool[id] = value
    return id
  }

  fun get(id: String): Any? {
    return pool[id]
  }

  fun delete(id: String) {
    pool.remove(id)
  }
}

fun NavHostController.navigateByArguments(
  arguments: RouteArguments,
  builder: (NavOptionsBuilder.() -> Unit) = { }
) {
  this.navigate(arguments.createRouteNameWithArguments(), builder)
}