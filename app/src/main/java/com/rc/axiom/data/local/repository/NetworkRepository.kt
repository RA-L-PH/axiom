package com.rc.axiom.data.local.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.rc.axiom.BuildConfig
import com.rc.axiom.R
import com.rc.axiom.data.model.Song
import com.rc.axiom.data.model.network.LoginParams
import com.rc.axiom.data.model.network.LoginState
import com.rc.axiom.data.model.network.ScrobblingResult
import com.rc.axiom.data.model.network.ScrobblingService
import com.rc.axiom.data.model.network.lastfm.LastFmFailure
import com.rc.axiom.data.remote.deezer.DeezerService
import com.rc.axiom.data.remote.musicbrainz.MusicBrainzService
import com.rc.axiom.data.remote.wikipedia.WikipediaService
import com.rc.axiom.data.remote.spotify.SpotifyService
import com.rc.axiom.data.remote.audiodb.AudioDbService
import com.rc.axiom.data.model.network.NetworkFeature
import com.rc.axiom.data.remote.deezer.model.DeezerAlbum
import com.rc.axiom.data.remote.deezer.model.DeezerArtist
import com.rc.axiom.data.remote.deezer.model.DeezerTrack
import com.rc.axiom.data.remote.lastfm.LastFmService
import com.rc.axiom.data.remote.lastfm.model.LastFmAlbum
import com.rc.axiom.data.remote.lastfm.model.LastFmArtist
import com.rc.axiom.data.remote.lastfm.model.LastFmError
import com.rc.axiom.data.remote.lastfm.model.LastFmSessionResponse
import com.rc.axiom.data.remote.lastfm.model.LastFmUser
import com.rc.axiom.data.remote.lastfm.model.NowPlayingResponse
import com.rc.axiom.data.remote.lastfm.model.ScrobbleResponse
import com.rc.axiom.data.remote.listenbrainz.ListenBrainzService
import com.rc.axiom.data.remote.listenbrainz.model.ListenBrainzListen
import com.rc.axiom.data.remote.listenbrainz.model.ListenBrainzSubmission
import com.rc.axiom.data.remote.listenbrainz.model.ListenBrainzTrackAdditionalInfo
import com.rc.axiom.data.remote.listenbrainz.model.ListenBrainzTrackMetadata
import com.rc.axiom.extensions.media.displayArtistName
import com.rc.axiom.util.CryptoUtil
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.ConnectException
import java.net.SocketTimeoutException
import kotlin.io.encoding.Base64

interface NetworkRepository {
    fun getLoginState(service: ScrobblingService): Flow<LoginState>
    suspend fun loginToService(service: ScrobblingService, params: LoginParams)
    suspend fun logoutFromService(service: ScrobblingService)
    suspend fun scrobble(service: ScrobblingService, song: Song, timestamp: Long): ScrobblingResult
    suspend fun updateNowPlaying(service: ScrobblingService, song: Song): ScrobblingResult
    suspend fun artistInfo(name: String, lang: String?, cache: String?, songTitles: List<String> = emptyList(), albumTitles: List<String> = emptyList()): LastFmArtist?
    suspend fun albumInfo(artist: String, album: String, lang: String?): LastFmAlbum?
    suspend fun deezerTrack(artist: String, title: String): DeezerTrack?
    suspend fun deezerArtist(name: String, limit: Int, index: Int, songTitles: List<String> = emptyList(), albumTitles: List<String> = emptyList()): DeezerArtist?
    suspend fun deezerAlbum(artist: String, name: String): DeezerAlbum?
    fun clearArtistCache(name: String)
}

