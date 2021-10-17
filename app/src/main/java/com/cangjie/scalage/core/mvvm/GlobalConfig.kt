package com.cangjie.scalage.core.mvvm

import androidx.lifecycle.ViewModelProvider

/**
 * @author nvwa@cangjie
 * Create by AS at 2020/8/7 22:10
 */
class GlobalConfig {
    var viewModelFactory: ViewModelProvider.NewInstanceFactory = ViewModelFactory()
}