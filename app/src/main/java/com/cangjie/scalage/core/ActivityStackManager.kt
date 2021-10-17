package com.cangjie.scalage.core

import android.app.Activity

/**
 * @author nvwa@cangjie
 * Create by AS at 2020/7/7 09:31
 */
object ActivityStackManager {

    private val activities = mutableListOf<Activity>()

    fun addActivity(activity: Activity) = activities.add(activity)

    fun removeActivity(activity: Activity) {
        if (activities.contains(activity)) {
            activities.remove(activity)
            activity.finish()
        }
    }

    fun getTopActivity(): Activity? =
        if (activities.isEmpty()) null else activities[activities.size - 1]

    fun finishAll() =
        activities.filter { it.isFinishing }.forEach { it.finish() }
}