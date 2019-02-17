package logging

class StatsSender(private val filePathProvider: FilePathProvider,
                  private val requestService: RequestService) {

    fun sendStatsData(answer: String): Boolean {
        val url = filePathProvider.localhost
        val isSentSuccessfully = sendContent(url, answer)
        return (isSentSuccessfully)
    }

    private fun sendContent(url: String, str: String): Boolean {
        val data = requestService.post(url, str)
        if (data != null && data.statusIsOk()) {
            return true
        }
        return false
    }

}