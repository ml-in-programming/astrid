package reporting

import com.intellij.diagnostic.ReportMessages
import com.intellij.ide.DataManager
import com.intellij.idea.IdeaLogger
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.application.ex.ApplicationInfoEx
import com.intellij.openapi.diagnostic.ErrorReportSubmitter
import com.intellij.openapi.diagnostic.IdeaLoggingEvent
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.util.Consumer
import java.awt.Component
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import utils.MessageBundle

class GitHubErrorReporter : ErrorReportSubmitter() {
    override fun submit(events: Array<IdeaLoggingEvent>, additionalInfo: String?,
                        parentComponent: Component, consumer: Consumer<SubmittedReportInfo>): Boolean {
        return doSubmit(parentComponent, consumer, events[0].throwable, IdeaLogger.ourLastActionId.orEmpty(), additionalInfo)
    }

    private fun doSubmit(parentComponent: Component,
                         callback: Consumer<SubmittedReportInfo>,
                         error: Throwable,
                         lastAction: String,
                         description: String?): Boolean {
        val dataContext = DataManager.getInstance().getDataContext(parentComponent)
        val pluginName = "astrid"
        val pluginVersion = "1.0"

        val errorReportInformation = ErrorReportInformation(error, lastAction,
                pluginName,
                pluginVersion,
                description,
                ApplicationInfo.getInstance() as ApplicationInfoEx,
                ApplicationNamesInfo.getInstance())

        val project = CommonDataKeys.PROJECT.getData(dataContext)
        val notifyingCallback = CallbackWithNotification(callback, project)

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, MessageBundle.message("report.error.progress.dialog.text"), true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                notifyingCallback.consume(AnonymousFeedback.sendFeedback(errorReportInformation))
            }
        })
        return true
    }

    override fun getReportActionText(): String =
            MessageBundle.message("report.error.to.plugin.vendor")

    internal class CallbackWithNotification(private val myOriginalConsumer: Consumer<SubmittedReportInfo>, private val myProject: Project?) : Consumer<SubmittedReportInfo> {

        override fun consume(reportInfo: SubmittedReportInfo) {
            myOriginalConsumer.consume(reportInfo)

            if (reportInfo.status == SubmittedReportInfo.SubmissionStatus.FAILED) {
                ReportMessages.GROUP.createNotification(
                        ReportMessages.ERROR_REPORT,
                        reportInfo.linkText,
                        NotificationType.ERROR,
                        NotificationListener.URL_OPENING_LISTENER).setImportant(false).notify(myProject)
            } else {
                ReportMessages.GROUP.createNotification(
                        ReportMessages.ERROR_REPORT,
                        reportInfo.linkText,
                        NotificationType.INFORMATION,
                        NotificationListener.URL_OPENING_LISTENER).setImportant(false).notify(myProject)
            }
        }
    }
}
