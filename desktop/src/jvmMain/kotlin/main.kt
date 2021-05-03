import androidx.compose.desktop.Window
import com.flannaghan.cheetah.common.DesktopApplicationContext
import com.flannaghan.cheetah.common.DesktopSearchModel
import com.flannaghan.cheetah.common.gui.App

private val CONTEXT = DesktopApplicationContext()
private val SEARCHER = DesktopSearchModel(CONTEXT)

fun main() = Window {
    App(SEARCHER)
}