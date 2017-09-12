package org.libvirt;

import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;

public class VirtTLSSessionManager {
	private SSLEngineConfigurator _engineConfigurator;
	public  VirtTLSSessionManager(){
		this(initializeSSL());
	}
	public VirtTLSSessionManager(SSLEngineConfigurator configurator) {
		_engineConfigurator = configurator;
	}
	private static  SSLEngineConfigurator initializeSSL() {
		SSLContextConfigurator sslContextConfig = new SSLContextConfigurator();
        return new SSLEngineConfigurator(sslContextConfig.createSSLContext(),
                false, false, false);
	}
	public SSLEngineConfigurator getEngineConfigurator() {
		return _engineConfigurator;
	}
	
}
