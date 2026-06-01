package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.HexRepository
import com.example.data.Hexagon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.Job

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HexRepository
    
    init {
        val db = AppDatabase.getDatabase(application)
        repository = HexRepository(db)
    }

    private val _currentNation = MutableStateFlow<String?>("Global")
    val currentNation: StateFlow<String?> = _currentNation.asStateFlow()

    private val _hexagons = MutableStateFlow<List<Hexagon>>(emptyList())
    val hexagons: StateFlow<List<Hexagon>> = _hexagons.asStateFlow()
    
    private var hexJob: Job? = null

    fun selectNation(nationId: String) {
        _currentNation.value = nationId
        hexJob?.cancel()
        hexJob = viewModelScope.launch {
            repository.getHexagonsForNation(nationId).collectLatest { list ->
                _hexagons.value = list
            }
        }
    }

    fun purchaseHexagon(q: Int, r: Int, color: Long) {
        val nation = _currentNation.value ?: return
        viewModelScope.launch {
            val existing = repository.getHexagon(nation, q, r)
            val flips = (existing?.numFlips ?: 0) + 1
            val hex = Hexagon(
                nationId = nation,
                q = q,
                r = r,
                color = color,
                ownerId = "currentUser",
                numFlips = flips,
                lastPurchaseTimestamp = System.currentTimeMillis()
            )
            repository.insertHexagon(hex)
        }
    }
}
