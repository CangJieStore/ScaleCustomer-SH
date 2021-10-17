package com.cangjie.scalage.core.binding

/**
 * @author nvwa@cangjie
 * Create by AS at 2020/7/8 13:45
 */
interface BindingFunction<T> {
    fun call(): T
}