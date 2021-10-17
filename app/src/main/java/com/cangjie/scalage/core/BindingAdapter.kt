package com.cangjie.scalage.core

import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager.widget.ViewPager
import com.cangjie.scalage.core.binding.BindingCommand
import com.cangjie.scalage.core.widget.ErrorReload
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.jakewharton.rxbinding2.view.RxView
import java.util.concurrent.TimeUnit


/**
 * @author nvwa@cangjie
 * Create by AS at 2020/7/9 18:07
 */
private const val CLICK_INTERVAL = 1L


@BindingAdapter(value = ["onClickCommand", "isThrottleFirst"], requireAll = false)
fun onClickCommand(
    view: View?,
    clickCommand: BindingCommand<*>?,
    isThrottleFirst: Boolean
) {
    if (isThrottleFirst) {
        RxView.clicks(view!!)
            .subscribe { clickCommand?.execute() }
    } else {
        RxView.clicks(view!!)
            .throttleFirst(CLICK_INTERVAL, TimeUnit.SECONDS)
            .subscribe { clickCommand?.execute() }
    }
}

@BindingAdapter(value = ["url", "placeholderRes"], requireAll = false)
fun setImageUri(
    imageView: ImageView,
    url: String?,
    placeholderRes: Int
) {
    if (!TextUtils.isEmpty(url)) {
        Glide.with(imageView.context)
            .load(url)
            .apply(RequestOptions().placeholder(placeholderRes))
            .into(imageView)
    }
}

@BindingAdapter(value = ["imgUrl", "placeHolder", "errorHolder"], requireAll = false)
fun loadImageWithPlaceHolder(
    @NonNull
    imageView: AppCompatImageView,
    url: String,
    placeHolder: Drawable,
    errorHolder: Drawable
) {
    Glide.with(imageView.context)
        .load(url)
        .apply(
            RequestOptions.centerCropTransform()
                .placeholder(placeHolder).error(errorHolder)
        ).into(imageView)

}

@BindingAdapter("circleImgUrl")
fun loadCircleImage(@NonNull imageView: AppCompatImageView, url: String) {
    Glide.with(imageView.context)
        .load(url)
        .apply(RequestOptions.bitmapTransform(RoundedCorners(360)))
        .into(imageView)
}

@BindingAdapter(value = ["scrollTo", "offset"])
fun bindScrollTo(@NonNull recyclerView: RecyclerView, position: Int, offset: Int) {
    recyclerView.layoutManager.let {
        when (it) {
            is LinearLayoutManager -> it.scrollToPositionWithOffset(position, offset)

            is GridLayoutManager -> it.scrollToPositionWithOffset(position, offset)

            is StaggeredGridLayoutManager -> it.scrollToPositionWithOffset(position, offset)
        }
    }
}

@BindingAdapter("limitOffset")
fun bindOffscreenPageLimit(@NonNull viewPager: ViewPager, limit: Int) {
    viewPager.offscreenPageLimit = limit
}

@BindingAdapter(value = ["reversed", "transformer"], requireAll = false)
fun bindTransformer(
    @NonNull
    viewPager: ViewPager,
    reversed: Boolean,
    transformer: ViewPager.PageTransformer
) {
    viewPager.setPageTransformer(reversed, transformer)
}

@BindingAdapter(value = ["currentItem", "smoothScroll"])
fun bindCurrentItem(@NonNull viewPager: ViewPager, current: Int, smoothScroll: Boolean) {
    viewPager.setCurrentItem(current, smoothScroll)
}

@BindingAdapter("reload")
fun bindReloadHandler(statusError: StatusError, handler: ErrorReload?) {
    statusError.errorReload = handler
}