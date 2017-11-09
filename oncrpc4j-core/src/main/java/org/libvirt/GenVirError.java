package org.libvirt;

import org.dcache.xdr.XdrAble;
import org.libvirt.types.remote_domain;
import org.libvirt.types.remote_network;
import org.libvirt.types.remote_nonnull_domain;
import org.libvirt.types.remote_nonnull_string;
import org.libvirt.types.remote_uuid;
/**
 * See https://github.com/djs55/libvirt/blob/master/include/libvirt/virterror.h
 * @author jmk
 *
 */
public class GenVirError {

    private static final remote_nonnull_domain voidDomain = new remote_nonnull_domain();

    private static final remote_uuid voidUUid = new remote_uuid(new byte[0]);

    private static final remote_nonnull_string NAString = new remote_nonnull_string("N/A");
    static{
        voidDomain.setId(0);
        voidDomain.setUuid(voidUUid);
        voidDomain.setName(NAString);
    }
    private static final remote_domain VoidDom = new remote_domain();
    private static final remote_string VoidString = new remote_string();

    private static final remote_network VoidNetwork = new remote_network();

    
    public static XdrAble createProgramUnavailable(GenVirRpcCall virRpcCall) {
       remote_error error = new remote_error();
       error.setCode(39);
       error.setDomain(7);;
       error.setLevel(2);
       error.setMessage(new remote_string(new remote_nonnull_string("Bad program number " + virRpcCall.getProgram())));
       error.setDom(VoidDom);
       error.setStr1(VoidString);
       error.setStr2(VoidString);
       error.setStr3(VoidString);
       error.setNet(VoidNetwork);
       return error;
    }


    public static XdrAble createRuntimeError(GenVirRpcCall virRpcCall, Exception e) {
        remote_error error = new remote_error();
        error.setCode(1);
        error.setDomain(0);;
        error.setLevel(2);
        error.setMessage(new remote_string(new remote_nonnull_string("Runtime exception " + e.getMessage())));
        error.setDom(VoidDom);
        error.setStr1(VoidString);
        error.setStr2(VoidString);
        error.setStr3(VoidString);
        error.setNet(VoidNetwork);
        return error;
       
    }

}
