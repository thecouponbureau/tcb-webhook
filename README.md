# Sample TCB Webhook endpoint in JAVA & NodeJS

[TBC Developer](https://try.thecouponbureau.org/developer/getting_started)

# Java
1. Install JDK 8
1. Install Tomcat 8
1. Install Intellij IDEA
1. Open project folder in intelliJ
1. Create Tomcat Configuration
1. Set env variables "PRIMARY_CALLBACK_SECRET" and "SECONDARY_CALLBACK_SECRET"   
1. Run application
1. Complete "Common Steps" below

# Node
1. Set env variables "PRIMARY_CALLBACK_SECRET" and "SECONDARY_CALLBACK_SECRET" in ".env" file
1. Start node with index.js `node index.js`
1. Complete "Common Steps" below

# Common Steps
1. Install ngrok
1. Run ngrok and get https url
1. Put the https endpoint as TCB Webhook
1. It should be confirmed automatically and will be ready to receive webhook notiifcation.