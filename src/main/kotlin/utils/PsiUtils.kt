package utils

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiModifierList
import com.intellij.psi.PsiType
import com.intellij.openapi.actionSystem.DataConstants
import com.intellij.ide.DataManager
import com.intellij.openapi.editor.Editor

object PsiUtils {
    /**
     * Extracts method signature and body, then concatenates them
     */
    fun getMethodBody(method: PsiMethod): String {
        val methodBody = method.body ?: return ""
        val space = " "
        val modifierList: PsiModifierList = method.modifierList
        val parameters = method.parameterList.parameters
        val methodSignature = StringBuilder(256)
        if (modifierList.hasModifierProperty(PsiModifier.PUBLIC)) {
            methodSignature.append(PsiModifier.PUBLIC).append(space)
        } else if (modifierList.hasModifierProperty(PsiModifier.PRIVATE)) {
            methodSignature.append(PsiModifier.PRIVATE).append(space)
        }
        if (modifierList.hasModifierProperty(PsiModifier.STATIC)) {
            methodSignature.append(PsiModifier.STATIC).append(space)
        }
        val returnType = method.returnType?.presentableText
        methodSignature.append(returnType).append(space).append(method.name).append('(')
        for (i in parameters.indices) {
            if (i != 0) {
                methodSignature.append(',')
            }
            val parameterName: String? = parameters[i].name
            val parameterType: PsiType = parameters[i].type
            val parameterTypeText = parameterType.presentableText
            methodSignature.append(parameterTypeText).append(space).append(parameterName)
        }
        methodSignature.append(')')
        return methodSignature.toString() + space + methodBody.text
    }

    fun caretInsideMethodBlock(method: PsiMethod): Boolean {
        val methodTextRange = method.textRange
        val editor = DataManager.getInstance().dataContext.getData(DataConstants.EDITOR) as Editor? ?: return true
        val caret = editor.caretModel.primaryCaret.offset
        return (caret > methodTextRange.startOffset) && (caret < methodTextRange.endOffset)
    }

    fun hasSuperMethod(method: PsiMethod): Boolean {
        return method.findSuperMethods().isNotEmpty()
    }

    fun executeWriteAction(project: Project, file: PsiFile, body: () -> Unit) {
        object : WriteCommandAction.Simple<Any>(project, file) {
            override fun run() {
                body()
            }
        }.execute()
    }

}