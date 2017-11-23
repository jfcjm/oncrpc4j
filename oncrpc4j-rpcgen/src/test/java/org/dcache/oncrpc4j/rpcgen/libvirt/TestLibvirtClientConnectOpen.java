package org.dcache.oncrpc4j.rpcgen.libvirt;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.RpcAuth;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tests.credentials.TestCredentials4Libvirt;
@Ignore
public class TestLibvirtClientConnectOpen {
	private static final String TARHET_HOST = TestCredentials4Libvirt.TARHET_HOST;
	final static Logger logger = LoggerFactory.getLogger(TestLibvirtClientConnectOpen.class);
	private LibvirtProtocolClient client;
	
	@Before
	public void prepare () throws OncRpcException, UnknownHostException, IOException{
		logger.debug("start -- debug");
		logger.info("start -- info");
		client = new LibvirtProtocolClient(InetAddress.getByName(TARHET_HOST), 16509);
		logger.info("new client");
		assertNotNull(client);
		System.out.println("************************ Connect OPen *************************" +client);
		remote_connect_open_args arg1 = new remote_connect_open_args();
		
		
	}
	@Test
	public void test() throws OncRpcException, UnknownHostException, IOException, TimeoutException {
		
		
		System.out.println("************************ starting tests *************************" +client);
		
		
		
		remote_string s = new remote_string();
		remote_nonnull_string s1 = new remote_nonnull_string();
		s1.value="test:///default";
		s.value=s1;
		remote_connect_open_args arg1 = new remote_connect_open_args();
        arg1.setName(s);
		long _timeoutValue = 10;
		TimeUnit _timeoutUnit = TimeUnit.SECONDS;
		RpcAuth _auth = null;
		client.ConnectOpen_1(arg1, _timeoutValue, _timeoutUnit);
		logger.info("connected open");
		
		remote_connect_num_of_defined_domains_ret res = client.ConnectNumOfDefinedDomains_1(_timeoutValue, _timeoutUnit);
		assertEquals(0,res.getNum());
		
		remote_connect_list_all_domains_args argDomain = new remote_connect_list_all_domains_args();
		argDomain.setFlags(0);
		argDomain.setNeed_results(1);
		remote_connect_list_all_domains_ret res2 = client.ConnectListAllDomains_1(argDomain, _timeoutValue, _timeoutUnit);
		assertEquals(1,res2.getDomains().length);
		assertEquals("test",res2.getDomains()[0].getName().value);
		assertEquals("8c4514c8-c94a-3def-85da-94db24089e0a",UUID.nameUUIDFromBytes(res2.getDomains()[0].getUuid().value).toString());
	
		remote_connect_list_interfaces_args argInterfaces = new remote_connect_list_interfaces_args();
		argInterfaces.setMaxnames(10);
		remote_connect_list_interfaces_ret resItf = client.ConnectListInterfaces_1(argInterfaces, _timeoutValue, _timeoutUnit);
		assertNotNull(resItf);
		assertEquals(1,resItf.names.length);
		assertEquals("eth1",resItf.names[0].value);
		
		
		
	}
	
	
	
	
	@Test
	public void testWothError() throws OncRpcException, UnknownHostException, IOException, TimeoutException {
		System.out.println(client);
		remote_connect_open_args arg1 = new remote_connect_open_args();
		
		remote_string s = new remote_string();
		remote_nonnull_string s1 = new remote_nonnull_string();
		s1.value="test:///default";
		s.value=s1;
		arg1.setName(s);
		long _timeoutValue = 10;
		TimeUnit _timeoutUnit = TimeUnit.SECONDS;
		RpcAuth _auth = null;
		client.ConnectOpen_1(arg1, _timeoutValue, _timeoutUnit);
		remote_connect_list_defined_domains_args arg2 = 
		        new remote_connect_list_defined_domains_args();
        remote_connect_list_defined_domains_ret domains = 
                client.ConnectListDefinedDomains_1(arg2, _timeoutValue, _timeoutUnit);
        assertEquals(1,domains.names.length);
	}

}
