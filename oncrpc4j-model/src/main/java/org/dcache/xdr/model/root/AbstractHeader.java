package org.dcache.xdr.model.root;

import java.io.IOException;

import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.RpcAuth;
import org.dcache.xdr.RpcCredential;
import org.dcache.xdr.RpcMismatchReply;
import org.dcache.xdr.Xdr;
import org.dcache.xdr.XdrAble;
import org.dcache.xdr.XdrDecodingStream;
import org.dcache.xdr.XdrEncodingStream;
import org.dcache.xdr.model.itf.HeaderItf;

public class AbstractHeader implements  HeaderItf {

    private static final int RPCVERS = 0;
    private int _rpcvers;
    private int _prog;
    private int _version;
    private int _proc;
    private RpcAuth _cred;


    public AbstractHeader(Xdr xdr) throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    /* (non-Javadoc)
     * @see org.dcache.xdr.model.root.HeaderItf2#getRpcVers()
     */
    @Override
    public int getRpcVers() {
        return _rpcvers;
    }

    /* (non-Javadoc)
     * @see org.dcache.xdr.model.root.HeaderItf2#getProg()
     */
    @Override
    public int getProg() {
        return _prog;
    }

    /* (non-Javadoc)
     * @see org.dcache.xdr.model.root.HeaderItf2#getVersion()
     */
    @Override
    public int getVersion() {
        return _version;
    }

    /* (non-Javadoc)
     * @see org.dcache.xdr.model.root.HeaderItf2#getProc()
     */
    @Override
    public int getProc() {
        return _proc;
    }

    /* (non-Javadoc)
     * @see org.dcache.xdr.model.root.HeaderItf2#getCredential()
     */
    @Override
    public  RpcAuth getCredential() {
        return _cred;
    }

    @Override
    public void xdrDecode(XdrDecodingStream xdr) throws OncRpcException, IOException {
        _rpcvers = xdr.xdrDecodeInt();
        if (_rpcvers != RPCVERS) {
           throw new RpcMismatchReply(_rpcvers, 2);
        }

       _prog = xdr.xdrDecodeInt();
       _version = xdr.xdrDecodeInt();
       _proc = xdr.xdrDecodeInt();
       _cred = RpcCredential.decode(xdr);

    }

    @Override
    public void xdrEncode(XdrEncodingStream xdr) throws OncRpcException, IOException {
        // TODO Auto-generated method stub

    }
    
}
