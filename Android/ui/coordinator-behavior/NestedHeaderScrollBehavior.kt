@file:Suppress(
    "FunctionName", "UnnecessaryVariable", "NonAsciiCharacters"
)

package com.crow.mangax.ui.cordinator

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.crow.mangax.R

/**
* CoordinatorLayout 滚动View头部需加入此behavior !!! 需要加入id "base_child" !!!
*
* @author:crow
* @time:2024-10-15 13:49:39 下午 星期二
*/
class NestedHeaderScrollBehavior @JvmOverloads constructor(context: Context? = null, attrs: AttributeSet? = null) :
        CoordinatorLayout.Behavior<ViewGroup>(context, attrs) {
    override fun layoutDependsOn(parent: CoordinatorLayout, child: ViewGroup, dependency: View): Boolean {
        // child: 当前 Behavior 所关联的 View，此处是 HeaderView
        // dependency: 待判断是否需要监听的其他子 View
        return dependency.id == R.id.base_child
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: ViewGroup, dependency: View): Boolean {
        child.translationY = dependency.translationY
        // 如果改变了 child 的大小位置必须返回 true 来刷新
        return true
    }
}