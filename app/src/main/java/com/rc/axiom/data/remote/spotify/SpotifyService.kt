package com.rc.axiom.data.remote.spotify

import android.content.SharedPreferences
import android.util.Base64
import com.rc.axiom.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpotifyTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("expires_in") val expiresIn: Int
)

@Serializable
data class SpotifySearchResponse(
    @SerialName("artists") val artists: SpotifyArtistsContainer? = null
)

@Serializable
data class SpotifyArtistsContainer(
    @SerialName("items") val items: List<SpotifyArtistItem> = emptyList()
)

@Serializable
data class SpotifyArtistItem(
    @SerialName("name") val name: String,
    @SerialName("id") val id: String,
    @SerialName("images") val images: List<SpotifyImage> = emptyList()
)

@Serializable
data class SpotifyImage(
    @SerialName("url") val url: String,
    @SerialName("height") val height: Int? = null,
    @SerialName("width") val width: Int? = null
)

@Serializable
data class SpotifyTopTracksResponse(
    @SerialName("tracks") val tracks: List<SpotifyTrack> = emptyList()
)

@Serializable
data class SpotifyTrack(
    @SerialName("name") val name: String
)

@Serializable
data class SpotifyAlbumsResponse(
    @SerialName("items") val items: List<SpotifyAlbumItem> = emptyList()
)

@Serializable
data class SpotifyAlbumItem(
    @SerialName("name") val name: String
)

class SpotifyService(
    client: HttpClient,
    preferences: SharedPreferences
) {
    private val client: HttpClient = client
    private val preferences: SharedPreferences = preferences

    companion object {
        private val tokenMutex = Mutex()
        private val rateLimitMutex = Mutex()
        private var cachedToken: String? = null
        private var tokenExpiryTime = 0L
        private var lastRequestTime = 0L
        private const val MIN_DELAY_MS = 1500L // 1.5s delay between queries to Spotify
    }

    private suspend fun enforceRateLimit() {
        val now = System.currentTimeMillis()
        val elapsed = now - lastRequestTime
        if (elapsed < MIN_DELAY_MS) {
            delay(MIN_DELAY_MS - elapsed)
        }
        lastRequestTime = System.currentTimeMillis()
    }

    private suspend fun getAccessToken(): String? {
        val now = System.currentTimeMillis()
        tokenMutex.withLock {
            if (cachedToken != null && now < tokenExpiryTime) {
                return cachedToken
            }

            val clientId = BuildConfig.SPOTIFY_CLIENT_ID.takeIf { it.isNotBlank() }
                ?: preferences.getString("spotify_client_id", null)?.takeIf { it.isNotBlank() }
            val clientSecret = BuildConfig.SPOTIFY_CLIENT_SECRET.takeIf { it.isNotBlank() }
                ?: preferences.getString("spotify_client_secret", null)?.takeIf { it.isNotBlank() }
            if (clientId.isNullOrBlank() || clientSecret.isNullOrBlank()) {
                return null
            }

            return try {
                val rawAuth = "$clientId:$clientSecret"
                val encodedAuth = Base64.encodeToString(rawAuth.toByteArray(), Base64.NO_WRAP)

                val response = client.submitForm(
                    url = "https://accounts.spotify.com/api/token",
                    formParameters = io.ktor.http.Parameters.build {
                        append("grant_type", "client_credentials")
                    }
                ) {
                    header("Authorization", "Basic $encodedAuth")
                }.body<SpotifyTokenResponse>()

                cachedToken = response.accessToken
                tokenExpiryTime = System.currentTimeMillis() + (response.expiresIn * 1000) - 60000 // 1 minute buffer
                cachedToken
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getArtistImageUrl(
        artistName: String,
        songTitles: List<String> = emptyList(),
        albumTitles: List<String> = emptyList()
    ): String? = rateLimitMutex.withLock {
        enforceRateLimit()
        val token = getAccessToken() ?: return null
        return try {
            val response = client.get("https://api.spotify.com/v1/search") {
                header("Authorization", "Bearer $token")
                parameter("q", "artist:\"$artistName\"")
                parameter("type", "artist")
                parameter("limit", "5")
            }.body<SpotifySearchResponse>()

            val items = response.artists?.items ?: return null
            if (items.isEmpty()) return null

            var bestCandidate = items.firstOrNull { com.rc.axiom.util.MusicUtil.levenshteinDistance(it.name, artistName) > 0.85 }
            if (songTitles.isNotEmpty() || albumTitles.isNotEmpty()) {
                for (item in items) {
                    if (com.rc.axiom.util.MusicUtil.levenshteinDistance(item.name, artistName) > 0.85) {
                        val topTracks = try {
                            client.get("https://api.spotify.com/v1/artists/${item.id}/top-tracks") {
                                header("Authorization", "Bearer $token")
                                parameter("market", "US")
                            }.body<SpotifyTopTracksResponse>()
                        } catch (e: Exception) {
                            null
                        }

                        val trackMatches = topTracks?.tracks?.any { track ->
                            songTitles.any { localTitle ->
                                track.name.contains(localTitle, ignoreCase = true) ||
                                localTitle.contains(track.name, ignoreCase = true)
                            }
                        } ?: false

                        val albums = try {
                            client.get("https://api.spotify.com/v1/artists/${item.id}/albums") {
                                header("Authorization", "Bearer $token")
                                parameter("limit", "5")
                            }.body<SpotifyAlbumsResponse>()
                        } catch (e: Exception) {
                            null
                        }

                        val albumMatches = albums?.items?.any { album ->
                            albumTitles.any { localTitle ->
                                album.name.contains(localTitle, ignoreCase = true) ||
                                localTitle.contains(album.name, ignoreCase = true)
                            }
                        } ?: false

                        if (trackMatches || albumMatches) {
                            bestCandidate = item
                            break
                        }
                    }
                }
            }

            val finalArtist = bestCandidate ?: items.firstOrNull() ?: return null
            finalArtist.images.firstOrNull()?.url
        } catch (e: Exception) {
            null
        }
    }
}
