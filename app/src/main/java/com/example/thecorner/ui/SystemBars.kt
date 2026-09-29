package com.example.thecorner.ui

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.max

/**
 * Shared Views inset helpers.
 *
 * MainActivity owns the nav-host and BottomNavigationView relationship. These
 * helpers are for foreground content that intentionally draws inside a
 * fragment, and never consume the insets so sibling views can also receive them.
 */
fun View.applyTopSystemBarInset() {
    applySystemBarInsets(top = true)
}

fun View.applyTopAndImeInsets() {
    val initial = Padding(paddingLeft, paddingTop, paddingRight, paddingBottom)
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val safe = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
        view.setPadding(
            initial.left + safe.left,
            initial.top + safe.top,
            initial.right + safe.right,
            initial.bottom + imeBottom,
        )
        insets
    }
    ViewCompat.requestApplyInsets(this)
}

fun View.applyTopAndBottomSystemBarInsets(includeIme: Boolean = false) {
    applySystemBarInsets(top = true, bottom = true, includeIme = includeIme)
}

private fun View.applySystemBarInsets(
    top: Boolean = false,
    bottom: Boolean = false,
    includeIme: Boolean = false,
) {
    val initial = Padding(
        left = paddingLeft,
        top = paddingTop,
        right = paddingRight,
        bottom = paddingBottom,
    )

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val safe = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        val imeBottom = if (includeIme) {
            insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
        } else {
            0
        }

        view.setPadding(
            initial.left + safe.left,
            initial.top + if (top) safe.top else 0,
            initial.right + safe.right,
            initial.bottom + if (bottom) max(safe.bottom, imeBottom) else 0,
        )
        insets
    }
    ViewCompat.requestApplyInsets(this)
}

fun View.addTopInsetToLayoutMargin() {
    val layoutParams = layoutParams as? ViewGroup.MarginLayoutParams ?: return
    val initialTopMargin = layoutParams.topMargin

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val safe = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        val params = view.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = initialTopMargin + safe.top
        view.layoutParams = params
        insets
    }
    ViewCompat.requestApplyInsets(this)
}

private data class Padding(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
)
