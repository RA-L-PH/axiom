package com.rc.axiom.data.remote.ytmusic

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class InnerTubeResponse(
    val contents: Contents? = null
)

@Serializable
data class Contents(
    val tabbedSearchResultsRenderer: TabbedSearchResultsRenderer? = null
)

@Serializable
data class TabbedSearchResultsRenderer(
    val tabs: List<Tab>? = null
)

@Serializable
data class Tab(
    val tabRenderer: TabRenderer? = null
)

@Serializable
data class TabRenderer(
    val content: SectionList? = null
)

@Serializable
data class SectionList(
    val sectionListRenderer: SectionListRenderer? = null
)

@Serializable
data class SectionListRenderer(
    val contents: List<SectionContent>? = null
)

@Serializable
data class SectionContent(
    val musicShelfRenderer: MusicShelfRenderer? = null
)

@Serializable
data class MusicShelfRenderer(
    val contents: List<ShelfItem>? = null
)

@Serializable
data class ShelfItem(
    val musicResponsiveListItemRenderer: MusicListItem? = null
)

@Serializable
data class MusicListItem(
    val thumbnail: ThumbnailContainer? = null,
    val flexColumns: List<FlexColumn>? = null,
    val playlistItemData: PlaylistItemData? = null,
    val overlay: MusicItemOverlay? = null
)

@Serializable
data class PlaylistItemData(
    val videoId: String? = null
)

@Serializable
data class MusicItemOverlay(
    val musicItemThumbnailOverlayRenderer: MusicItemThumbnailOverlayRenderer? = null
)

@Serializable
data class MusicItemThumbnailOverlayRenderer(
    val content: OverlayContent? = null
)

@Serializable
data class OverlayContent(
    val musicPlayButtonRenderer: MusicPlayButtonRenderer? = null
)

@Serializable
data class MusicPlayButtonRenderer(
    val playNavigationEndpoint: PlayNavigationEndpoint? = null
)

@Serializable
data class PlayNavigationEndpoint(
    val watchEndpoint: WatchEndpoint? = null
)

@Serializable
data class WatchEndpoint(
    val videoId: String? = null
)

@Serializable
data class ThumbnailContainer(
    val musicThumbnailRenderer: MusicThumbnailRenderer? = null
)

@Serializable
data class MusicThumbnailRenderer(
    val thumbnail: Thumbnails? = null
)

@Serializable
data class Thumbnails(
    val thumbnails: List<ThumbnailUrl>? = null
)

@Serializable
data class ThumbnailUrl(
    val url: String,
    val width: Int,
    val height: Int
)

@Serializable
data class FlexColumn(
    val musicResponsiveListItemFlexColumnRenderer: FlexColumnRenderer? = null
)

@Serializable
data class FlexColumnRenderer(
    val text: RunsContainer? = null
)

@Serializable
data class RunsContainer(
    val runs: List<TextRun>? = null
)

@Serializable
data class TextRun(
    val text: String
)

@Serializable
data class YtMusicItem(
    val title: String,
    val artist: String,
    val album: String?,
    val imageUrl: String?,
    val videoId: String? = null
)

@Serializable
data class YtmTrackDetails(
    val videoId: String,
    val title: String,
    val artists: List<String>,
    val album: String?,
    val durationSeconds: Int,
    val thumbnailUrl: String?,
    val recommendations: List<YtMusicItem> = emptyList()
)

data class ArtistMetadata(val name: String, val description: String, val imageUrl: String?)

