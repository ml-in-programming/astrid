package utils

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiModifierList
import com.intellij.psi.PsiType

class PsiUtils {
    companion object {
        /**
         * Extracts method signature and body, then concatenates them
         */
        fun getMethodBody(method: PsiMethod): String {
            val methodBody = method.body ?: return ""
            val space = " "
            val modifierList: PsiModifierList = method.modifierList
            val parameterList = method.parameterList
            val parameters = parameterList.parameters
            val methodName = method.name
            val methodSignature = StringBuilder(256)
            if (modifierList.hasModifierProperty(PsiModifier.PUBLIC)) {
                methodSignature.append(PsiModifier.PUBLIC)
                methodSignature.append(space)
            } else if (modifierList.hasModifierProperty(PsiModifier.PRIVATE)) {
                methodSignature.append(PsiModifier.PRIVATE)
                methodSignature.append(space)
            }
            if (modifierList.hasModifierProperty(PsiModifier.STATIC)) {
                methodSignature.append(PsiModifier.STATIC)
                methodSignature.append(space)
            }
            val returnType = method.returnType?.presentableText
            methodSignature.append(returnType)
            methodSignature.append(space)
            methodSignature.append(methodName)
            methodSignature.append('(')
            for (i in parameters.indices) {
                if (i != 0) {
                    methodSignature.append(',')
                }
                val parameterName: String? = parameters[i].name
                val parameterType: PsiType = parameters[i].type
                val parameterTypeText = parameterType.presentableText
                methodSignature.append(parameterTypeText)
                methodSignature.append(space)
                methodSignature.append(parameterName)
            }
            methodSignature.append(')')
            return methodSignature.toString() + space + methodBody.text
        }

        fun executeWriteAction(project: Project, file: PsiFile, body: () -> Unit) {
            object : WriteCommandAction.Simple<Any>(project, file) {
                override fun run() {
                    body()
                }
            }.execute()
        }
    }
}