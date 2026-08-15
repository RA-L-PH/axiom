package com.rc.axiom.data.remote.genius

import android.content.SharedPreferences
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeniusSearchResponse(
    @SerialName("response") val response: GeniusSearchResponseData
)

@Serializable
data class GeniusSearchResponseData(
    @SerialName("hits") val hits: List<GeniusHit> = emptyList()
)

@Serializable
data class GeniusHit(
    @SerialName("result") val result: GeniusSongResult
)

@Serializable
data class GeniusSongResult(
    @SerialName("id") val id: Long,
    @SerialName("title") val title: String,
    @SerialName("primary_artist") val primaryArtist: GeniusArtistInfo,
    @SerialName("song_art_image_url") val songArtImageUrl: String? = null
)

@Serializable
data class GeniusArtistInfo(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("image_url") val imageUrl: String? = null
)

@Serializable
data class GeniusArtistResponse(
    @SerialName("response") val response: GeniusArtistResponseData
)

@Serializable
data class GeniusArtistResponseData(
    @SerialName("artist") val artist: GeniusArtistDetail
)

@Serializable
data class GeniusArtistDetail(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("description") val description: GeniusDescription? = null
)

@Serializable
data class GeniusDescription(
    @SerialName("plain") val plain: String? = null
)

@Serializable
data class GeniusSongResponse(
    @SerialName("response") val response: GeniusSongResponseData
)

@Serializable
data class GeniusSongResponseData(
    @SerialName("song") val song: GeniusSongDetail
)

@Serializable
data class GeniusSongDetail(
    @SerialName("id") val id: Long,
    @SerialName("title") val title: String,
    @SerialName("description") val description: GeniusDescription? = null,
    @SerialName("song_art_image_url") val songArtImageUrl: String? = null,
    @SerialName("album") val album: GeniusAlbumInfo? = null
)

@Serializable
data class GeniusAlbumInfo(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("cover_art_url") val coverArtUrl: String? = null
)

@Serializable
data class GeniusAlbumResponse(
    @SerialName("response") val response: GeniusAlbumResponseData
)

@Serializable
data class GeniusAlbumResponseData(
    @SerialName("album") val album: GeniusAlbumDetail
)

@Serializable
data class GeniusAlbumDetail(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("cover_art_url") val coverArtUrl: String? = null,
    @SerialName("description") val description: GeniusDescription? = null
)

class GeniusService(
    private val client: HttpClient,
    private val preferences: SharedPreferences
) {
    private val accessToken: String?
        get() = preferences.getString("genius_access_token", null)?.takeIf { it.isNotBlank() }

    suspend fun searchSong(query: String): List<GeniusSongResult> {
        val token = accessToken ?: return emptyList()
        return try {
            val response: GeniusSearchResponse = client.get("https://api.genius.com/search") {
                header("Authorization", "Bearer $token")
                // Use Ktor's parameter() instead of manual URLEncoder.encode — handles
                // special chars (&, +, #) correctly and is safer for arbitrary song/artist strings.
                url { parameters.append("q", query) }
            }.body()
            response.response.hits.map { it.result }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getArtistDetails(artistId: Long): GeniusArtistDetail? {
        val token = accessToken ?: return null
        val url = "https://api.genius.com/artists/$artistId"
        return try {
            val response: GeniusArtistResponse = client.get(url) {
                header("Authorization", "Bearer $token")
            }.body()
            response.response.artist
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getSongDetails(songId: Long): GeniusSongDetail? {
        val token = accessToken ?: return null
        val url = "https://api.genius.com/songs/$songId"
        return try {
            val response: GeniusSongResponse = client.get(url) {
                header("Authorization", "Bearer $token")
            }.body()
            response.response.song
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getAlbumDetails(albumId: Long): GeniusAlbumDetail? {
        val token = accessToken ?: return null
        val url = "https://api.genius.com/albums/$albumId"
        return try {
            val response: GeniusAlbumResponse = client.get(url) {
                header("Authorization", "Bearer $token")
            }.body()
            response.response.album
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getArtistSongs(artistId: Long): List<GeniusArtistSong> {
        val token = accessToken ?: return emptyList()
        val url = "https://api.genius.com/artists/$artistId/songs?per_page=20"
        return try {
            val response: GeniusArtistSongsResponse = client.get(url) {
                header("Authorization", "Bearer $token")
            }.body()
            response.response.songs
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

@Serializable
data class GeniusArtistSongsResponse(
    @SerialName("response") val response: GeniusArtistSongsResponseData
)

@Serializable
data class GeniusArtistSongsResponseData(
    @SerialName("songs") val songs: List<GeniusArtistSong> = emptyList()
)

@Serializable
data class GeniusArtistSong(
    @SerialName("id") val id: Long,
    @SerialName("title") val title: String
)
