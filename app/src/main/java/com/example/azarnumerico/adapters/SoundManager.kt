package com.example.azarnumerico.adapters

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object SoundManager {
    private val _soundEnabled = MutableLiveData(true)
    val soundEnabled: LiveData<Boolean> = _soundEnabled

    fun changeSound() {
        _soundEnabled.value = _soundEnabled.value != true
    }
}