package inspections

import com.intellij.psi.PsiMethod
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.SmartPointerManager

class SuggestionsStorage {
    companion object {
        private var map: HashMap<SmartPsiElementPointer<PsiMethod>, Suggestion> = HashMap()

        fun getSuggestions(method: PsiMethod): Suggestion? {
            val pointer = SmartPointerManager.getInstance(method.project).createSmartPsiElementPointer(method)
            val suggestion = map.get(pointer)
            if (suggestion != null) {
                return suggestion
            }
            return null
        }

        fun put(method: PsiMethod, suggestion: Suggestion) {
            val pointer = SmartPointerManager.getInstance(method.project).createSmartPsiElementPointer(method)
            if (map.contains(pointer)) {
                map.replace(pointer, suggestion)
            } else {
                map.put(pointer, suggestion)
            }
        }

        fun contains(method: PsiMethod): Boolean {
            val pointer = SmartPointerManager.getInstance(method.project).createSmartPsiElementPointer(method)
            return map.contains(pointer)
        }

        fun needRecalculate(method: PsiMethod): Boolean {
            val pointer = SmartPointerManager.getInstance(method.project).createSmartPsiElementPointer(method)
            val suggestion = map.get(pointer) ?: return false
            return suggestion.needRecalculate
        }

        fun recalculateLater(method: PsiMethod) {
            val pointer = SmartPointerManager.getInstance(method.project).createSmartPsiElementPointer(method)
            if (map.containsKey(pointer)) {
                val suggestion = map.get(pointer)
                if (suggestion != null) {
                    suggestion.setRecalculate()
                    map.replace(pointer, suggestion)
                }
            }
            return
        }

        fun ignore(method: PsiMethod): Boolean {
            val pointer = SmartPointerManager.getInstance(method.project).createSmartPsiElementPointer(method)
            val suggestion = map.get(pointer) ?: return false
            return suggestion.ignore
        }

        fun setIgnore(method: PsiMethod) {
            val pointer = SmartPointerManager.getInstance(method.project).createSmartPsiElementPointer(method)
            if (map.containsKey(pointer)) {
                val suggestion = map.get(pointer)
                if (suggestion != null) {
                    suggestion.setIgnore()
                    map.replace(pointer, suggestion)
                }
            }
            return
        }
    }
}