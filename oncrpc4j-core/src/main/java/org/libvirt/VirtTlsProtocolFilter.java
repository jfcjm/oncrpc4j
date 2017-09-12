package org.libvirt;

import org.glassfish.grizzly.ssl.SSLFilter;

public class VirtTlsProtocolFilter extends SSLFilter {

	private VirtTLSSessionManager _tlsSessionManager;

	public VirtTlsProtocolFilter(VirtTLSSessionManager tlsSessionManager) {
		super(null,tlsSessionManager.getEngineConfigurator());
		_tlsSessionManager = tlsSessionManager;
	}
}
