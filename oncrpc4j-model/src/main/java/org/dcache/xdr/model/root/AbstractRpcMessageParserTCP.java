package org.dcache.xdr.model.root;

import java.io.IOException;
import java.nio.ByteOrder;

import org.dcache.xdr.GrizzlyMemoryManager;
import org.dcache.xdr.Xdr;
import org.dcache.xdr.model.itf.RpcMessageParserTCPItf;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;

public abstract class AbstractRpcMessageParserTCP<SVC_T extends RpcSvcItf<SVC_T>> extends BaseFilter implements RpcMessageParserTCPItf<SVC_T>{


    /**
     * RPC fragment record marker mask, doit aller au niveau correspondant  au protocole
     */
    @Deprecated
    private final static int RPC_LAST_FRAG = 0x80000000;
    
    public AbstractRpcMessageParserTCP() {
        super();
    }
    abstract protected boolean isAllFragmentsArrived(Buffer messageBuffer) throws IOException ;
    abstract protected Xdr assembleXdr(Buffer messageBuffer);
    abstract protected int getMessageSize(int marker);
    abstract protected  boolean isLastFragment(int marker);
    @Override
    public NextAction handleRead(FilterChainContext ctx) throws IOException {

        Buffer messageBuffer = ctx.getMessage();
        if (messageBuffer == null) {
            return ctx.getStopAction();
        }

        if (!isAllFragmentsArrived(messageBuffer)) {
            return ctx.getStopAction(messageBuffer);
        }

        ctx.setMessage(assembleXdr(messageBuffer));

        final Buffer reminder = messageBuffer.hasRemaining()
                ? messageBuffer.split(messageBuffer.position()) : null;

        return ctx.getInvokeAction(reminder);
    }

    @Override
    public NextAction handleWrite(FilterChainContext ctx) throws IOException {

        Buffer b = ctx.getMessage();
        int len = b.remaining() | RPC_LAST_FRAG;

        Buffer marker = GrizzlyMemoryManager.allocate(4);
        marker.order(ByteOrder.BIG_ENDIAN);
        marker.putInt(len);
        marker.flip();
        marker.allowBufferDispose(true);
        b.allowBufferDispose(true);
        Buffer composite = GrizzlyMemoryManager.createComposite(marker, b);
        composite.allowBufferDispose(true);
        ctx.setMessage(composite);
        return ctx.getInvokeAction();
    }
}