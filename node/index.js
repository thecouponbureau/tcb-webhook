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

app.listen(8080, () => console.log(`SNS App listening on port ${8080}!`));
