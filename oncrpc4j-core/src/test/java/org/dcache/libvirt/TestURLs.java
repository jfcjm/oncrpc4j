package org.dcache.libvirt;

import static org.junit.Assert.*;
import static org.libvirt.LibvirtURLs.*;

import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Test;
import org.libvirt.LibvirtURLs;


public class TestURLs {

	/**
	 * Test if protocol "test+tcp" is registered by the call
	 * of the static block in LibvirtURLs
	 * @throws MalformedURLException if the protocol "test+tcp"
	 * is not registered as a null handler
	 * @throws URISyntaxException 
	 */
	@Test
	public void testProtocolISRegisteredTest() throws URISyntaxException{
		System.out.println(LibvirtURLs.dumpAsString());

		LibvirtUrlScheme value = LibvirtUrlScheme.TestTcp;
		assertEquals("test+tcp",value.getProtocol());
		assertTrue(LibvirtURLs.isRegistered("test+tcp"));
		URI testUri =  new URI("test+tcp:///");
		assertEquals("test+tcp",testUri.getScheme());
		assertEquals("test",LibvirtURLs.getLocalPart(testUri.getScheme()));
		
	}
	
	@Test
	public void testProtocolISRegisteredQemu() throws  URISyntaxException {
		LibvirtUrlScheme value = LibvirtUrlScheme.QemuTcp;
		assertEquals("qemu+tcp",value.getProtocol());
		assertTrue(LibvirtURLs.isRegistered("qemu+tcp"));
		URI testUri = new URI("qemu+tcp://user:password@localhost/system");
		assertEquals("qemu",LibvirtURLs.getLocalPart(testUri.getScheme()));
		assertEquals(0,new URI("qemu:///system").compareTo(LibvirtURLs.getLocalUri(testUri)));
	}
	

}
