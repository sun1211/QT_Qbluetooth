#include "bluetoothgui.h"
#include <jni.h>
#include <QDebug>
#include <QString>
// callback from java in Android thread
// define our native methods
static void onReceiveNativeDevice(JNIEnv *env, jobject /*obj*/,jstring jmessage)
{
    // Delegate the call to Qt thread.
    const char* cmessage = env->GetStringUTFChars(jmessage, NULL);
    QString qmessage(cmessage);
    QMetaObject::invokeMethod(&BluetoothGUI::instance(), "onReceiveNativeDevice", Qt::BlockingQueuedConnection,Q_ARG(QString, qmessage));
}

static void onReceiveChat(JNIEnv *env, jobject /*obj*/,jstring jmessage)
{
    // Delegate the call to Qt thread.
    const char* cmessage = env->GetStringUTFChars(jmessage, NULL);
    QString qmessage(cmessage);
    QMetaObject::invokeMethod(&BluetoothGUI::instance(), "onReceiveChat", Qt::BlockingQueuedConnection,Q_ARG(QString, qmessage));
}

 static void onReceiveScanFinised(JNIEnv */*env*/, jobject /*obj*/)
 {
     // Delegate the call to Qt thread.
     QMetaObject::invokeMethod(&BluetoothGUI::instance(), "onReceiveScanFinised", Qt::BlockingQueuedConnection);
 }

 //add
 static void onReceiveVersion(JNIEnv */*env*/, jobject /*obj*/,int Version){

     QMetaObject::invokeMethod(&BluetoothGUI::instance(), "onReceiveVersion", Qt::BlockingQueuedConnection,Q_ARG(int, Version));
 }

 static void onReceiveReadData(JNIEnv *env, jobject /*obj*/,jstring readData){
     const char* cReadData = env->GetStringUTFChars(readData, NULL);
     QString qmessage(cReadData);
     QMetaObject::invokeMethod(&BluetoothGUI::instance(), "onReceiveReadData", Qt::BlockingQueuedConnection,Q_ARG(QString, qmessage));
 }

 static void onReceiveStateChange(JNIEnv */*env*/, jobject /*obj*/,int state)
 {
     // Delegate the call to Qt thread.
     QMetaObject::invokeMethod(&BluetoothGUI::instance(), "onReceiveStateChange", Qt::BlockingQueuedConnection,Q_ARG(int, state));
 }



 //create a vector with all our JNINativeMethod(s)
 static JNINativeMethod methods[] = {
     {"onReceiveNativeDevice", "(Ljava/lang/String;)V", (void *)onReceiveNativeDevice},
     {"onReceiveScanFinised", "()V", (void *)onReceiveScanFinised},
     {"onReceiveChat", "(Ljava/lang/String;)V", (void *)onReceiveChat},

     {"onReceiveVersion","(I)V",(void *)onReceiveVersion},
     {"onReceiveStateChange", "(I)V", (void *)onReceiveStateChange},

     {"onReceiveReadData", "(Ljava/lang/String;)V", (void *)onReceiveReadData},
 };

 // this method is called automatically by Java after the .so file is loaded
 JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* /*reserved*/)
 {

      JNIEnv* env; // get the JNIEnv pointer.
      if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK)
      return JNI_ERR;

      // search for Java class which declares the native methods
      jclass javaClass = env->FindClass("org/qtproject/example/bluetooth/NativeFunctions");
      if (!javaClass)
      return JNI_ERR;

      // register our native methods
      if (env->RegisterNatives(javaClass, methods,sizeof(methods) / sizeof(methods[0])) < 0)
      {
        return JNI_ERR;
      }
  return JNI_VERSION_1_6;
  }