class NetworkRepositoryImpl(
    private val context: Context,
    private val preferences: SharedPreferences,
    private val listenBrainzService: ListenBrainzService,
    private val deezerService: DeezerService,
    private val musicBrainzService: MusicBrainzService,
    private val wikipediaService: WikipediaService,
    private val spotifyService: SpotifyService,
    private val audioDbService: AudioDbService
) : NetworkRepository {

    private val lastFmLoginStateFlow = MutableStateFlow<LoginState>(LoginState.Empty)
    private val lastFmLoginState get() = lastFmLoginStateFlow.value

    private val listenBrainzLoginStateFlow = MutableStateFlow<LoginState>(LoginState.Empty)
    private val listenBrainzLoginState get() = listenBrainzLoginStateFlow.value

    private val appName = context.getString(R.string.app_name)

    private fun getCachedValue(key: String): String? {
        return preferences.getString("net_cache_$key", null)
    }

    private fun setCachedValue(key: String, value: String) {
        preferences.edit().putString("net_cache_$key", value).apply()
    }

    override fun clearArtistCache(name: String) {
        val cacheKeyBio = "artist_bio_${name.lowercase()}"
        val cacheKeyImg = "artist_img_${name.lowercase()}"
        preferences.edit {
            remove("net_cache_$cacheKeyBio")
            remove("net_cache_$cacheKeyImg")
            remove("net_cache_${cacheKeyBio}_debut")
            remove("net_cache_${cacheKeyBio}_genre")
            remove("net_cache_${cacheKeyBio}_style")
            remove("net_cache_${cacheKeyBio}_mood")
            remove("net_cache_${cacheKeyBio}_country")
        }
    }

    init {
        val lastFmSessionInfo = getLastFmSessionInfo()
        if (lastFmSessionInfo != null) {
            lastFmLoginStateFlow.value = LoginState.LoggedIn(
                username = lastFmSessionInfo.user.name,
                url = lastFmSessionInfo.user.url
            )
        }

        val listenBrainzSessionInfo = getListenBrainzSessionInfo()
        if (listenBrainzSessionInfo != null) {
            listenBrainzLoginStateFlow.value = LoginState.LoggedIn(
                username = listenBrainzSessionInfo.user,
                url = listenBrainzSessionInfo.url
            )
        }
    }

    override fun getLoginState(service: ScrobblingService): Flow<LoginState> {
        return when (service) {
            ScrobblingService.Lastfm -> lastFmLoginStateFlow
            ScrobblingService.ListenBrainz -> listenBrainzLoginStateFlow
        }
    }

    override suspend fun loginToService(service: ScrobblingService, params: LoginParams) {
        when (service) {
            ScrobblingService.Lastfm -> loginToLastFm(
                params["username"].orEmpty(),
                params["password"].orEmpty()
            )
            ScrobblingService.ListenBrainz -> loginToListenBrainz(
                params["token"].orEmpty()
            )
        }
    }

    override suspend fun logoutFromService(service: ScrobblingService) {
        when (service) {
            ScrobblingService.Lastfm -> logoutFromLastFm()
            ScrobblingService.ListenBrainz -> logoutFromListenBrainz()
        }
    }

    override suspend fun scrobble(
        service: ScrobblingService,
        song: Song,
        timestamp: Long
    ): ScrobblingResult {
        if (service == ScrobblingService.Lastfm) {
            return ScrobblingResult.Failure("Last.fm is disabled")
        }
        return when (service) {
            ScrobblingService.Lastfm -> scrobbleToLastfm(song, timestamp)
            ScrobblingService.ListenBrainz -> scrobbleToListenBrainz(song, timestamp)
        }
    }

    override suspend fun updateNowPlaying(
        service: ScrobblingService,
        song: Song
    ): ScrobblingResult {
        if (service == ScrobblingService.Lastfm) {
            return ScrobblingResult.Failure("Last.fm is disabled")
        }
        return when (service) {
            ScrobblingService.Lastfm -> updateNowPlayingOnLastfm(song)
            ScrobblingService.ListenBrainz -> updateNowPlayingOnListenBrainz(song)
        }
    }

    override suspend fun artistInfo(
        name: String,
        lang: String?,
        cache: String?,
        songTitles: List<String>,
        albumTitles: List<String>
    ): LastFmArtist? {
        val cacheKey = "artist_bio_${name.lowercase()}"
        val cachedBio = getCachedValue(cacheKey)
        if (cachedBio != null) {
            return LastFmArtist(
                artist = LastFmArtist.Artist(
                    bio = LastFmArtist.Bio(
                        content = cachedBio
                    )
                ),
                debutYear = getCachedValue("${cacheKey}_debut"),
                genre = getCachedValue("${cacheKey}_genre"),
                style = getCachedValue("${cacheKey}_style"),
                mood = getCachedValue("${cacheKey}_mood"),
                country = getCachedValue("${cacheKey}_country")
            )
        }

        if (NetworkFeature.Services.Wikipedia.isAvailable) {
            val wikiBio = wikipediaService.getArtistBio(name, songTitles, albumTitles)
            if (!wikiBio.isNullOrEmpty()) {
                setCachedValue(cacheKey, wikiBio)
                return LastFmArtist(
                    artist = LastFmArtist.Artist(
                        bio = LastFmArtist.Bio(
                            content = wikiBio
                        )
                    )
                )
            }
        }

        if (NetworkFeature.Services.AudioDb.isAvailable) {
            val audioDbArtist = audioDbService.getArtistDetails(name)
            val audioDbBio = audioDbArtist?.strBiographyEN
            if (!audioDbBio.isNullOrEmpty()) {
                setCachedValue(cacheKey, audioDbBio)
                if (!audioDbArtist.intFormedYear.isNullOrEmpty()) setCachedValue("${cacheKey}_debut", audioDbArtist.intFormedYear)
                if (!audioDbArtist.strGenre.isNullOrEmpty()) setCachedValue("${cacheKey}_genre", audioDbArtist.strGenre)
                if (!audioDbArtist.strStyle.isNullOrEmpty()) setCachedValue("${cacheKey}_style", audioDbArtist.strStyle)
                if (!audioDbArtist.strMood.isNullOrEmpty()) setCachedValue("${cacheKey}_mood", audioDbArtist.strMood)
                if (!audioDbArtist.strCountry.isNullOrEmpty()) setCachedValue("${cacheKey}_country", audioDbArtist.strCountry)
                
                return LastFmArtist(
                    artist = LastFmArtist.Artist(
                        bio = LastFmArtist.Bio(
                            content = audioDbBio
                        )
                    ),
                    debutYear = audioDbArtist.intFormedYear,
                    genre = audioDbArtist.strGenre,
                    style = audioDbArtist.strStyle,
                    mood = audioDbArtist.strMood,
                    country = audioDbArtist.strCountry
                )
            }
        }

        if (NetworkFeature.Services.MusicBrainz.isAvailable) {
            val mbBio = musicBrainzService.getArtistInfo(name)
            if (!mbBio.isNullOrEmpty()) {
                setCachedValue(cacheKey, mbBio)
                return LastFmArtist(
                    artist = LastFmArtist.Artist(
                        bio = LastFmArtist.Bio(
                            content = mbBio
                        )
                    )
                )
            }
        }
        return null
    }

    override suspend fun albumInfo(artist: String, album: String, lang: String?): LastFmAlbum? {
        val cacheKey = "album_bio_${artist.lowercase()}_${album.lowercase()}"
        val cachedBio = getCachedValue(cacheKey)
        if (cachedBio != null) {
            return LastFmAlbum(
                album = LastFmAlbum.Album(
                    wiki = LastFmAlbum.Wiki(
                        content = cachedBio
                    )
                )
            )
        }

        if (NetworkFeature.Services.Wikipedia.isAvailable) {
            val wikiBio = wikipediaService.getAlbumBio(artist, album)
            if (!wikiBio.isNullOrEmpty()) {
                setCachedValue(cacheKey, wikiBio)
                return LastFmAlbum(
                    album = LastFmAlbum.Album(
                        wiki = LastFmAlbum.Wiki(
                            content = wikiBio
                        )
                    )
                )
            }
        }

        if (NetworkFeature.Services.MusicBrainz.isAvailable) {
            val mbBio = musicBrainzService.getAlbumInfo(artist, album)
            if (!mbBio.isNullOrEmpty()) {
                setCachedValue(cacheKey, mbBio)
                return LastFmAlbum(
                    album = LastFmAlbum.Album(
                        wiki = LastFmAlbum.Wiki(
                            content = mbBio
                        )
                    )
                )
            }
        }
        return null
    }

    override suspend fun deezerTrack(artist: String, title: String): DeezerTrack? {
        val cacheKey = "track_img_${artist.lowercase()}_${title.lowercase()}"
        val cachedUrl = getCachedValue(cacheKey)
        if (cachedUrl != null) {
            return DeezerTrack(
                data = listOf(
                    DeezerTrack.TrackData(
                        album = DeezerTrack.TrackData.Album(
                            image = cachedUrl,
                            smallImage = cachedUrl,
                            mediumImage = cachedUrl,
                            largeImage = cachedUrl
                        )
                    )
                )
            )
        }

        if (NetworkFeature.Services.MusicBrainz.isAvailable) {
            val mbUrl = musicBrainzService.getTrackCoverUrl(artist, title)
            if (!mbUrl.isNullOrEmpty()) {
                setCachedValue(cacheKey, mbUrl)
                return DeezerTrack(
                    data = listOf(
                        DeezerTrack.TrackData(
                            album = DeezerTrack.TrackData.Album(
                                image = mbUrl,
                                smallImage = mbUrl,
                                mediumImage = mbUrl,
                                largeImage = mbUrl
                            )
                        )
                    )
                )
            }
        }
        return try {
            deezerService.track(artist, title)
        } catch (e: Exception) {
            Log.e(TAG, "Deezer: track info couldn't be retrieved!", e)
            null
        }
    }

    override suspend fun deezerArtist(
        name: String,
        limit: Int,
        index: Int,
        songTitles: List<String>,
        albumTitles: List<String>
    ): DeezerArtist? {
        val cacheKey = "artist_img_${name.lowercase()}"
        val cachedUrl = getCachedValue(cacheKey)
        if (cachedUrl != null) {
            return DeezerArtist(
                result = listOf(
                    DeezerArtist.Result(
                        artistName = name,
                        image = cachedUrl,
                        smallImage = cachedUrl,
                        mediumImage = cachedUrl,
                        largeImage = cachedUrl
                    )
                ),
                total = 1
            )
        }

        if (NetworkFeature.Services.Spotify.isAvailable) {
            val spotifyUrl = spotifyService.getArtistImageUrl(name, songTitles, albumTitles)
            if (!spotifyUrl.isNullOrEmpty()) {
                setCachedValue(cacheKey, spotifyUrl)
                return DeezerArtist(
                    result = listOf(
                        DeezerArtist.Result(
                            artistName = name,
                            image = spotifyUrl,
                            smallImage = spotifyUrl,
                            mediumImage = spotifyUrl,
                            largeImage = spotifyUrl
                        )
                    ),
                    total = 1
                )
            }
        }
        if (NetworkFeature.Services.AudioDb.isAvailable) {
            val audioDbArtist = audioDbService.getArtistDetails(name)
            val audioDbImg = audioDbArtist?.strArtistThumb
            if (!audioDbImg.isNullOrEmpty()) {
                setCachedValue(cacheKey, audioDbImg)
                return DeezerArtist(
                    result = listOf(
                        DeezerArtist.Result(
                            artistName = name,
                            image = audioDbImg,
                            smallImage = audioDbImg,
                            mediumImage = audioDbImg,
                            largeImage = audioDbImg
                        )
                    ),
                    total = 1
                )
            }
        }
        if (NetworkFeature.Services.Wikipedia.isAvailable) {
            val wikiUrl = wikipediaService.getArtistImageUrl(name, songTitles)
            if (!wikiUrl.isNullOrEmpty()) {
                setCachedValue(cacheKey, wikiUrl)
                return DeezerArtist(
                    result = listOf(
                        DeezerArtist.Result(
                            artistName = name,
                            image = wikiUrl,
                            smallImage = wikiUrl,
                            mediumImage = wikiUrl,
                            largeImage = wikiUrl
                        )
                    ),
                    total = 1
                )
            }
        }
        return try {
            deezerService.artist(name, limit, index)
        } catch (e: Exception) {
            Log.e(TAG, "Deezer: artist info couldn't be retrieved!", e)
            null
        }
    }

    override suspend fun deezerAlbum(artist: String, name: String): DeezerAlbum? {
        val cacheKey = "album_img_${artist.lowercase()}_${name.lowercase()}"
        val cachedUrl = getCachedValue(cacheKey)
        if (cachedUrl != null) {
            return DeezerAlbum(
                data = listOf(
                    DeezerAlbum.AlbumData(
                        title = name,
                        image = cachedUrl,
                        smallImage = cachedUrl,
                        mediumImage = cachedUrl,
                        largeImage = cachedUrl
                    )
                )
            )
        }

        if (NetworkFeature.Services.MusicBrainz.isAvailable) {
            val mbUrl = musicBrainzService.getAlbumCoverUrl(artist, name)
            if (!mbUrl.isNullOrEmpty()) {
                setCachedValue(cacheKey, mbUrl)
                return DeezerAlbum(
                    data = listOf(
                        DeezerAlbum.AlbumData(
                            title = name,
                            image = mbUrl,
                            smallImage = mbUrl,
                            mediumImage = mbUrl,
                            largeImage = mbUrl
                        )
                    )
                )
            }
        }
        if (NetworkFeature.Services.Wikipedia.isAvailable) {
            val wikiUrl = wikipediaService.getAlbumCoverUrl(artist, name)
            if (!wikiUrl.isNullOrEmpty()) {
                setCachedValue(cacheKey, wikiUrl)
                return DeezerAlbum(
                    data = listOf(
                        DeezerAlbum.AlbumData(
                            title = name,
                            image = wikiUrl,
                            smallImage = wikiUrl,
                            mediumImage = wikiUrl,
                            largeImage = wikiUrl
                        )
                    )
                )
            }
        }
        return try {
            deezerService.album(artist, name)
        } catch (e: Exception) {
            Log.e(TAG, "Deezer: album info couldn't be retrieved!", e)
            null
        }
    }

    private suspend fun loginToLastFm(username: String, password: String) {
        lastFmLoginStateFlow.value = LoginState.Empty
    }

    private fun logoutFromLastFm() {
        lastFmLoginStateFlow.value = LoginState.Empty
    }

    private suspend fun scrobbleToLastfm(song: Song, timestamp: Long): ScrobblingResult {
        return ScrobblingResult.Failure("Last.fm is disabled")
    }

    private suspend fun updateNowPlayingOnLastfm(song: Song): ScrobblingResult {
        return ScrobblingResult.Failure("Last.fm is disabled")
    }

    private fun getLastFmSessionInfoOrLogout(): LastFmSessionInfo? {
        val currentLoginState = lastFmLoginState
        if (currentLoginState is LoginState.LoggingIn)
            return null

        val sessionInfo = getLastFmSessionInfo()
        if (sessionInfo == null) {
            if (lastFmLoginState is LoginState.LoggedIn) {
                logoutFromLastFm()
            }
            return null
        }
        return sessionInfo
    }

    private fun LastFmError.toScrobblingResult(): ScrobblingResult {
        val errorCode = LastFmFailure.fromCode(this.error)
        if (errorCode == LastFmFailure.Auth ||
            errorCode == LastFmFailure.InvalidCredentials) {
            logoutFromLastFm()
        }
        return ScrobblingResult.Failure(context.getString(errorCode.messageRes))
    }

    private fun setLastfmSessionInfo(user: LastFmUser, sessionKey: String): Boolean {
        try {
            val encryptedKey = CryptoUtil.encrypt(sessionKey)
            val sessionInfo = Json.encodeToString(LastFmSessionInfo(user, encryptedKey))
            val encodedValue = Base64.encode(sessionInfo.toByteArray())
            preferences.edit(commit = true) {
                putString(LAST_FM_SESSION_INFO, encodedValue)
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Couldn't save Last.fm session info.", e)
        }
        return false
    }

    private fun getLastFmSessionInfo(): LastFmSessionInfo? {
        if (preferences.contains("session")) {
            val sessionInfo = preferences.getString("session", null)
            preferences.edit {
                if (!preferences.contains(LAST_FM_SESSION_INFO)) {
                    putString(LAST_FM_SESSION_INFO, sessionInfo)
                }
                remove("session")
            }
        }
        val encodedValue = preferences.getString(LAST_FM_SESSION_INFO, null)
        if (!encodedValue.isNullOrBlank()) {
            try {
                val decodedValue = Base64.decode(encodedValue)
                val sessionInfo = Json.decodeFromString<LastFmSessionInfo>(String(decodedValue))
                return sessionInfo.copy(key = CryptoUtil.decrypt(sessionInfo.key))
            } catch (e: Exception) {
                Log.e(TAG, "Couldn't decrypt Last.fm session info. Removing...", e)
            }
        }
        return null
    }

    private suspend fun loginToListenBrainz(token: String) {
        if (listenBrainzLoginState is LoginState.LoggingIn) return
        listenBrainzLoginStateFlow.value = LoginState.LoggingIn
        try {
            val response = listenBrainzService.validateToken(token)
            if (response.valid && response.userName != null) {
                if (setListenBrainzToken(token, response.userName)) {
                    listenBrainzLoginStateFlow.value = LoginState.LoggedIn(
                        username = response.userName,
                        url = "https://listenbrainz.org/user/${response.userName}/"
                    )
                } else {
                    listenBrainzLoginStateFlow.value = LoginState.Failure(
                        context.getString(R.string.error_listenbrainz_generic)
                    )
                }
            } else {
                listenBrainzLoginStateFlow.value = LoginState.Failure(
                    context.getString(R.string.error_listenbrainz_invalid_token)
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "ListenBrainz: login error", e)
            listenBrainzLoginStateFlow.value = LoginState.Failure(
                context.getString(R.string.error_listenbrainz_invalid_token)
            )
        }
    }

    private fun logoutFromListenBrainz() {
        try {
            preferences.edit(commit = true) {
                remove(LISTEN_BRAINZ_SESSION_INFO)
            }
            listenBrainzLoginStateFlow.value = LoginState.Empty
        } catch (e: Exception) {
            Log.e(TAG, "ListenBrainz: logout error", e)
        }
    }

    private suspend fun scrobbleToListenBrainz(song: Song, timestamp: Long): ScrobblingResult {
        val sessionInfo = getListenBrainzSessionInfo()
            ?: return ScrobblingResult.Failure()
        try {
            val additionalInfo = if (!BuildConfig.DEBUG) {
                ListenBrainzTrackAdditionalInfo(
                    player = appName,
                    playerVersion = BuildConfig.VERSION_NAME
                )
            } else null

            val submission = ListenBrainzSubmission(
                listenType = "single",
                payload = listOf(
                    ListenBrainzListen(
                        listenedAt = timestamp,
                        trackMetadata = ListenBrainzTrackMetadata(
                            artistName = song.displayArtistName(),
                            trackName = song.title,
                            releaseName = song.albumName,
                            additionalInfo = additionalInfo
                        )
                    )
                )
            )
            val response = listenBrainzService.submitListen(sessionInfo.token, submission)
            return if (response.status == "ok") {
                ScrobblingResult.Success(song.id)
            } else {
                ScrobblingResult.Failure(response.error)
            }
        } catch (e: Exception) {
            Log.e(TAG, "ListenBrainz: scrobble call failed!", e)
            return ScrobblingResult.Failure(context.getString(R.string.error_listenbrainz_generic))
        }
    }

    private suspend fun updateNowPlayingOnListenBrainz(song: Song): ScrobblingResult {
        val sessionInfo = getListenBrainzSessionInfo()
            ?: return ScrobblingResult.Failure()
        try {
            val additionalInfo = if (!BuildConfig.DEBUG) {
                ListenBrainzTrackAdditionalInfo(
                    player = appName,
                    playerVersion = BuildConfig.VERSION_NAME
                )
            } else null

            val submission = ListenBrainzSubmission(
                listenType = "playing_now",
                payload = listOf(
                    ListenBrainzListen(
                        trackMetadata = ListenBrainzTrackMetadata(
                            artistName = song.displayArtistName(),
                            trackName = song.title,
                            releaseName = song.albumName,
                            additionalInfo = additionalInfo
                        )
                    )
                )
            )
            val response = listenBrainzService.submitListen(sessionInfo.token, submission)
            return if (response.status == "ok") {
                ScrobblingResult.Success(song.id)
            } else {
                ScrobblingResult.Failure(response.error)
            }
        } catch (e: Exception) {
            Log.e(TAG, "ListenBrainz: updateNowPlaying call failed!", e)
            return ScrobblingResult.Failure(context.getString(R.string.error_listenbrainz_generic))
        }
    }

    private fun setListenBrainzToken(token: String, userName: String): Boolean {
        return try {
            val encryptedToken = CryptoUtil.encrypt(token)
            val sessionInfo = Json.encodeToString(ListenBrainzSessionInfo(userName, encryptedToken))
            val encodedValue = Base64.encode(sessionInfo.toByteArray())
            preferences.edit(commit = true) {
                putString(LISTEN_BRAINZ_SESSION_INFO, encodedValue)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Couldn't save ListenBrainz token.", e)
            false
        }
    }

    private fun getListenBrainzSessionInfo(): ListenBrainzSessionInfo? {
        val encodedValue = preferences.getString(LISTEN_BRAINZ_SESSION_INFO, null)
        if (!encodedValue.isNullOrBlank()) {
            try {
                val decodedValue = Base64.decode(encodedValue)
                val sessionInfo = Json.decodeFromString<ListenBrainzSessionInfo>(String(decodedValue))
                return sessionInfo.copy(token = CryptoUtil.decrypt(sessionInfo.token))
            } catch (e: Exception) {
                Log.e(TAG, "Couldn't decrypt ListenBrainz session info. Removing...", e)
            }
        }
        return null
    }

    @Serializable
    private data class LastFmSessionInfo(
        @SerialName("user")
        val user: LastFmUser,
        @SerialName("session")
        val key: String
    )

    @Serializable
    private data class ListenBrainzSessionInfo(
        @SerialName("user_name")
        val user: String,
        val token: String
    ) {
        val url = "https://listenbrainz.org/user/$user/"
    }

    companion object {
        private const val TAG = "NetworkRepository"

        private const val LAST_FM_SESSION_INFO = "lastfm_session"
        private const val LISTEN_BRAINZ_SESSION_INFO = "listenbrainz_session"
    }
}