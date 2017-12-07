package org.dcache.generics.alt.dispatchable;

import org.dcache.xdr.model.root.AbstractRpcCall;

public class RpcCallAlt extends AbstractRpcCall<OncSvcAlt, RpcCallAlt> {

    public RpcCallAlt(AbstractRpcCall<OncSvcAlt, RpcCallAlt> call) {
        super(call);
    }
}
