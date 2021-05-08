import androidx.compose.desktop.Window
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.flannaghan.cheetah.common.DesktopApplicationContext
import com.flannaghan.cheetah.common.DesktopSearchModel
import com.flannaghan.cheetah.common.gui.App

private val CONTEXT = DesktopApplicationContext()

fun main() = Window {
    val scope = rememberCoroutineScope()
    val searcher = remember { DesktopSearchModel(CONTEXT, scope) }
    App(searcher)
}