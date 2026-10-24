import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.flannaghan.cheetah.common.DesktopApplicationContext
import com.flannaghan.cheetah.common.DesktopSearchModel
import com.flannaghan.cheetah.common.gui.App

private val CONTEXT = DesktopApplicationContext()

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        val scope = rememberCoroutineScope()
        val searcher = remember { DesktopSearchModel(CONTEXT, scope) }
        App(searcher)
    }
}
