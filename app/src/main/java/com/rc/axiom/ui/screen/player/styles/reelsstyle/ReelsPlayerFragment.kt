package com.rc.axiom.ui.screen.player.styles.reelsstyle

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.core.view.updatePadding
import com.rc.axiom.R
import com.rc.axiom.core.model.action.NowPlayingAction
import com.rc.axiom.core.model.player.*
import com.rc.axiom.core.model.theme.NowPlayingScreen
import com.rc.axiom.databinding.FragmentReelsPlayerBinding
import com.rc.axiom.extensions.getOnBackPressedDispatcher
import com.rc.axiom.extensions.whichFragment
import com.rc.axiom.ui.component.base.AbsPlayerControlsFragment
import com.rc.axiom.ui.component.base.AbsPlayerFragment
import com.rc.axiom.ui.screen.player.PlayerGesturesController
import com.rc.axiom.ui.screen.player.PlayerGesturesController.GestureType
import androidx.navigation.fragment.findNavController
import com.rc.axiom.util.Preferences

class ReelsPlayerFragment : AbsPlayerFragment(R.layout.fragment_reels_player),
    SharedPreferences.OnSharedPreferenceChangeListener,
    View.OnClickListener {

    private var _binding: FragmentReelsPlayerBinding? = null
    private val binding get() = _binding!!

    private lateinit var controlsFragment: ReelsPlayerControlsFragment

    override val colorSchemeMode: PlayerColorSchemeMode
        get() = Preferences.getNowPlayingColorSchemeMode(NowPlayingScreen.Reels)

    override val playerControlsFragment: AbsPlayerControlsFragment
        get() = controlsFragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentReelsPlayerBinding.bind(view)

        // Setup Window Insets if needed
        ViewCompat.setOnApplyWindowInsetsListener(view) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(Type.systemBars())
            v.updatePadding(top = systemBars.top)
            insets
        }
        Preferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPrepareViewGestures(view: View) {
        val gesturesController = PlayerGesturesController(
            context = view.context,
            acceptedGestures = setOf(
                GestureType.Tap,
                GestureType.DoubleTap(GestureType.DoubleTap.TYPE_CENTER),
                GestureType.Fling(GestureType.Fling.DIRECTION_LEFT),
                GestureType.Fling(GestureType.Fling.DIRECTION_RIGHT),
                GestureType.Fling(GestureType.Fling.DIRECTION_UP),
                GestureType.Fling(GestureType.Fling.DIRECTION_BOTTOM)
            ),
            listener = this
        )
        // Use findViewById — binding is not yet assigned when this is called from super.onViewCreated
        view.findViewById<android.view.View>(R.id.playbackControlsFragment)
            ?.setOnTouchListener(gesturesController)
    }

    override fun gestureDetected(gestureType: GestureType): Boolean {
        return when (gestureType) {
            is GestureType.Tap -> {
                playerViewModel.togglePlayPause()
                true
            }
            is GestureType.DoubleTap -> {
                onQuickActionEvent(NowPlayingAction.ToggleFavoriteState)
                (playerControlsFragment as? ReelsPlayerControlsFragment)?.showDoubleTapHeartAnimation()
                true
            }
            is GestureType.Fling -> {
                when (gestureType.direction) {
                    GestureType.Fling.DIRECTION_LEFT -> {
                        val navOptions = androidx.navigation.navOptions {
                            anim {
                                enter = R.anim.axiom_fragment_open_enter
                                exit = R.anim.axiom_fragment_open_exit
                                popEnter = R.anim.axiom_fragment_close_enter
                                popExit = R.anim.axiom_fragment_close_exit
                            }
                        }
                        findNavController().navigate(R.id.nav_queue, null, navOptions)
                        true
                    }
                    GestureType.Fling.DIRECTION_RIGHT -> {
                        onQuickActionEvent(NowPlayingAction.AddToPlaylist)
                        true
                    }
                    GestureType.Fling.DIRECTION_UP -> {
                        playerViewModel.seekToNext()
                        true
                    }
                    GestureType.Fling.DIRECTION_BOTTOM -> {
                        playerViewModel.seekToPrevious()
                        true
                    }
                    else -> false
                }
            }
            else -> super.gestureDetected(gestureType)
        }
    }

    override fun onClick(view: View) {
        // Handle generic clicks if any
    }

    override fun onMenuInflated(menu: Menu) {
        super.onMenuInflated(menu)
        menu.removeItem(R.id.action_favorite)
    }

    override fun onCreateChildFragments() {
        super.onCreateChildFragments()
        controlsFragment = whichFragment(R.id.playbackControlsFragment)
    }

    override fun getTintTargets(scheme: PlayerColorScheme): List<PlayerTintTarget> {
        val targets = mutableListOf<PlayerTintTarget>()
        playerControlsFragment.let {
            targets.addAll(it.getTintTargets(scheme))
        }
        return targets
    }

    override fun onIsFavoriteChanged(isFavorite: Boolean, withAnimation: Boolean) {
        controlsFragment.setFavorite(isFavorite, withAnimation)
    }

    override fun onLyricsVisibilityChange(animatorSet: android.animation.AnimatorSet, lyricsVisible: Boolean) {
        super.onLyricsVisibilityChange(animatorSet, lyricsVisible)
        controlsFragment.updateLyricsButtonState(lyricsVisible)
    }

    override fun onDestroyView() {
        Preferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroyView()
        _binding = null
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String?) {
        // Handle preference changes if needed
    }
}
