package inspections.ifstatement

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.DataConstants
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaElementVisitor
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiExpression
import com.intellij.psi.PsiIfStatement
import com.intellij.psi.PsiMethod
import downloader.Downloader
import model.ModelFacade
import org.jetbrains.uast.getContainingClass
import java.nio.file.Files

class IfStatementInspection : AbstractBaseJavaLocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return IfStatementVisitor(holder)
    }

    private class IfStatementVisitor(private val holder: ProblemsHolder) : JavaElementVisitor() {
        override fun visitIfStatement(statement: PsiIfStatement?) {
            if (statement == null) return
            if (!Files.exists(Downloader.getModelPath())) return
            val condition = statement.condition ?: return
            // TODO: Implement more meaningful conditions
            if (condition.textLength > 100) {
                holder.registerProblem(condition, "Condition is too complex", ProblemHighlightType.WEAK_WARNING,
                        ExtractIfStatementToMethod())
            }
            super.visitIfStatement(statement)
        }

    }

    class ExtractIfStatementToMethod : LocalQuickFix {
        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            if (descriptor.psiElement == null) return
            val currentMethod = descriptor.psiElement.parent.parent.parent as PsiMethod
            val parametersArray = currentMethod.parameterList.parameters
            var newMethodParameters = ""
            var newMethodCallArgs = ""
            val condition: String = descriptor.psiElement.text ?: ""
            for (parameter in parametersArray) {
                val parameterName: String = parameter.name ?: return
                if (condition.contains(parameterName)) {
                    if (newMethodParameters.isNotEmpty()) {
                        newMethodParameters += ", "
                        newMethodCallArgs += ", "
                    }
                    newMethodParameters += parameter.type.presentableText + " " + parameterName
                    newMethodCallArgs += parameterName
                }
            }
            val temporarySignature = "public boolean f() {"
            val newMethodBody = "$condition;"
            val classMethodNames = arrayListOf<String>()
            descriptor.psiElement.getContainingClass()?.methods?.forEach { m -> classMethodNames.add(m.name) }
            val methodNameSuggestions = ArrayList(ModelFacade().getSuggestions("$temporarySignature$newMethodBody\n }"))
            // Exclude name if class contains method with the same name
            methodNameSuggestions.removeAll(classMethodNames)
            val newMethodName = methodNameSuggestions.get(0)
            val newMethodText = "private boolean $newMethodName($newMethodParameters) { return $newMethodBody\n }"

            WriteCommandAction.runWriteCommandAction(project, addNewMethod(newMethodText, descriptor, project))
            if (descriptor.psiElement is PsiExpression) {
                val editor = DataManager.getInstance().dataContext.getData(DataConstants.EDITOR) as Editor?
                descriptor.psiElement.delete()
                EditorModificationUtil.insertStringAtCaret(editor, "$newMethodName($newMethodCallArgs)", true)
            }
        }

        private fun addNewMethod(newMethodText: String, descriptor: ProblemDescriptor, project: Project): Runnable {
            return Runnable {
                val facade = JavaPsiFacade.getInstance(project)
                val factory = facade.elementFactory
                val newPsiMethod = factory.createMethodFromText(newMethodText, null)
                descriptor.psiElement.getContainingClass()?.add(newPsiMethod)
                val editor = DataManager.getInstance().dataContext.getData(DataConstants.EDITOR) as Editor?
                if (editor != null) {
                    PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.document)
                }
            }
        }

        override fun getFamilyName(): String {
            return "Extract condition to method"
        }

    }

    override fun getDisplayName(): String {
        return "Extract condition to method"
    }

    override fun getGroupDisplayName(): String {
        return "Plugin Astrid"
    }

    override fun getShortName(): String {
        return "IfStatementInspection"
    }
}