package com.rc.axiom.ui.screen.player.styles.reelsstyle

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.content.SharedPreferences
import android.graphics.Color
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.view.MenuItem
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.core.view.updatePadding
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.rc.axiom.R
import com.rc.axiom.core.model.action.NowPlayingAction
import com.rc.axiom.core.model.player.PlayerColorScheme
import com.rc.axiom.core.model.player.PlayerTintTarget
import com.rc.axiom.core.model.player.iconButtonTintTarget
import com.rc.axiom.core.model.player.tintTarget
import com.rc.axiom.data.model.Song
import com.rc.axiom.databinding.FragmentReelsPlayerPlaybackControlsBinding
import com.rc.axiom.extensions.getShareSongIntent
import com.rc.axiom.extensions.dp
import com.rc.axiom.extensions.resources.showBounceAnimation
import com.rc.axiom.ui.component.base.AbsPlayerControlsFragment
import com.rc.axiom.ui.component.menu.newPopupMenu
import com.rc.axiom.ui.component.views.MusicSlider
import com.rc.axiom.ui.screen.player.PlayerAnimator
import com.rc.axiom.util.DISPLAY_NEXT_SONG
import com.rc.axiom.util.Preferences
import androidx.navigation.fragment.findNavController
import java.util.LinkedList
import com.rc.axiom.extensions.media.asReadableDuration
import kotlinx.coroutines.flow.combine
import com.rc.axiom.extensions.launchAndRepeatWithViewLifecycle

class ReelsPlayerControlsFragment : AbsPlayerControlsFragment(R.layout.fragment_reels_player_playback_controls) {

    private var _binding: FragmentReelsPlayerPlaybackControlsBinding? = null
    private val binding get() = _binding!!

    override val playPauseFab: FloatingActionButton
        get() = binding.playPauseButton

    override val repeatButton: MaterialButton
        get() = binding.repeatButton

    override val shuffleButton: MaterialButton
        get() = binding.shuffleButton

    override val musicSlider: MusicSlider
        get() = binding.progressSlider

    override val songCurrentProgress: TextView
        get() = binding.songCurrentProgress

    override val songTotalTime: TextView
        get() = binding.songTotalTime

    override val songTitleView: TextView
        get() = binding.title

    override val songArtistView: TextView
        get() = binding.text

    override val songInfoView: TextView
        get() = binding.songInfo

    private var isFavorite: Boolean = false

    override fun onStart() {
        super.onStart()
        // Apply track colors after base class inflates the Slider view
        applyCustomTrackColors()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentReelsPlayerPlaybackControlsBinding.bind(view)

        binding.playPauseButton.setOnClickListener(this)
        binding.shuffleButton.setOnClickListener(this)
        binding.repeatButton.setOnClickListener(this)

        setViewAction(binding.favorite, NowPlayingAction.ToggleFavoriteState)
        binding.addToPlaylistButton.setOnClickListener {
            val currentSong = playerViewModel.currentSong
            if (currentSong != Song.emptySong) {
                com.rc.axiom.ui.dialogs.playlists.AddToPlaylistDialog.create(currentSong)
                    .show(childFragmentManager, "ADD_TO_PLAYLIST")
            }
        }
        setViewAction(binding.speakerButton, NowPlayingAction.SoundSettings)
        binding.lyricsButton.setOnClickListener {
            playerFragment?.onQuickActionEvent(NowPlayingAction.Lyrics)
        }
        
        binding.topRightQueueButton.setOnClickListener {
            val navOptions = androidx.navigation.navOptions {
                anim {
                    enter = R.anim.axiom_fragment_open_enter
                    exit = R.anim.axiom_fragment_open_exit
                    popEnter = R.anim.axiom_fragment_close_enter
                    popExit = R.anim.axiom_fragment_close_exit
                }
            }
            findNavController().navigate(R.id.nav_queue, null, navOptions)
        }

        binding.moreInfoButton.setOnClickListener {
            val currentSong = playerViewModel.currentSong
            if (currentSong != Song.emptySong) {
                findNavController().navigate(
                    R.id.nav_song_details,
                    com.rc.axiom.extensions.navigation.songDetailArgs(currentSong)
                )
            }
        }

        binding.queueButton.setOnClickListener { button ->
            val context = button.context
            val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(context)
            val layout = android.widget.LinearLayout(context).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setBackgroundColor(Color.parseColor("#121212"))
                setPadding(0, 16, 0, 16)
            }
            
            val handle = View(context).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(40.dp(context), 4.dp(context)).apply {
                    gravity = android.view.Gravity.CENTER_HORIZONTAL
                    bottomMargin = 16.dp(context)
                }
                setBackgroundColor(Color.parseColor("#333333"))
            }
            layout.addView(handle)
            
