# Example code for an Amazon SNS endpoint Java servlet

#Reference Links
[AWS SNS](https://docs.aws.amazon.com/sns/latest/dg/sns-example-code-endpoint-java-servlet.html)

[TBC Developer](https://try.thecouponbureau.org/developer/getting_started)

# Java
1. Install JDK 8
1. Install Tomcat 8
1. Install Intellij IDEA
1. Open project folder in intelliJ
1. Create Tomcat Configuration
1. Set env variables "PRIMARY_CALLBACK_SIGNATURE" and "SECONDARY_CALLBACK_SIGNATURE"   
1. Run application
1. Complete "Common Steps" below

# Node
1. Set env variables "PRIMARY_CALLBACK_SIGNATURE" and "SECONDARY_CALLBACK_SIGNATURE" in ".env" file
1. Start node with index.js `node index.js`
1. Complete "Common Steps" below

# Common Steps
1. Install ngrok
1. Run ngrok and get https url
1. Create SNS topic and subscribe with https endpoint (sample ngrok url : https://ff3014f7ec5d.ngrok.io/SNSServlet)
1. It should be confirmed by the running app.
1. Send some SNS message which should be received by the running app.
1. Ensure that "TCB Signature verified" is printed.