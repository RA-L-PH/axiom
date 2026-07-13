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

package com.rc.axiom.ui.screen.library.artists

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.doOnPreDraw
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil3.request.crossfade
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import com.rc.axiom.R
import com.rc.axiom.coil.artistImage
import com.rc.axiom.core.sort.AlbumSortMode
import com.rc.axiom.core.sort.SongSortMode
import com.rc.axiom.core.sort.SortMode
import com.rc.axiom.data.mapper.searchFilter
import com.rc.axiom.data.model.Album
import com.rc.axiom.data.model.Artist
import com.rc.axiom.data.model.Song
import com.rc.axiom.data.remote.lastfm.model.LastFmArtist
import com.rc.axiom.databinding.FragmentArtistDetailBinding
import com.rc.axiom.extensions.applyHorizontalWindowInsets
import com.rc.axiom.extensions.defaultGridColumns
import com.rc.axiom.extensions.dp
import com.rc.axiom.extensions.isLandscape
import com.rc.axiom.extensions.materialSharedAxis
import com.rc.axiom.extensions.media.artistInfo
import com.rc.axiom.extensions.media.displayName
import com.rc.axiom.extensions.navigation.albumDetailArgs
import com.rc.axiom.extensions.navigation.artistDetailArgs
import com.rc.axiom.extensions.navigation.asFragmentExtras
import com.rc.axiom.extensions.navigation.playInfoArgs
import com.rc.axiom.extensions.navigation.searchArgs
import com.rc.axiom.extensions.plurals
import com.rc.axiom.extensions.resources.removeHorizontalMarginIfRequired
import com.rc.axiom.extensions.resources.setMarkdownText
import com.rc.axiom.extensions.resources.setupStatusBarForeground
import com.rc.axiom.extensions.resources.surfaceColor
import com.rc.axiom.extensions.setSupportActionBar
import com.rc.axiom.core.model.shuffle.OpenShuffleMode
import com.rc.axiom.ui.IAlbumCallback
import com.rc.axiom.ui.IArtistCallback
import com.rc.axiom.ui.ISongCallback
import com.rc.axiom.ui.adapters.HeaderAdapter
import com.rc.axiom.ui.adapters.HorizontalListAdapter
import com.rc.axiom.ui.adapters.SectionHeaderAdapter
import com.rc.axiom.ui.adapters.WikiAdapter
import com.rc.axiom.ui.adapters.ArtistExtraInfoAdapter
import com.rc.axiom.ui.adapters.album.SimpleAlbumAdapter
import com.rc.axiom.ui.adapters.artist.ArtistAdapter
import com.rc.axiom.ui.adapters.song.SimpleSongAdapter
import com.rc.axiom.extensions.launchAndRepeatWithViewLifecycle
import com.rc.axiom.ui.component.base.AbsMainActivityFragment
import com.rc.axiom.ui.component.menu.onAlbumsMenu
import com.rc.axiom.ui.component.menu.onArtistMenu
import com.rc.axiom.ui.component.menu.onArtistsMenu
import com.rc.axiom.ui.component.menu.onSongMenu
import com.rc.axiom.ui.component.menu.onSongsMenu
import com.rc.axiom.util.Preferences
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.util.Locale

/**
 * @author Christians M. A. (rc)
 */