            val menu = newPopupMenu(button, R.menu.menu_now_playing).menu
            val items = mutableListOf<MenuItem>()
            for (i in 0 until menu.size()) {
                val item = menu.getItem(i)
                if (!item.isVisible) continue
                if (item.hasSubMenu()) {
                    val subMenu = item.subMenu
                    if (subMenu != null) {
                        for (j in 0 until subMenu.size()) {
                            val subItem = subMenu.getItem(j)
                            if (subItem.isVisible) {
                                items.add(subItem)
                            }
                        }
                    }
                } else {
                    items.add(item)
                }
            }
            
            for (item in items) {
                if (item.icon == null) {
                    val iconRes = when (item.itemId) {
                        R.id.action_go_to_album -> R.drawable.ic_album_24dp
                        R.id.action_go_to_artist -> R.drawable.ic_person_24dp
                        R.id.action_go_to_genre -> R.drawable.ic_library_music_24dp
                        else -> null
                    }
                    if (iconRes != null) {
                        item.icon = AppCompatResources.getDrawable(context, iconRes)
                    }
                }
            }
            
            val columns = 4
            var currentRow: android.widget.LinearLayout? = null
            
            for (index in items.indices) {
                val item = items[index]
                if (index % columns == 0) {
                    currentRow = android.widget.LinearLayout(context).apply {
                        orientation = android.widget.LinearLayout.HORIZONTAL
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    }
                    layout.addView(currentRow)
                }
                
                val itemView = android.widget.LinearLayout(context).apply {
                    orientation = android.widget.LinearLayout.VERTICAL
                    gravity = android.view.Gravity.CENTER
                    setPadding(8.dp(context), 12.dp(context), 8.dp(context), 12.dp(context))
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        0,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                    
                    val outValue = android.util.TypedValue()
                    context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
                    setBackgroundResource(outValue.resourceId)
                    
                    setOnClickListener {
                        playerFragment?.onMenuItemClick(item)
                        dialog.dismiss()
                    }
                }
                
                val iconView = android.widget.ImageView(context).apply {
                    layoutParams = android.widget.LinearLayout.LayoutParams(24.dp(context), 24.dp(context))
                    setImageDrawable(item.icon)
                    setColorFilter(Color.WHITE)
                }
                
                val labelView = android.widget.TextView(context).apply {
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = 8.dp(context)
                    }
                    text = item.title
                    setTextColor(Color.parseColor("#A0A0A0"))
                    textSize = 10f
                    gravity = android.view.Gravity.CENTER
                    maxLines = 1
                    ellipsize = android.text.TextUtils.TruncateAt.END
                }
                
