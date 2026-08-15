/*
 * Copyright (c) 2024 Christians Martínez Alvarado
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.rc.axiom.ui.component.views

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.view.updateLayoutParams
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.shape.MaterialShapeDrawable
import com.rc.axiom.databinding.CollapsingAppbarLayoutBinding
import com.rc.axiom.databinding.SimpleAppbarLayoutBinding
import com.rc.axiom.util.Preferences

class TopAppBarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1,
) : AppBarLayout(context, attrs, defStyleAttr) {
    private var simpleAppbarBinding: SimpleAppbarLayoutBinding? = null
    private var collapsingAppbarBinding: CollapsingAppbarLayoutBinding? = null

    val mode: AppBarMode = Preferences.appBarMode

    init {
        if (mode == AppBarMode.COLLAPSING) {
            collapsingAppbarBinding =
                CollapsingAppbarLayoutBinding.inflate(LayoutInflater.from(context), this, true)
            val isLandscape =
                context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            if (isLandscape) {
                fitsSystemWindows = false
            }

        } else {
            simpleAppbarBinding =
                SimpleAppbarLayoutBinding.inflate(LayoutInflater.from(context), this, true)
            statusBarForeground = MaterialShapeDrawable.createWithElevationOverlay(context)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        simpleAppbarBinding = null
        collapsingAppbarBinding = null
    }

    fun pinWhenScrolled() {
        simpleAppbarBinding?.root?.updateLayoutParams<LayoutParams> {
            scrollFlags = SCROLL_FLAG_NO_SCROLL
        }
    }

    val toolbar: MaterialToolbar
        get() = if (mode == AppBarMode.COLLAPSING) {
            collapsingAppbarBinding?.toolbar!!
        } else {
            simpleAppbarBinding?.toolbar!!
        }

    var title: CharSequence
        get() = if (mode == AppBarMode.COLLAPSING) {
            collapsingAppbarBinding?.collapsingToolbarLayout?.title.toString()
        } else {
            simpleAppbarBinding?.toolbar?.title.toString()
        }
        set(value) {
            val titleStr = value.toString()
            val formattedValue: CharSequence = if (titleStr.equals("axiom", ignoreCase = true) || titleStr.equals("axiom.", ignoreCase = true)) {
                val builder = android.text.SpannableStringBuilder("axiom.")
                for (i in 0 until builder.length) {
                    val char = builder[i].lowercaseChar()
                    if (char == 'x' || char == 'o') {
                        builder.setSpan(
                            android.text.style.ForegroundColorSpan(android.graphics.Color.RED),
                            i,
                            i + 1,
                            android.text.SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
                builder
            } else if (value is android.text.Spanned) {
                value
            } else {
                titleStr.uppercase()
            }

            if (mode == AppBarMode.COLLAPSING) {
                collapsingAppbarBinding?.collapsingToolbarLayout?.title = formattedValue
            } else {
                simpleAppbarBinding?.toolbar?.title = formattedValue
            }
        }

    enum class AppBarMode {
        COLLAPSING,
        SIMPLE
    }
}
