package fr.florianpal.beerrecipe.ui.screens.equipment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.florianpal.beerrecipe.data.entity.Equipment
import fr.florianpal.beerrecipe.data.repository.BrewRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EquipmentViewModel(private val repository: BrewRepository) : ViewModel() {

    val equipments: StateFlow<List<Equipment>> = repository.equipments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Enregistre l'installation, en recalant brassin et épaisseur sur l'eau de départ si elle est fixée. */
    fun save(equipment: Equipment) =
        viewModelScope.launch { repository.saveEquipment(equipment.withDerivedValues()) }
    fun delete(equipment: Equipment) = viewModelScope.launch { repository.deleteEquipment(equipment) }

    fun create() = viewModelScope.launch {
        repository.saveEquipment(Equipment(name = "Nouvelle installation"))
    }

    companion object {
        fun factory(repository: BrewRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                EquipmentViewModel(repository) as T
        }
    }
}