                itemView.addView(iconView)
                itemView.addView(labelView)
                currentRow?.addView(itemView)
            }
            
            dialog.setContentView(layout)
            dialog.show()
        }

        binding.progressSlider.currentColor = Color.parseColor("#D71921")
        binding.progressSlider.setOnTouchListener { view, event ->
            val sliderView = binding.progressSlider.progressView as? com.google.android.material.slider.Slider
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    view.animate().scaleY(2.2f).setDuration(150).start()
                    sliderView?.thumbRadius = 6.dp(view.context)
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    view.animate().scaleY(1.0f).setDuration(150).start()
                    sliderView?.thumbRadius = 0
                }
            }
            false
        }
        applyCustomTrackColors()

        binding.shareButton.setOnClickListener {
            val currentSong = playerViewModel.currentSong
            if (currentSong != Song.emptySong) {
                try {
                    requireContext().startActivity(requireContext().getShareSongIntent(currentSong))
                } catch (e: Exception) {
                    // Ignore or fallback
                }
            }
        }

        binding.close.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.title.setOnTouchListener { _, event ->
            val startX = binding.title.paddingLeft.toFloat()
            val textWidth = if (binding.title.layout != null && binding.title.layout.lineCount > 0) {
                var maxLineWidth = 0f
                for (i in 0 until binding.title.layout.lineCount) {
                    maxLineWidth = maxLineWidth.coerceAtLeast(binding.title.layout.getLineWidth(i))
                }
                maxLineWidth
            } else {
                binding.title.paint.measureText(binding.title.text.toString())
            }

            val progressPercent = if (textWidth > 0) {
                ((event.x - startX) / textWidth).coerceIn(0f, 1f)
            } else {
                0f
            }

            val sliderWidth = binding.progressSlider.width.toFloat()
            val mappedX = progressPercent * sliderWidth

            val localEvent = android.view.MotionEvent.obtain(event)
            localEvent.setLocation(mappedX, binding.progressSlider.height / 2f)
            binding.progressSlider.dispatchTouchEvent(localEvent)
            localEvent.recycle()
            true
        }

        viewLifecycleOwner.launchAndRepeatWithViewLifecycle {
            combine(
                playerViewModel.progressFlow,
                playerViewModel.durationFlow
            ) { progress, duration -> Pair(progress, duration) }
                .collect { (progress, duration) ->
                    val progressText = if (duration > 0) {
                        val currentStr = progress.asReadableDuration()
                        val totalStr = duration.asReadableDuration()
                        "$currentStr / $totalStr"
                    } else {
                        "00:00 / 00:00"
                    }
                    binding.timeProgressText.text = progressText
                    
                    val progressPercent = if (duration > 0) {
                        progress.toFloat() / duration.toFloat()
                    } else {
                        0f
                    }
                    binding.title.setProgress(progressPercent)
                }
        }


        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            insets
        }
    }

    override fun onClick(view: View) {
        super.onClick(view)
        when (view) {
            binding.shuffleButton -> playerViewModel.toggleShuffleMode()
            binding.repeatButton -> playerViewModel.cycleRepeatMode()
            binding.playPauseButton -> {
                playerViewModel.togglePlayPause()
                if (isControlAnimationEnabled) {
                    view.showBounceAnimation()
                }
            }
        }
    }

    override fun onCreatePlayerAnimator(): PlayerAnimator {
        return ReelsPlayerAnimator(binding, isControlAnimationEnabled)
    }

    var isLyricsVisible: Boolean = false
        private set

    internal fun updateLyricsButtonState(visible: Boolean) {
        isLyricsVisible = visible
        val color = if (visible) Color.parseColor("#D71921") else Color.WHITE
        binding.lyricsButton.iconTint = ColorStateList.valueOf(color)
    }

    override fun onUpdateShuffleMode(shuffleModeEnabled: Boolean) {
        val color = if (shuffleModeEnabled) Color.parseColor("#D71921") else Color.parseColor("#888888")
        binding.shuffleButton.iconTint = ColorStateList.valueOf(color)
    }

    override fun onUpdateRepeatMode(repeatMode: Int) {
        val iconResource = when (repeatMode) {
            androidx.media3.common.Player.REPEAT_MODE_ONE -> R.drawable.ic_repeat_one_24dp
            else -> R.drawable.ic_repeat_24dp
        }
        binding.repeatButton.setIconResource(iconResource)
        val color = if (repeatMode != androidx.media3.common.Player.REPEAT_MODE_OFF) {
            Color.parseColor("#D71921")
        } else {
            Color.parseColor("#888888")
        }
        binding.repeatButton.iconTint = ColorStateList.valueOf(color)
    }

    private fun applyCustomTrackColors() {
        val view = binding.progressSlider.progressView
        if (view is android.widget.SeekBar) {
            view.thumbTintList = ColorStateList.valueOf(Color.parseColor("#D71921"))
            view.progressTintList = ColorStateList.valueOf(Color.parseColor("#D71921"))
            view.progressBackgroundTintList = ColorStateList.valueOf(Color.WHITE)
        } else if (view is com.google.android.material.slider.Slider) {
            view.thumbTintList = ColorStateList.valueOf(Color.parseColor("#D71921"))
            view.trackActiveTintList = ColorStateList.valueOf(Color.parseColor("#D71921"))
            view.trackInactiveTintList = ColorStateList.valueOf(Color.WHITE)
            view.thumbRadius = 0
        }
    }

    override fun getTintTargets(scheme: PlayerColorScheme): List<PlayerTintTarget> {
        val oldShuffleColor = binding.shuffleButton.iconTint?.defaultColor ?: Color.parseColor("#888888")
        val newShuffleColor = if (isShuffleModeOn) Color.parseColor("#D71921") else Color.parseColor("#888888")

        val oldRepeatColor = binding.repeatButton.iconTint?.defaultColor ?: Color.parseColor("#888888")
        val newRepeatColor = if (isRepeatModeOn) Color.parseColor("#D71921") else Color.parseColor("#888888")

        val oldLyricsColor = binding.lyricsButton.iconTint?.defaultColor ?: Color.WHITE
        val newLyricsColor = if (isLyricsVisible) Color.parseColor("#D71921") else Color.WHITE

        // We want progress bar to match Nothing red or vibrant colors, let's keep it red (#D71921)
        applyCustomTrackColors()
        return listOfNotNull(
            binding.shuffleButton.iconButtonTintTarget(oldShuffleColor, newShuffleColor),
            binding.repeatButton.iconButtonTintTarget(oldRepeatColor, newRepeatColor),
            binding.lyricsButton.iconButtonTintTarget(oldLyricsColor, newLyricsColor)
        )
    }

    override fun onSongInfoChanged(currentSong: Song, nextSong: Song) {
        _binding?.let { nonNullBinding ->
            nonNullBinding.title.text = currentSong.title
            nonNullBinding.text.text = getSongArtist(currentSong)
        }
    }

    override fun onExtraInfoChanged(extraInfo: String?) {
        _binding?.let { nonNullBinding ->
            if (isExtraInfoEnabled()) {
                nonNullBinding.songInfo.text = extraInfo
                nonNullBinding.songInfo.visibility = View.VISIBLE
            } else {
                nonNullBinding.songInfo.visibility = View.GONE
            }
        }
    }

    private fun showPlayPauseAnimation(isPlaying: Boolean) {
        val indicator = _binding?.centerPlayPauseIndicator ?: return
        indicator.visibility = View.VISIBLE
        indicator.setImageResource(if (isPlaying) R.drawable.ic_pause_24dp else R.drawable.ic_play_24dp)
        indicator.alpha = 0f
        indicator.scaleX = 0.5f
        indicator.scaleY = 0.5f

        indicator.animate()
            .alpha(0.6f)
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(120)
            .withEndAction {
                indicator.animate()
                    .alpha(0f)
                    .scaleX(1.6f)
                    .scaleY(1.6f)
                    .setDuration(130)
                    .withEndAction {
                        indicator.visibility = View.INVISIBLE
                    }
                    .start()
            }
            .start()
    }

    override fun onUpdatePlayPause(isPlaying: Boolean) {
        showPlayPauseAnimation(isPlaying)
    }

    internal fun showDoubleTapHeartAnimation() {
        val indicator = _binding?.doubleTapHeartIndicator ?: return
        indicator.visibility = View.VISIBLE
        indicator.alpha = 0f
        indicator.scaleX = 0.3f
        indicator.scaleY = 0.3f

        indicator.animate()
            .alpha(0.9f)
            .scaleX(1.4f)
            .scaleY(1.4f)
            .setDuration(150)
            .withEndAction {
                indicator.animate()
                    .alpha(0f)
                    .scaleX(1.8f)
                    .scaleY(1.8f)
                    .setDuration(150)
                    .withEndAction {
                        indicator.visibility = View.INVISIBLE
                    }
                    .start()
            }
            .start()
    }

    internal fun setFavorite(isFavorite: Boolean, withAnimation: Boolean) {
        this.isFavorite = isFavorite
        playerFragment?.let { fragment ->
            with(fragment) {
                binding.favorite.setIsFavorite(isFavorite, withAnimation)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class ReelsPlayerAnimator(
        private val binding: FragmentReelsPlayerPlaybackControlsBinding,
        isEnabled: Boolean
    ) : PlayerAnimator(isEnabled) {
        private fun addAlphaAnimation(animators: LinkedList<Animator>, view: View, interpolator: TimeInterpolator) {
            animators.add(ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f).apply {
                this.interpolator = interpolator
                this.duration = 500
            })
        }

        override fun onAddAnimation(animators: LinkedList<Animator>, interpolator: TimeInterpolator) {
            addAlphaAnimation(animators, binding.playPauseButton, interpolator)
            addAlphaAnimation(animators, binding.shuffleButton, interpolator)
            addAlphaAnimation(animators, binding.repeatButton, interpolator)
            addAlphaAnimation(animators, binding.favorite, interpolator)
            addAlphaAnimation(animators, binding.addToPlaylistButton, interpolator)
            addAlphaAnimation(animators, binding.lyricsButton, interpolator)
            addAlphaAnimation(animators, binding.speakerButton, interpolator)
            addAlphaAnimation(animators, binding.shareButton, interpolator)
            addAlphaAnimation(animators, binding.queueButton, interpolator)
            addAlphaAnimation(animators, binding.title, interpolator)
            addAlphaAnimation(animators, binding.text, interpolator)
        }

        override fun onPrepareForAnimation() {
            binding.playPauseButton.alpha = 0f
            binding.shuffleButton.alpha = 0f
            binding.repeatButton.alpha = 0f
            binding.favorite.alpha = 0f
            binding.addToPlaylistButton.alpha = 0f
            binding.lyricsButton.alpha = 0f
            binding.speakerButton.alpha = 0f
            binding.shareButton.alpha = 0f
            binding.queueButton.alpha = 0f
            binding.title.alpha = 0f
            binding.text.alpha = 0f
        }
    }
}

class ProgressTextView @JvmOverloads constructor(
    context: android.content.Context,
    attrs: android.util.AttributeSet? = null,
    defStyleAttr: Int = 0
) : com.google.android.material.textview.MaterialTextView(context, attrs, defStyleAttr) {

    private var progress: Float = 0f
    private var isDrawingProgress = false

    fun setProgress(value: Float) {
        if (progress != value) {
            progress = value
            invalidate()
        }
    }

    override fun invalidate() {
        if (!isDrawingProgress) {
            super.invalidate()
        }
    }

    override fun requestLayout() {
        if (!isDrawingProgress) {
            super.requestLayout()
        }
    }

    override fun onDraw(canvas: android.graphics.Canvas) {
        super.onDraw(canvas)

        isDrawingProgress = true
        val originalColors = textColors
        setTextColor(android.graphics.Color.parseColor("#D71921"))

        val textWidth = if (layout != null && layout.lineCount > 0) {
            var maxLineWidth = 0f
            for (i in 0 until layout.lineCount) {
                maxLineWidth = maxLineWidth.coerceAtLeast(layout.getLineWidth(i))
            }
            maxLineWidth
        } else {
            paint.measureText(text.toString())
        }

        canvas.save()
        val startX = paddingLeft.toFloat()
        val clipWidth = startX + textWidth * progress
        canvas.clipRect(0f, 0f, clipWidth, height.toFloat())
        super.onDraw(canvas)
        canvas.restore()

        setTextColor(originalColors)
        isDrawingProgress = false
    }
}
