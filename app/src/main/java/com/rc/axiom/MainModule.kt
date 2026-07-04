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

package com.rc.axiom

import androidx.preference.PreferenceManager
import androidx.room.Room
import com.rc.axiom.coil.CustomArtistImageManager
import com.rc.axiom.coil.CustomPlaylistImageManager
import com.rc.axiom.core.AxiomDatabase
import com.rc.axiom.core.audio.AudioOutputObserver
import com.rc.axiom.data.local.AlbumCoverSaver
import com.rc.axiom.data.local.EditTarget
import com.rc.axiom.data.local.MediaStoreWriter
import com.rc.axiom.data.local.repository.AlbumRepository
import com.rc.axiom.data.local.repository.ArtistRepository
import com.rc.axiom.data.local.repository.GenreRepository
import com.rc.axiom.data.local.repository.LyricsRepository
import com.rc.axiom.data.local.repository.NetworkRepository
import com.rc.axiom.data.local.repository.NetworkRepositoryImpl
import com.rc.axiom.data.local.repository.PlaylistRepository
import com.rc.axiom.data.local.repository.RealAlbumRepository
import com.rc.axiom.data.local.repository.RealArtistRepository
import com.rc.axiom.data.local.repository.RealGenreRepository
import com.rc.axiom.data.local.repository.RealLyricsRepository
import com.rc.axiom.data.local.repository.RealPlaylistRepository
import com.rc.axiom.data.local.repository.RealRepository
import com.rc.axiom.data.local.repository.RealSearchRepository
import com.rc.axiom.data.local.repository.RealSmartRepository
import com.rc.axiom.data.local.repository.RealSongRepository
import com.rc.axiom.data.local.repository.RealSpecialRepository
import com.rc.axiom.data.local.repository.Repository
import com.rc.axiom.data.local.repository.SearchRepository
import com.rc.axiom.data.local.repository.SmartRepository
import com.rc.axiom.data.local.repository.SongRepository
import com.rc.axiom.data.local.repository.SpecialRepository
import com.rc.axiom.data.model.Genre
import com.rc.axiom.data.remote.deezer.DeezerService
import com.rc.axiom.data.remote.github.GitHubService
import com.rc.axiom.data.remote.jsonHttpClient
import com.rc.axiom.data.remote.lastfm.LastFmService
import com.rc.axiom.data.remote.listenbrainz.ListenBrainzService
import com.rc.axiom.data.remote.lyrics.LyricsDownloadService
import com.rc.axiom.data.remote.provideOkHttp
import com.rc.axiom.playback.SleepTimer
import com.rc.axiom.playback.equalizer.EqualizerManager
import com.rc.axiom.playback.processor.BalanceAudioProcessor
import com.rc.axiom.playback.processor.ReplayGainAudioProcessor
import com.rc.axiom.ui.screen.equalizer.EqualizerViewModel
import com.rc.axiom.ui.screen.info.InfoViewModel
import com.rc.axiom.ui.screen.library.LibraryViewModel
import com.rc.axiom.ui.screen.library.albums.AlbumDetailViewModel
import com.rc.axiom.ui.screen.library.artists.ArtistDetailViewModel
import com.rc.axiom.ui.screen.library.folders.FolderDetailViewModel
import com.rc.axiom.ui.screen.library.genres.GenreDetailViewModel
import com.rc.axiom.ui.screen.library.playlists.PlaylistDetailViewModel
import com.rc.axiom.ui.screen.library.search.SearchViewModel
import com.rc.axiom.ui.screen.library.years.YearDetailViewModel
import com.rc.axiom.ui.screen.lyrics.LyricsViewModel
import com.rc.axiom.ui.screen.player.PlayerViewModel
import com.rc.axiom.ui.screen.sleeptimer.SleepTimerViewModel
import com.rc.axiom.ui.screen.tageditor.TagEditorViewModel
import com.rc.axiom.ui.screen.update.UpdateViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val networkModule = module {
    factory {
        jsonHttpClient(okHttpClient = get())
    }
    factory {
        provideOkHttp(context = get())
    }
    single {
        GitHubService(context = androidContext(), client = get())
    }
    single {
        DeezerService(client = get())
    }
    single {
        LastFmService(client = get())
    }
    single {
        ListenBrainzService(client = get())
    }
    single {
        LyricsDownloadService(client = get())
    }
}

private val mainModule = module {
    single {
        androidContext().contentResolver
    }
    single {
        PreferenceManager.getDefaultSharedPreferences(androidContext())
    }
    single {
        SleepTimer(context = androidContext())
    }
    single {
        BalanceAudioProcessor()
    }
    single {
        ReplayGainAudioProcessor()
    }
    single {
        EqualizerManager(
            context = androidContext(),
            balanceProcessor = get(),
            replayGainProcessor = get(),
            audioOutputObserver = get()
        )
    }
    single {
        MediaStoreWriter(context = androidContext(), contentResolver = get())
    }
    single {
        AlbumCoverSaver(context = androidContext(), mediaStoreWriter = get())
    }
    single {
        CustomArtistImageManager(context = androidContext())
    }
    single {
        CustomPlaylistImageManager(context = androidContext())
    }
    single {
        AudioOutputObserver(context = androidContext())
    }
}

