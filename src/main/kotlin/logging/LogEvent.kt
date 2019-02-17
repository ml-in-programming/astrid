package logging

import com.google.gson.Gson
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.application.PermanentInstallationID
import java.util.UUID;

class LogEvent(@Transient val statsData: Any) {
    @Transient
    private val sessionUid = UUID.randomUUID().toString()
    @Transient
    val recorderId = "astrid-plugin"
    @Transient
    val timestamp = System.currentTimeMillis()
    @Transient
    val build: String = ApplicationInfo.getInstance().build.asStringWithoutProductCodeAndSnapshot()
    @Transient
    val actionType: String = "rename_method"
    @Transient
    val product: String = ApplicationNamesInfo.getInstance().productName
    @Transient
    val userUid: String = PermanentInstallationID.get()
    @Transient
    val bucket = "-1"

    override fun toString(): String {
        return "$userUid\t$recorderId\t$product\t$build\t$timestamp\t$sessionUid\t$bucket\t$actionType\t${toJsonFormat(statsData)}\t"
    }

    private fun toJsonFormat(data: Any): String {
        val gson = Gson()
        return gson.toJson(data)
    }
}