package reporting

import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.application.PermanentInstallationID
import com.intellij.openapi.application.ex.ApplicationInfoEx
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.ExceptionUtil
import enums.ReportInformationType
import enums.ReportInformationType.*
import java.util.*

/**
 * Collects information about the running IDEA and the error
 */
class ErrorReportInformation(
        throwable: Throwable,
        lastAction: String,
        pluginName: String,
        pluginVersion: String,
        description: String?,
        appInfo: ApplicationInfoEx,
        namesInfo: ApplicationNamesInfo) {

    private val information = EnumMap<ReportInformationType, String>(ReportInformationType::class.java)

    init {
        information[PLUGIN_NAME] = pluginName
        information[PLUGIN_VERSION] = pluginVersion
        information[PLUGIN_NAME] = "astrid"
        information[OS_NAME] = SystemInfo.OS_NAME
        information[JAVA_VERSION] = SystemInfo.JAVA_VERSION
        information[JAVA_VM_VENDOR] = SystemInfo.JAVA_VENDOR
        information[APP_NAME] = namesInfo.productName
        information[APP_FULL_NAME] = namesInfo.fullProductName
        information[APP_VERSION_NAME] = appInfo.versionName
        information[IS_EAP] = java.lang.Boolean.toString(appInfo.isEAP)
        information[APP_BUILD] = appInfo.build.asString()
        information[APP_VERSION] = appInfo.fullVersion
        information[PERMANENT_INSTALLATION_ID] = PermanentInstallationID.get()
        information[LAST_ACTION] = lastAction
        information[ERROR_MESSAGE] = throwable.message
        information[ERROR_STACKTRACE] = ExceptionUtil.getThrowableText(throwable)
        information[ERROR_HASH] = getHashOfThrowable(throwable)
        information[ERROR_DESCRIPTION] = description ?: ""
    }

    operator fun get(informationType: ReportInformationType): String? {
        return information[informationType]
    }

    private fun getHashOfThrowable(throwable: Throwable): String? {
        return java.lang.Long.toHexString(Integer.toUnsignedLong(Arrays.hashCode(throwable.stackTrace)))
    }
}