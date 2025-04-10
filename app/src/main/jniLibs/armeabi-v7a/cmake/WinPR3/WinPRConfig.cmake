include(CMakeFindDependencyMacro)

if(NOT "ON")
	if(NOT "OFF")
	    if("" AND NOT "OFF")
	        find_dependency(JSONC)
	    elseif("")
	        find_dependency(cJSON)
	    endif()
	endif()

	if("OFF")
		find_dependency(uriparser)
	endif()
endif()


####### Expanded from @PACKAGE_INIT@ by configure_package_config_file() #######
####### Any changes to this file will be overwritten by the next CMake run ####
####### The input file was WinPRConfig.cmake.in                            ########

get_filename_component(PACKAGE_PREFIX_DIR "${CMAKE_CURRENT_LIST_DIR}/../../" ABSOLUTE)

macro(set_and_check _var _file)
  set(${_var} "${_file}")
  if(NOT EXISTS "${_file}")
    message(FATAL_ERROR "File or directory ${_file} referenced by variable ${_var} does not exist !")
  endif()
endmacro()

macro(check_required_components _NAME)
  foreach(comp ${${_NAME}_FIND_COMPONENTS})
    if(NOT ${_NAME}_${comp}_FOUND)
      if(${_NAME}_FIND_REQUIRED_${comp})
        set(${_NAME}_FOUND FALSE)
      endif()
    endif()
  endforeach()
endmacro()

####################################################################################

set(WinPR_VERSION_MAJOR "3")
set(WinPR_VERSION_MINOR "12")
set(WinPR_VERSION_REVISION "1")
set(WITH_WINPR_JSON "ON")

set_and_check(WinPR_INCLUDE_DIR "${PACKAGE_PREFIX_DIR}/include/winpr3")

include("${CMAKE_CURRENT_LIST_DIR}/WinPRTargets.cmake")
