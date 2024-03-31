package info.thanhtunguet.wakeserver.repositories

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import info.thanhtunguet.wakeserver.BuildConfig
import info.thanhtunguet.wakeserver.models.DnsPayload
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType

class ClientRepository {
    companion object {
        private val cloudflareClient: HttpClient = HttpClient(Android) {
            install(Logging)
            defaultRequest {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "api.cloudflare.com"
                }
                header("Accept", ContentType.Application.Json)
                header("Content-Type", ContentType.Application.Json)
                if (BuildConfig.CF_EMAIL.isEmpty()) {
                    header("Authorization", "Bearer ${BuildConfig.CF_API_KEY}")
                } else {
                    header("X-Auth-Email", BuildConfig.CF_EMAIL)
                    header("X-Auth-Key", BuildConfig.CF_API_KEY)
                }
                contentType(ContentType.Application.Json)
            }
        }

        private val telegramClient: HttpClient = HttpClient(Android) {
            install(Logging)
            defaultRequest {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "api.cloudflare.com"

                }
                header("Accept", ContentType.Application.FormUrlEncoded)
                header("Content-Type", ContentType.Application.FormUrlEncoded)
            }
        }

        suspend fun sendChat(message: String): String {
            val response = telegramClient.post("/bot${BuildConfig.BOT_TOKEN}/sendMessage") {
                setBody("chat_id=${BuildConfig.CHAT_ID}&text=$message")
            }
            return response.bodyAsText()
        }

        suspend fun currentIpAddress(): String {
            val client = HttpClient(Android)
            val response = client.get("https://checkip.amazonaws.com") {
                ///
            }
            return response.bodyAsText().trim()
        }

        suspend fun getDNS(): String {
            val response = cloudflareClient.get(dnsUrl) {
                ///
            }
            val text = response.bodyAsText()
            val jsonObject = JsonParser.parseString(text).asJsonObject
            return jsonObject.get("result").asJsonObject.get("content").asString
        }

        suspend fun setDNS(ipAddress: String): String {
            val payload = DnsPayload("*", ipAddress)
            val response = cloudflareClient.put(dnsUrl) {
                setBody(GsonBuilder().create().toJson(payload))
            }
            return response.bodyAsText()
        }

        private val dnsUrl: String
            get() = "/client/v4/zones/${BuildConfig.ZONE_ID}/dns_records/${BuildConfig.RECORD_ID}"
    }
}
