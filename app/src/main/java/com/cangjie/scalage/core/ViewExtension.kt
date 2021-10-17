package com.cangjie.scalage.core

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/**
 * @author nvwa@cangjie
 * Create by AS at 2020/7/7 11:35
 */
fun EditText.hideSoftInput() {
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
        ?.hideSoftInputFromWindow(windowToken, 0)
}

fun EditText.showSoftInput() {
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
        ?.showSoftInput(this, InputMethodManager.SHOW_FORCED)
}

fun EditText.clearText() {
    setText("")
}

