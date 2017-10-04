package org.libvirt;



import java.nio.ByteBuffer;

import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;

import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.OncRpcRejectedException;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.memory.Buffers;
import org.glassfish.grizzly.memory.ByteBufferArray;
import org.glassfish.grizzly.memory.ByteBufferWrapper;
import org.glassfish.grizzly.memory.HeapMemoryManager;
import org.glassfish.grizzly.memory.MemoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SASLPacketWrapper implements RpcPacketWrapper {
	private final static Logger _log = LoggerFactory.getLogger(SASLPacketWrapper.class);

	private SaslClient _sc;
	private ByteBufferArray _inputByteBufferArray = ByteBufferArray.create();

	private SASLPacketWrapper() {
	}

	public SASLPacketWrapper(SaslClient sc) {
		this();
		_sc = sc;
	}

	@Override
	public Buffer unwrap(byte[] msg) throws VirRpcException {
		try {
			byte[] unwrapped = _sc.unwrap(msg, 0, msg.length);
			
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
			return _sc.wrap(msg, 0, msg.length);
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
		_log.debug("b output " +bOutput.length);
		
		
		Buffer output = new ByteBufferWrapper(ByteBuffer.wrap(bOutput));
		_log.debug("output " +output.remaining());
		_log.debug("output " +output.position());
		return output;
	}

}
