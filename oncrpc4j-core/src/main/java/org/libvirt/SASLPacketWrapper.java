package org.libvirt;



import java.nio.ByteBuffer;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.memory.Buffers;
import org.glassfish.grizzly.memory.ByteBufferArray;
import org.glassfish.grizzly.memory.ByteBufferWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * This class will take an SASLserver or SASLClient as constructor parameter
 * Its wrap/unwrap methods can then be called in order to apply SASL data confidentiality
 * or Integrity algorithms
 * @author jmk
 *
 */
public class SASLPacketWrapper implements RpcPacketWrapper {
	private final static Logger _log = LoggerFactory.getLogger(SASLPacketWrapper.class);
	
	private ByteBufferArray _inputByteBufferArray = ByteBufferArray.create();
    @FunctionalInterface
    interface FunctionWithException<R,T,E extends Throwable>  {
        R get(T arg) throws E;
    }
    
    
    FunctionWithException<byte[],byte[],SaslException> wrapper ;
    FunctionWithException<byte[],byte[],SaslException> unwrapper ;
	private SASLPacketWrapper() {
	    _log.debug("Create a packet wrapper");
	}

	public SASLPacketWrapper(SaslClient sc) {
		this();
		wrapper   = data-> sc.wrap(data, 0, data.length);
		unwrapper = data-> sc.unwrap(data, 0, data.length);
		_log.debug("ready to apply SASLClient wrapping");
	}

	public SASLPacketWrapper(SaslServer saslSrv) {
	    this();
	    wrapper   = data-> saslSrv.wrap(data, 0, data.length);
	    unwrapper = data-> saslSrv.unwrap(data, 0, data.length);
        _log.debug("ready to apply SASLServer wrapping");
    }

    @Override
	public Buffer unwrap(byte[] msg) throws VirRpcException {
		try {
			byte[] unwrapped = unwrapper.get(msg);
			
			Buffer output = new ByteBufferWrapper(ByteBuffer.wrap(unwrapped));
			//Clonage car problème dans la version actuelle de grizzly lors 
			// d'un slice sur un ByteBufferWrapper
			return Buffers.cloneBuffer(output);
		} catch (SaslException e) {
			VirRpcSASLException exc = new VirRpcSASLException("Unable to unwrap sasl packet");
			exc.initCause(e);
			throw exc;
		}
	}
	protected byte[] wrap(byte[] msg) throws  VirRpcException{
		try {
			return wrapper.get(msg);
		} catch (SaslException e) {
			VirRpcSASLException exc = new VirRpcSASLException("Unable to wrap sasl packet");
			exc.initCause(e);
			throw exc;
		}
	}

	@Override
	public Buffer wrap(Buffer input) throws VirRpcException {
		_log.debug("input " +input.remaining());
		_log.debug("input " +input.position());
		final ByteBufferArray bba =
				input.toByteBufferArray(_inputByteBufferArray);
		final ByteBuffer bb = input.toByteBuffer();
		_log.debug("input " +bb.remaining());
		_log.debug("input " +bb.position());
		
		byte[] b = new byte[bb.remaining()];
		bb.get(b, 0, b.length);
		byte[] bOutput = wrap(b);
		_log.debug("length of output " +bOutput.length);
		
		
		Buffer output = new ByteBufferWrapper(ByteBuffer.wrap(bOutput));
		_log.debug("output " +output.remaining());
		_log.debug("output " +output.position());
		return output;
	}

}
