package inspections

import com.intellij.psi.PsiMethod
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.SmartPointerManager

class SuggestionsStorage {
    companion object {
        private var map: HashMap<SmartPsiElementPointer<PsiMethod>, Pair<List<String>, Boolean>> = HashMap()

        fun getSuggestions(method: PsiMethod): List<String> {
            val pointer = SmartPointerManager.getInstance(method.project).createSmartPsiElementPointer(method)
            val list = map.get(pointer) ?: Pair(emptyList(), false)
            return list.first
        }

        fun put(method: PsiMethod, list: List<String>) {
            val pointer = SmartPointerManager.getInstance(method.project).createSmartPsiElementPointer(method)
            if (map.contains(pointer)) {
                map.replace(pointer, Pair(list, false))
            } else {
                map.put(pointer, Pair(list, false))
            }
        }

        fun contains(method: PsiMethod): Boolean {
            val pointer = SmartPointerManager.getInstance(method.project).createSmartPsiElementPointer(method)
            return map.contains(pointer)
        }

        fun needRecalculate(method: PsiMethod): Boolean {
            val pointer = SmartPointerManager.getInstance(method.project).createSmartPsiElementPointer(method)
            val pair = map.get(pointer) ?: return false
            return pair.second
        }

        fun recalculateLater(method: PsiMethod) {
            val pointer = SmartPointerManager.getInstance(method.project).createSmartPsiElementPointer(method)
            if (map.containsKey(pointer)) {
                val pair = map.get(pointer)
                if (pair != null) {
                    map.replace(pointer, pair.copy(pair.first, true))
                }
            }
            return
        }
    }
}