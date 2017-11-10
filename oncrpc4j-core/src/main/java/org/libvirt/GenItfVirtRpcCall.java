package org.libvirt;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.RpcAuth;
import org.dcache.xdr.XdrAble;
import org.dcache.xdr.model.itf.GenItfRpcCall;

public interface GenItfVirtRpcCall extends GenItfRpcCall<GenVirOncRpcSvc>{

    @Override
    void call(int procedure, XdrAble args, XdrAble result, long timeoutValue, TimeUnit timeoutUnits, RpcAuth auth)
            throws IOException, TimeoutException;

    @Override
    void accept() throws IOException, OncRpcException;

    @Override
    void acceptedReply(int state, XdrAble reply);

    @Override
    void failProgramUnavailable();

    /**
     * Reply to client with error procedure unavailable.
     */
    @Override
    void failProcedureUnavailable();

    void failRuntimeError(Exception e);

    @Override
    String toString();

}