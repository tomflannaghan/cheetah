import com.flannaghan.cheetah.common.App
import androidx.compose.desktop.Window
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.flannaghan.cheetah.common.DesktopApplicationContext
import com.flannaghan.cheetah.common.Searcher
import com.flannaghan.cheetah.common.wordSources
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

private val CONTEXT = DesktopApplicationContext()
private val SEARCHER = Searcher(CoroutineScope(Dispatchers.Main), Dispatchers.IO, wordSources(CONTEXT)[0])

fun main() = Window {
    App(CONTEXT, SEARCHER)
}