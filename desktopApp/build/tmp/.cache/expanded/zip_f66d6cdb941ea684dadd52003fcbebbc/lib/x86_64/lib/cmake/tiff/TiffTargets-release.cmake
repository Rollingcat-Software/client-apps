#----------------------------------------------------------------
# Generated CMake target import file for configuration "Release".
#----------------------------------------------------------------

# Commands may need to know the format version.
set(CMAKE_IMPORT_FILE_VERSION 1)

# Import target "TIFF::tiff" for configuration "Release"
set_property(TARGET TIFF::tiff APPEND PROPERTY IMPORTED_CONFIGURATIONS RELEASE)
set_target_properties(TIFF::tiff PROPERTIES
  IMPORTED_LINK_INTERFACE_LANGUAGES_RELEASE "C"
  IMPORTED_LOCATION_RELEASE "/home/runner/work/javacpp-presets/javacpp-presets/leptonica/cppbuild/android-x86_64/lib/libtiff.a"
  )

list(APPEND _cmake_import_check_targets TIFF::tiff )
list(APPEND _cmake_import_check_files_for_TIFF::tiff "/home/runner/work/javacpp-presets/javacpp-presets/leptonica/cppbuild/android-x86_64/lib/libtiff.a" )

# Commands beyond this point should not need to know the version.
set(CMAKE_IMPORT_FILE_VERSION)
