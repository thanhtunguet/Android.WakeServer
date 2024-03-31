package info.thanhtunguet.wakeserver.models

data class DnsPayload(var name: String = "*", var content: String = "") {
    var type: String = "A"

    var proxied: Boolean = false

    var ttl = 1L
}
