package inspections.ifstatement

import com.intellij.codeInspection.InspectionToolProvider
import downloader.Downloader.checkArchive
import model.ModelFacade

class IfStatementProvider : InspectionToolProvider {
    init {
        checkArchive()
        ModelFacade()
    }

    override fun getInspectionClasses(): Array<Class<IfStatementInspection>> {
        return arrayOf(IfStatementInspection::class.java)
    }

}