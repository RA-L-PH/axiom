package com.rc.axiom.data.remote.audiodb

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AudioDbResponse(
    @SerialName("artists") val artists: List<AudioDbArtist>? = null
)

@Serializable
data class AudioDbArtist(
    @SerialName("strArtist") val strArtist: String? = null,
    @SerialName("strBiographyEN") val strBiographyEN: String? = null,
    @SerialName("strArtistThumb") val strArtistThumb: String? = null,
    @SerialName("strGenre") val strGenre: String? = null,
    @SerialName("intFormedYear") val intFormedYear: String? = null,
    @SerialName("strStyle") val strStyle: String? = null,
    @SerialName("strMood") val strMood: String? = null,
    @SerialName("strCountry") val strCountry: String? = null
)

@Serializable
data class AudioDbAlbumResponse(
    @SerialName("album") val album: List<AudioDbAlbum>? = null
)

@Serializable
data class AudioDbAlbum(
    @SerialName("strAlbum") val strAlbum: String? = null,
    @SerialName("strGenre") val strGenre: String? = null
)

@Serializable
data class AudioDbTrackResponse(
    @SerialName("track") val track: List<AudioDbTrack>? = null
)

@Serializable
data class AudioDbTrack(
    @SerialName("strTrack") val strTrack: String? = null,
    @SerialName("strGenre") val strGenre: String? = null
)

class AudioDbService(private val client: HttpClient) {

    companion object {
        private val mutex = Mutex()
        private var lastRequestTime = 0L
        private const val MIN_DELAY_MS = 1500L // 1.5s delay to be safe
    }

    private suspend fun enforceRateLimit() {
        val now = System.currentTimeMillis()
        val elapsed = now - lastRequestTime
        if (elapsed < MIN_DELAY_MS) {
            delay(MIN_DELAY_MS - elapsed)
        }
        lastRequestTime = System.currentTimeMillis()
    }

    suspend fun getArtistDetails(artistName: String): AudioDbArtist? = mutex.withLock {
        enforceRateLimit()
        return try {
            val response = client.get("https://www.theaudiodb.com/api/v1/json/2/search.php") {
                parameter("s", artistName)
            }.body<AudioDbResponse>()
            response.artists?.firstOrNull { it.strArtist.equals(artistName, ignoreCase = true) }
                ?: response.artists?.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAlbumDetails(artistName: String, albumName: String): AudioDbAlbum? = mutex.withLock {
        enforceRateLimit()
        return try {
            val response = client.get("https://www.theaudiodb.com/api/v1/json/2/searchalbum.php") {
                parameter("s", artistName)
                parameter("a", albumName)
            }.body<AudioDbAlbumResponse>()
            response.album?.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getTrackDetails(artistName: String, trackName: String): AudioDbTrack? = mutex.withLock {
        enforceRateLimit()
        return try {
            val response = client.get("https://www.theaudiodb.com/api/v1/json/2/searchtrack.php") {
                parameter("s", artistName)
                parameter("t", trackName)
            }.body<AudioDbTrackResponse>()
            response.track?.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }
}
