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

package com.rc.axiom.ui.component.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import com.rc.axiom.util.Preferences;
import com.rc.axiom.core.model.theme.NowPlayingScreen;

/**
 * @author Christians M. A. (rc)
 */
public class AlbumCoverViewPager extends ViewPager {

	private boolean allowSwiping;

	public AlbumCoverViewPager(@NonNull Context context) {
		this(context, null);
	}

	public AlbumCoverViewPager(@NonNull Context context, AttributeSet attrs) {
		super(context, attrs);
		setAllowSwiping(Preferences.INSTANCE.getSwipeOnCover());
	}

	public void setAllowSwiping(boolean allowSwiping) {
		this.allowSwiping = allowSwiping;
	}

	private MotionEvent swapXY(MotionEvent ev) {
		float width = getWidth();
		float height = getHeight();
		float newX = (ev.getY() / height) * width;
		float newY = (ev.getX() / width) * height;
		ev.setLocation(newX, newY);
		return ev;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (Preferences.INSTANCE.getNowPlayingScreen() == NowPlayingScreen.Reels) {
			if (getParent() != null) {
				getParent().requestDisallowInterceptTouchEvent(true);
			}
			boolean intercepted = super.onInterceptTouchEvent(swapXY(ev));
			swapXY(ev); // swap back
			return intercepted;
		}
		if (allowSwiping) {
			return super.onInterceptTouchEvent(ev);
		}
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (Preferences.INSTANCE.getNowPlayingScreen() == NowPlayingScreen.Reels) {
			if (getParent() != null) {
				getParent().requestDisallowInterceptTouchEvent(true);
			}
			return super.onTouchEvent(swapXY(ev));
		}
		if (allowSwiping) {
			return super.onTouchEvent(ev);
		}
		return true;
	}
}
