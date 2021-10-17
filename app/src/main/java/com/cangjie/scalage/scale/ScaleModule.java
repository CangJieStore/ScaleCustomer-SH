package com.cangjie.scalage.scale;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import android_serialport_api.SerialPort;

/**
 * used for scale module.
 */

public class ScaleModule {
    private static final String TAG = "MainWeightLog";

    public static final String WeightValueChanged = "broadcast_receiver_weight_changed";
    public static final String ERROR = "broadcast_receiver_error_message";
    private Context context;

    private byte[] cmd_get_weight = {0x02, 0x41, 0x4e, 0x31, 0x03};//读取重量

    private byte[] cmd_zero_clear = {0x02, 0x41, 0x5a, 0x03};  //置零
    private byte[] cmd_clear_tare = {0x02, 0x41, 0x54, 0x03};  //去皮

    private byte[] cmd_set_piont = {0x02, 0x41, 0x41, 0x33, 0x03};          //小数点设置
    private byte[] cmd_set_devision = {0x02, 0x41, 0x42, 0x31, 0x30, 0x03};  //分度值设置
    private byte[] cmd_set_uint = {0x02, 0x41, 0x48, 0x31, 0x03};           //单位设置

    private byte[] cmd_set_maxweight = {0x02, 0x41, 0x43, 0x30, 0x31, 0x35, 0x30, 0x30, 0x30, 0x03};  //满量程设置
    private byte[] cmd_set_filter = {0x02, 0x41, 0x44, 0x32, 0x03};                                   //滤波强度设置


    private byte[] cmd_calibration_zero = {0x02, 0x41, 0x45, 0x03};                                      //标定零点
    private byte[] cmd_calibration_full = {0x02, 0x41, 0x46, 0x30, 0x32, 0x30, 0x30, 0x30, 0x30, 0x03};  //加载点与加载值确认


    private byte[] cmd_set_zero_trace = {0x02, 0x41, 0x48, 0x31, 0x03};      //零点跟踪范围
    private byte[] cmd_set_zerorange_hand = {0x02, 0x41, 0x4a, 0x31, 0x03};  //手动置零范围
    private byte[] cmd_set_zerorange_start = {0x02, 0x41, 0x4b, 0x31, 0x03}; //开机置零范围
    private byte[] cmd_set_addr = {0x02, 0x41, 0x4c, 0x31, 0x30, 0x03};      //设置仪表地址
    private byte[] cmd_set_samplerate = {0x02, 0x41, 0x4d, 0x31, 0x03};      //采样速率

    private byte[] cmd_get_config = {0x02, 0x41, 0x51, 0x03};                //读取仪表内部参数
    private byte[] cmd_get_maxweight = {0x02, 0x41, 0x53, 0x03};                //读取最大量程

    private byte[] recv_buf = new byte[1024];

    public double RawValue;

    public double NetWeight;
    public double ZeroWeight;
    public double TareWeight = 0;
    public int SetMaxWeight;
    public int SetDotPoint;
    public int SetDevision;
    public int SetFilter;
    public int SetUint;
    public int SetZeroTrace;
    public int SetZeroByHand;
    public int SetZeroByStart;
    public int SetSampleRate;

    /// <summary>
    /// 称重主板
    /// </summary>
    private ScaleModule(Context context) throws Exception {
        this.context = context;
        borad_init();
    }

    private static ScaleModule singleton = null;

    /// <summary>
    /// 单件模式
    /// </summary>
    public static ScaleModule Instance(Context context) throws Exception {
        if (singleton == null) {
            singleton = new ScaleModule(context);

        }
        return singleton;
    }

    /// <summary>
    /// Set指令
    /// </summary>
    /// <param name="cmd"></param>
    /// <returns></returns>
    public void ExecWithEmpty(byte[] cmd) throws Exception {
        //发送的命令都以02(XON)开始03(X0FF)结束,第二位Addr(A-Z)为地址，第三位A-Z为命令字符
        synchronized (SerialPort.LOCK) {
            if (SerialPortUtilForScale.mSerialPort != null && SerialPortUtilForScale.isOpen) {
                SerialPortUtilForScale.mOutputStream.write(cmd);
                SerialPortUtilForScale.mOutputStream.flush();
            }
        }
    }

