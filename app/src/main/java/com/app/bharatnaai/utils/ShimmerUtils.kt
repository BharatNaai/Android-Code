package com.app.bharatnaai.utils

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.LinearInterpolator

object ShimmerUtils {
    
    /**
     * Creates a simple shimmer effect using alpha animation
     * For a more advanced shimmer effect, consider using Facebook's Shimmer library
     */
    fun startShimmer(view: View): ValueAnimator {
        val shimmerAnimator = ValueAnimator.ofFloat(0.3f, 1.0f, 0.3f)
        shimmerAnimator.duration = 1500
        shimmerAnimator.repeatCount = ValueAnimator.INFINITE
        shimmerAnimator.interpolator = LinearInterpolator()
        
        shimmerAnimator.addUpdateListener { animation ->
            val alpha = animation.animatedValue as Float
            view.alpha = alpha
        }
        
        shimmerAnimator.start()
        return shimmerAnimator
    }
    
    fun stopShimmer(animator: ValueAnimator?, view: View) {
        animator?.cancel()
        view.alpha = 1.0f
    }
    
    /**
     * Apply shimmer effect to multiple views
     */
    fun startShimmerOnViews(vararg views: View): List<ValueAnimator> {
        return views.map { startShimmer(it) }
    }
    
    fun stopShimmerOnViews(animators: List<ValueAnimator>, vararg views: View) {
        animators.forEach { it.cancel() }
        views.forEach { it.alpha = 1.0f }
    }
}
