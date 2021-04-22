import androidx.compose.desktop.Window
import com.flannaghan.cheetah.common.DesktopApplicationContext
import com.flannaghan.cheetah.common.DesktopSearchModel
import com.flannaghan.cheetah.common.db.DatabaseDriverFactory
import com.flannaghan.cheetah.common.gui.App
import com.flannaghan.cheetah.common.wordSources

private val CONTEXT = DesktopApplicationContext()
private val SEARCHER = DesktopSearchModel()

fun main() = Window {
    DatabaseDriverFactory().doStuff()
    SEARCHER.wordSources = wordSources(CONTEXT)
    App(SEARCHER)
}