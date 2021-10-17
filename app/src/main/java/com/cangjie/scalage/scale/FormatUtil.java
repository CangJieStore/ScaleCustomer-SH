package com.cangjie.scalage.scale;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FormatUtil {
    /**
     * 将double格式化为指定小数位的String，不足小数位用0补全
     *
     * @param v 需要格式化的数字
     * @param scale 小数点后保留几位
     * @return 指定小数位数的数值字符串
     */
    public static String roundByScale(double v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The   scale   must   be   a   positive   integer   or   zero");
        }
        if (scale == 0) {
            return new DecimalFormat("0").format(v);
        }
        String formatStr = "0.";
        for (int i = 0; i < scale; i++) {
            formatStr = formatStr + "0";
        }
        return new DecimalFormat(formatStr).format(v);

    }

    /* 反转数组并将其存储在另一个数组中的函数*/
    public static byte[] reverse(byte a[], int n) {
        byte[] b = new byte[n];
        int j = n;
        for (int i = 0; i < n; i++) {
            b[j - 1] = a[i];
            j = j - 1;
        }
        return b;
    }

    /**
     * 使用正则表达式来判断字符串验证正数,负数,小数,0
     *
     * @param str 待检验的字符串
     * @return 返回是否包含 true: 可以验证正数,负数,小数,0 ;false
     */
    public static boolean judgeContainsStr(String str) {
        Matcher m = Pattern.compile("-*[0-9]+.*[0-9]*").matcher(str);
        return m.matches();
    }
}
