package com.mistpaag.test

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*

class ViewModel(
    val testDispatcher: CoroutineDispatcher = Dispatchers.IO
): ViewModel() {
    private val _state= MutableLiveData<SearchStates>()
    val state: LiveData<SearchStates>
        get() = _state

    fun getMethodsByAsync(){
        viewModelScope.launch(testDispatcher) {
            _state.postValue(SearchStates.Loading)
            val lol = async{
                first()
            }
            val lel = async{
                second()
            }
            val todo = lol.await() + lel.await()
            _state.postValue(SearchStates.AwaitText(todo))
        }
    }

    suspend fun first(): Int {
        delay(5000)
        return 1
    }

    suspend fun second(): Int {
        delay(5000)
        return 2
    }

}

sealed class SearchStates {
    data class AwaitText(val todo: Int): SearchStates()
    object Loading : SearchStates()
}