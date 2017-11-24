package org.dcache.oncrpc4j.rpcgen;

import org.dcache.xdr.OncRpcProgram;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.dcache.xdr.model.root.AbstractOncRpcSvc;
import org.dcache.xdr.model.root.AbstractOncRpcSvcBuilder;
import org.junit.After;
import org.junit.Before;

import java.net.InetAddress;

public abstract class AbstractBlobStoreTest {
    protected BlobStoreServerImpl serverImpl = new BlobStoreServerImpl();
    RpcSvcItf<?> server;
    protected BlobStoreClient client;
    protected String address = "127.0.0.1";
    protected int port = 6666;

    @Before
    public void setup() throws Exception{
         server = new AbstractOncRpcSvcBuilder<>()
                .withTCP()
                .withoutAutoPublish() //so we dont need rpcbind
                .withPort(port)
                .withSameThreadIoStrategy()
                .withBindAddress(address)
                .build();
        server.register(new OncRpcProgram(BlobStore.BLOB_STORAGE, BlobStore.BLOB_STORAGE_VERS), serverImpl);
        server.start();
        client = new BlobStoreClient(InetAddress.getByName(address), port);
    }

    @After
    public void teardown() throws Exception {
        server.stop();
    }
}
