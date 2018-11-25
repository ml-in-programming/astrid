package inspections

import com.intellij.codeInspection.InspectionToolProvider
import downloader.Downloader.checkArchive

class MethodNamesProvider : InspectionToolProvider {
    init {
        checkArchive()
    }

    override fun getInspectionClasses(): Array<Class<*>> {
        return arrayOf(MethodNamesInspection::class.java)
    }

}