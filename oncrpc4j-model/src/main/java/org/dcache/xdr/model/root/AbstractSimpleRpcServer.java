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
package org.dcache.xdr.model.root;

import org.dcache.xdr.OncRpcProgram;
import org.dcache.xdr.XdrVoid;
import org.dcache.xdr.model.itf.OncRpcSvcBuilderItf;
import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.RpcReplyItf;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.dcache.xdr.model.itf.XdrTransportItf;

public abstract class AbstractSimpleRpcServer<
    SVC_T extends RpcSvcItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>,
    CALL_T extends RpcCallItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>,
    BUILDER_T extends OncRpcSvcBuilderItf<SVC_T,CALL_T,BUILDER_T,TRANSPORT_T,REPLY_T>,
    TRANSPORT_T extends XdrTransportItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>,
    REPLY_T extends RpcReplyItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T>
    > {

    static final int DEFAULT_PORT = 1717;
    private static final int PROG_NUMBER = 100017;
    private static final int PROG_VERS = 1;

    protected  void process(String[] args) throws Exception {

        if( args.length > 1) {
            System.err.println("Usage: SimpleRpcServer <port>");
            System.exit(1);
        }

        int port = DEFAULT_PORT;
        if( args.length == 1) {
            port = Integer.parseInt(args[0]);
        }
        doPreStartAction();
        RpcSvcItf<SVC_T,CALL_T,TRANSPORT_T,REPLY_T> svc =  createOncRpcSvcBuilder(port)         
                    .withPort(port)
                    .withSameThreadIoStrategy()
                    .withJMX()
                    .withRpcService(new OncRpcProgram(PROG_NUMBER, PROG_VERS),
                                    call -> call.reply(XdrVoid.XDR_VOID))
                    .build();
        svc.start();
        System.in.read();
        svc.stop();
    }

    protected abstract OncRpcSvcBuilderItf<SVC_T, CALL_T,BUILDER_T,TRANSPORT_T,REPLY_T> createOncRpcSvcBuilder(int port);
    protected abstract void doPreStartAction(); // 
    

}
