/*
 * Copyright (c) 2026 Christians Martínez Alvarado
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

package com.rc.axiom.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rc.axiom.databinding.ItemDetailHeaderBinding
import com.rc.axiom.databinding.ItemDetailHorizontalListBinding
import com.rc.axiom.databinding.ItemDetailSectionHeaderBinding
import com.rc.axiom.databinding.ItemDetailWikiBinding
import com.rc.axiom.extensions.resources.setMarkdownText
import com.rc.axiom.extensions.resources.show
import android.content.res.ColorStateList
import android.graphics.Color
import com.rc.axiom.R

class HeaderAdapter(
    private val onBind: (ItemDetailHeaderBinding) -> Unit
) : RecyclerView.Adapter<HeaderAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemDetailHeaderBinding) : RecyclerView.ViewHolder(binding.root)

    private val stableId = View.generateViewId().toLong()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemDetailHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        onBind(holder.binding)
    }

    override fun getItemCount(): Int = 1

    override fun getItemId(position: Int): Long = stableId
}

class SectionHeaderAdapter(
    private var title: String,
    private val onSortClick: ((View) -> Unit)? = null
) : RecyclerView.Adapter<SectionHeaderAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemDetailSectionHeaderBinding) : RecyclerView.ViewHolder(binding.root)

    private val stableId = View.generateViewId().toLong()
    private var visible = true

    init {
        setHasStableIds(true)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setVisible(visible: Boolean) {
        if (this.visible != visible) {
            this.visible = visible
            notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateTitle(title: String) {
        this.title = title
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemDetailSectionHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.title.text = title
        holder.binding.sortOrder.isVisible = onSortClick != null
        holder.binding.sortOrder.setOnClickListener { onSortClick?.invoke(it) }
    }

    override fun getItemCount(): Int = if (visible) 1 else 0

    override fun getItemId(position: Int): Long = stableId
}

class WikiAdapter : RecyclerView.Adapter<WikiAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemDetailWikiBinding) : RecyclerView.ViewHolder(binding.root)

    private val stableId = View.generateViewId().toLong()
    private var title: String? = null
    private var content: String? = null

    init {
        setHasStableIds(true)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(title: String?, content: String?) {
        this.title = title
        this.content = content
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemDetailWikiBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.wikiTitle.text = title
        if (content != null) {
            holder.binding.wiki.show()
            holder.binding.wiki.setMarkdownText(content!!)
            holder.binding.wikiChevron.visibility = View.VISIBLE
            val toggleExpansion = {
                if (holder.binding.wiki.maxLines == 4) {
                    holder.binding.wiki.maxLines = Integer.MAX_VALUE
                    holder.binding.wikiChevron.setImageResource(R.drawable.ic_keyboard_arrow_up_24dp)
                } else {
                    holder.binding.wiki.maxLines = 4
                    holder.binding.wikiChevron.setImageResource(R.drawable.ic_keyboard_arrow_down_24dp)
                }
            }
            holder.binding.wiki.setOnClickListener { toggleExpansion() }
            holder.binding.wikiChevron.setOnClickListener { toggleExpansion() }
        } else {
            holder.binding.wiki.isVisible = false
            holder.binding.wikiChevron.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = if (title != null || content != null) 1 else 0

    override fun getItemId(position: Int): Long = stableId
}

class HorizontalListAdapter(
    private var title: String,
    val innerAdapter: RecyclerView.Adapter<*>,
    private val onSortClick: ((View) -> Unit)? = null
) : RecyclerView.Adapter<HorizontalListAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemDetailHorizontalListBinding) : RecyclerView.ViewHolder(binding.root)

    private val stableId = View.generateViewId().toLong()
    private var visible = false

    init {
        setHasStableIds(true)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setVisible(visible: Boolean) {
        if (this.visible != visible) {
            this.visible = visible
            notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateTitle(title: String) {
        this.title = title
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemDetailHorizontalListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.header.title.text = title
        holder.binding.header.sortOrder.isInvisible = onSortClick == null
        holder.binding.header.sortOrder.setOnClickListener { onSortClick?.invoke(it) }
        holder.binding.recyclerView.apply {
            if (layoutManager == null) {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
            adapter = innerAdapter
        }
    }

    override fun getItemCount(): Int = if (visible) 1 else 0

    override fun getItemId(position: Int): Long = stableId
}

class ArtistExtraInfoAdapter : RecyclerView.Adapter<ArtistExtraInfoAdapter.ViewHolder>() {

    class ViewHolder(val binding: com.rc.axiom.databinding.ItemDetailExtraInfoBinding) : RecyclerView.ViewHolder(binding.root)

    private val stableId = View.generateViewId().toLong()
    private val tags = mutableListOf<String>()
    private var titleText: String = "ARTIST INFO"

    init {
        setHasStableIds(true)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(title: String = "ARTIST INFO", debut: String? = null, genre: String? = null, style: String? = null, mood: String? = null, country: String? = null, extraTags: List<String> = emptyList()) {
        this.titleText = title
        tags.clear()
        if (!debut.isNullOrBlank()) tags.add("DEBUT: $debut")
        if (!genre.isNullOrBlank()) tags.add("GENRE: $genre")
        if (!style.isNullOrBlank()) tags.add("STYLE: $style")
        if (!mood.isNullOrBlank()) tags.add("MOOD: $mood")
        if (!country.isNullOrBlank()) tags.add("LOCATION: $country")
        tags.addAll(extraTags)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(com.rc.axiom.databinding.ItemDetailExtraInfoBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.extraTitle.text = titleText
        holder.binding.chipGroup.removeAllViews()
        val context = holder.binding.root.context
        for (tag in tags) {
            val chip = com.google.android.material.chip.Chip(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                text = tag
                setTextColor(Color.WHITE)
                chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#121212"))
                chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#444444"))
                chipStrokeWidth = 2f
                shapeAppearanceModel = shapeAppearanceModel.toBuilder()
                    .setAllCornerSizes(0f)
                    .build()
                typeface = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.letteramonoll)
                isClickable = false
                isFocusable = false
            }
            holder.binding.chipGroup.addView(chip)
        }
    }

    override fun getItemCount(): Int = if (tags.isNotEmpty()) 1 else 0

    override fun getItemId(position: Int): Long = stableId
}