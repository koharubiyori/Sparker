package koharubiyori.sparker.screen.scanDevices

import koharubiyori.sparker.util.RouteArguments
import koharubiyori.sparker.util.RouteName
import koharubiyori.sparker.util.ScanResult
import kotlinx.coroutines.CompletableDeferred

@RouteName("scanDevices")
class ScanDevicesRouteArguments (
  val returner: CompletableDeferred<ScanResultEx?>,
) : RouteArguments() {
  constructor() : this(CompletableDeferred())
}