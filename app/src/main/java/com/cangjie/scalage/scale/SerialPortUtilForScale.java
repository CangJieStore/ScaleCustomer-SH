package com.cangjie.scalage.scale;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;



public class SerialPortUtilForScale {
    public static SerialPort mSerialPort;
    public static InputStream mInputStream;
    public static OutputStream mOutputStream;
    public static boolean isOpen = false;

    private String prot = "ttyS1";//1
    private int baudrate = 9600;

    private SerialPortUtilForScale() {}

    public void OpenSerialPort() {
        try {
            mSerialPort = new SerialPort(new File("/dev/" + prot), baudrate,
                    0);
            mInputStream = mSerialPort.getInputStream();
            mOutputStream = mSerialPort.getOutputStream();

            isOpen = true;
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.i("test", "打开失败");
            e.printStackTrace();
        }
    }

    private static SerialPortUtilForScale singleTon = null;

    public static SerialPortUtilForScale Instance()
    {
        if(null == singleTon)
            singleTon = new SerialPortUtilForScale();
        return  singleTon;
    }

    /**
     * 关闭串口
     */
    public void CloseSerialPort() {

        if (mSerialPort != null) {
            mSerialPort.close();
        }
        if (mInputStream != null) {
            try {
                mInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        isOpen = false;
    }

}
