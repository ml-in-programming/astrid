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

    override fun onChosen(selectedValue: String?, finalChoice: Boolean): PopupStep<String> {
        renameMethodRefactoring(selectedValue as String)
        return super.onChosen(selectedValue, finalChoice) as PopupStep<String>
    }

    private fun renameMethodRefactoring(selectedValue: String) {
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
}
