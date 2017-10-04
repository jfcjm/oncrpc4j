package org.libvirt;

import org.dcache.xdr.OncRpcAcceptedException;

public class VirRpcAcceptedException extends OncRpcAcceptedException {
    
    private static final long serialVersionUID = -2011194579113079006L;

    public VirRpcAcceptedException(int acceptStatus) {
        super(acceptStatus);
    }

}
