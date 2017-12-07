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
import org.dcache.xdr.gss.RpcAuthGss;

/**
 * The RPC call message has two authentication fields - the credential and verifier.
 * The verifier may change it's value depending on state of credential (RPCGSS_SEC).
 * A reply uses only verifier.
 */
public class RpcCredential extends BaseRpcCredential{

    private RpcCredential() {}

    public static RpcAuth decode(XdrDecodingStream xdr) throws OncRpcException, IOException {
        RpcAuth credential;
        try {
            credential = BaseRpcCredential.decode(xdr);
        } catch(RpcAuthException e) {
            int authType = xdr.xdrDecodeInt();
            if (RpcAuthType.RPCGSS_SEC == authType) {
                credential = new RpcAuthGss();
            } else {
                throw new RpcAuthException("Unsuported type: " + authType,
                                new RpcAuthError(RpcAuthStat.AUTH_FAILED));
            }
            credential.xdrDecode(xdr);
        }
        return credential;
        
    }
}
