package org.libvirt;

import org.dcache.xdr.OncRpcException;
import org.glassfish.grizzly.Buffer;

public interface RpcPacketWrapper {

	Buffer unwrap(byte[] msg) throws OncRpcException;

	Buffer wrap(Buffer msgAsBuf)  throws OncRpcException;

}
