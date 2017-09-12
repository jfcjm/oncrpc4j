package org.libvirt.pureapi;

import static org.junit.Assert.*;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.libvirt.LibvirtURLs;

@SuppressWarnings("unused")
public class virtURITest {
	URI uri;
	private VirtURI virtUri;
	@Before
	public void prepare() throws URISyntaxException{
		System.err.println(LibvirtURLs.dumpAsString());
		uri = new URI("qemu+tcp://user:password@localhost/system");
		virtUri = new VirtURI(uri);
	}
	@Test
	public void testVirtURI() {
		
	}

	@Test
	public void testGetPassword() {
		assertEquals("password",virtUri.getPassword());
	}

	@Test
	public void testGetUserName() {
		assertEquals("user",virtUri.getUserName());
	}

	@Test
	public void testGetLocalURI() {
		assertEquals("qemu:///system",virtUri.getLocalURI().toString());
	}

}
