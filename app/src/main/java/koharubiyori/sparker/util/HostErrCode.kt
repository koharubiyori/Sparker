package koharubiyori.sparker.util

enum class HostErrorCode(val code: Int) {
  SUCCESS(0),
  PARAMETER_ERROR(1),
  UNKNOWN_ERROR(99999),
  INTERNAL_ERROR(99998),
  GRPC_CONNECT_ERROR(99997),

  // Device errors
  DEVICE_USERNAME_OR_PASSWORD_INVALID(10000),
  DEVICE_NOT_PAIRED(10001),
  DEVICE_INVALID_PAIRING_CODE(10002);

  companion object {
    fun fromCode(code: Int): HostErrorCode? =
      HostErrorCode.entries.find { it.code == code }

    fun isSuccess(code: Int): Boolean = fromCode(code) == SUCCESS
  }
}