private val roomModule = module {
    single {
        Room.databaseBuilder(androidContext(), AxiomDatabase::class.java, "music_database.db")
            .addMigrations(
                AxiomDatabase.MIGRATION_1_2,
                AxiomDatabase.MIGRATION_2_3,
                AxiomDatabase.MIGRATION_3_4,
                AxiomDatabase.MIGRATION_4_5,
                AxiomDatabase.MIGRATION_5_6
            )
            .build()
    }

    factory {
        get<AxiomDatabase>().playlistDao()
    }

    factory {
        get<AxiomDatabase>().playCountDao()
    }

    factory {
        get<AxiomDatabase>().historyDao()
    }

    factory {
        get<AxiomDatabase>().queueDao()
    }

    factory {
        get<AxiomDatabase>().inclExclDao()
    }

    factory {
        get<AxiomDatabase>().lyricsDao()
    }
}

private val dataModule = module {
    single {
        RealRepository(
            context = androidContext(),
            songRepository = get(),
            albumRepository = get(),
            artistRepository = get(),
            genreRepository = get(),
            smartRepository = get(),
            specialRepository = get(),
            playlistRepository = get(),
            searchRepository = get(),
            networkRepository = get()
        )
    } bind Repository::class

    single {
        RealSongRepository(context = get(), inclExclDao = get())
    } bind SongRepository::class

    single {
        RealAlbumRepository(songRepository = get())
    } bind AlbumRepository::class

    single {
        RealArtistRepository(songRepository = get(), albumRepository = get())
    } bind ArtistRepository::class

    single {
        RealPlaylistRepository(
            context = androidContext(),
            songRepository = get(),
            playlistDao = get()
        )
    } bind PlaylistRepository::class

    single {
        RealGenreRepository(contentResolver = get(), songRepository = get())
    } bind GenreRepository::class

    single {
        RealSearchRepository(
            albumRepository = get(),
            songRepository = get(),
            artistRepository = get(),
            playlistRepository = get(),
            genreRepository = get(),
            specialRepository = get()
        )
    } bind SearchRepository::class

    single {
        RealSmartRepository(
            context = androidContext(),
            songRepository = get(),
            albumRepository = get(),
            artistRepository = get(),
            historyDao = get(),
            playCountDao = get()
        )
    } bind SmartRepository::class

    single {
        RealSpecialRepository(songRepository = get())
    } bind SpecialRepository::class

    single {
        RealLyricsRepository(
            context = androidContext(),
            preferences = get(),
            lyricsDownloadService = get(),
            lyricsDao = get()
        )
    } bind LyricsRepository::class

    single {
        NetworkRepositoryImpl(
            context = androidContext(),
            preferences = get(),
            lastFmService = get(),
            listenBrainzService = get(),
            deezerService = get()
        )
    } bind NetworkRepository::class
}

private val viewModule = module {
    viewModel {
        LibraryViewModel(repository = get(), inclExclDao = get(), customPlaylistImageManager = get())
    }

    viewModel {
        PlayerViewModel(preferences = get(), repository = get(), albumCoverSaver = get())
    }

    viewModel {
        EqualizerViewModel(
            contentResolver = get(),
            equalizerManager = get(),
            audioOutputObserver = get(),
            mediaStoreWriter = get()
        )
    }

    viewModel {
        SleepTimerViewModel(
            application = androidApplication(),
            sleepTimer = get()
        )
    }

    viewModel { (albumId: Long) ->
        AlbumDetailViewModel(
            application = androidApplication(),
            repository = get(),
            albumId = albumId
        )
    }

    viewModel { (artistId: Long, artistName: String?) ->
        ArtistDetailViewModel(
            application = androidApplication(),
            repository = get(),
            artistId = artistId,
            artistName = artistName
        )
    }

    viewModel { (playlistId: Long) ->
        PlaylistDetailViewModel(playlistRepository = get(), playlistId = playlistId)
    }

    viewModel { (genre: Genre) ->
        GenreDetailViewModel(repository = get(), genre = genre)
    }

    viewModel { (year: Int) ->
        YearDetailViewModel(repository = get(), year = year)
    }

    viewModel { (path: String) ->
        FolderDetailViewModel(repository = get(), folderPath = path)
    }

    viewModel {
        SearchViewModel(repository = get())
    }

    viewModel { (target: EditTarget) ->
        TagEditorViewModel(
            repository = get(),
            customArtistImageManager = get(),
            target = target
        )
    }

    viewModel {
        LyricsViewModel(application = androidApplication(), preferences = get(), repository = get())
    }

    viewModel {
        InfoViewModel(repository = get())
    }

    viewModel {
        UpdateViewModel(updateService = get())
    }
}

val appModules = listOf(networkModule, mainModule, roomModule, dataModule, viewModule)