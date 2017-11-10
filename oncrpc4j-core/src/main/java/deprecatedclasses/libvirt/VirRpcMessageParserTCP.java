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
 ******************************************************************************/

package deprecatedclasses.libvirt;

import java.io.IOException;

import java.nio.ByteOrder;

import org.dcache.xdr.GrizzlyMemoryManager;
import org.dcache.xdr.Xdr;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.memory.BuffersBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import deprecatedclasses.oncrpc.RpcMessageParserTCP;
@Deprecated
public class VirRpcMessageParserTCP extends  RpcMessageParserTCP{
	final static Logger logger = LoggerFactory.getLogger(VirRpcMessageParserTCP.class);
    /**
     * RPC fragment record marker mask
     */
    private final static int RPC_LAST_FRAG = 0x00000000;
    /**
     * RPC fragment size mask
     */
    private final static int RPC_SIZE_MASK = 0xffffffff;

    
    
    
    
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

    @Override
    protected boolean isAllFragmentsArrived(Buffer messageBuffer) throws IOException {
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
    

    @Override
    protected  int getMessageSize(int marker) {
        return marker & RPC_SIZE_MASK;
    }
    @Override
    protected  boolean isLastFragment(int marker) {
        return (marker & RPC_LAST_FRAG) == 0;
    }
    
    protected Xdr assembleXdr(Buffer messageBuffer) {

        Buffer currentFragment = null;
        BuffersBuffer multipleFragments = null;

        boolean messageComplete;
        do {
            int messageMarker = messageBuffer.getInt();

            int size = getMessageSize(messageMarker);
            messageComplete = isLastFragment(messageMarker);

            int pos = messageBuffer.position();
            logger.debug("processing msg in reassemble loop: size {} limit {}, pos {}, pos+zize {} ",size,messageBuffer.limit(),pos,pos+size);
            try {
            currentFragment = messageBuffer.slice(pos, pos + size);
            } catch (Exception e){
            	e.printStackTrace();
            }
            currentFragment.limit(size);
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
            logger.debug("Reassemble loop: is message complete : {}", messageComplete);
        } while (!messageComplete);

        return new Xdr(multipleFragments == null ? currentFragment : multipleFragments);
    }
}
