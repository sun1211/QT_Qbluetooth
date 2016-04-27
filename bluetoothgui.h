#ifndef BLUETOOTHGUI_H
#define BLUETOOTHGUI_H

#include <QWidget>
#include <QStackedWidget>
#include <QVBoxLayout>
#include <QComboBox>
#include <QHBoxLayout>
#include <QListWidget>
#include <QListWidgetItem>
#include <QPushButton>
#include <QTextEdit>
#include <QLabel>
#include <QtAndroidExtras/QAndroidJniObject>
#include <qdebug.h>
#include <QAndroidJniObject>
#include <QString>

#include <QMovie>


class BluetoothGUI : public QWidget
{
    Q_OBJECT
public:
    static BluetoothGUI &instance(QWidget *parent = 0);

signals:
    void scanFinished();

public slots:
    void ScanDevice();
    void clearText();
    void onReceiveNativeDevice(QString deviceName);
    void onReceiveScanFinised();
    void connectDevice(QListWidgetItem*);
//    void onReceiveChat(QString message);


    void onReceiveStateChange(int state);

    void getVersion();
//    void onReceiveVersion(int verSion);

//    void readDataChannel();
    void onReceiveReadData(QString readDataValue);
    void sendDataQTtoJava(QString dataValue);



private:
    explicit BluetoothGUI(QWidget *parent = 0);
    ~BluetoothGUI();
    void OpenBluetooth();
    QListWidget *deviceNameWidget;
    QLabel *chatLabel;
    QLabel *statusLabel;

    QPushButton *scanButton;
    QPushButton *clearButton;
    QPushButton *quitButton;


    QPushButton *getVerBtn;
    QPushButton *readChanelBtn;
    QPushButton *writeChanelBtn;

    QLabel *processLabel;

    float HexToFloat(const QByteArray &_array);



};

#endif // BLUETOOTHGUI_H
