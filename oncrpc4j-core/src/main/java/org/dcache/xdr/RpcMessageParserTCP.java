/*
 * Copyright (c) 2009 - 2012 Deutsches Elektronen-Synchroton,
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program (see the file COPYING.LIB for more
 * details); if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.dcache.xdr;

import java.io.IOException;

import java.nio.ByteOrder;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.memory.BuffersBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcMessageParserTCP extends BaseFilter {
	final static Logger logger = LoggerFactory.getLogger(RpcMessageParserTCP.class);
    /**
     * RPC fragment record marker mask
     */
    private final static int RPC_LAST_FRAG = 0x00000000;
    /**
     * RPC fragment size mask
     */
    private final static int RPC_SIZE_MASK = 0xffffffff;

    @Override
    public NextAction handleRead(FilterChainContext ctx) throws IOException {
    	logger.debug("handle read");
        Buffer messageBuffer = ctx.getMessage();
        if (messageBuffer == null) {
        	logger.debug("Message buffer is null, invoke stopAction");
            return ctx.getStopAction();
        }

        if (!isAllFragmentsArrived(messageBuffer)) {
        	logger.debug("Not all fragment are arrived, invoke stopAction");
            return ctx.getStopAction(messageBuffer);
        }
        ctx.setMessage(assembleXdr(messageBuffer));

        final Buffer reminder = messageBuffer.hasRemaining()
                ? messageBuffer.split(messageBuffer.position()) : null;
        logger.debug("NextAction");
        return ctx.getInvokeAction(reminder);
    }

    @Override
    public NextAction handleWrite(FilterChainContext ctx) throws IOException {
    	logger.debug("will write a message");
        Buffer b = ctx.getMessage();
        int len = b.remaining() | RPC_LAST_FRAG;
        logger.debug("Length of ctx message: {}", len);
        Buffer marker = GrizzlyMemoryManager.allocate(4);
        marker.order(ByteOrder.BIG_ENDIAN);
        logger.debug("Length of sent message: {}", len+4);
        marker.putInt(len+4);
        marker.flip();
        marker.allowBufferDispose(true);
        b.allowBufferDispose(true);
        Buffer composite = GrizzlyMemoryManager.createComposite(marker, b);
        composite.allowBufferDispose(true);
        ctx.setMessage(composite);
        logger.debug("Invoke next action");
        return ctx.getInvokeAction();
    }

    private boolean isAllFragmentsArrived(Buffer messageBuffer) throws IOException {
        final Buffer buffer = messageBuffer.duplicate();
        buffer.order(ByteOrder.BIG_ENDIAN);
        logger.debug("buffer remaining: {}",buffer.remaining());
        while (buffer.remaining() >= 4) {
        	
            int messageMarker = buffer.getInt();
            int size = getMessageSize(messageMarker);
            
            logger.debug("message marker {}, size: {}, buffer remaining: {}",messageMarker,size,buffer.remaining());
            /*
             * fragmen size bigger than we have received
             */
            if (size > buffer.remaining()+4) {
            	logger.debug("size is > buffer.remaining()+4, will return false");
                return false;
            }
            logger.debug("size is < buffer.remaining()+4, continue");
            /*
             * complete fragment received
             */
            if (isLastFragment(messageMarker)) {
            	logger.debug("message is last fragment, will return true");
                return true;
            }
            logger.debug("message is not last fragment");
            /*
             * seek to the end of the current fragment
             */
            logger.debug("going to end of fragment at {}",buffer.position() + size);
            buffer.position(buffer.position() + size);
        }
        logger.debug("isAllFragmentsArrived will return false");
        return false;
    }

    private static int getMessageSize(int marker) {
        return marker & RPC_SIZE_MASK;
    }

    private static boolean isLastFragment(int marker) {
    	//JMK : no message continaution in libvirt
        return (marker & RPC_LAST_FRAG) == 0;
    }

    private Xdr assembleXdr(Buffer messageBuffer) {

        Buffer currentFragment = null;
        BuffersBuffer multipleFragments = null;

        boolean messageComplete;
        do {
        	logger.debug("Message is not complete");
        	logger.debug("msgBufferClass "+messageBuffer.getClass().getName());
            int messageMarker = messageBuffer.getInt();
            logger.debug("mitan reassemble 1");

            int size = getMessageSize(messageMarker);
            logger.debug("mitan reassemble 2");
            messageComplete = isLastFragment(messageMarker);
            logger.debug("mitan reassemble 3");

            int pos = messageBuffer.position();
            logger.debug("limit {}, pos {}, size {}, pos+zize {} ",messageBuffer.limit(),pos,size,pos+size);
            try {
            currentFragment = messageBuffer.slice(pos, pos + size);
            } catch (Exception e){
            	e.printStackTrace();
            }
            logger.debug("mitan reassemble 5");
            currentFragment.limit(size);
            logger.debug("mitan reassemble");
            messageBuffer.position(pos + size);
            if (!messageComplete & multipleFragments == null) {
                /*
                 * we use composite buffer only if required
                 * as they not for free.
                 */
                multipleFragments = GrizzlyMemoryManager.create();
            }

            if (multipleFragments != null) {
                multipleFragments.append(currentFragment);
            }
            logger.debug("end reassemble");
        } while (!messageComplete);

        return new Xdr(multipleFragments == null ? currentFragment : multipleFragments);
    }
}
