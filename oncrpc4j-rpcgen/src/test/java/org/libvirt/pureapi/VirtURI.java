package org.libvirt.pureapi;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.libvirt.LibvirtURLs;

public class VirtURI  implements Comparable<VirtURI>, Serializable{
	
	private static final long serialVersionUID = 3575723149789966966L;
	URI _uri;

	public VirtURI(URI uri) throws URISyntaxException{
		LibvirtURLs.checkIsLibvirtURI(uri);
		_uri = uri;
    }
	
	
	
	public URI parseServerAuthority() throws URISyntaxException {
		return _uri.parseServerAuthority();
	}

	public URI normalize() {
		return _uri.normalize();
	}

	public URI resolve(URI uri) {
		return _uri.resolve(uri);
	}

	public URI resolve(String str) {
		return _uri.resolve(str);
	}

	public URI relativize(URI uri) {
		return _uri.relativize(uri);
	}

	public URL toURL() throws MalformedURLException {
		return _uri.toURL();
	}

	public String getScheme() {
		return _uri.getScheme();
	}

	public boolean isAbsolute() {
		return _uri.isAbsolute();
	}

	public boolean isOpaque() {
		return _uri.isOpaque();
	}

	public String getRawSchemeSpecificPart() {
		return _uri.getRawSchemeSpecificPart();
	}

	public String getSchemeSpecificPart() {
		return _uri.getSchemeSpecificPart();
	}

	public String getRawAuthority() {
		return _uri.getRawAuthority();
	}

	public String getAuthority() {
		return _uri.getAuthority();
	}

	public String getRawUserInfo() {
		return _uri.getRawUserInfo();
	}

	public String getUserInfo() {
		return _uri.getUserInfo();
	}

	public String getHost() {
		return _uri.getHost();
	}

	public int getPort() {
		return _uri.getPort();
	}

	public String getRawPath() {
		return _uri.getRawPath();
	}

	public String getPath() {
		return _uri.getPath();
	}

	public String getRawQuery() {
		return _uri.getRawQuery();
	}

	public String getQuery() {
		return _uri.getQuery();
	}

	public String getRawFragment() {
		return _uri.getRawFragment();
	}

	public String getFragment() {
		return _uri.getFragment();
	}

	public boolean equals(Object ob) {
		return _uri.equals(ob);
	}

	public int hashCode() {
		return _uri.hashCode();
	}

	public int compareTo(URI that) {
		return _uri.compareTo(that);
	}

	public String toString() {
		return _uri.toString();
	}

	public String toASCIIString() {
		return _uri.toASCIIString();
	}

	@Override
	public int compareTo(VirtURI o) {
		return _uri.compareTo(o._uri);
	}




	String getPassword() {
		String userInfo = getUserInfo();
		if (userInfo != null){
			return _uri.getUserInfo().split(":")[1];
		} else return null;
	}

	String getUserName() {
		String userInfo = getUserInfo();
		if (userInfo != null){
			return _uri.getUserInfo().split(":")[0];
		} else return null;
	}


	/**
	 * @param connectUri
	 * @return the local part of the connection URI
	 * @throws URISyntaxException 
	 */
	public URI getLocalURI() {
		return LibvirtURLs.getLocalUri(_uri);
	}
}