class YTMusicScraper(private val client: HttpClient) {
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun scrapeArtistMetadata(artistName: String): ArtistMetadata? = withContext(Dispatchers.IO) {
        val searchUrl = "https://music.youtube.com/search?q=${java.net.URLEncoder.encode(artistName, "UTF-8")}"
        try {
            val response = client.get(searchUrl) {
                header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                header("Accept-Language", "en-US,en;q=0.9")
            }
            val htmlContent = response.bodyAsText()
            parseYoutubeJson(htmlContent, artistName)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseYoutubeJson(html: String, targetArtistName: String): ArtistMetadata? {
        val regex = "window\\[\"ytInitialData\"\\]\\s*=\\s*(\\{.*?\\});".toRegex()
        val matchResult = regex.find(html) ?: return null
        val jsonString = matchResult.groups[1]?.value ?: return null

        val rootElement = jsonParser.parseToJsonElement(jsonString).jsonObject

        // Try standard header renderer extraction first
        val header = rootElement["header"]?.jsonObject?.get("musicImmersiveHeaderRenderer")?.jsonObject
            ?: rootElement["header"]?.jsonObject?.get("musicVisualHeaderRenderer")?.jsonObject

        val artistName = header?.get("title")?.jsonObject
            ?.get("runs")?.jsonArray?.get(0)?.jsonObject?.get("text")?.jsonPrimitive?.content ?: targetArtistName

        val description = header?.get("description")?.jsonObject
            ?.get("runs")?.jsonArray?.get(0)?.jsonObject?.get("text")?.jsonPrimitive?.content ?: ""

        // Use recursive crawler to get the absolute best high-res profile/thumbnail image matching target artist
        val imageUrl = findArtistImageInJson(rootElement, targetArtistName)

        return ArtistMetadata(name = artistName, description = description, imageUrl = imageUrl)
    }

    private fun findArtistImageInJson(element: JsonElement, targetName: String): String? {
        if (element is JsonObject) {
            val isMatch = element.values.any {
                it is JsonPrimitive &&
                        it.isString &&
                        it.content.equals(targetName, ignoreCase = true)
            }
            if (isMatch) {
                val thumbnail = findThumbnailUrl(element)
                if (thumbnail != null) return thumbnail
            }
            for (value in element.values) {
                val found = findArtistImageInJson(value, targetName)
                if (found != null) return found
            }
        } else if (element is JsonArray) {
            for (value in element) {
                val found = findArtistImageInJson(value, targetName)
                if (found != null) return found
            }
        }
        return null
    }

    private fun findThumbnailUrl(element: JsonElement): String? {
        if (element is JsonObject) {
            val url = element["url"]?.jsonPrimitive?.content
            if (url != null && (url.contains("googleusercontent.com") || url.contains("ggpht.com") || url.contains("ytimg.com"))) {
                return url
            }
            for (value in element.values) {
                val found = findThumbnailUrl(value)
                if (found != null) return found
            }
        } else if (element is JsonArray) {
            for (value in element) {
                val found = findThumbnailUrl(value)
                if (found != null) return found
            }
        }
        return null
    }
}

class YtMusicService(private val client: HttpClient) {
    val scraper = YTMusicScraper(client)

    suspend fun searchMusic(query: String): List<YtMusicItem> {
        val url = "https://music.youtube.com/youtubei/v1/search"
        
        val requestBody = mapOf(
            "context" to mapOf(
                "client" to mapOf(
                    "clientName" to "WEB_REMIX",
                    "clientVersion" to "1.20240101.01.00"
                )
            ),
            "query" to query
        )

        return try {
            val response: InnerTubeResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

            parseResponse(response)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun parseResponse(response: InnerTubeResponse): List<YtMusicItem> {
        val items = mutableListOf<YtMusicItem>()
        
        val shelves = response.contents
            ?.tabbedSearchResultsRenderer?.tabs?.firstOrNull()
            ?.tabRenderer?.content?.sectionListRenderer?.contents ?: return emptyList()

        for (shelf in shelves) {
            val listItems = shelf.musicShelfRenderer?.contents ?: continue
            for (listItem in listItems) {
                val renderItem = listItem.musicResponsiveListItemRenderer ?: continue
                
                val imageUrl = renderItem.thumbnail
                    ?.musicThumbnailRenderer?.thumbnail?.thumbnails
                    ?.maxByOrNull { it.width * it.height }?.url

                val columns = renderItem.flexColumns ?: continue
                
                val title = columns.getOrNull(0)
                    ?.musicResponsiveListItemFlexColumnRenderer?.text?.runs
                    ?.firstOrNull()?.text ?: "Unknown Title"

                val secondaryRuns = columns.getOrNull(1)
                    ?.musicResponsiveListItemFlexColumnRenderer?.text?.runs
                    ?.map { it.text } ?: emptyList()

                val cleanMetadata = secondaryRuns.filter { it.trim() != "•" }
                
                val artist = cleanMetadata.getOrNull(0) ?: "Unknown Artist"
                val album = cleanMetadata.getOrNull(1)

                val videoId = renderItem.playlistItemData?.videoId
                    ?: renderItem.overlay?.musicItemThumbnailOverlayRenderer?.content
                        ?.musicPlayButtonRenderer?.playNavigationEndpoint?.watchEndpoint?.videoId

                items.add(YtMusicItem(title, artist, album, imageUrl, videoId))
            }
        }
        return items
    }

    suspend fun fetchNextDetails(videoId: String): YtmTrackDetails? = withContext(Dispatchers.IO) {
        val endpoint = "https://music.youtube.com/youtubei/v1/next"
        val payload = mapOf(
            "context" to mapOf(
                "client" to mapOf(
                    "clientName" to "WEB_REMIX",
                    "clientVersion" to "1.20240101.01.00"
                )
            ),
            "videoId" to videoId
        )
        try {
            val responseText: String = client.post(endpoint) {
                contentType(ContentType.Application.Json)
                setBody(payload)
            }.bodyAsText()

            val json = Json { ignoreUnknownKeys = true; isLenient = true }
            val root = json.parseToJsonElement(responseText).jsonObject

            var title = "Unknown Title"
            val artists = mutableListOf<String>()
            var album: String? = null
            var durationSeconds = 0
            var thumbnailUrl: String? = null
            val recommendations = mutableListOf<YtMusicItem>()

            val watchNext = root["contents"]?.jsonObject
                ?.get("singleColumnMusicResultsRenderer")?.jsonObject
                ?.get("tabbedRenderer")?.jsonObject
                ?.get("watchNextTabbedResultsRenderer")?.jsonObject

            val tabs = watchNext?.get("tabs")?.jsonArray
            val tabRenderer = tabs?.getOrNull(0)?.jsonObject?.get("tabRenderer")?.jsonObject
            val content = tabRenderer?.get("content")?.jsonObject
            val sectionList = content?.get("sectionListRenderer")?.jsonObject
            val sectionContents = sectionList?.get("contents")?.jsonArray

            if (sectionContents != null) {
                for (item in sectionContents) {
                    val shelf = item.jsonObject.get("musicQueueRenderer")?.jsonObject
                    val queueItems = shelf?.get("content")?.jsonObject?.get("playlistPanelRenderer")?.jsonObject?.get("contents")?.jsonArray
                    if (queueItems != null) {
                        for (qItem in queueItems) {
                            val panelRenderer = qItem.jsonObject.get("playlistPanelVideoRenderer")?.jsonObject ?: continue
                            val curVideoId = panelRenderer["videoId"]?.jsonPrimitive?.content
                            if (curVideoId == videoId) {
                                title = panelRenderer["title"]?.jsonObject?.get("runs")?.jsonArray?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.content ?: title
                                
                                val artistRuns = panelRenderer["longBylineText"]?.jsonObject?.get("runs")?.jsonArray
                                if (artistRuns != null) {
                                    for (run in artistRuns) {
                                        val name = run.jsonObject["text"]?.jsonPrimitive?.content ?: continue
                                        if (name != "•" && !name.contains("views") && !name.contains("likes")) {
                                            artists.add(name)
                                        }
                                    }
                                }
                                val durationText = panelRenderer["lengthText"]?.jsonObject?.get("runs")?.jsonArray?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.content
                                if (durationText != null) {
                                    val parts = durationText.split(":")
                                    durationSeconds = if (parts.size == 2) {
                                        parts[0].toInt() * 60 + parts[1].toInt()
                                    } else if (parts.size == 3) {
                                        parts[0].toInt() * 3600 + parts[1].toInt() * 60 + parts[2].toInt()
                                    } else {
                                        0
                                    }
                                }
                                thumbnailUrl = panelRenderer["thumbnail"]?.jsonObject?.get("thumbnails")?.jsonArray
                                    ?.maxByOrNull { it.jsonObject["width"]?.jsonPrimitive?.int ?: 0 }
                                    ?.jsonObject?.get("url")?.jsonPrimitive?.content
                            } else {
                                val recTitle = panelRenderer["title"]?.jsonObject?.get("runs")?.jsonArray?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.content
                                val recArtistRuns = panelRenderer["longBylineText"]?.jsonObject?.get("runs")?.jsonArray
                                val recArtist = recArtistRuns?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.content
                                val recAlbum = recArtistRuns?.getOrNull(2)?.jsonObject?.get("text")?.jsonPrimitive?.content
                                val recThumbnail = panelRenderer["thumbnail"]?.jsonObject?.get("thumbnails")?.jsonArray
                                    ?.maxByOrNull { it.jsonObject["width"]?.jsonPrimitive?.int ?: 0 }
                                    ?.jsonObject?.get("url")?.jsonPrimitive?.content
                                
                                if (recTitle != null && recArtist != null) {
                                    recommendations.add(YtMusicItem(recTitle, recArtist, recAlbum, recThumbnail, curVideoId))
                                }
                            }
                        }
                    }
                }
            }

            if (artists.isEmpty()) {
                artists.add("Unknown Artist")
            }

            YtmTrackDetails(
                videoId = videoId,
                title = title,
                artists = artists,
                album = album,
                durationSeconds = durationSeconds,
                thumbnailUrl = thumbnailUrl,
                recommendations = recommendations
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
