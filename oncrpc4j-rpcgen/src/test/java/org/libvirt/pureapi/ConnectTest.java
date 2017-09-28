package org.libvirt.pureapi;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

import org.dcache.oncrpc4j.rpcgen.libvirt.remote_nonnull_node_device;
import org.dcache.xdr.OncRpcException;
import org.junit.Test;
import tests.credentials.TestCredentials4Libvirt;
public class ConnectTest {

	private static final String FULL_HOSTNAME = TestCredentials4Libvirt.FULL_HOSTNAME;
    private static final String QEMU_TCP_URL = TestCredentials4Libvirt.QEMU_TCP_URL;

    @Test
	public void test() throws OncRpcException, IOException, TimeoutException, URISyntaxException {
		Connect connect = new Connect(QEMU_TCP_URL);
		
		String caps = connect.getCapabilities();
		long freeMem = connect.getFreeMemory();
		String hostname = connect.getHostName();
		assertEquals(FULL_HOSTNAME,hostname);
	}

}
