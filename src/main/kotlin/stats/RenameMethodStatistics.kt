package stats

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "RenameMethodStatistics", storages = [(Storage("astrid_stats.xml"))])
class RenameMethodStatistics : PersistentStateComponent<RenameMethodState> {
    private var statsState: RenameMethodState = RenameMethodState()

    override fun loadState(state: RenameMethodState) {
        statsState = state
    }

    override fun getState(): RenameMethodState {
        return statsState
    }

    companion object {
        fun applyCount() {
            val stats = getInstance().statsState
            stats.applyRenameMethodCount()
        }

        fun ignoreCount() {
            val stats = getInstance().statsState
            stats.ignoreRenameMethodCount()
        }

        fun getInstance(): RenameMethodStatistics = ServiceManager.getService(RenameMethodStatistics::class.java)
    }
}

class RenameMethodState {

    var applied: Int = 0
    var ignored: Int = 0

    fun applyRenameMethodCount() {
        applied += 1
    }

    fun ignoreRenameMethodCount() {
        ignored += 1
    }

}