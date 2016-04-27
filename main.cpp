#include "bluetoothgui.h"
#include <QApplication>
#include <QAndroidJniObject>
int main(int argc, char *argv[])
{
    QApplication a(argc, argv);
    BluetoothGUI::instance().show();
    return a.exec();
}
