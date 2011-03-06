#include <jni.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <android/log.h>
#include <sys/system_properties.h>

#define LOG_TAG "libnative-fun"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

const int WIFI_NETWORK = 0;
const int G3_NETWORK = 1;   


JNIEXPORT jstring JNICALL Java_com_android_example_interfacemanager_NativeFunc_getTest(JNIEnv * env, jobject obj)   
{   
    LOGI("Hello Native Function Call Refreshed\n");   
    return (*env)->NewStringUTF(env, (char *)"Test Native Function Call\n");   
} 


JNIEXPORT jint JNICALL Java_com_android_example_interfacemanager_NativeFunc_delDefaultRoute(JNIEnv * env, jobject obj, jint type)
{
    char command[128];
    char device[10];
    
    LOGI("network type is %d\n", type);
    if ((int)type == G3_NETWORK) {
        LOGI("Delete 3G Default Route\n");
        strncpy(device, "rmnet0", 10);   
    } else {
        LOGI("Delete WIFI Default Route\n");
        strncpy(device, "eth0", 10);
    }
    snprintf(command, 128, "su -c \"ip route del default dev %s\"", device);

    LOGI("call del default route: %s\n", command);
    int exitcode = system(command);
    return (jint) exitcode;
}
   

JNIEXPORT jint JNICALL Java_com_android_example_interfacemanager_NativeFunc_addDefaultRoute(JNIEnv * env, jobject obj, jint type)
{
    char command[128];
    char gatewayIP[25];

    LOGI("network type is %d\n", type);
    if ((int)type == WIFI_NETWORK) {
        __system_property_get("dhcp.eth0.gateway", gatewayIP);
        LOGI("eth0 gateway is %s\n", gatewayIP);
    } else {
        __system_property_get("net.rmnet0.gw", gatewayIP);
        LOGI("rmnet0 gateway is %s\n", gatewayIP);
    }
    snprintf(command, 128, "su -c \"ip route add default via %s\"", gatewayIP);

    LOGI("add default route: %s\n", command);
    int exitcode = system(command);
    return (jint)exitcode;
}
