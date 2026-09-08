package web
import kotlinx.browser.window
data class QrTarget(
    val programId: Int,
    val stampId: Int,
    val qrCode: String,
)

object QrTargetParser {

    fun parse(): QrTarget? {
        val pathname = window.location.pathname

        val parts = pathname
            .trim('/')
            .split('/')

        if (parts.size != 3) {
            return null
        }

        if (parts[0] != "stamp") {
            return null
        }

        val programId = parts[1].toIntOrNull()
            ?: return null

        val stampId = parts[2].toIntOrNull()
            ?: return null

        val search = window.location.search
            .removePrefix("?")

        val qrCode = search
            .split("&")
            .mapNotNull {
                val pair = it.split("=", limit = 2)

                if (pair.size == 2 && pair[0] == "qr") {
                    pair[1]
                } else {
                    null
                }
            }
            .firstOrNull()
            ?: return null

        if (qrCode.isBlank()) {
            return null
        }


        println("QR pathname = ${window.location.pathname}")
        println("QR search = ${window.location.search}")
        println("QR parts = $parts")

        return QrTarget(
            programId = programId,
            stampId = stampId,
            qrCode = qrCode,
        )
    }
}