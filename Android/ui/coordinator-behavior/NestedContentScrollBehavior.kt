package com.crow.mangax.ui.cordinator

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.widget.OverScroller
import androidx.compose.material3.surfaceColorAtElevation
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.customview.view.AbsSavedState
import com.crow.base.tools.extensions.log
import com.crow.mangax.R

/**
* CoordinatorLayout 滚动View头部需加入此behavior !!! 需要加入id "base_child" !!!
*
* @author:crow
* @time:2024-10-15 13:49:39 下午 星期二
*/
class NestedContentScrollBehavior @JvmOverloads constructor(context: Context? = null, attrs: AttributeSet? = null) :
        CoordinatorLayout.Behavior<View>(context, attrs) {

    
class SavedState : AbsSavedState {
        var mTranslationY: Float = 0f

        constructor(parcel: Parcel) : super(parcel) {
            mTranslationY = parcel.readFloat()
}

        constructor(source: Parcel, loader: ClassLoader?) : super(source, loader) {
            mTranslationY = source.readFloat()
        }

        constructor(superState: Parcelable) : super(superState)

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeFloat(mTranslationY)
        }


        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : ClassLoaderCreator<SavedState> {
            override fun createFromParcel(source: Parcel, loader: ClassLoader?): SavedState {
                return SavedState(source, loader)
            }

            override fun createFromParcel(source: Parcel): SavedState {
                return SavedState(source, null)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

    private lateinit var mContentView: View // 其实就是 RecyclerView
    private var mHeaderHeight = 0
    private var mSavedState: SavedState? = null
    private var mScroller: OverScroller? = null
    private val mScrollRunnable = object : Runnable {
        override fun run() {
            mScroller?.let { scroller ->
                if (scroller.computeScrollOffset()) {
                    mContentView.translationY = scroller.currY.toFloat()
                    ViewCompat.postOnAnimation(mContentView, this)
                }
            }
        }
    }

    override fun onSaveInstanceState(parent: CoordinatorLayout, child: View): Parcelable {
        val susperState = super.onSaveInstanceState(parent, child)
        val state = SavedState(susperState ?: AbsSavedState.EMPTY_STATE)
        state.mTranslationY = child.translationY
        return state
    }
    override fun onRestoreInstanceState(parent: CoordinatorLayout, child: View, state: Parcelable) {
        "onRestoreInstanceState : $state \t super : $".log()
        if (state is SavedState) {
            mSavedState = state
            super.onRestoreInstanceState(parent, child, state.superState!!)
        } else {
            super.onRestoreInstanceState(parent, child, state)

        }
    }
    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        mContentView = child
        // 首先让父布局按照标准方式解析
        parent.onLayoutChild(child, layoutDirection)
        // 获取到 HeaderView 的高度
        mHeaderHeight = parent.findViewById<View>(R.id.base_header).height
        // 设置 top 从而排在 HeaderView的下面
//        ViewCompat.offsetTopAndBottom(child, headerHeight)

        "ChildTop : ${child.top} \t mHeaderHeight : $mHeaderHeight \t ${mSavedState?.mTranslationY}".log()
        if (child.top < mHeaderHeight) {
            ViewCompat.offsetTopAndBottom(child, mHeaderHeight - child.top)
        } else {
            ViewCompat.offsetTopAndBottom(child, mHeaderHeight)
        }
        if (mSavedState != null && mSavedState?.mTranslationY != 0f) {
            child.translationY = mSavedState!!.mTranslationY
        }

        return true // true 表示我们自己完成了解析 不要再自动解析了
    }
    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout, child: View, directTargetChild: View,
        target: View, axes: Int, type: Int,
    ): Boolean {
        super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)
        // 如果是垂直滑动的话就声明需要处理
        // 只有这里返回 true 才会收到下面一系列滑动事件的回调
        if (child.translationY < 0) {
            child.parent.requestDisallowInterceptTouchEvent(true)
        } else {
            child.parent.requestDisallowInterceptTouchEvent(false)
        }
        return (axes and ViewCompat.SCROLL_AXIS_VERTICAL) != 0
    }

    /**
     * 处理RecyclerView的y轴偏移以及滑动距离
     *
     * 2024-06-23 01:40:36 周日 上午
     * @author crowforkotlin
     */
    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout, child: View, target: View, dx: Int, dy: Int,
        consumed: IntArray, type: Int,
    ) {
        // 此时 RecyclerView 还没开始滑动
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
//        stopAutoScroll()
        if (dy > 0) { // 只处理手指上滑
            val newTransY = child.translationY - dy
            if (newTransY >= -mHeaderHeight) {
                // 完全消耗滑动距离后没有完全贴顶或刚好贴顶
                // 那么就声明消耗所有滑动距离，并上移 RecyclerView
                consumed[1] = dy // consumed[0/1] 分别用于声明消耗了x/y方向多少滑动距离
                child.translationY = newTransY
            } else {
                // 如果完全消耗那么会导致 RecyclerView 超出可视区域
                // 那么只消耗恰好让 RecyclerView 贴顶的距离
                consumed[1] = -mHeaderHeight + child.translationY.toInt()
                child.translationY = -mHeaderHeight.toFloat()
            }
        }
    }
    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout, child: View, target: View, dxConsumed: Int,
        dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int, consumed: IntArray,
    ) {
        // 此时 RV 已经完成了滑动，dyUnconsumed 表示剩余未消耗的滑动距离
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
            type, consumed)
//        stopAutoScroll()
        if (child.translationY >= 0f) {
            (child.parent as CoordinatorLayout).requestDisallowInterceptTouchEvent(false)
        }
        if (dyUnconsumed < 0) { // 只处理手指向下滑动的情况
            val newTransY = child.translationY - dyUnconsumed
            if (newTransY <= 0) {
                child.translationY = newTransY
            } else {
                child.translationY = 0f
            }
        }
    }

    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: View, target: View, type: Int) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type)
        if (child.translationY >= 0f || child.translationY <= -mHeaderHeight) {
            // RV 已经归位（完全折叠或完全展开）
            return
        }
        if (child.translationY <= -mHeaderHeight * 0.5f) {
//            stopAutoScroll()
//            startAutoScroll(child.translationY.toInt(), -headerHeight, 1000)
        } else {
//            stopAutoScroll()
//            startAutoScroll(child.translationY.toInt(), 0, 600)
        }
    }

    private fun startAutoScroll(current: Int, target: Int, duration: Int) {
        if (mScroller == null) {
            mScroller = OverScroller(mContentView.context)
        }
        if (mScroller!!.isFinished) {
            mContentView.removeCallbacks(mScrollRunnable)
            mScroller!!.startScroll(0, current, 0, target - current, duration)
            ViewCompat.postOnAnimation(mContentView, mScrollRunnable)
        }
    }

    private fun stopAutoScroll() {
        mScroller?.let {
            if (!it.isFinished) {
                it.abortAnimation()
                mContentView.removeCallbacks(mScrollRunnable)
            }
        }
    }
}
