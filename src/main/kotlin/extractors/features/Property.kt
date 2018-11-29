package extractors.features

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.BinaryExpr
import com.github.javaparser.ast.expr.IntegerLiteralExpr
import com.github.javaparser.ast.expr.UnaryExpr
import com.github.javaparser.ast.type.ClassOrInterfaceType
import extractors.common.Common

class Property(node: Node, isLeaf: Boolean, isGenericParent: Boolean) {
    val rawType: String
    var type: String
    var name: String
    private var splitName: String
    private var operator: String
    private val numericalValues: List<String> = listOf("0", "1", "32", "64")
    private val primitiveType = "PrimitiveType"
    private val genericClassType = "GenericClass"
    private val num = "<NUM>"

    init {
        val nodeClass = node.javaClass
        type = nodeClass.simpleName
        rawType = type
        if (node is ClassOrInterfaceType && node.isBoxedType) {
            type = primitiveType
        }
        operator = ""
        when (node) {
            is BinaryExpr -> operator = node.operator.toString()
            is UnaryExpr -> operator = node.operator.toString()
            is AssignExpr -> operator = node.operator.toString()
        }
        if (operator.isNotEmpty()) {
            type += ":$operator"
        }

        var nameToSplit = node.toString()
        if (isGenericParent) {
            nameToSplit = (node as ClassOrInterfaceType).name
            if (isLeaf) {
                type = genericClassType
            }
        }
        val splitNameParts = Common.splitToSubtokens(nameToSplit)
        splitName = splitNameParts.joinToString(Common.INTERNAL_SEPARATOR)

        name = Common.normalizeName(node.toString(), Common.BLANK)
        when {
            name.length > Common.MAX_LABEL_LENGTH -> name = name.substring(0, Common.MAX_LABEL_LENGTH)
            node is ClassOrInterfaceType && node.isBoxedType -> name = node.toUnboxedType().toString()
        }

        if (Common.isMethod(node, type)) {
            splitName = Common.METHOD_NAME
            name = splitName
        }

        if (splitName.isEmpty()) {
            splitName = name
            if (node is IntegerLiteralExpr && !numericalValues.contains(splitName)) {
                splitName = num
            }
        }
    }
}
