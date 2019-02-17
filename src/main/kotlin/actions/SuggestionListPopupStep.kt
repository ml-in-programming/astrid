package actions

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.openapi.impl.RefactoringFactoryImpl
import inspections.SuggestionsStorage
import logging.FilePathProvider
import logging.LogEvent
import logging.RequestService
import logging.StatsSender
import stats.RenameMethodStatistics

class SuggestionListPopupStep(
        aTitle: String, aValues: List<String>, private var editor: Editor, private val psiFile: PsiFile
) : BaseListPopupStep<String>(aTitle, aValues) {

    private var selectedMethodName: String? = null

    override fun onChosen(selectedValue: String?, finalChoice: Boolean): PopupStep<*>? {
        selectedMethodName = selectedValue
        return super.onChosen(selectedValue, finalChoice)
    }

    private fun doRenameMethodRefactoring(selectedValue: String) {
        val elementAt = psiFile.findElementAt(editor.caretModel.offset) ?: return
        if (selectedMethodName == "Suppress on this method") {
            val psiMethod = PsiTreeUtil.getParentOfType(elementAt, PsiMethod::class.java) ?: return
            SuggestionsStorage.setIgnore(psiMethod)
            RenameMethodStatistics.ignoreCount()
            return
        }
        val refactoringFactory = RefactoringFactoryImpl.getInstance(editor.project)
        val rename = refactoringFactory.createRename(findNamedElement(elementAt), selectedValue)
        val usages = rename.findUsages()
        RenameMethodStatistics.applyCount()
        rename.doRefactoring(usages)
/*        StatsSender(FilePathProvider(),
                RequestService()).sendStatsData(LogEvent(RenameMethodStatistics.getInstance().state).toString())*/
    }

    private fun findNamedElement(element: PsiElement): PsiElement {
        when (element) {
            is PsiNamedElement -> return element
            else -> return findNamedElement(element.parent)
        }
    }

    override fun getFinalRunnable(): Runnable? {
        return Runnable { doRenameMethodRefactoring(selectedMethodName as String) }
    }
}
