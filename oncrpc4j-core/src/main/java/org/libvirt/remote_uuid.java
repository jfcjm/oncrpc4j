/*
 * Automatically generated by jrpcgen 1.0.7+ on 18/04/17 19:06
 * jrpcgen is part of the "Remote Tea" ONC/RPC package for Java
 * See http://remotetea.sourceforge.net for details
 *
 * This version of jrpcgen adopted by dCache project
 * See http://www.dCache.ORG for details
 */
package org.libvirt;
import org.dcache.xdr.*;
import java.io.IOException;

public class remote_uuid implements XdrAble, java.io.Serializable {

    public byte [] value;

    private static final long serialVersionUID = 8041053161017178530L;
    private static final int VIR_UUID_BUFLEN = 16;
    public remote_uuid() {
    }

    public remote_uuid(byte [] value) {
        this.value = value;
    }

    public remote_uuid(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        xdr.xdrEncodeOpaque(value, VIR_UUID_BUFLEN);
    }

    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        value = xdr.xdrDecodeOpaque(VIR_UUID_BUFLEN);
    }

}
// End of remote_uuid.java
