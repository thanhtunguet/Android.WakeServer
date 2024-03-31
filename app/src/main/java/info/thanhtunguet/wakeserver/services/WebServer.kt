package info.thanhtunguet.wakeserver.services

import android.content.Context
import fi.iki.elonen.NanoHTTPD
import info.thanhtunguet.wakeserver.BuildConfig
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLServerSocketFactory

class WebServer(private val context: Context, port: Int) : NanoHTTPD(port) {
    init {
        makeSecure(loadKeystore(context), null)
    }

    @Throws(IOException::class)
    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        return when (uri) {
            "/api/pc/status" -> {
                val isOnline =
                    isServerOnline() // Assuming HTTP port for simplicity
                newFixedLengthResponse(
                    Response.Status.OK,
                    "text/plain",
                    if (isOnline) "ONLINE" else "OFFLINE"
                )
            }

            "/api/pc/power-on" -> {
                val result =
                    sendMagicPacket(BuildConfig.PC_MAC) // Replace with the actual MAC address
                newFixedLengthResponse(
                    Response.Status.OK,
                    "text/plain",
                    "Log from the server: $result"
                )
            }

            else -> newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found")
        }
    }

    private fun loadKeystore(
        context: Context,
    ): SSLServerSocketFactory {
        // Use "PKCS12" instead of KeyStore.getDefaultType() to specify the type
        val keystore = KeyStore.getInstance("PKCS12")
        context.assets.open(BuildConfig.CERTIFICATE_FILENAME).use { keystoreInputStream ->
            keystore.load(keystoreInputStream, BuildConfig.CERTIFICATE_PASSWORD.toCharArray())
        }

        val keyManagerFactory =
            KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        keyManagerFactory.init(keystore, BuildConfig.CERTIFICATE_PASSWORD.toCharArray())

        val sslContext = javax.net.ssl.SSLContext.getInstance("TLS")
        sslContext.init(keyManagerFactory.keyManagers, null, null)
        return sslContext.serverSocketFactory
    }


    private fun sendMagicPacket(macAddress: String): String {
        val macBytes = getMacBytes(macAddress)

        val bytes = ByteArray(6 + 16 * macBytes.size).apply {
            for (i in 0..5) {
                this[i] = 0xff.toByte()
            }
            for (i in 6 until this.size step macBytes.size) {
                macBytes.forEachIndexed { index, value ->
                    this[i + index] = value
                }
            }
        }

        try {
            DatagramSocket().use { socket ->
                val broadcastAddress = InetAddress.getByName("255.255.255.255")
                socket.broadcast = true
                val packet = DatagramPacket(bytes, bytes.size, broadcastAddress, 9)
                socket.send(packet)
                return "Magic packet sent successfully"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error sending magic packet: ${e.message}"
        }
    }

    private fun getMacBytes(macStr: String): ByteArray {
        val bytes = ByteArray(6)
        val hex = macStr.split("(\\:|\\-)".toRegex()).toTypedArray()
        if (hex.size != 6) {
            throw IllegalArgumentException("Invalid MAC address")
        }
        try {
            for (i in hex.indices) {
                bytes[i] = Integer.parseInt(hex[i], 16).toByte()
            }
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Invalid hex digit in MAC address")
        }
        return bytes
    }


    private fun isServerOnline(): Boolean {
        Socket().use { socket ->
            try {
                socket.connect(
                    InetSocketAddress(BuildConfig.PC_IP, BuildConfig.PC_PORT),
                    BuildConfig.PC_TIMEOUT
                )
                return true // Connection successful, server is online
            } catch (e: IOException) {
                // Connection failed, server is offline or unreachable
                return false
            }
        }
    }
}
