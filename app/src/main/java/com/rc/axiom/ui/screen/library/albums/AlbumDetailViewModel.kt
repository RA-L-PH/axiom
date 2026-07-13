package com.rc.axiom.ui.screen.library.albums

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.rc.axiom.data.local.repository.Repository
import com.rc.axiom.data.model.Album
import com.rc.axiom.data.model.network.NetworkFeature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlbumDetailViewModel(
    application: Application,
    private val repository: Repository,
    private val albumId: Long
) : AndroidViewModel(application) {

    private val _albumDetail = MutableLiveData<Album>()

    fun getAlbumDetail(): LiveData<Album> = _albumDetail

    fun getAlbum() = getAlbumDetail().value ?: Album.empty

    fun loadAlbumDetail() = viewModelScope.launch(Dispatchers.IO) {
        _albumDetail.postValue(repository.albumById(albumId))
    }

    fun getSimilarAlbums(album: Album): LiveData<List<Album>> = liveData(Dispatchers.IO) {
        repository.similarAlbums(album).let {
            if (it.isNotEmpty()) emit(it)
        }
    }

    fun getAlbumWiki(album: Album, lang: String?) = liveData(Dispatchers.IO) {
        if (NetworkFeature.isOnline(ignoreWifiSetting = true)) {
            emit(repository.albumInfo(album.albumArtistName ?: album.artistName, album.name, lang))
        }
    }
}