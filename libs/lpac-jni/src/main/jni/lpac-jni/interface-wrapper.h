#pragma once
#undef NDEBUG
#include <assert.h>
#include <euicc/interface.h>
#include "lpac-jni.h"

void interface_wrapper_init();

extern struct euicc_apdu_interface lpac_jni_apdu_interface;
extern struct euicc_http_interface lpac_jni_http_interface;

#define LPAC_JNI_SETUP_ENV \
    JNIEnv *env; \
    (*jvm)->AttachCurrentThread(jvm, &env, NULL)