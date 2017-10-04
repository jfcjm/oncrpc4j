package org.libvirt;

import org.dcache.xdr.OncRpcException;

public class VirRpcRejectedException extends OncRpcException {
    
    private static final long serialVersionUID = 8947480749178768718L;
    private int _status;

    public VirRpcRejectedException(int rejectStatus) {
        super(Integer.toString(rejectStatus));
        _status = rejectStatus;
    }

    public VirRpcRejectedException(remote_error error) {
        this(error.code,error.message);
    }

    public VirRpcRejectedException(int code, remote_string message) {
        super(message.value.value + ";Error code "+Integer.toString(code));
        _status= code;
    }

}
