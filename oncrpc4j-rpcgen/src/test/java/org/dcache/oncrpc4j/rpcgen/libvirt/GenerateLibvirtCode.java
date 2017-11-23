package org.dcache.oncrpc4j.rpcgen.libvirt;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import org.acplt.oncrpc.apps.jrpcgen.jrpcgen;
import org.junit.Test;

public class GenerateLibvirtCode {

	@Test
	public void test() throws FileNotFoundException, Exception {
		String[] args = {
				"-debug",
				"-verbose",
				"-d", "/var/tmp",
				"-p","org.dcache.oncrpc4j.rpcgen",
				"-bean","-asyncfuture",
				"-c","LibvirtProtocolClient",
				"/home/jmk/src/git/virrpc4j/oncrpc4j-rpcgen/src/test/xdr/remote.x",
				
				
		};
		jrpcgen.main(args);
		//jrpcgen.doParse();
	}

}
