package org.libvirt;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.dcache.xdr.GrizzlyMemoryManager;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.memory.BuffersBuffer;
import org.glassfish.grizzly.memory.ByteBufferArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketWrapperFilter extends BaseFilter {
	private final static Logger _log = LoggerFactory.getLogger(PacketWrapperFilter.class);
	RpcPacketWrapper _packetWrapper;
	
	public PacketWrapperFilter(){
		_packetWrapper = null;
	}
	public synchronized void  setPacketWrapper(RpcPacketWrapper packetWrapper){
		_log.debug(this+" setting packetwrapper");
		_packetWrapper = packetWrapper;
		_log.debug("now active ?"+isActive());
	}
	@Override
	public NextAction handleRead(FilterChainContext ctx) throws IOException {
		_log.debug(this+" HandleRead isActive: " + isActive());
		if (isActive()){
			Buffer msg = ctx.getMessage();
			_log.debug("position (reveived)" + msg.position());
			_log.debug("remaining (reveived)" + msg.remaining());
			msg.mark();
			int len = msg.getInt();
			_log.debug("length (reveived)" + len);
			_log.debug("remaining (reveived)" + msg.remaining());
			if (msg.remaining() < len){
				_log.debug("Not enough characters, will rerun filterchain");
				msg.reset();
				return ctx.getStopAction(msg);
			}
			byte[] saslData = new byte[len];
			msg.get(saslData);
			
			Buffer xdrBuffer = _packetWrapper.unwrap(saslData);
			xdrBuffer.dumpHex(System.err);
			ctx.setMessage(xdrBuffer);
			return ctx.getInvokeAction();
		} else {
			_log.debug("not atctive");
			return ctx.getInvokeAction();
		}
	}

	@Override
	public NextAction handleWrite(FilterChainContext ctx) throws IOException {
		_log.debug(this+" HandleWrite: isActive: " + isActive());
		if (isActive()){
			_log.debug("HandleWrite active");
			Buffer msgAsBuf = ctx.getMessage();
			_log.debug("Msg, " + msgAsBuf.position());
			_log.debug("Msg, " + msgAsBuf.limit());
			_log.debug("Msg, " + msgAsBuf.remaining());
			Buffer output = _packetWrapper.wrap(msgAsBuf);
			Buffer marker = GrizzlyMemoryManager.allocate(4);
			marker.order(ByteOrder.BIG_ENDIAN);
			marker.putInt(output.remaining());
			marker.flip();
	        marker.allowBufferDispose(true);
	        Buffer composite = GrizzlyMemoryManager.createComposite(marker, output);
			msgAsBuf.dispose();
			_log.debug("out, " + output.position());
			_log.debug("out, " + output.limit());
			_log.debug("out, " + output.remaining());
			output.dumpHex(System.out);
			_log.debug("compo, " + composite.position());
			_log.debug("sompo, " + composite.limit());
			_log.debug("sompo, " + composite.remaining());
			composite.dumpHex(System.out);
			ctx.setMessage(composite);
			
			
			return ctx.getInvokeAction();
		} else {
			_log.debug("not atctive");
			return ctx.getInvokeAction();
		}
	}
	
	
	private boolean isActive() {
		return (null != _packetWrapper);
	}

}
