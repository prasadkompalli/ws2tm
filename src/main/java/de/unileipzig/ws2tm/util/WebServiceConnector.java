package de.unileipzig.ws2tm.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.namespace.QName;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.log4j.Logger;

import de.unileipzig.ws2tm.Factory;
import de.unileipzig.ws2tm.ws.soap.Connection;

/**
 * @author Torsten Grigull
 * @version 0.1 (2011/02/03)
 *
 */
//TODO currently only http requests and responses, but no rpc calls or something else supported
public class WebServiceConnector implements Factory {

	private static String HTTP1_1 = "HTTP/1.1";
	
	private static Logger log = Logger.getLogger(WebServiceConnector.class);

	private static SSLSocketFactory sslSocketFactory;
	
	private static final WebServiceConnector FACTORY = new WebServiceConnector(new File(WebServiceConfigurator.getTrustStore()),WebServiceConfigurator.getTrustStorePassword());
	
	private HashMap<URL,WebServiceConnector> ws = null;

	private URL url;
	
	/**
	 * Constructor of class: FACTORY constructor
	 *
	 * @param file
	 * @param pw
	 */
	private WebServiceConnector(File file, String pw) {
		log.info("Initializing factory class "+WebServiceConnector.class.getCanonicalName()+". Setting up trustStore for certificates. Existing certificates will be overwritten if the trust- or KeyStore already exists.");
		System.setProperty("javax.net.ssl.trustStore", file.getAbsolutePath());
		System.setProperty("javax.net.ssl.trustStorePassword", pw);
		
		
		// the next lines are using class WebServiceConnector to establish to send and receive the response
		if (WebServiceConfigurator.isProxySettingExisting()) {
			setProxySettings(WebServiceConfigurator.getProxyIP(), WebServiceConfigurator.getProxyPort());
		}
		if (WebServiceConfigurator.isProxyAuthenticationExisting()) {
			setProxyAuthentification(WebServiceConfigurator.getProxyUserName(), WebServiceConfigurator.getProxyUserPassword());
		}		
		
		this.ws = new HashMap<URL,WebServiceConnector>();
	}
	
	/**
	 * Constructor of class
	 *
	 * @param url
	 */
	public WebServiceConnector(URL url) {
		this.url = url;
		
	}
	
	/**
	 * @param url
	 * @return
	 */
	public static WebServiceConnector newConnection(URL url) {
		return FACTORY.newInstance(url);
	}
	
	/**
	 * @param host
	 * @param port
	 */
	public static void setProxySettings(String host, String port) {
		System.setProperty("http.proxyHost", host);
		System.setProperty("http.proxyPort", port);
	}
	
	/**
	 * @param user
	 * @param pw
	 */
	public static void setProxyAuthentification(String user, String pw) {
		Authenticator.setDefault(new ProxyAuthenticator(user, pw));
	}
	
	/**
	 * @param url
	 * @return
	 */
	private WebServiceConnector newInstance(URL url) {
		if (ws.containsKey(url)) {
			return ws.get(url);
		}
		WebServiceConnector wsc = new WebServiceConnector(url);
		
		ws.put(url, wsc);
		return wsc;
	}
	
	@SuppressWarnings("unchecked")
	private String createConnectionText(SOAPMessage msg) {
		
		StringBuffer msgText = new StringBuffer();
		
		try {
			
			// the following lines are required for pipeing the content of the soap message to the request text.
			StringBuffer text = new StringBuffer();
			PipedOutputStream out = new PipedOutputStream();
			PipedInputStream in = new PipedInputStream();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			out.connect(in);
			msg.writeTo(out);
			out.close();
			
			String line;
			while (reader.ready() && (line = reader.readLine()) != null) {
				text.append(line+"\n");
			}
			
			// The following lines are required because of SOAP 1.0 protocol. It should contain the soap actions.
			Iterator<SOAPElement> it = msg.getSOAPBody().getChildElements();
			StringBuffer soapactions = new StringBuffer("SOAPAction: ");
			while (it.hasNext()) {
				SOAPElement e = it.next();
				QName name = e.getElementQName();
				soapactions.append("\""+name.getNamespaceURI()+name.getLocalPart()+"\"");
				if (it.hasNext()) {
					soapactions.append(", ");
				}
			}
			
			// SOAP message requests will always be POST messages. The request will follow the HTTP1.1 directive.
			msgText.append("POST "+url.getPath()+" "+HTTP1_1+"\n");
			// Receiving HOST will be found via the URL-information
			msgText.append("Host: "+url.getHost()+"\n");
			// Content-Type will always be text/xml. It is encoded with UTF-8. Although this is explicity done.
			//TODO check if the content will always be UTF-8 encoded
			msgText.append("Content-Type: text/xml; charset=utf-8\n");
			// Content-Length is analyzed through evaluating the length of the soap message
			msgText.append("Content-Length: "+text.length()+"\n");
			// soap actions will be added if available. If SOAP1.1 or SOAP1.2 should be supported, this line can be removed or it could be evaluated if SOAP 1.0 is set.
			if (soapactions.length() > 0) {
				msgText.append(soapactions.toString()+"\n");
			}
			// finally the real request is added to the request text
			msgText.append("\n<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"+text.toString());
			
		} catch (SOAPException e) {
			log.error("Error while accessing the provided soap message and its subparts. The soap message ("+msg.getContentDescription()+") was probably not properly initialized.");
		} catch (IOException e) {
			// error should not occur in any case, otherwise file system is probably corrupt.
			log.fatal("IOException occurred while connection output with input to pipe the content of a soap message to the web service inputstream using also a bufferedreader. Check also the availability of enough cache space.",e);
		}
		
		return msgText.toString();
	}
	
