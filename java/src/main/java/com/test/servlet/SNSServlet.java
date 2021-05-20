package com.test.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.commons.codec.binary.Base64;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Scanner;

/**
 * Servlet implementation class SNSServlet
 */

/**
 * @author preetham
 *
 */
public class SNSServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ObjectReader readerSnsMessage;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SNSServlet() {
        System.out.println("SNSServlet Constructor called!");
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		System.out.println("SNSServlet \"Init\" method called");
		readerSnsMessage = new ObjectMapper().readerFor(SNSMessage.class);
	}

	/**
	 * @see Servlet#destroy()
	 */
	public void destroy() {
		System.out.println("SNSServlet \"Destroy\" method called");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SecurityException {
		//Get the message type header.
		String messagetype = request.getHeader("x-amz-sns-message-type");
		//If message doesn't have the message type header, don't process it.
		if (messagetype == null) {
			return;
		}

		// Parse the JSON message in the message body
		// and hydrate a Message object with its contents
		// so that we have easy access to the name-value pairs
		// from the JSON message.
		Scanner scan = new Scanner(request.getInputStream());
		StringBuilder builder = new StringBuilder();
		while (scan.hasNextLine()) {
			builder.append(scan.nextLine());
		}
		String str = builder.toString();
		System.out.println(str);

		SNSMessage msg = readerSnsMessage.readValue(str);

		// The signature is based on SignatureVersion 1.
		// If the sig version is something other than 1,
		// throw an exception.
		if (msg.getSignatureVersion().equals("1")) {
			// Check the signature and throw an exception if the signature verification fails.
			if (isMessageSignatureValid(msg)) {
				System.out.println(">>Signature verification succeeded");
			} else {
				System.out.println(">>Signature verification failed");
				throw new SecurityException("Signature verification failed.");
			}
		} else {
			System.out.println(">>Unexpected signature version. Unable to verify signature.");
			throw new SecurityException("Unexpected signature version. Unable to verify signature.");
		}

		// Process the message based on type.
		if (messagetype.equals("Notification")) {
			//Do something with the Message and Subject.
			//Just log the subject (if it exists) and the message.
			String logMsgAndSubject = ">>Notification received from topic " + msg.getTopicArn();
			if (msg.getSubject() != null) {
				logMsgAndSubject += " Subject: " + msg.getSubject();
			}
			logMsgAndSubject += " Message: " + msg.getMessage();
			System.out.println(logMsgAndSubject);
		} else if (messagetype.equals("SubscriptionConfirmation")) {
			//You should make sure that this subscription is from the topic you expect. Compare topicARN to your list of topics
			//that you want to enable to add this endpoint as a subscription.

			//Confirm the subscription by going to the subscribeURL location
			//and capture the return value (XML message body as a string)
			Scanner sc = new Scanner(new URL(msg.getSubscribeUrl()).openStream());
			StringBuilder sb = new StringBuilder();
			while (sc.hasNextLine()) {
				sb.append(sc.nextLine());
			}
			System.out.println(">>Subscription confirmation (" + msg.getSubscribeUrl() + ") Return value: " + sb.toString());
			//Process the return value to ensure the endpoint is subscribed.
		} else if (messagetype.equals("UnsubscribeConfirmation")) {
			//Handle UnsubscribeConfirmation message.
			//For example, take action if unsubscribing should not have occurred.
			//You can read the SubscribeURL from this message and
			//re-subscribe the endpoint.
			System.out.println(">>Unsubscribe confirmation: " + msg.getMessage());
		} else {
			//Handle unknown message type.
			System.out.println(">>Unknown message type.");
		}
		System.out.println(">>Done processing message: " + msg.getMessageId());
	}

	private static boolean isMessageSignatureValid(SNSMessage msg) {
		try {
			URL url = new URL(msg.getSigningCertUrl());
			verifyMessageSignatureURL(msg, url);

			InputStream inStream = url.openStream();
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
			inStream.close();

			Signature sig = Signature.getInstance("SHA1withRSA");
			sig.initVerify(cert.getPublicKey());
			sig.update(getMessageBytesToSign(msg));
			return sig.verify(Base64.decodeBase64(msg.getSignature()));
		} catch (Exception e) {
			throw new SecurityException("Verify method failed.", e);
		}
	}

	private static void verifyMessageSignatureURL(SNSMessage msg, URL endpoint) {
		URI certUri = URI.create(msg.getSigningCertUrl());

		if (!"https".equals(certUri.getScheme())) {
			throw new SecurityException("SigningCertURL was not using HTTPS: " + certUri.toString());
		}

		if (!endpoint.getHost().equals(certUri.getHost())) {
			throw new SecurityException(
					String.format("SigningCertUrl does not match expected endpoint. " +
									"Expected %s but received endpoint was %s.",
							endpoint, certUri.getHost()));

		}
	}

	private static byte [] getMessageBytesToSign (SNSMessage msg) {
		byte [] bytesToSign = null;
		if (msg.getType().equals("Notification"))
			bytesToSign = buildNotificationStringToSign(msg).getBytes();
		else if (msg.getType().equals("SubscriptionConfirmation") || msg.getType().equals("UnsubscribeConfirmation"))
			bytesToSign = buildSubscriptionStringToSign(msg).getBytes();
		return bytesToSign;
	}

	//Build the string to sign for Notification messages.
	public static String buildNotificationStringToSign(SNSMessage msg) {
		String stringToSign = null;

		//Build the string to sign from the values in the message.
		//Name and values separated by newline characters
		//The name value pairs are sorted by name
		//in byte sort order.
		stringToSign = "Message\n";
		stringToSign += msg.getMessage() + "\n";
		stringToSign += "MessageId\n";
		stringToSign += msg.getMessageId() + "\n";
		if (msg.getSubject() != null) {
			stringToSign += "Subject\n";
			stringToSign += msg.getSubject() + "\n";
		}
		stringToSign += "Timestamp\n";
		stringToSign += msg.getTimestamp() + "\n";
		stringToSign += "TopicArn\n";
		stringToSign += msg.getTopicArn() + "\n";
		stringToSign += "Type\n";
		stringToSign += msg.getType() + "\n";
		return stringToSign;
	}

	//Build the string to sign for SubscriptionConfirmation
//and UnsubscribeConfirmation messages.
	public static String buildSubscriptionStringToSign(SNSMessage msg) {
		String stringToSign = null;
		//Build the string to sign from the values in the message.
		//Name and values separated by newline characters
		//The name value pairs are sorted by name
		//in byte sort order.
		stringToSign = "Message\n";
		stringToSign += msg.getMessage() + "\n";
		stringToSign += "MessageId\n";
		stringToSign += msg.getMessageId() + "\n";
		stringToSign += "SubscribeURL\n";
		stringToSign += msg.getSubscribeUrl() + "\n";
		stringToSign += "Timestamp\n";
		stringToSign += msg.getTimestamp() + "\n";
		stringToSign += "Token\n";
		stringToSign += msg.getToken() + "\n";
		stringToSign += "TopicArn\n";
		stringToSign += msg.getTopicArn() + "\n";
		stringToSign += "Type\n";
		stringToSign += msg.getType() + "\n";
		return stringToSign;
	}

}
