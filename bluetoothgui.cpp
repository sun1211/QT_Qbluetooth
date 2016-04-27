#include "bluetoothgui.h"
#include <QDebug>
#include<QListWidgetItem>
#include <QSizePolicy>

#include <QtCore>
#include <iostream>
#include <QProgressBar>

#define STATE_NONE  0       // we're doing nothing
#define STATE_LISTEN  1     // now listening for incoming connections
#define STATE_CONNECTING  2 // now initiating an outgoing connection
#define STATE_CONNECTED  3  // now connected to a remote device
#define CONNECTION_FAIL  4
#define CLOSE_SOCKET_FAIL 5



BluetoothGUI::BluetoothGUI(QWidget *parent) : QWidget(parent)
{
    qDebug()<<"start bluetooth";
    deviceNameWidget = new QListWidget(this);
    chatLabel = new QLabel(this);
    statusLabel = new QLabel(this);

    scanButton = new QPushButton("Scan", this);
    clearButton = new QPushButton("Clear", this);
    quitButton = new QPushButton("Back", this);
    getVerBtn = new QPushButton("Get Ver",this);
    readChanelBtn = new QPushButton("Read Channel",this);
    writeChanelBtn = new QPushButton("Write Channel",this);
    //edit size of widgets
    QSizePolicy spScanBtn(QSizePolicy::Preferred, QSizePolicy::Preferred);
    spScanBtn.setHorizontalStretch(1);
    scanButton->setSizePolicy(spScanBtn);

    QSizePolicy spClearBtn(QSizePolicy::Preferred, QSizePolicy::Preferred);
    spClearBtn.setHorizontalStretch(1);
    clearButton->setSizePolicy(spClearBtn);

    QSizePolicy spBackBtn(QSizePolicy::Preferred, QSizePolicy::Preferred);
    spBackBtn.setHorizontalStretch(1);
    quitButton->setSizePolicy(spBackBtn);

    QSizePolicy spGetBtn(QSizePolicy::Preferred, QSizePolicy::Preferred);
    spGetBtn.setHorizontalStretch(1);
    getVerBtn->setSizePolicy(spGetBtn);

    QSizePolicy spReadChannelBtn(QSizePolicy::Preferred, QSizePolicy::Preferred);
    spReadChannelBtn.setHorizontalStretch(1);
    readChanelBtn->setSizePolicy(spReadChannelBtn);

    QSizePolicy spWriteChannelBtn(QSizePolicy::Preferred, QSizePolicy::Preferred);
    spWriteChannelBtn.setHorizontalStretch(1);
    writeChanelBtn->setSizePolicy(spWriteChannelBtn);

    QMovie *movie = new QMovie(":/icon/searchBL");
    processLabel = new QLabel(this);
    processLabel->setMovie(movie);
    movie->start();


    QHBoxLayout *sendLayout =  new QHBoxLayout;
    sendLayout->addWidget(getVerBtn);
    sendLayout->addWidget(readChanelBtn);
    sendLayout->addWidget(writeChanelBtn);



    QHBoxLayout *centerLayout = new QHBoxLayout();
    centerLayout->addLayout(sendLayout);


    QHBoxLayout *bottomLayout = new QHBoxLayout;
    bottomLayout->addWidget(scanButton);
    bottomLayout->addWidget(clearButton);
    bottomLayout->addWidget(quitButton);

    QHBoxLayout *statuslayout = new QHBoxLayout();
    statuslayout->addWidget(statusLabel);
    statuslayout->addStretch();
    statuslayout->addWidget(processLabel);

    QVBoxLayout *mainLayout = new QVBoxLayout(this);
    mainLayout->addLayout(statuslayout,1);
    mainLayout->addWidget(deviceNameWidget,10);
    mainLayout->addWidget(chatLabel,2);
    mainLayout->addLayout(centerLayout,1);
    mainLayout->addLayout(bottomLayout,1);

    deviceNameWidget->show();
    getVerBtn->setDisabled(true);
    readChanelBtn->setDisabled(true);
    writeChanelBtn->setDisabled(true);
    processLabel->hide();

    OpenBluetooth();
    connect(scanButton, SIGNAL(clicked()), this, SLOT(ScanDevice()));
    connect(clearButton, SIGNAL(clicked()), this, SLOT(clearText()));
    connect(quitButton, SIGNAL(clicked()), this, SLOT(close()));
    connect(deviceNameWidget, SIGNAL(itemClicked(QListWidgetItem*)),
            this, SLOT(connectDevice(QListWidgetItem*)));

    connect(getVerBtn,SIGNAL(clicked()),this,SLOT(getVersion()));
    //connect(readChanelBtn,SIGNAL(clicked()),this,SLOT(readDataChannel()));


}

BluetoothGUI::~BluetoothGUI()
{


}

void BluetoothGUI::getVersion(){
    qDebug()<<"get verison";
    sendDataQTtoJava("205-00-00-00-00-08-00-00");
}

//void BluetoothGUI::readDataChannel(){
//    qDebug()<<"read data channel";
//    QAndroidJniObject::callStaticMethod<void>("org/qtproject/example/bluetooth/Bluetooth",
//                                              "readDataChannel");
//}

void BluetoothGUI::OpenBluetooth(){
    qDebug()<<"OpenBluetooth";
    QAndroidJniObject::callStaticMethod<void>("org/qtproject/example/bluetooth/Bluetooth",
                                              "OpenBluetooth");
}

