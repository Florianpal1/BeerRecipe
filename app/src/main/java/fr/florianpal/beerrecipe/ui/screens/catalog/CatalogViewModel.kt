package fr.florianpal.beerrecipe.ui.screens.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.florianpal.beerrecipe.data.entity.Additive
import fr.florianpal.beerrecipe.data.entity.BeerStyle
import fr.florianpal.beerrecipe.data.entity.Hop
import fr.florianpal.beerrecipe.data.entity.Malt
import fr.florianpal.beerrecipe.data.entity.WaterProfile
import fr.florianpal.beerrecipe.data.entity.Yeast
import fr.florianpal.beerrecipe.data.repository.BrewRepository
import fr.florianpal.beerrecipe.ui.common.CatalogData
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CatalogViewModel(private val repository: BrewRepository) : ViewModel() {

    val catalog: StateFlow<CatalogData> = combine(
        repository.malts,
        repository.hops,
        repository.yeasts,
        repository.additives,
        repository.styles,
    ) { malts, hops, yeasts, additives, styles ->
        CatalogData(malts = malts, hops = hops, yeasts = yeasts, additives = additives, styles = styles)
    }.combine(repository.waterProfiles) { data, waters ->
        data.copy(waterProfiles = waters)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CatalogData())

    fun save(malt: Malt) = viewModelScope.launch { repository.saveMalt(malt) }
    fun delete(malt: Malt) = viewModelScope.launch { repository.deleteMalt(malt) }
    fun save(hop: Hop) = viewModelScope.launch { repository.saveHop(hop) }
    fun delete(hop: Hop) = viewModelScope.launch { repository.deleteHop(hop) }
    fun save(yeast: Yeast) = viewModelScope.launch { repository.saveYeast(yeast) }
    fun delete(yeast: Yeast) = viewModelScope.launch { repository.deleteYeast(yeast) }
    fun save(additive: Additive) = viewModelScope.launch { repository.saveAdditive(additive) }
    fun delete(additive: Additive) = viewModelScope.launch { repository.deleteAdditive(additive) }
    fun save(style: BeerStyle) = viewModelScope.launch { repository.saveStyle(style) }
    fun delete(style: BeerStyle) = viewModelScope.launch { repository.deleteStyle(style) }
    fun save(profile: WaterProfile) = viewModelScope.launch { repository.saveWaterProfile(profile) }
    fun delete(profile: WaterProfile) = viewModelScope.launch { repository.deleteWaterProfile(profile) }

    companion object {
        fun factory(repository: BrewRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                CatalogViewModel(repository) as T
        }
    }
}
