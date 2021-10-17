package com.cangjie.scalage.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter

/**
 * @author nvwa@cangjie
 * Create by AS at 2020/7/11 10:08
 */
class BaseFragmentPagerAdapter(
    fm: FragmentManager,
    fragments: MutableList<Fragment>,
    titles: MutableList<String> = arrayListOf()
) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private var mFragments = fragments
    private var mTitles = titles


    override fun getItem(position: Int): Fragment = mFragments[position]
    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE;
    }

    fun update(fragments: MutableList<Fragment>) {
        this.mFragments.clear();
        this.mFragments.addAll(fragments);
        notifyDataSetChanged();
    }

    override fun getCount(): Int = mFragments.size

    override fun getPageTitle(position: Int): CharSequence? =
        if (mTitles.isNullOrEmpty()) super.getPageTitle(position) else mTitles[position]
}