package org.dcache.libvirt;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URL;

import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.junit.Test;
import org.libvirt.VirtTLSSessionManager;
import org.libvirt.VirtTlsProtocolFilter;

public class TestVirtTLS {
	/**
	 * Teste si SSLEngineConfigurator utilise le truststore par défaut
	 */
	@Test
	public void test() {

		final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
		
		System.setOut(new PrintStream(errContent));
		System.setProperty("javax.net.debug", "all");
		VirtTLSSessionManager manager = new VirtTLSSessionManager();
		SSLEngineConfigurator ec = manager.getEngineConfigurator();
		assertNotNull(ec);;
		System.err.println(errContent);
		String[] lines = errContent.toString().split("\n");
		assertTrue(lines[0].matches(".*trustStore is: .*"));
	}

	/**
	 * Teste si l'utilisation de la propriété javax.net.ssl.trustStore
	 */
	@Test
	public void testwithCustomStore() {
		URL url = getClass().getClassLoader().getResource("test-cacerts.jks");
		assertNotNull(url);
		System.setProperty("javax.net.ssl.trustStore",url.getPath());
		final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
		System.setOut(new PrintStream(errContent));
		System.setProperty("javax.net.debug", "all");
		VirtTLSSessionManager manager = new VirtTLSSessionManager();
		SSLEngineConfigurator ec = manager.getEngineConfigurator();
		assertNotNull(ec);;
		String[] lines = errContent.toString().split("\n");
		System.err.println(lines[0]);
		
		assertTrue(lines[0].matches(".*adding as trusted cert:.*"));
	}


	@Test
	public void testVirtTlsProtocolFilter() {
		VirtTLSSessionManager manager = new VirtTLSSessionManager();
		VirtTlsProtocolFilter filter = new VirtTlsProtocolFilter(manager);
		fail("TODO");
	}
	
}
