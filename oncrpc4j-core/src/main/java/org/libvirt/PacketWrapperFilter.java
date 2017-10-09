/*******************************************************************************
 * Copyright (C) 2017 INU Champollion, Albi, France
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 ******************************************************************************/package org.libvirt;

import java.io.IOException;
import java.nio.ByteOrder;

import org.dcache.xdr.GrizzlyMemoryManager;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketWrapperFilter extends BaseFilter {
	private final static Logger _log = LoggerFactory.getLogger(PacketWrapperFilter.class);
	private  RpcPacketWrapper _packetWrapper           = null;
    private SASLPacketWrapper _nextWritePacketWrapper   = null;
	
	public synchronized void  setPacketWrapper(RpcPacketWrapper packetWrapper){
		_log.debug(this+" setting packetwrapper");
		_packetWrapper = packetWrapper;
		_log.debug("now active ?"+isActive());
	}

    public synchronized void setPacketWrapperAfterNextWrite(SASLPacketWrapper pw) {
        _log.debug(this+" will install packetwrapper after nex write");
        _nextWritePacketWrapper = pw;
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
			ctx.setMessage(composite);
			
			
			return ctx.getInvokeAction();
		} else {
			_log.debug("not atctive");
            
            if (isPacketWrapperWaiting()){
                
                _packetWrapper = _nextWritePacketWrapper;
                _nextWritePacketWrapper = null;
            }
			return ctx.getInvokeAction();
		}
	}
	
	
	private boolean isPacketWrapperWaiting() {
        return (null != _nextWritePacketWrapper);
    }
    private boolean isActive() {
		return (null != _packetWrapper);
	}

}
