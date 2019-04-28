package utils

import org.apache.commons.codec.binary.Base64
import java.io.InputStream
import java.io.ObjectInputStream
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    private const val initVector = "RandomInitVector"
    private const val key = "GitHubErrorToken"

    private fun encryptAES256String(value: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.ENCRYPT_MODE,
                SecretKeySpec(key.toByteArray(charset("UTF-8")), "AES"),
                IvParameterSpec(initVector.toByteArray(charset("UTF-8"))))
        return Base64.encodeBase64String(cipher.doFinal(value.toByteArray()))
    }

    fun decryptAES256String(input: InputStream): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.DECRYPT_MODE,
                SecretKeySpec(key.toByteArray(charset("UTF-8")), "AES"),
                IvParameterSpec(initVector.toByteArray(charset("UTF-8"))))
        val original = cipher.doFinal(Base64.decodeBase64(ObjectInputStream(input).readObject() as String))
        return String(original)
    }
}
