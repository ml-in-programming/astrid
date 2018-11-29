package extractors.common

import java.util.ArrayList
import com.github.javaparser.ast.Node

class MethodContent(val leaves: ArrayList<Node>, val name: String, val length: Long)