package koharubiyori.sparker.util

enum class LoadStatus {
  INITIAL,
  LOADING,
  INIT_LOADING,
  SUCCESS,
  DONE,
  EMPTY,
  FAILED;

  companion object {
    fun isAllLoaded(loadStatus: LoadStatus) = listOf(EMPTY, DONE).contains(loadStatus)
    fun isLoading(loadStatus: LoadStatus) = listOf(LOADING, INIT_LOADING).contains(loadStatus)
  }
}

