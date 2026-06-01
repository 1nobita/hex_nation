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

import com.example.data.UserProfile
import com.example.data.HexOwnershipLog

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HexRepository
    
    init {
        val db = AppDatabase.getDatabase(application)
        repository = HexRepository(db)
    }

    private val _currentUser = MutableStateFlow<String?>("user@gmail.com") // mocked authenticated user
    val currentUser: StateFlow<String?> = _currentUser.asStateFlow()

    private val _currentProfile = MutableStateFlow<UserProfile?>(null)
    val currentProfile: StateFlow<UserProfile?> = _currentProfile.asStateFlow()

    private val _ownedHexagons = MutableStateFlow<List<Hexagon>>(emptyList())
    val ownedHexagons: StateFlow<List<Hexagon>> = _ownedHexagons.asStateFlow()

    private val _currentNation = MutableStateFlow<String?>("Global")
    val currentNation: StateFlow<String?> = _currentNation.asStateFlow()

    private val _hexagons = MutableStateFlow<List<Hexagon>>(emptyList())
    val hexagons: StateFlow<List<Hexagon>> = _hexagons.asStateFlow()

    private val _selectedHexLogs = MutableStateFlow<List<HexOwnershipLog>>(emptyList())
    val selectedHexLogs: StateFlow<List<HexOwnershipLog>> = _selectedHexLogs.asStateFlow()
    
    private var hexJob: Job? = null
    private var profileJob: Job? = null
    private var ownedHexJob: Job? = null
    private var logJob: Job? = null
    
    fun observeHexLogs(q: Int, r: Int) {
        val nation = _currentNation.value ?: return
        logJob?.cancel()
        logJob = viewModelScope.launch {
            repository.getHexOwnershipLogs(nation, q, r).collectLatest { logs ->
                _selectedHexLogs.value = logs
            }
        }
    }

    fun login(email: String) {
        _currentUser.value = email
        observeUserProfile(email)
        observeOwnedHexagons(email)
        // Ensure profile exists
        viewModelScope.launch {
            repository.insertUserProfile(UserProfile(userId = email, placeOfOrigin = "Internet", bio = "I love coloring hexagons!"))
        }
    }
    
    private fun observeUserProfile(userId: String) {
        profileJob?.cancel()
        profileJob = viewModelScope.launch {
            repository.getUserProfile(userId).collectLatest { profile ->
                _currentProfile.value = profile
            }
        }
    }

    private fun observeOwnedHexagons(userId: String) {
        ownedHexJob?.cancel()
        ownedHexJob = viewModelScope.launch {
            repository.getOwnedHexagons(userId).collectLatest { list ->
                _ownedHexagons.value = list
            }
        }
    }

    fun updateUserProfile(placeOfOrigin: String, bio: String) {
        val userId = _currentUser.value ?: return
        viewModelScope.launch {
            val curr = _currentProfile.value ?: UserProfile(userId = userId)
            repository.insertUserProfile(curr.copy(placeOfOrigin = placeOfOrigin, bio = bio))
        }
    }

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
        val currentUserId = _currentUser.value ?: return
        val timeNow = System.currentTimeMillis()
        
        viewModelScope.launch {
            val existing = repository.getHexagon(nation, q, r)
            
            if (existing != null) {
                // calculate duration for previous owner
                val duration = (timeNow - existing.lastPurchaseTimestamp) / 1000
                val log = HexOwnershipLog(
                    nationId = nation,
                    q = q,
                    r = r,
                    ownerId = existing.ownerId,
                    acquiredAt = existing.lastPurchaseTimestamp,
                    durationSeconds = duration
                )
                repository.insertHexOwnershipLog(log)
            }

            val flips = (existing?.numFlips ?: 0) + 1
            val hex = Hexagon(
                nationId = nation,
                q = q,
                r = r,
                color = color,
                ownerId = currentUserId,
                numFlips = flips,
                lastPurchaseTimestamp = timeNow
            )
            repository.insertHexagon(hex)
        }
    }
}
