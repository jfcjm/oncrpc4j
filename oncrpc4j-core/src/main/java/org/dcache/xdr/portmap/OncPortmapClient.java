package org.dcache.xdr.portmap;

import org.dcache.xdr.OncRpcSvc;
import org.dcache.xdr.RpcProgUnavailable;
import org.dcache.xdr.model.itf.ImplementationGetterItf;
import org.dcache.xdr.model.itf.XdrTransportItf;

public interface OncPortmapClient extends OncPortmapClientItf<OncRpcSvc>, ImplementationGetterItf{

    static OncPortmapClient getImpl(XdrTransportItf<OncRpcSvc> transport) throws RpcProgUnavailable {
        return new GenericPortmapClient(transport);
    }

}
