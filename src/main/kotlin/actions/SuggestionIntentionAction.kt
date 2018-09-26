package actions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.psi.util.PsiTreeUtil

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import model.ModelFacade
import com.intellij.openapi.ui.popup.JBPopupFactory

class SuggestionIntentionAction : IntentionAction {

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null) return
        val offset: Int = editor.caretModel.offset
        val psiMethod = PsiTreeUtil.getParentOfType(file.findElementAt(offset), PsiMethod::class.java) ?: return
        val methodBody = psiMethod.body ?: return
        ModelFacade().generateSuggestions(methodBody.text)
        val suggestionsList = ModelFacade().getSuggestions()
        val listPopup = JBPopupFactory.getInstance().createListPopup(
                SuggestionListPopupStep("Suggestions", suggestionsList, editor, file)
        )
        listPopup.showInBestPositionFor(editor)
    }

    override fun getText(): String {
        return "Generate suggestions"
    }

    override fun getFamilyName(): String {
        return text
    }

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        //TODO("Need to implement conditions")
        return true
    }

    override fun startInWriteAction(): Boolean {
        return true
    }

}