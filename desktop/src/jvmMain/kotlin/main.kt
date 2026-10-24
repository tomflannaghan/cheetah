import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flannaghan.cheetah.common.DesktopPlatform
import com.flannaghan.cheetah.common.SearchViewModel
import com.flannaghan.cheetah.common.gui.App

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Cheetah") {
        App(viewModel { SearchViewModel(DesktopPlatform()) })
    }
}