void BluetoothGUI::connectDevice(QListWidgetItem *item){
    QString device = item->text();
    QStringList list = device.split("\n");
    QString deviceAddress = list[1];
    qDebug()<<deviceAddress;
    QAndroidJniObject javaDeviceAddress = QAndroidJniObject::fromString(deviceAddress);
    QAndroidJniObject::callStaticMethod<void>("org/qtproject/example/bluetooth/Bluetooth",
                                              "connectDevice",
                                              "(Ljava/lang/String;)V",
                                              javaDeviceAddress.object<jstring>());
}
void BluetoothGUI::sendDataQTtoJava(QString dataValue){
    QAndroidJniObject javaDataValue = QAndroidJniObject::fromString(dataValue);
    QAndroidJniObject::callStaticMethod<void>("org/qtproject/example/bluetooth/Bluetooth",
                                              "sendDataValue",
                                              "(Ljava/lang/String;)V",
                                              javaDataValue.object<jstring>());
}

void BluetoothGUI::clearText(){
    deviceNameWidget->clear();
//    QString testta= "04-00-02-00-65-4-204-205-00-01-02-03";

//    QStringList list = testta.split("-");

//    QByteArray buffer;
//    for(int i =0; i<list.size(); i++ ){
//        buffer[i] = list[i].toInt();

//    }

//    QByteArray ba;
//    for(int k = 0; k<4;k++){
//        ba[k] = buffer[k+4];
//    }
//    float a = HexToFloat(ba);
//    qDebug() << a;
//    QString xuat = QString::number(a);
//    qDebug() << "faf"+xuat;

}
float BluetoothGUI::HexToFloat(const QByteArray &_array){
    bool ok;
    int sign = 1;
    QByteArray array(_array.toHex());
    array = QByteArray::number(array.toLongLong(&ok,16),2); //convert hex to binary -you don't need this since your incoming data is binary
    if(array.length()==32)
    {
        if(array.at(0)=='1') sign =-1; // if bit 0 is 1 number is negative
        array.remove(0,1); // remove sign bit
    }
    QByteArray fraction =array.right(23); //get the fractional part
    double mantissa = 0;
    for(int i=0;i<fraction.length();i++) // iterate through the array to claculate the fraction as a decimal.
        if(fraction.at(i)=='1') mantissa += 1.0/(pow(2,i+1));
    int exponent = array.left(array.length()-23).toLongLong(&ok,2)-127; //claculate the exponent
    return (sign*pow(2,exponent)*(mantissa+1.0));
}

void BluetoothGUI::ScanDevice(){
    qDebug()<<"ScanDevice";
    deviceNameWidget->clear();
    scanButton->setDisabled(true);
    clearButton->setDisabled(true);
    QAndroidJniObject::callStaticMethod<void>("org/qtproject/example/bluetooth/Bluetooth",
                                              "ScanDevice");
}

BluetoothGUI &BluetoothGUI::instance(QWidget *parent)
{
    static BluetoothGUI mainWindow(parent);
    return mainWindow;
}

// Callback in Qt thread
void BluetoothGUI::onReceiveNativeDevice(QString deviceName)
{
    qDebug()<<deviceName;
    deviceNameWidget->addItem(deviceName);

}

void BluetoothGUI::onReceiveScanFinised()
{
    qDebug()<<"Scan Finished ";
    scanButton->setEnabled(true);
    clearButton->setEnabled(true);
}

//void BluetoothGUI::onReceiveChat(QString message){//do dai message = 13
//    deviceNameWidget->clear();
//    deviceNameWidget->addItem("String Back:"+message);

//}
//void BluetoothGUI::onReceiveVersion(int verSion){
//    chatLabel->clear();

//    QString s = QString::number(verSion,16);
//    s.toStdString();
//    chatLabel->show();
//    chatLabel->setText("ECU Version:0x"+s);
//    deviceNameWidget->clear();
//    qDebug()<<"your version back:";
//    qDebug() << s;
//    qDebug()<<verSion;
//}
//void BluetoothGUI::onReceiveReadData(QString readDataValue){
//    chatLabel->show();
//    chatLabel->setText(readDataValue);
    //qDebug() << readDataValue;
//    QStringList list = readDataValue.split("-");
//    QString saveDataBack = "";
//    QByteArray buffer;
//    for(int i =0; i< 16; i++ ){
//       buffer[i] = list[i].toInt();
//        if(list[i].size()!=2){
//            list[i] +="0";
//        }
//        saveDataBack+=list[i];
//    }
//    qDebug()<<"your data channel: "+saveDataBack;
//    QByteArray dataValue;
//    for(int k = 0; k<4;k++){
//        dataValue[k] = buffer[k+12];
//    }
//    float dataFromChannel = HexToFloat(dataValue);
//    qDebug() << dataFromChannel;
//    QString outPut = QString::number(dataFromChannel);
//    chatLabel->setText("Channel 0xF001 = "+outPut);


//}
void BluetoothGUI::onReceiveReadData(QString readDataValue){
    qDebug() << "rec from java"+readDataValue;


}

void BluetoothGUI::onReceiveStateChange(int state){
    switch(state){
    case STATE_CONNECTED:
        qDebug()<<"connected";
        statusLabel->clear();
        statusLabel->setText("Connected");
        getVerBtn->setDisabled(false);
        readChanelBtn->setDisabled(false);
        writeChanelBtn->setDisabled(false);
        processLabel->hide();
        break;
    case STATE_CONNECTING:
        qDebug()<<"connecting";
        statusLabel->clear();
        chatLabel->clear();
        statusLabel->setText("Connecting...");
        getVerBtn->setDisabled(true);
        processLabel->show();
        break;
    case CONNECTION_FAIL:
        qDebug()<<"CONNECTION_FAIL";
        statusLabel->clear();
        chatLabel->clear();
        statusLabel->setText("Connection fail");
        getVerBtn->setDisabled(true);
        processLabel->hide();
        break;
    case CLOSE_SOCKET_FAIL:
        statusLabel->setText("Close socket fail");
        processLabel->hide();
        break;

    }

}


