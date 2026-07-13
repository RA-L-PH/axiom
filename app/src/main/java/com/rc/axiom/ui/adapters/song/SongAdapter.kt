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

package com.rc.axiom.ui.adapters.song

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.annotation.MenuRes
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.rc.axiom.R
import com.rc.axiom.coil.DEFAULT_SONG_IMAGE
import com.rc.axiom.core.model.action.SongClickBehavior
import com.rc.axiom.core.model.sort.SortKey
import com.rc.axiom.core.sort.SongSortMode
import com.rc.axiom.data.model.Song
import com.rc.axiom.extensions.dp
import com.rc.axiom.extensions.isActivated
import com.rc.axiom.extensions.loadPaletteImage
import com.rc.axiom.extensions.resources.textColorPrimary
import com.rc.axiom.extensions.resources.textColorSecondary
import com.rc.axiom.extensions.media.asSectionName
import com.rc.axiom.extensions.media.displayArtistName
import com.rc.axiom.extensions.media.songInfo
import com.rc.axiom.extensions.resources.hide
import com.rc.axiom.extensions.showToast
import com.rc.axiom.extensions.utilities.buildInfoString
import com.rc.axiom.ui.ISongCallback
import com.rc.axiom.ui.component.base.AbsMultiSelectAdapter
import com.rc.axiom.ui.component.base.MediaEntryViewHolder
import com.rc.axiom.ui.component.menu.OnClickMenu
import com.rc.axiom.ui.screen.player.PlayerViewModel
import com.rc.axiom.util.Preferences
import me.zhanghai.android.fastscroll.PopupTextProvider
import org.koin.androidx.viewmodel.ext.android.getViewModel

