package org.dcache.xdr.model.itf;

import org.dcache.xdr.RpcAuth;
import org.dcache.xdr.XdrAble;
/**
 * This cable define header of rpc packets under a form suitable for
 * extension or modifications by alternatives implementations
 * @author jmk
 *
 */
public interface HeaderItf<SVC_T extends RpcSvcItf<SVC_T>> extends XdrAble{
    
    int getRpcVers();

    int getProg();

    int getVersion();

    int getProc();

    RpcAuth getCredential();

}