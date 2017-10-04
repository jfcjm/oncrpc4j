package org.libvirt;

import org.dcache.xdr.OncRpcException;

public class VirRpcException extends OncRpcException {

    private static final long serialVersionUID = 7382039670752984126L;
    
    public VirRpcException(String msg) {
        super(msg);
    }

}
