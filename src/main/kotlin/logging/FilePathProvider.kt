package logging

import com.intellij.openapi.components.ServiceManager

class FilePathProvider {
    companion object {
        fun getInstance(): FilePathProvider = ServiceManager.getService(FilePathProvider::class.java)
    }

    val localhost = "http://localhost:8080"
}
