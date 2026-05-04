package com.underthykilt.poketypes.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.underthykilt.poketypes.data.PokemonType
import com.underthykilt.poketypes.data.RoomTypeStatRepository
import com.underthykilt.poketypes.data.TypeAccuracyRow
import com.underthykilt.poketypes.data.TypeStatDatabase
import com.underthykilt.poketypes.data.TypeStatRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class TypeStat(val type: PokemonType, val correct: Int, val total: Int) {
    val percent: Int get() = if (total == 0) 0 else (correct * 100 / total)
}

data class StatsUiState(
    val attackingStats: List<TypeStat> = emptyList(),
    val defendingStats: List<TypeStat> = emptyList(),
)

class StatsViewModel(
    application: Application,
    private val repo: TypeStatRepository,
) : AndroidViewModel(application) {

    val state: StateFlow<StatsUiState> = combine(
        repo.getAttackingAccuracy(),
        repo.getDefendingAccuracy(),
    ) { attacking, defending ->
        StatsUiState(
            attackingStats = attacking.toTypeStats(),
            defendingStats = defending.toTypeStats(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                StatsViewModel(
                    application,
                    RoomTypeStatRepository(TypeStatDatabase.get(application).typeStatDao()),
                )
            }
        }
    }
}

private fun List<TypeAccuracyRow>.toTypeStats(): List<TypeStat> =
    mapNotNull { row ->
        PokemonType.entries.find { it.name == row.typeName }
            ?.let { TypeStat(it, row.correct, row.total) }
    }.sortedBy { it.percent }
