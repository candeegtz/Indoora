import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.indoora.app.data.repository.HomeRepository
import com.indoora.app.feature.deviceconfig.DeviceConfigViewModel

class DeviceConfigViewModelFactory(
    private val homeRepository: HomeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeviceConfigViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeviceConfigViewModel(homeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}