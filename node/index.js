const https = require('https');
const express = require('express');
const app = express();

app.use(express.json({type: '*/*'}));

app.post('/', (req, res) => {
    const messageType = req.headers['x-amz-sns-message-type'];
    console.log(messageType);

    if(messageType === "Notification") {
        console.log(">>Notification received from topic " + req.body.TopicArn);
        console.log("Subject: " + req.body.Subject);
        console.log("Message: " + req.body.Message);

        let bufferObj = Buffer.from(req.body.Message.data, "base64");
        let decodedData = bufferObj.toString("utf8");

        const jsonObject = JSON.parse(decodedData);
        let primary_signature_calculated = "";
        let secondary_signature_calculated = "";
        if(action.equals("report")) {
            primary_signature_calculated = getMd5(jsonObject.reportURL + jsonObject.action + jsonObject.job_id + "secret-primary");
            secondary_signature_calculated = getMd5(jsonObject.reportURL + jsonObject.action + jsonObject.job_id + "secret-secondary");
        } else {
            primary_signature_calculated = getMd5(jsonObject.gs1 + jsonObject.action + "secret-primary");
            secondary_signature_calculated = getMd5(jsonObject.gs1 + jsonObject.action + "secret-secondary");
        }
        if(primary_signature_calculated === jsonObject.primary_signature
        || secondary_signature_calculated === jsonObject.secondary_signature) {
            console.log("TCB Signature verified");
        } else {
            console.log("TCB Signature could not be verified");
        }
    } else if(messageType === "SubscriptionConfirmation") {
        const options = {
            method: 'GET'
        };
        const req2 = https.request(req.body.SubscribeURL, options, res => {
            res.on('data', data => {
                console.log("Return value:" + data);
            })
        });
        req2.on('error', error => {
            console.error(error)
        });
        req2.end();
        console.log(">>Subscription confirmation (" + req.body.SubscribeURL + ")");
    } else if(messageType === "UnsubscribeConfirmation") {
        console.log(">>Unsubscribe confirmation: " + req.body.Message);
    } else {
        console.log(">>Unknown message type.");
    }
    console.log(">>Done processing message: " + req.body.MessageId);
});

const crypto = require('crypto');
function getMd5(str) {
    return crypto.createHash('md5').update(str).digest("hex");
}

app.listen(8080, () => console.log(`SNS App listening on port ${8080}!`));
