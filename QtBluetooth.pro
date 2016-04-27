QT += quick androidextras core gui
greaterThan(QT_MAJOR_VERSION, 4): QT += widgets


SOURCES += \
    main.cpp \
    bluetoothgui.cpp \
    native.cpp

OTHER_FILES += \
    android-sources/src/org/qtproject/example/bluetooth/Bluetooth.java \
    android-sources/AndroidManifest.xml \

HEADERS += \
    bluetoothgui.h

ANDROID_PACKAGE_SOURCE_DIR = $$PWD/android-sources
CONFIG += mobility
MOBILITY =

DISTFILES += \
    android-sources/src/org/qtproject/example/bluetooth/NativeFunctions.java

RESOURCES += \
    icon.qrc
