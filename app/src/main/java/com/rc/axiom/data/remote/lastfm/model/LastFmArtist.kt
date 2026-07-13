package com.rc.axiom.data.remote.lastfm.model

import kotlinx.serialization.Serializable

@Serializable
class LastFmArtist(
    val artist: Artist?,
    val debutYear: String? = null,
    val genre: String? = null,
    val style: String? = null,
    val mood: String? = null,
    val country: String? = null
) {
    @Serializable
    class Artist(val bio: Bio?)

    @Serializable
    class Bio(val content: String?)
}