@SuppressLint("NotifyDataSetChanged")
@Suppress("LeakingThis")
open class SongAdapter(
    protected val activity: FragmentActivity,
    dataSet: List<Song>,
    @LayoutRes protected val itemLayoutRes: Int = R.layout.item_list,
    protected val sortMode: SongSortMode? = null,
    protected val callback: ISongCallback? = null,
) : AbsMultiSelectAdapter<SongAdapter.ViewHolder, Song>(activity, R.menu.menu_media_selection), PopupTextProvider {

    open var dataSet: List<Song> = dataSet
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var currentSongId: Long = -1L
        set(value) {
            if (field != value) {
                val oldPosition = dataSet.indexOfFirst { it.id == field }
                val newPosition = dataSet.indexOfFirst { it.id == value }
                field = value
                if (oldPosition >= 0) notifyItemChanged(oldPosition)
                if (newPosition >= 0) notifyItemChanged(newPosition)
            }
        }

    protected open fun createViewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(itemLayoutRes, parent, false)
        return createViewHolder(view, viewType)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song: Song = dataSet[position]
        val isChecked = isChecked(song)
        holder.isActivated = isChecked
        holder.menu?.isGone = isChecked
        holder.title?.text = getSongTitle(song)
        holder.text?.text = getSongText(song)
        
        val isCurrentSong = song.id == currentSongId
        val redColor = 0xFFD71921.toInt()
        val defaultTitleColor = holder.itemView.context.textColorPrimary()
        val defaultTextColor = holder.itemView.context.textColorSecondary()
        holder.title?.setTextColor(if (isCurrentSong) redColor else defaultTitleColor)
        holder.text?.setTextColor(if (isCurrentSong) redColor else defaultTextColor)
        
        // Red border on artwork for current song
        val shapeImage = holder.image as? com.google.android.material.imageview.ShapeableImageView
        if (shapeImage != null) {
            if (isCurrentSong) {
                shapeImage.strokeWidth = 3.dp(holder.itemView.resources).toFloat()
                shapeImage.strokeColor = ColorStateList.valueOf(redColor)
            } else {
                shapeImage.strokeWidth = 1.dp(holder.itemView.resources).toFloat()
                shapeImage.strokeColor = ColorStateList.valueOf(Color.WHITE)
            }
        }
        
        // Red tint on menu for current song
        holder.menu?.iconTint = if (isCurrentSong) {
            ColorStateList.valueOf(redColor)
        } else {
            null
        }
        
        // Check if imageContainer exists, so we can have a smooth transition without
        // CardView clipping, if it doesn't exist in current layout set transition name to image instead.
        if (holder.imageContainer != null) {
            holder.imageContainer.transitionName = song.id.toString()
        } else {
            holder.image?.transitionName = song.id.toString()
        }
        holder.loadPaletteImage(song, DEFAULT_SONG_IMAGE)
    }

    private fun getSongTitle(song: Song): String {
        return when (sortMode?.selectedKey) {
            SortKey.FileName -> song.fileName
            else -> song.title
        }
    }

    protected open fun getSongText(song: Song): String? {
        return when (sortMode?.selectedKey) {
            SortKey.Year -> if (song.year > 0) {
                buildInfoString(song.displayArtistName(), song.year.toString())
            } else {
                song.displayArtistName()
            }

            SortKey.Album -> buildInfoString(song.displayArtistName(), song.albumName)

            else -> song.songInfo()
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun getItemId(position: Int): Long {
        return dataSet[position].id
    }

    override fun getIdentifier(position: Int): Song? {
        return dataSet[position]
    }

    override fun getName(item: Song): String? {
        return item.title
    }

    override fun onMultipleItemAction(menuItem: MenuItem, selection: List<Song>) {
        callback?.songsMenuItemClick(selection, menuItem)
    }

    override fun getPopupText(view: View, position: Int): CharSequence {
        val song = dataSet.getOrNull(position) ?: return ""
        return when (sortMode?.selectedKey) {
            SortKey.Album -> song.albumName.asSectionName(sortMode)
            SortKey.Artist -> song.displayArtistName().asSectionName(sortMode)
            SortKey.AZ -> song.title.asSectionName(sortMode)
            SortKey.Year -> ""
            SortKey.FileName -> song.fileName.asSectionName(sortMode)
            else -> song.title.asSectionName(sortMode)
        }
    }

    open inner class ViewHolder(view: View) : MediaEntryViewHolder(view) {
        protected open val song: Song
            get() = dataSet[bindingAdapterPosition]

        @get:MenuRes
        protected open val songMenuRes: Int
            get() = R.menu.menu_item_song

        protected val sharedElements: Array<Pair<View, String>>?
            get() = if (image != null && image.isVisible) arrayOf(image to image.transitionName) else null

        protected val songClickBehavior: SongClickBehavior
            get() = Preferences.songClickAction

        @CallSuper
        protected open fun onPrepareSongMenu(menu: Menu) {
            menu.findItem(R.id.action_play)
                ?.isVisible = !songClickBehavior.isAbleToPlay || Preferences.playOptionAlwaysVisible
        }

        protected open fun onSongMenuItemClick(item: MenuItem): Boolean {
            if (item.itemId == R.id.action_play) {
                val playerViewModel = activity.getViewModel<PlayerViewModel>()
                val playOptionBehavior = Preferences.playOptionClickBehavior
                playerViewModel.openSongs(bindingAdapterPosition, dataSet, playOptionBehavior)
                return true
            }
            return callback?.songMenuItemClick(song, item, sharedElements) ?: false
        }

        override fun onClick(view: View) {
            if (isInQuickSelectMode) {
                toggleChecked(bindingAdapterPosition)
            } else {
                val songClickBehavior = Preferences.songClickAction
                val playerViewModel = activity.getViewModel<PlayerViewModel>()
                playerViewModel.openSongs(bindingAdapterPosition, dataSet, songClickBehavior)
                if (!songClickBehavior.isAbleToPlay) {
                    activity.showToast(R.string.added_title_to_playing_queue)
                }
            }
        }

        override fun onLongClick(view: View): Boolean {
            return toggleChecked(bindingAdapterPosition)
        }

        init {
            play?.hide()
            menu?.setOnClickListener(object : OnClickMenu() {
                override val popupMenuRes: Int
                    get() = songMenuRes

                override fun onPreparePopup(menu: Menu) {
                    onPrepareSongMenu(menu)
                }

                override fun onMenuItemClick(item: MenuItem): Boolean {
                    return onSongMenuItemClick(item)
                }
            })
        }
    }

    init {
        setHasStableIds(true)
    }

}