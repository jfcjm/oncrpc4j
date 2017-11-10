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

import org.dcache.xdr.model.root.GenAbstractRpcMessageParserTCP;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.memory.BuffersBuffer;

public final class RpcMessageParserTCP extends GenAbstractRpcMessageParserTCP<OncRpcSvc>  {

    /**
     * RPC fragment record marker mask
     */
    private final static int RPC_LAST_FRAG = 0x80000000;
    /**
     * RPC fragment size mask
     */
    private final static int RPC_SIZE_MASK = 0x7fffffff;

    @Override
    protected boolean isAllFragmentsArrived(Buffer messageBuffer) throws IOException {
        final Buffer buffer = messageBuffer.duplicate();
        buffer.order(ByteOrder.BIG_ENDIAN);

        while (buffer.remaining() >= 4) {

            int messageMarker = buffer.getInt();
            int size = getMessageSize(messageMarker);

            /*
             * fragmen size bigger than we have received
             */
            if (size > buffer.remaining()) {
                return false;
            }

            /*
             * complete fragment received
             */
            if (isLastFragment(messageMarker)) {
                return true;
            }

            /*
             * seek to the end of the current fragment
             */
            buffer.position(buffer.position() + size);
        }

        return false;
    }

    protected  int getMessageSize(int marker) {
        return marker & RPC_SIZE_MASK;
    }

    protected  boolean isLastFragment(int marker) {
        return (marker & RPC_LAST_FRAG) != 0;
    }
    @Override
    protected Xdr assembleXdr(Buffer messageBuffer) {

        Buffer currentFragment;
        BuffersBuffer multipleFragments = null;

        boolean messageComplete;
        do {
            int messageMarker = messageBuffer.getInt();

            int size = getMessageSize(messageMarker);
            messageComplete = isLastFragment(messageMarker);

            int pos = messageBuffer.position();
            currentFragment = messageBuffer.slice(pos, pos + size);
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
        } while (!messageComplete);

        return new Xdr(multipleFragments == null ? currentFragment : multipleFragments);
    }
}