    /// <summary>
    /// Set指令
    /// </summary>
    /// <param name="cmd"></param>
    /// <returns></returns>
    public byte[] ExecWithResponse(byte[] cmd) throws Exception {
        //发送的命令都以02(XON)开始03(X0FF)结束,第二位Addr(A-Z)为地址，第三位A-Z为命令字符
        if (SerialPortUtilForScale.mSerialPort != null && SerialPortUtilForScale.isOpen) {
            synchronized (SerialPort.LOCK) {
                //SerialPortUtilForScale.mInputStream.reset();
                SerialPortUtilForScale.mOutputStream.write(cmd);
                SerialPortUtilForScale.mOutputStream.flush();
                Thread.sleep(90);
                if (SerialPortUtilForScale.mInputStream.available() > 0) {
                    byte[] recv = new byte[SerialPortUtilForScale.mInputStream.available()];
                    int size = SerialPortUtilForScale.mInputStream.read(recv);
                    if (size > 0) {
                        return recv;
                    } else {
                        throw new Exception("串口无响应或操作失败");
                    }
                }
            }

        }
        return null;
    }

    private void borad_init() throws Exception {
        if (!ReadConfig()) {
            throw new Exception("参数初始化失败！");
        } else {
            if (!ReadMaxWeightConfig()) {
                throw new Exception("最大量程参数获取失败！");
            }
            if (weighing_thread == null) {
                weighing_thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Weighing();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Intent intent = new Intent(ERROR);
                            intent.putExtra("error", e.getMessage());
                            context.sendBroadcast(intent);
                            Log.d(TAG, "Weighing run error: " + e.getMessage());
                        }
                    }
                });
                weighing_thread.start();
            }
        }
    }

    private Thread weighing_thread;

    private void Weighing() throws Exception {
        byte[] result = null;
        double old_weight = 0;
        byte[] tmpBuf = new byte[7];

        while (true) {
            result = ExecWithResponse(cmd_get_weight);

            if (result != null) {
                if (CheckDataProtocol(result)) {
                    try {
                        System.arraycopy(result, 3, tmpBuf, 0, 7);
                        tmpBuf = FormatUtil.reverse(tmpBuf, tmpBuf.length);
                        String str = new String(tmpBuf, 0, tmpBuf.length);
                        Log.d("TmpTag", "Weighing: "+str);
                        RawValue = Double.valueOf(str);
                        NetWeight = RawValue - ZeroWeight - TareWeight;
                        if (old_weight != RawValue) {
                            context.sendBroadcast(new Intent(WeightValueChanged));
                            old_weight = RawValue;
                        }
                    } catch (Exception ee) {
                        continue;
                    }
                }
            }
            Thread.sleep(80);
        }
    }

    private boolean CheckDataProtocol(byte[] result) {
        if (result[0] == 0x02 && result[result.length - 1] == 0x03)
            return true;
        else
            return false;
    }

    private boolean ReadConfig() throws Exception {
        byte[] ret = ExecWithResponse(cmd_get_config);

        if (ret.length > 0) {
            if (CheckDataProtocol(ret)) {
                SetDotPoint = (int) ret[2] - 48;
                SetDevision = ((int) ret[3] - 48) * 10 + (int) ret[4] - 48;

                SetFilter = (int) ret[5] - 48;
                SetUint = (int) ret[6] - 48;
                SetZeroTrace = (int) ret[7] - 48;
                SetZeroByHand = (int) ret[8] - 48;
                SetZeroByStart = (int) ret[9] - 48;
                SetSampleRate = (int) ret[10] - 48;

                return true;
            }
        }
        return false;

    }

    private boolean ReadMaxWeightConfig() throws Exception {
        byte[] ret = ExecWithResponse(cmd_get_maxweight);

        if (ret.length > 0) {
            if (CheckDataProtocol(ret)) {
                byte[] tmp = new byte[6];
                System.arraycopy(ret, 2, tmp, 0, 6);
                SetMaxWeight = (int)(Integer.parseInt(new String(tmp, 0, tmp.length)) / Math.pow(10, SetDotPoint));

                return true;
            }
        }
        return false;

    }

    /// <summary>
    ///置零(清零)
    /// </summary>
    public void ZeroClear() throws Exception {
        ExecWithEmpty(cmd_zero_clear);
    }

    /// <summary>
    ///去皮
    /// </summary>
    public void ClearTare() throws Exception {
        TareWeight = RawValue;
    }

    /// <summary>
    ///清皮
    /// </summary>
    public void TareClear() {
        TareWeight = 0;
    }

    /// <summary>
    /// 读取皮重
    /// </summary>
    /// <returns></returns>
    public void GetTareWeight() {
    }

    //标定
    public void SetDeadLoadWeight() throws Exception
    {
        ExecWithEmpty(cmd_calibration_zero);
    }

    public void SetFullLoadWeight(double double_value) throws Exception {
        int result = 0;
        int raw_value = (int) (double_value * Math.pow(10, SetDotPoint));

        result = (raw_value) / 100000;
        cmd_calibration_full[3] = (byte) (result + 48);
        raw_value = raw_value - result * 100000;

        result = (raw_value) / 10000;
        cmd_calibration_full[4] = (byte) (result + 48);
        raw_value = raw_value - result * 10000;

        result = (raw_value) / 1000;
        cmd_calibration_full[5] = (byte) (result + 48);
        raw_value = raw_value - result * 1000;
        result = (raw_value) / 100;
        cmd_calibration_full[6] = (byte) (result + 48);
        raw_value = raw_value - result * 100;

        result = (raw_value) / 10;
        cmd_calibration_full[7] = (byte) (result + 48);
        raw_value = raw_value - result * 10;

        result = (raw_value) / 1;
        cmd_calibration_full[8] = (byte) (result + 48);
        raw_value = raw_value - result * 1;

        ExecWithEmpty(cmd_calibration_full);
    }

    //save the parameters
    public void Save_DotPoint(int data) throws Exception {
        cmd_set_piont[3] = (byte) (data + 48);

        ExecWithEmpty(cmd_set_piont);
    }

    public void Save_Devision(int data) throws Exception {
        int i, j;
        i = data / 10;
        j = data % 10;

        cmd_set_devision[3] = (byte) (i + 48);
        cmd_set_devision[4] = (byte) (j + 48);

        ExecWithEmpty(cmd_set_devision);
    }

    public void Save_Uint(int data) throws Exception {
        cmd_set_uint[3] = (byte) (byte) (data + 48);

        ExecWithEmpty(cmd_set_uint);
    }

    public void Save_Filter(int data) throws Exception {
        cmd_set_filter[3] = (byte) (byte) (data + 48);

        ExecWithEmpty(cmd_set_filter);
    }

    public void Save_ZeroTrace(int data) throws Exception {
        cmd_set_zero_trace[3] = (byte) (byte) (data + 48);

        ExecWithEmpty(cmd_set_zero_trace);
    }

    public void Save_ZeroRange_Hand(int data) throws Exception {
        cmd_set_zerorange_hand[3] = (byte) (byte) (data + 48);

        ExecWithEmpty(cmd_set_zerorange_hand);
    }

    public void Save_ZeroRange_Start(int data) throws Exception {
        cmd_set_zerorange_start[3] = (byte) (byte) (data + 48);

        ExecWithEmpty(cmd_set_zerorange_start);
    }

    public void Save_SampleRate(int data) throws Exception {
        cmd_set_samplerate[3] = (byte) (byte) (data + 48);

        ExecWithEmpty(cmd_set_samplerate);
    }

    public void Save_MaxWeight(double double_value) throws Exception {
        int result = 0;

        int raw_value = (int) (double_value * Math.pow(10, SetDotPoint));

        result = (raw_value) / 100000;
        cmd_set_maxweight[3] = (byte) (result + 48);
        raw_value = raw_value - result * 100000;

        result = (raw_value) / 10000;
        cmd_set_maxweight[4] = (byte) (result + 48);
        raw_value = raw_value - result * 10000;

        result = (raw_value) / 1000;
        cmd_set_maxweight[5] = (byte) (result + 48);
        raw_value = raw_value - result * 1000;
        result = (raw_value) / 100;
        cmd_set_maxweight[6] = (byte) (result + 48);
        raw_value = raw_value - result * 100;

        result = (raw_value) / 10;
        cmd_set_maxweight[7] = (byte) (result + 48);
        raw_value = raw_value - result * 10;

        result = (raw_value) / 1;
        cmd_set_maxweight[8] = (byte) (result + 48);
        raw_value = raw_value - result * 1;

        ExecWithEmpty(cmd_set_maxweight);
    }
}
