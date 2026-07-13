package com.rc.axiom.data.remote.musicbrainz

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MusicBrainzReleaseResponse(
    @SerialName("releases") val releases: List<MBRelease> = emptyList()
)

@Serializable
data class MBRelease(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String? = null,
    @SerialName("disambiguation") val disambiguation: String? = null,
    @SerialName("tags") val tags: List<MBTag> = emptyList()
)

@Serializable
data class MusicBrainzRecordingResponse(
    @SerialName("recordings") val recordings: List<MBRecording> = emptyList()
)

@Serializable
data class MBRecording(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String? = null,
    @SerialName("releases") val releases: List<MBRelease> = emptyList()
)

@Serializable
data class CoverArtResponse(
    @SerialName("images") val images: List<CoverImage> = emptyList()
)

@Serializable
data class CoverImage(
    @SerialName("image") val image: String,
    @SerialName("front") val front: Boolean
)

@Serializable
data class MusicBrainzArtistResponse(
    @SerialName("artists") val artists: List<MBArtist> = emptyList()
)

@Serializable
data class MBArtist(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String? = null,
    @SerialName("disambiguation") val disambiguation: String? = null,
    @SerialName("tags") val tags: List<MBTag> = emptyList(),
    @SerialName("life-span") val lifeSpan: MBLifeSpan? = null
)

@Serializable
data class MBTag(
    @SerialName("name") val name: String
)

@Serializable
data class MBLifeSpan(
    @SerialName("begin") val begin: String? = null,
    @SerialName("end") val end: String? = null
)

class MusicBrainzService(private val client: HttpClient) {

    companion object {
        private val mutex = Mutex()
        private var lastRequestTime = 0L
        private const val MIN_DELAY_MS = 2000L // 2.0s delay to be extremely safe from rate limits
    }

    private suspend fun enforceRateLimit() {
        val now = System.currentTimeMillis()
        val elapsed = now - lastRequestTime
        if (elapsed < MIN_DELAY_MS) {
            delay(MIN_DELAY_MS - elapsed)
        }
        lastRequestTime = System.currentTimeMillis()
    }

    private suspend fun getCoverUrlFromReleaseId(releaseId: String): String? = mutex.withLock {
        enforceRateLimit()
        return try {
            val response = client.get("https://coverartarchive.org/release/$releaseId") {
                header("User-Agent", "AxiomMusicPlayer/0.1.2 (https://github.com/rc/axiom)")
            }.body<CoverArtResponse>()
            response.images.firstOrNull { it.front }?.image ?: response.images.firstOrNull()?.image
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAlbumCoverUrl(artistName: String, albumName: String): String? {
        val releases = mutex.withLock {
            enforceRateLimit()
            try {
                val query = "release:\"$albumName\" AND artist:\"$artistName\""
                val response = client.get("https://musicbrainz.org/ws/2/release/") {
                    header("User-Agent", "AxiomMusicPlayer/0.1.2 (https://github.com/rc/axiom)")
                    url {
                        parameters.append("query", query)
                        parameters.append("fmt", "json")
                    }
                }.body<MusicBrainzReleaseResponse>()
                response.releases
            } catch (e: Exception) {
                emptyList()
            }
        }

        for (release in releases) {
            val coverUrl = getCoverUrlFromReleaseId(release.id)
            if (coverUrl != null) {
                return coverUrl
            }
        }
        return null
    }

    suspend fun getTrackCoverUrl(artistName: String, trackTitle: String): String? {
        val recordings = mutex.withLock {
            enforceRateLimit()
            try {
                val query = "recording:\"$trackTitle\" AND artist:\"$artistName\""
                val response = client.get("https://musicbrainz.org/ws/2/recording/") {
                    header("User-Agent", "AxiomMusicPlayer/0.1.2 (https://github.com/rc/axiom)")
                    url {
                        parameters.append("query", query)
                        parameters.append("fmt", "json")
                    }
                }.body<MusicBrainzRecordingResponse>()
                response.recordings
            } catch (e: Exception) {
                emptyList()
            }
        }

        for (recording in recordings) {
            for (release in recording.releases) {
                val coverUrl = getCoverUrlFromReleaseId(release.id)
                if (coverUrl != null) {
                    return coverUrl
                }
            }
        }
        return null
    }

    suspend fun getArtistInfo(artistName: String): String? = mutex.withLock {
        enforceRateLimit()
        try {
            val query = "artist:\"$artistName\""
            val response = client.get("https://musicbrainz.org/ws/2/artist/") {
                header("User-Agent", "AxiomMusicPlayer/0.1.2 (https://github.com/rc/axiom)")
                url {
                    parameters.append("query", query)
                    parameters.append("fmt", "json")
                }
            }.body<MusicBrainzArtistResponse>()

            val artist = response.artists.firstOrNull {
                com.rc.axiom.util.MusicUtil.levenshteinDistance(it.name ?: "", artistName) > 0.85
            } ?: response.artists.firstOrNull() ?: return null
            val desc = StringBuilder()
            artist.name?.let { desc.append(it) }
            artist.disambiguation?.let { desc.append(" (").append(it).append(")") }
            
            val begin = artist.lifeSpan?.begin
            if (!begin.isNullOrBlank()) {
                desc.append(". Active since: ").append(begin)
                val end = artist.lifeSpan.end
                if (!end.isNullOrBlank()) {
                    desc.append(" to ").append(end)
                }
            }
            
            if (artist.tags.isNotEmpty()) {
                desc.append(". Tags: ").append(artist.tags.take(5).joinToString { it.name })
            }
            
            desc.toString().takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAlbumInfo(artistName: String, albumName: String): String? = mutex.withLock {
        enforceRateLimit()
        try {
            val query = "release:\"$albumName\" AND artist:\"$artistName\""
            val response = client.get("https://musicbrainz.org/ws/2/release/") {
                header("User-Agent", "AxiomMusicPlayer/0.1.2 (https://github.com/rc/axiom)")
                url {
                    parameters.append("query", query)
                    parameters.append("fmt", "json")
                }
            }.body<MusicBrainzReleaseResponse>()

            val release = response.releases.firstOrNull {
                com.rc.axiom.util.MusicUtil.levenshteinDistance(it.title ?: "", albumName) > 0.85
            } ?: response.releases.firstOrNull() ?: return null
            val desc = StringBuilder()
            release.title?.let { desc.append(it) }
            release.disambiguation?.let { desc.append(" (").append(it).append(")") }
            if (release.tags.isNotEmpty()) {
                desc.append(". Tags: ").append(release.tags.take(5).joinToString { it.name })
            }
            desc.toString().takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }
}
