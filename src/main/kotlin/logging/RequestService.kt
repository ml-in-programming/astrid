package logging

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType
import org.apache.http.util.EntityUtils
import java.io.IOException

class RequestService {
    private val LOG = Logger.getInstance(RequestService::class.java)

    companion object {
        fun getInstance(): RequestService = ServiceManager.getService(RequestService::class.java)
    }

    fun post(url: String, str: String): ResponseData? {
        return try {
            val response = Request.Post(url).bodyString(str, ContentType.TEXT_PLAIN).execute()
            val httpResponse = response.returnResponse()
            val text = EntityUtils.toString(httpResponse.entity)
            ResponseData(httpResponse.statusLine.statusCode, text)
        } catch (e: IOException) {
            LOG.debug(e)
            null
        }
    }

    fun get(url: String): ResponseData? {
        return try {
            var data: ResponseData? = null
            Request.Get(url).execute().handleResponse {
                val text = EntityUtils.toString(it.entity)
                data = ResponseData(it.statusLine.statusCode, text)
            }
            data
        } catch (e: IOException) {
            LOG.debug(e)
            null
        }
    }

    data class ResponseData(val code: Int, val text: String = "") {
        fun statusIsOk() = code in 200..299
    }
}