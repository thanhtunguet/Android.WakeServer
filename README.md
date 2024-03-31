WakeServer
==========

Turns your old Android phone into a web server that provides APIs:

- Wake a PC on LAN
- Check PC status
- Dynamic DNS using Cloudflare DNS

## Configuration

Add this to `local.properties`:

```properties
# Cloudflare
CF_API_KEY="<Cloudflare API Token>"
CF_EMAIL="<Cloudflare Email"
ZONE_ID="<Cloudflare DNS Zone ID"
RECORD_ID="<Cloudflare DNS Record ID>"
# Telegram Bot
BOT_TOKEN="<Your Telegram Bot Token>"
CHAT_ID="<Your Telegram Chat ID to receives notifications>"
# Web server
WEB_PORT=8443
PC_MAC="<PC ethernet MAC>"
PC_IP="<PC static IP address>"
# If your PC is Windows, use port 3389, otherwise use port 22 for SSH
PC_PORT=3389
PC_TIMEOUT=1500
CERTIFICATE_FILENAME="<SSL certificate in p12 format"
CERTIFICATE_PASSWORD="<SSL p12 password>"
```

## Build the app

```shell
./gradlew assembleRelease
```

## Android configuration

You should set your Android device to use static IP. This will help you can NAT the port of web server running on Android to Internet.
