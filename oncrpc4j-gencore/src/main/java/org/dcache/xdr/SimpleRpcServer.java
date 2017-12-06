/*
 * Copyright (c) 2009 - 2017 Deutsches Elektronen-Synchroton,
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

import org.dcache.xdr.model.itf.OncRpcSvcBuilderItf;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.dcache.xdr.model.root.AbstractOncRpcSvc;
import org.dcache.xdr.model.root.AbstractOncRpcSvcBuilder;
import org.dcache.xdr.model.root.AbstractSimpleRpcServer;
import org.dcache.xdr.portmap.OncRpcEmbeddedPortmap;

public class SimpleRpcServer extends AbstractSimpleRpcServer<IOncRpcSvc,IOncRpcCall,IOncRpcSvcBuilder>{

    public static void main(String[] args) throws Exception {
        new SimpleRpcServer().process(args);
    }
    
    @Override
    protected void doPreStartAction() {
        new OncRpcEmbeddedPortmap();
        
    }

    @Override
    protected OncRpcSvcBuilderItf<IOncRpcSvc, IOncRpcCall, IOncRpcSvcBuilder> createOncRpcSvcBuilder(int port) {
        return new GenOncRpcSvcBuilder()
                .withTCP()
                .withAutoPublish();
    }

}
