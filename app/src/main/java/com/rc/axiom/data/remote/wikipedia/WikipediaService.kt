package com.rc.axiom.data.remote.wikipedia

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class WikiSearchResponse(
    @SerialName("query") val query: SearchQuery? = null
) {
    @Serializable
    data class SearchQuery(
        @SerialName("search") val search: List<SearchItem> = emptyList()
    ) {
        @Serializable
        data class SearchItem(
            @SerialName("title") val title: String,
            @SerialName("snippet") val snippet: String? = null
        )
    }
}

class WikipediaService(private val client: HttpClient) {

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

    private suspend fun getPageTitle(query: String): String? = mutex.withLock {
        enforceRateLimit()
        return try {
            val searchResponse = client.get("https://en.wikipedia.org/w/api.php") {
                url {
                    parameters.append("action", "query")
                    parameters.append("list", "search")
                    parameters.append("srsearch", query)
                    parameters.append("format", "json")
                    parameters.append("utf8", "1")
                }
            }.body<WikiSearchResponse>()
            searchResponse.query?.search?.firstOrNull()?.title
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun getPageSummary(title: String): String? = mutex.withLock {
        enforceRateLimit()
        return try {
            val response = client.get("https://en.wikipedia.org/w/api.php") {
                url {
                    parameters.append("action", "query")
                    parameters.append("prop", "extracts")
                    parameters.append("exintro", "1")
                    parameters.append("explaintext", "1")
                    parameters.append("titles", title)
                    parameters.append("format", "json")
                }
            }.body<JsonObject>()
            val pages = response["query"]?.jsonObject?.get("pages")?.jsonObject
            val pageKey = pages?.keys?.firstOrNull() ?: return null
            pages[pageKey]?.jsonObject?.get("extract")?.jsonPrimitive?.content
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun getArtistPageTitle(
        artistName: String,
        songTitles: List<String>,
        albumTitles: List<String> = emptyList()
    ): String? = mutex.withLock {
        enforceRateLimit()
        return try {
            val query = "\"$artistName\" AND (\"music\" OR \"band\" OR \"singer\" OR \"musician\" OR \"rapper\")"
            val searchResponse = client.get("https://en.wikipedia.org/w/api.php") {
                url {
                    parameters.append("action", "query")
                    parameters.append("list", "search")
                    parameters.append("srsearch", query)
                    parameters.append("format", "json")
                    parameters.append("utf8", "1")
                    parameters.append("srlimit", "10")
                }
            }.body<WikiSearchResponse>()

            val items = searchResponse.query?.search ?: return null
            if (items.isEmpty()) return null

            // Filter out disambiguation pages
            val filteredItems = items.filter { item ->
                !item.title.contains("(disambiguation)", ignoreCase = true) &&
                !(item.snippet?.contains("may refer to", ignoreCase = true) ?: false)
            }

            if (filteredItems.isEmpty()) return null

            // Prioritize standard artist names/suffixes
            val disambiguatedSuffixes = listOf("", " (musician)", " (band)", " (singer)", " (music group)", " (rapper)", " (composer)")
            var bestTitle = filteredItems.firstOrNull { item ->
                disambiguatedSuffixes.any { suffix ->
                    val cleanTitle = if (suffix.isNotEmpty() && item.title.endsWith(suffix, ignoreCase = true)) {
                        item.title.substring(0, item.title.length - suffix.length)
                    } else {
                        item.title
                    }
                    com.rc.axiom.util.MusicUtil.levenshteinDistance(cleanTitle, artistName) > 0.85
                }
            }?.title

            // Match snippet against local song titles or album titles
            if (bestTitle == null && (songTitles.isNotEmpty() || albumTitles.isNotEmpty())) {
                bestTitle = filteredItems.firstOrNull { item ->
                    val cleanTitle = item.title.substringBefore(" (")
                    val isSimilar = com.rc.axiom.util.MusicUtil.levenshteinDistance(cleanTitle, artistName) > 0.80
                    val snippetLower = item.snippet?.lowercase() ?: ""
                    isSimilar && (
                        songTitles.any { song -> snippetLower.contains(song.lowercase()) } ||
                        albumTitles.any { album -> snippetLower.contains(album.lowercase()) }
                    )
                }?.title
            }

            bestTitle ?: filteredItems.firstOrNull { item ->
                val cleanTitle = item.title.substringBefore(" (")
                com.rc.axiom.util.MusicUtil.levenshteinDistance(cleanTitle, artistName) > 0.80
            }?.title
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getArtistImageUrl(
        artistName: String,
        songTitles: List<String>,
        albumTitles: List<String> = emptyList()
    ): String? {
        val title = getArtistPageTitle(artistName, songTitles, albumTitles) ?: return null
        return mutex.withLock {
            enforceRateLimit()
            try {
                val imageResponse = client.get("https://en.wikipedia.org/w/api.php") {
                    url {
                        parameters.append("action", "query")
                        parameters.append("titles", title)
                        parameters.append("prop", "pageimages")
                        parameters.append("piprop", "original")
                        parameters.append("format", "json")
                    }
                }.body<JsonObject>()

                val pages = imageResponse["query"]?.jsonObject?.get("pages")?.jsonObject
                val pageKey = pages?.keys?.firstOrNull() ?: return null
                val pageObj = pages[pageKey]?.jsonObject
                val originalObj = pageObj?.get("original")?.jsonObject
                originalObj?.get("source")?.jsonPrimitive?.content
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getArtistBio(
        artistName: String,
        songTitles: List<String> = emptyList(),
        albumTitles: List<String> = emptyList()
    ): String? {
        val title = getArtistPageTitle(artistName, songTitles, albumTitles) ?: return null
        return mutex.withLock {
            enforceRateLimit()
            try {
                // Query full text of the article to parse sections
                val response = client.get("https://en.wikipedia.org/w/api.php") {
                    url {
                        parameters.append("action", "query")
                        parameters.append("prop", "extracts")
                        parameters.append("explaintext", "1")
                        parameters.append("titles", title)
                        parameters.append("format", "json")
                    }
                }.body<JsonObject>()

                val pages = response["query"]?.jsonObject?.get("pages")?.jsonObject
                val pageKey = pages?.keys?.firstOrNull() ?: return null
                val fullText = pages[pageKey]?.jsonObject?.get("extract")?.jsonPrimitive?.content ?: return null

                // Try to extract the Career section
                val careerBio = extractCareerSection(fullText)
                if (!careerBio.isNullOrBlank()) {
                    return careerBio
                }

                // Fallback to page summary (introductory paragraph)
                val summaryResponse = client.get("https://en.wikipedia.org/w/api.php") {
                    url {
                        parameters.append("action", "query")
                        parameters.append("prop", "extracts")
                        parameters.append("exintro", "1")
                        parameters.append("explaintext", "1")
                        parameters.append("titles", title)
                        parameters.append("format", "json")
                    }
                }.body<JsonObject>()
                val summaryPages = summaryResponse["query"]?.jsonObject?.get("pages")?.jsonObject
                val summaryPageKey = summaryPages?.keys?.firstOrNull() ?: return null
                summaryPages[summaryPageKey]?.jsonObject?.get("extract")?.jsonPrimitive?.content
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun extractCareerSection(fullText: String): String? {
        val lines = fullText.lines()
        val careerStartIndex = lines.indexOfFirst { line ->
            val trimmed = line.trim()
            trimmed.matches(Regex("(?i)==+\\s*(career|history|musical career|life and career)\\s*==+"))
        }
        if (careerStartIndex == -1) return null

        val careerText = StringBuilder()
        for (i in (careerStartIndex + 1) until lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("==") && line.endsWith("==")) {
                break
            }
            if (line.isNotEmpty()) {
                careerText.append(line).append("\n\n")
            }
        }
        return careerText.toString().trim().takeIf { it.isNotEmpty() }
    }

    suspend fun getAlbumBio(artistName: String, albumName: String): String? {
        val query = "\"$albumName\" AND \"$artistName\" AND (\"album\" OR \"music\")"
        val title = getPageTitle(query) ?: return null
        return getPageSummary(title)
    }

    suspend fun getAlbumCoverUrl(artistName: String, albumName: String): String? {
        val query = "\"$albumName\" AND \"$artistName\" AND (\"album\" OR \"music\")"
        val title = getPageTitle(query) ?: return null
        return mutex.withLock {
            enforceRateLimit()
            try {
                val imageResponse = client.get("https://en.wikipedia.org/w/api.php") {
                    url {
                        parameters.append("action", "query")
                        parameters.append("titles", title)
                        parameters.append("prop", "pageimages")
                        parameters.append("piprop", "original")
                        parameters.append("format", "json")
                    }
                }.body<JsonObject>()

                val pages = imageResponse["query"]?.jsonObject?.get("pages")?.jsonObject
                val pageKey = pages?.keys?.firstOrNull() ?: return null
                val pageObj = pages[pageKey]?.jsonObject
                val originalObj = pageObj?.get("original")?.jsonObject
                originalObj?.get("source")?.jsonPrimitive?.content
            } catch (e: Exception) {
                null
            }
        }
    }
}
