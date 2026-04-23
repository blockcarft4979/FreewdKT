#include <jni.h>
#include <string>
#include "security.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_freewdkt_bck_NativeHelper_getApiUrl(JNIEnv* env, jobject /* this */) {

    std::string result;
    for (size_t i = 0; i < ENCRYPTED_API_URL_LEN; ++i) {
        result.push_back(static_cast<char>(ENCRYPTED_API_URL[i] ^ XOR_KEY));
    }

    return env->NewStringUTF(result.c_str());
}