package actions

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNamedElement
import com.intellij.refactoring.openapi.impl.RefactoringFactoryImpl

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
        val refactoringFactory = RefactoringFactoryImpl.getInstance(editor.project)
        val rename = refactoringFactory.createRename(findNamedElement(elementAt), selectedValue)
        val usages = rename.findUsages()
        rename.doRefactoring(usages)
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
