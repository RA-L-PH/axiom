package com.rc.axiom.data

import com.rc.axiom.data.model.Song

interface SongProvider {
    val songs: List<Song>
}