class ArtistDetailFragment : AbsMainActivityFragment(R.layout.fragment_artist_detail),
    IAlbumCallback, IArtistCallback, ISongCallback {

    private val arguments by navArgs<ArtistDetailFragmentArgs>()
    private val detailViewModel by viewModel<ArtistDetailViewModel> {
        parametersOf(arguments.artistId, arguments.artistName)
    }

    private var _binding: FragmentArtistDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var headerAdapter: HeaderAdapter
    private lateinit var albumHeaderAdapter: SectionHeaderAdapter
    private lateinit var albumGridAdapter: SimpleAlbumAdapter
    private lateinit var albumHorizontalAdapter: HorizontalListAdapter
    private lateinit var songHeaderAdapter: SectionHeaderAdapter
    private lateinit var songAdapter: SimpleSongAdapter
    private lateinit var similarArtistAdapter: HorizontalListAdapter
    private lateinit var wikiAdapter: WikiAdapter
    private lateinit var extraInfoAdapter: ArtistExtraInfoAdapter
    private lateinit var concatAdapter: ConcatAdapter

    private var lang: String? = null
    private var biography: String? = null

    private enum class ArtistTab { Songs, Albums }
    private var currentTab = ArtistTab.Songs

    private val isAlbumArtist: Boolean
        get() = !arguments.artistName.isNullOrEmpty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragment_container
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(surfaceColor())
            setPathMotion(MaterialArcMotion())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentArtistDetailBinding.bind(view)
        setSupportActionBar(binding.toolbar, "")
        materialSharedAxis(view, prepareTransition = false)

        view.applyHorizontalWindowInsets()

        postponeEnterTransition()
        detailViewModel.getArtistDetail().observe(viewLifecycleOwner) { result ->
            view.doOnPreDraw {
                startPostponedEnterTransition()
            }
            showArtist(result)
        }

        libraryViewModel.getMiniPlayerMargin().observe(viewLifecycleOwner) {
            binding.recyclerView.updatePadding(bottom = it.getWithSpace(16.dp(resources)))
        }

        setupRecyclerView()

        detailViewModel.loadArtistDetail()

        viewLifecycleOwner.launchAndRepeatWithViewLifecycle {
            playerViewModel.currentSongFlow.collect { song ->
                songAdapter.currentSongId = song.id
            }
        }
    }

    private fun getArtist() = detailViewModel.getArtist()

    private fun createSongAdapter() {
        val itemLayoutRes = if (Preferences.compactArtistSongView) {
            R.layout.item_song
        } else {
            R.layout.item_song_detailed
        }
        songAdapter = SimpleSongAdapter(
            context = requireActivity(),
            songs = getArtist().sortedSongs,
            layoutRes = itemLayoutRes,
            sortMode = SongSortMode.ArtistSongs,
            callback = this
        )
    }

    private fun setupRecyclerView() {
        // Header
        headerAdapter = HeaderAdapter { headerBinding ->
            headerBinding.image.transitionName = if (isAlbumArtist) {
                arguments.artistName
            } else {
                arguments.artistId.toString()
            }
            headerBinding.image.removeHorizontalMarginIfRequired()
            headerBinding.image.artistImage(getArtist()) { crossfade(false) }

            headerBinding.title.text = getArtist().displayName()
            headerBinding.subtitle.text = getArtist().artistInfo(requireContext())

            val bio = biography
            if (!bio.isNullOrBlank()) {
                headerBinding.wikiCard.visibility = View.VISIBLE
                headerBinding.wikiText.setMarkdownText(bio)
                val toggleExpansion = {
                    if (headerBinding.wikiText.maxLines == 4) {
                        headerBinding.wikiText.maxLines = Integer.MAX_VALUE
                        headerBinding.wikiChevron.setImageResource(R.drawable.ic_keyboard_arrow_up_24dp)
                    } else {
                        headerBinding.wikiText.maxLines = 4
                        headerBinding.wikiChevron.setImageResource(R.drawable.ic_keyboard_arrow_down_24dp)
                    }
                }
                headerBinding.wikiText.setOnClickListener { toggleExpansion() }
                headerBinding.wikiChevron.setOnClickListener { toggleExpansion() }
            } else {
                headerBinding.wikiCard.visibility = View.GONE
            }

            headerBinding.playAction.setOnClickListener {
                playerViewModel.openQueue(getArtist().sortedSongs, shuffleMode = OpenShuffleMode.Off)
            }
            headerBinding.shuffleAction.setOnClickListener {
                playerViewModel.openAndShuffleQueue(getArtist().sortedSongs)
            }
            headerBinding.searchAction?.setOnClickListener { goToSearch() }

            headerBinding.tabContainer.visibility = View.VISIBLE

            val songsBg = if (currentTab == ArtistTab.Songs) Color.parseColor("#D71921") else Color.TRANSPARENT
            val albumsBg = if (currentTab == ArtistTab.Albums) Color.parseColor("#D71921") else Color.TRANSPARENT
            val songsColor = if (currentTab == ArtistTab.Songs) Color.parseColor("#FFFFFF") else Color.parseColor("#888888")
            val albumsColor = if (currentTab == ArtistTab.Albums) Color.parseColor("#FFFFFF") else Color.parseColor("#888888")

            headerBinding.tabSongs.setBackgroundColor(songsBg)
            headerBinding.tabAlbums.setBackgroundColor(albumsBg)
            headerBinding.tabSongs.setTextColor(songsColor)
            headerBinding.tabAlbums.setTextColor(albumsColor)

            headerBinding.tabSongs.setOnClickListener {
                if (currentTab != ArtistTab.Songs) {
                    currentTab = ArtistTab.Songs
                    updateConcatAdapter()
                    headerAdapter.notifyDataSetChanged()
                }
            }
            headerBinding.tabAlbums.setOnClickListener {
                if (currentTab != ArtistTab.Albums) {
                    currentTab = ArtistTab.Albums
                    updateConcatAdapter()
                    headerAdapter.notifyDataSetChanged()
                }
            }
        }

        // Grid albums
        albumHeaderAdapter = SectionHeaderAdapter(getString(R.string.albums_label)) {
            createSortOrderMenu(it, AlbumSortMode.ArtistAlbums)
        }
        albumGridAdapter = SimpleAlbumAdapter(
            requireActivity(),
            getArtist().sortedAlbums,
            R.layout.item_album,
            callback = this
        )

        // Horizontal albums
        val horizontalAlbumAdapter = SimpleAlbumAdapter(
            requireActivity(),
            getArtist().sortedAlbums,
            R.layout.item_image,
            callback = this
        )
        albumHorizontalAdapter = HorizontalListAdapter("", horizontalAlbumAdapter) {
            createSortOrderMenu(it, AlbumSortMode.ArtistAlbums)
        }

        // Songs
        songHeaderAdapter = SectionHeaderAdapter(getString(R.string.songs_label)) {
            createSortOrderMenu(it, SongSortMode.ArtistSongs)
        }
        createSongAdapter()

        // Similar artists
        val similarAdapter = ArtistAdapter(
            activity = requireActivity(),
            dataSet = emptyList(),
            itemLayoutRes = R.layout.item_artist,
            callback = this
        )
        similarArtistAdapter = HorizontalListAdapter(getString(R.string.similar_artists), similarAdapter)

        // Wiki
        wikiAdapter = WikiAdapter()
        extraInfoAdapter = ArtistExtraInfoAdapter()

        val spanCount = defaultGridColumns()
        val layoutManager = GridLayoutManager(requireContext(), spanCount)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val adapterAndPosition = concatAdapter.getWrappedAdapterAndPosition(position)
                return if (adapterAndPosition.first == albumGridAdapter && !Preferences.horizontalArtistAlbums) 1 else spanCount
            }
        }

        updateConcatAdapter()
        binding.recyclerView.layoutManager = layoutManager
    }

    private fun updateConcatAdapter() {
        val config = ConcatAdapter.Config.Builder()
            .setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
            .build()

        val adapters = mutableListOf<RecyclerView.Adapter<*>>()
        adapters.add(headerAdapter)

        if (currentTab == ArtistTab.Albums) {
            if (Preferences.horizontalArtistAlbums) {
                adapters.add(albumHorizontalAdapter)
            } else {
                adapters.add(albumHeaderAdapter)
                adapters.add(albumGridAdapter)
            }
        } else {
            adapters.add(songHeaderAdapter)
            adapters.add(songAdapter)
        }

        adapters.add(extraInfoAdapter)

        concatAdapter = ConcatAdapter(config, adapters)
        if (_binding != null) {
            binding.recyclerView.adapter = concatAdapter
        }
    }

    private fun createSortOrderMenu(view: View, sortMode: SortMode) {
        val popupMenu = PopupMenu(view.context, view).apply {
            sortMode.createMenu(menu, hasSubMenu = false)
            setOnMenuItemClickListener { item ->
                if (sortMode.sortItemSelected(item)) {
                    detailViewModel.loadArtistDetail()
                    true
                } else false
            }
        }
        popupMenu.show()
    }

    private fun showArtist(artist: Artist) {
        if (artist == Artist.empty || artist.songCount == 0) {
            findNavController().navigateUp()
            return
        }

        val songText = plurals(R.plurals.songs, artist.songCount)
        val albumText = plurals(R.plurals.albums, artist.albumCount)

        headerAdapter.notifyItemChanged(0)
        loadBiography(artist.name)

        songHeaderAdapter.updateTitle(songText)
        songAdapter.dataSet = artist.sortedSongs

        albumHeaderAdapter.updateTitle(albumText)
        albumGridAdapter.dataSet = artist.sortedAlbums

        albumHorizontalAdapter.updateTitle(albumText)
        (albumHorizontalAdapter.innerAdapter as SimpleAlbumAdapter).dataSet = artist.sortedAlbums

        updateAlbumsVisibility()

        if (artist.isAlbumArtist) {
            loadSimilarArtists(artist)
        }
    }

    private fun updateAlbumsVisibility() {
        val hasAlbums = getArtist().sortedAlbums.isNotEmpty()
        albumHorizontalAdapter.setVisible(hasAlbums && Preferences.horizontalArtistAlbums)
        albumHeaderAdapter.setVisible(hasAlbums && !Preferences.horizontalArtistAlbums)
    }

    private fun loadBiography(name: String, lang: String? = Locale.getDefault().language) {
        this.biography = null
        this.lang = lang
        detailViewModel.getArtistBio(name, lang, null).observe(viewLifecycleOwner) { lastFmArtist ->
            if (lastFmArtist != null) {
                artistInfo(lastFmArtist)
            }
        }
    }

    private fun artistInfo(lastFmArtist: LastFmArtist?) {
        if (lastFmArtist != null) {
            val bioContent = lastFmArtist.artist?.bio?.content
            if (!bioContent.isNullOrEmpty()) {
                biography = bioContent
                headerAdapter.notifyItemChanged(0)
            }
            val extraTags = mutableListOf<String>()
            if (!bioContent.isNullOrEmpty()) {
                extraTags.add("SOURCE: WIKIPEDIA")
            }
            if (com.rc.axiom.data.model.network.NetworkFeature.Services.Spotify.isAvailable) {
                extraTags.add("SOURCE: SPOTIFY")
            }
            if (com.rc.axiom.data.model.network.NetworkFeature.Services.MusicBrainz.isAvailable) {
                extraTags.add("SOURCE: MUSICBRAINZ")
            }
            if (com.rc.axiom.data.model.network.NetworkFeature.Services.AudioDb.isAvailable) {
                extraTags.add("SOURCE: AUDIODB")
            }

            extraInfoAdapter.update(
                title = "ARTIST INFO",
                debut = lastFmArtist.debutYear,
                genre = lastFmArtist.genre,
                style = lastFmArtist.style,
                mood = lastFmArtist.mood,
                country = lastFmArtist.country,
                extraTags = extraTags
            )
        }
        // If the "lang" parameter is set and no biography is given, retry with default language
        if (biography == null && lang != null) {
            loadBiography(getArtist().name, null)
        }
    }

    private fun loadSimilarArtists(artist: Artist) {
        detailViewModel.getSimilarArtists(artist).observe(viewLifecycleOwner) { artists ->
            similarArtists(artists)
        }
    }

    private fun similarArtists(artists: List<Artist>) {
        if (artists.isNotEmpty()) {
            similarArtistAdapter.setVisible(true)
            (similarArtistAdapter.innerAdapter as ArtistAdapter).dataSet = artists
        } else {
            similarArtistAdapter.setVisible(false)
        }
    }

    override fun albumClick(album: Album, sharedElements: Array<Pair<View, String>>?) {
        findNavController().navigate(
            R.id.nav_album_detail,
            albumDetailArgs(album.id),
            null,
            sharedElements.asFragmentExtras()
        )
    }

    override fun albumMenuItemClick(
        album: Album,
        menuItem: MenuItem,
        sharedElements: Array<Pair<View, String>>?
    ): Boolean {
        return false
    }

    override fun albumsMenuItemClick(albums: List<Album>, menuItem: MenuItem) {
        albums.onAlbumsMenu(this, menuItem)
    }

    override fun artistClick(artist: Artist, sharedElements: Array<Pair<View, String>>?) {
        findNavController().navigate(
            R.id.nav_artist_detail,
            artistDetailArgs(artist),
            null,
            sharedElements.asFragmentExtras()
        )
    }

    override fun artistMenuItemClick(
        artist: Artist,
        menuItem: MenuItem,
        sharedElements: Array<Pair<View, String>>?
    ): Boolean = false

    override fun artistsMenuItemClick(artists: List<Artist>, menuItem: MenuItem) {
        artists.onArtistsMenu(this, menuItem)
    }

    override fun songMenuItemClick(
        song: Song,
        menuItem: MenuItem,
        sharedElements: Array<Pair<View, String>>?
    ): Boolean {
        if (menuItem.itemId == R.id.action_go_to_artist) {
            return true
        }
        return song.onSongMenu(this, menuItem)
    }

    override fun songsMenuItemClick(songs: List<Song>, menuItem: MenuItem) {
        songs.onSongsMenu(this, menuItem)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_artist_detail, menu)
        if (!isLandscape()) {
            menu.removeItem(R.id.action_search)
        }
        menu.findItem(R.id.action_horizontal_albums)?.isChecked = Preferences.horizontalArtistAlbums
        menu.findItem(R.id.action_ignore_singles)?.isChecked = Preferences.ignoreSingles
        menu.findItem(R.id.action_toggle_compact_song_view)?.isChecked = Preferences.compactArtistSongView
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            android.R.id.home -> {
                findNavController().navigateUp()
                true
            }

            R.id.action_search -> {
                goToSearch()
                true
            }

            R.id.action_play_info -> {
                goToPlayInfo()
                true
            }

            R.id.action_horizontal_albums -> {
                val isChecked = !menuItem.isChecked
                Preferences.horizontalArtistAlbums = isChecked
                menuItem.isChecked = isChecked
                updateAlbumsVisibility()
                updateConcatAdapter()
                true
            }

            R.id.action_ignore_singles -> {
                val isChecked = !menuItem.isChecked
                Preferences.ignoreSingles = isChecked
                menuItem.isChecked = isChecked
                detailViewModel.loadArtistDetail()
                true
            }

            R.id.action_toggle_compact_song_view -> {
                val isChecked = !menuItem.isChecked
                Preferences.compactArtistSongView = isChecked
                menuItem.isChecked = isChecked
                createSongAdapter()
                updateConcatAdapter()
                true
            }

            else -> getArtist().onArtistMenu(this, menuItem)
        }
    }

    private fun goToSearch() {
        findNavController().navigate(R.id.nav_search, searchArgs(getArtist().searchFilter(requireContext())))
    }

    private fun goToPlayInfo() {
        findNavController().navigate(R.id.nav_play_info, playInfoArgs(getArtist()))
    }

    override fun onMediaContentChanged() {
        super.onMediaContentChanged()
        detailViewModel.loadArtistDetail()
    }

    override fun onDestroyView() {
        binding.recyclerView.layoutManager = null
        binding.recyclerView.adapter = null
        super.onDestroyView()
        _binding = null
    }
}