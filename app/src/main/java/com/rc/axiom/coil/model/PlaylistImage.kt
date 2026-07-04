package com.rc.axiom.coil.model

import com.rc.axiom.data.local.room.PlaylistEntity
import com.rc.axiom.data.model.Song

class PlaylistImage(val playlistEntity: PlaylistEntity, val songs: List<Song>) {
    override fun toString(): String {
        return buildString {
            append("PlaylistImage{")
            append("playlistEntity=$playlistEntity,")
            append("songs=$songs")
            append("}")
        }
    }
}