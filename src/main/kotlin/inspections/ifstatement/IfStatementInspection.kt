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
import model.ModelFacade
import org.jetbrains.uast.getContainingClass

class IfStatementInspection : AbstractBaseJavaLocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return IfStatementVisitor(holder)
    }

    private class IfStatementVisitor(private val holder: ProblemsHolder) : JavaElementVisitor() {
        override fun visitIfStatement(statement: PsiIfStatement?) {
            if (statement == null) return
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
            val temporarySignature = "public boolean f() {"
            val condition = descriptor.psiElement.text + ";"
            val methodName = ModelFacade().getSuggestions("$temporarySignature$condition\n }").get(0)
            val methodBody = "public boolean $methodName() { return $condition }"
            val facade = JavaPsiFacade.getInstance(project)
            val factory = facade.elementFactory
            val addNewMethod = Runnable {
                val newPsiMethod = factory.createMethodFromText(methodBody, null)
                descriptor.psiElement.getContainingClass()?.add(newPsiMethod)
                val editor = DataManager.getInstance().dataContext.getData(DataConstants.EDITOR) as Editor?
                if (editor != null) {
                    PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.document)
                }
            }
            WriteCommandAction.runWriteCommandAction(project, addNewMethod)
            if (descriptor.psiElement is PsiExpression) {
                val editor = DataManager.getInstance().dataContext.getData(DataConstants.EDITOR) as Editor?
                descriptor.psiElement.delete()
                EditorModificationUtil.insertStringAtCaret(editor, "$methodName()", true)
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