	/**
	 * This method tries to connect to any type of web server and establishes
	 * an inputstream.
	 * 
	 * @param msg - instance of class {@link SOAPMessage} containing the operations and parameters required for a proper request.
	 * @return an implementation of interface {@link Connection}, which encapsulates mime headers and the content of the web service response.
	 */
	public Connection sendRequest(SOAPMessage msg) {
		String requestText = this.createConnectionText(msg);
		
		if (log.isDebugEnabled()) {
			log.debug("Created the request text for web service at "+url.toString());
		}
		
		HttpURLConnection conn = null;
		if (url.getProtocol().toLowerCase().startsWith("https")) {
			try {
				conn = (HttpsURLConnection) url.openConnection();
				((HttpsURLConnection) conn).setSSLSocketFactory(WebServiceConnector.getSocketFactory());
				//TODO try to check certificates, can be done after connection
				((HttpsURLConnection) conn).setHostnameVerifier(new HostnameVerifier(){
					@Override
					public boolean verify(String hostname, SSLSession session) {
						//TODO implement a valid verification of ssl sessions
						log.info("Verfiying hostname "+hostname);
						return true;
					}});
			} catch (IOException e) {
				log.error("Could not enable any data from server url "+url.toString()+" because an IOException occurred.",e);
			}
		} else {
			try {
				conn = (HttpURLConnection) url.openConnection();
			} catch (IOException e) {
				log.error("Could not receive any data from url "+url.toString()+" because an IOException occurred.",e);
			}
		}
		
		try {
			if (this.verify(url) && conn != null) {
				conn.setDoInput(true);
				
				if (requestText != null && requestText.length() > 0) {
					
					if (log.isDebugEnabled()) {
						log.debug("Sending the following lines to web server url "+url.getPath());
						log.debug(requestText);
					}
					conn.setDoOutput(true);
					DataOutputStream out = new DataOutputStream(conn.getOutputStream());
					out.writeBytes(requestText);
					out.flush();
					out.close();
				}
				conn.connect();

				if (conn.getResponseCode() != 200) {
					throw new IOException("Server returned wrong and unexcepted http response code: "+conn.getResponseCode()+". "+conn.getResponseMessage());
				}

				// This should be actually the mime header information collected manually
/*				conn.getContentLength();
				conn.getContentType();
				conn.getContentEncoding();
				conn.getLastModified();
				conn.getExpiration();
				conn.getDate();
*/
				MimeHeaders headers = new MimeHeaders();
				for (Map.Entry<String, List<String>> e : conn.getHeaderFields().entrySet()) {
					for (String v : e.getValue()) {
						if (v.length()==1) {
							System.out.println(e.getKey() +" : "+ v);
							headers.addHeader(e.getKey(), v);
						}
					}
				}
				
				return new ConnectionImpl(headers, conn.getInputStream());
			}
		} catch (IOException e) {
			log.error("Could not send or receive data form web service url "+url.getPath() + " due to an IOException.",e);
		}
		
		return null;
	}

	/**
	 * Returns a SSL Factory instance that accepts all server certificates.
	 * 
	 * <pre>
	 * SSLSocket sock = (SSLSocket) getSocketFactory.createSocket(host, 443);
	 * </pre>
	 * 
	 * @return An SSL-specific socket factory.
	 * 
	 * @author Howard Abrams (2007/09/20) [http://www.howardism.org/Technical/Java/SelfSignedCerts.html]
	 **/
	private static final SSLSocketFactory getSocketFactory() {
		if (sslSocketFactory == null) {
			try {
				TrustManager[] tm = new TrustManager[] { new NaiveTrustManager() };
				SSLContext context = SSLContext.getInstance("SSL");
				context.init(new KeyManager[0], tm, new SecureRandom());
				sslSocketFactory = (SSLSocketFactory) context.getSocketFactory();
			} catch (KeyManagementException e) {
				log.error("No SSL algorithm support: " + e.getMessage(), e);
			} catch (NoSuchAlgorithmException e) {
				log.error("Exception when setting up the Naive key management.",e);
			}
		}
		return sslSocketFactory;
	}
	
	/**
	 * @param url
	 * @return
	 */
	private boolean verify(URL url) {
		//TODO check urls may be a positive white list of entrusted urls and uris via WebServiceConfigurator.
		return true;
	}
	
	private class ConnectionImpl implements Connection {
		
		private InputStream is;
		private MimeHeaders headers;
		
		private ConnectionImpl(MimeHeaders headers, InputStream is) {
			this.is = is;
			this.headers = headers;
		}
		
		@Override
		public InputStream getInputStream() {
			return this.is;
		}

		@Override
		public MimeHeaders getMimeHeaders() {
			return this.headers;
		}
		
	}
	
	/**
	 * Dummy implementation of interface {@link X509TrustManager}. This dummy implementation
	 * accepts every possible certificate it gets. Therefore man-in-the-middle attacks are possible
	 * by signing the server with invalid certificates. Currently exists only the idea to check the
	 * URLs received without checking the certificates. A check of the certificates however would be
	 * better and more secure. 
	 * 
	 * @author Torsten Grigull
	 * @version 0.1 (2011/02/04)
	 *
	 */
	//TODO check certificates if those are not trusted, and do not accept every certificate possible
	private static class NaiveTrustManager implements X509TrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] cert, String authType) throws CertificateException {
			// do nothing and trust all certificates
		}

		@Override
		public void checkServerTrusted(X509Certificate[] cert, String authType) throws CertificateException {
			// do nothing and trust all certificates
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
		
	}
	
}
