# 指定 CMake 的最低版本
cmake_minimum_required(VERSION 3.22.1)

# 声明项目名称
project("freewdsecurity")

# 添加一个名为 "native-lib" 的共享库（.so 文件）
# 它的源文件是 security.cpp
add_library(
        native-lib
        SHARED
        security.cpp
)

# 查找 Android 的 log 库，以便在 C++ 代码中打印日志
find_library(
        log-lib
        log
)

# 将目标库 native-lib 与 log 库链接起来
target_link_libraries(
        native-lib
        ${log-lib}
)