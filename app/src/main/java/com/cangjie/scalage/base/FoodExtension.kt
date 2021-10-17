package com.cangjie.scalage.base

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author nvwa@cangjie
 * Create by AS at 2020/8/14 18:17
 */
internal fun Calendar.formatDate(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(this.time)
}

internal fun getNow(): String {
    return if (android.os.Build.VERSION.SDK_INT >= 24) {
        SimpleDateFormat("yyyy-MM-dd").format(Date())
    } else {
        val tms = Calendar.getInstance()
        tms.get(Calendar.YEAR).toString() + "-" + tms.get(Calendar.MONTH)
            .toString() + "-" + tms.get(Calendar.DAY_OF_MONTH).toString()
    }
}

fun AppCompatActivity.showToast(text: String) {
    runOnUiThread { Toast.makeText(this, text, Toast.LENGTH_SHORT).show() }
}