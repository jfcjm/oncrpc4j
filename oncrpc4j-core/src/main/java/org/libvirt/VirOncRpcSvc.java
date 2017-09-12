package org.libvirt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.OncRpcSvc;
import org.dcache.xdr.ReplyQueue;
import org.dcache.xdr.RpcProtocolFilter;
import org.glassfish.grizzly.Transport;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirOncRpcSvc extends OncRpcSvc {
    private final static Logger _log = LoggerFactory.getLogger(VirOncRpcSvc.class);
    
    private final Map<Transport, PacketWrapperFilter> _transport2wrapper = 
            new HashMap<Transport, PacketWrapperFilter>();
    
    public VirOncRpcSvc(VirOncRpcSvcBuilder virOncRpcSvcBuilder) {
        super(virOncRpcSvcBuilder);
        _log.info("Creating a new VirOncRpcSvc");
    }
    public void start() throws IOException {
        if (!_isClient){
            _log.error("Cannot use libvirt rpc in server mode for now");
            throw new RuntimeException("Cannot use libvirt rpc in server mode for now");
        }
        if (!_publish){
            _log.warn("Libvirt does not publish its service through a portmapper");
            _publish=false;
        }
        for (Transport t : _transports) {
            if ( !( t instanceof TCPNIOTransport)){
                _log.error("Libvirt does not use UDP");
                throw new RuntimeException("Libvirt does not use UDP transport");
            }
        }
        super.start();
    }
    @Override
    protected RpcProtocolFilter getRpcProtocolFilter(ReplyQueue replyQueue) {
        return new VirRpcProtocolFilter(replyQueue);
    }

    @Override
    protected void addPostTransportProtocolFilters(FilterChainBuilder filterChain,Transport t) {
        PacketWrapperFilter pwf = new PacketWrapperFilter();
        _transport2wrapper.put(t,pwf);
        filterChain.add(pwf);
    }
    
    public synchronized void setPacketWrapper(SASLPacketWrapper sc) throws OncRpcException {
        if (! _isClient){
            throw new OncRpcException("Unimplemented server Operation"){
            };
        }
        for (Entry<Transport, PacketWrapperFilter> wrapper : _transport2wrapper.entrySet()){
            _log.debug("setting packet wrapper for" + wrapper.getValue());
            wrapper.getValue().setPacketWrapper(sc);
        }
        
    }
}
