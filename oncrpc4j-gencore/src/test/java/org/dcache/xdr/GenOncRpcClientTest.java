package org.dcache.xdr;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.dcache.xdr.model.itf.XdrTransportItf;
import org.junit.Test;

public class GenOncRpcClientTest {

    @Test
    public void testGenOncRpcClientInetAddressIntInt() throws IOException {
        GenOncRpcClient client = new GenOncRpcClient(InetAddress.getByName("127.0.0.1"),111,4);
        assertNotNull(client);
        
        XdrTransportItf<GenOncRpcSvc,GenOncRpcCall,XdrTransport,GenOncRpcReply> c = client.connect();
    }

}
