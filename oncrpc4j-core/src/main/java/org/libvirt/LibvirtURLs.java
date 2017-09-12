package org.libvirt;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.Map;



public class LibvirtURLs {
	static Map<String,LibvirtUrlScheme> _register = new HashMap<String,LibvirtUrlScheme>();
	static {
		URL.setURLStreamHandlerFactory(createCustomURLStreamHandlerFactory());
		// setting of value is necessary to trigger the static part ?
		@SuppressWarnings("unused")
		LibvirtUrlScheme value = LibvirtUrlScheme.TestTcp;
	}
	
	public static URLStreamHandlerFactory createCustomURLStreamHandlerFactory() {
		return new URLStreamHandlerFactory() {
			@Override
			public URLStreamHandler createURLStreamHandler(String protocol) {
				final LibvirtUrlScheme scheme = _register.get(protocol);
				return scheme == null ?  null : scheme.createCustomURLStreamHandler();
			}
			
		};
	};
	public static boolean isRegistered(String protocol){
		return _register.get(protocol) != null;
	}

	public static String getLocalPart(String string) {
		LibvirtUrlScheme scheme = _register.get(string);
		return scheme._string;
	}

	public static URI getLocalUri(URI testUri)  {
		String newScheme = getLocalPart(testUri.getScheme());
		try {
			return new URI(newScheme,null,testUri.getPath(),testUri.getFragment());
		} catch (URISyntaxException e) {
			throw new RuntimeException("Unable to create a local Uri from libvirt URI" + testUri.toString(),e);
		}
	}
	

	public static void checkIsLibvirtURI(URI uri) throws URISyntaxException {
		String scheme = uri.getScheme();
		if (! isRegistered(scheme)){
			throw new URISyntaxException(uri.toString(),uri.toString()+ " is not a libvirt registered uri, registered schems are: "+dumpAsString());
		}
	}
	
	public static String dumpAsString(){
		StringBuilder result = new StringBuilder();
		for (String key : _register.keySet()){
			result.append(key).append(", ");
		}
		return result.toString();
	}
	enum LibvirtTransport{
		SSH("ssh"),
		TCP("tcp"),
		LIBSSH2("libssh2"),
		LIBSSH("libssh"),
		UNIX("unix"),
		NULL;
		
		private String _string;

		LibvirtTransport(String aString){
			_string = aString;
		}
		LibvirtTransport(){
			_string = null;
		}
		public String getProtocol() {
			return _string;
		}
		
	};
	public enum LibvirtUrlScheme {
		Test("test") {
		},
		TestTcp("test",LibvirtTransport.TCP),
		Xen("xen") {
		},
		XenSSh("xen",LibvirtTransport.SSH) {
		},
		QemuUnix("qemu") {
		},
		QemuTcp("qemu",LibvirtTransport.TCP),
		;
		

		
		
		
		private String _string;
		private LibvirtTransport _transport;
		private String _scheme;
		LibvirtUrlScheme(String aString){
			this(aString,LibvirtTransport.NULL);
		}

		LibvirtUrlScheme(String aString,LibvirtTransport transport){
			_string = aString;
			_transport = transport;
			_scheme = aString+"+"+transport.getProtocol();
			_register.put(_scheme,this);
		}

		protected URLStreamHandler createCustomURLStreamHandler() {
			LibvirtUrlScheme scheme = this;
			return new URLStreamHandler(){

				@Override
				protected URLConnection openConnection(URL u) throws IOException {
					return scheme.getURLConnectionForScheme(u);
				}
				
			};
		}

		protected String getScheme() {
			return _scheme;
		}

		private  URLConnection getURLConnection(URL url) {
			return new URLConnection (url){

				@Override
				public void connect() throws IOException {
					// TODO Auto-generated method stub
					
				}
				
			};
		}
		private URLConnection getURLConnectionForScheme(URL url){
			LibvirtUrlScheme scheme = _register.get(url.getProtocol());
			return scheme.getURLConnection(url);
		}

		public String getProtocol() {
			return _scheme;
		}
	}
}
