package org.libvirt;

import org.glassfish.grizzly.Buffer;

public interface RpcPacketWrapper {

	Buffer unwrap(byte[] msg) throws VirRpcException;

	Buffer wrap(Buffer msgAsBuf)  throws VirRpcException;

}
