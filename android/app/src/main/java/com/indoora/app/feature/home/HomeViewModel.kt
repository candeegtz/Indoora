// HomeViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indoora.app.data.model.HomeRead
import com.indoora.app.data.repository.HomeRepository
import com.indoora.app.feature.auth.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _homeState = MutableStateFlow<UiState<HomeRead>>(UiState.Loading)
    val homeState: StateFlow<UiState<HomeRead>> = _homeState

    private val _refreshTrigger = MutableStateFlow(0)
    val refreshTrigger: StateFlow<Int> = _refreshTrigger

    fun loadHome(homeId: Int) {
        viewModelScope.launch {
            _homeState.value = UiState.Loading
            val result = homeRepository.getHome(homeId)
            _homeState.value = if (result.isSuccess) {
                UiState.Success(result.getOrNull()!!)
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }

    fun refreshHome() {
        _refreshTrigger.value += 1
    }
}