package org.dcache.oncrpc4j.rpcgen.libvirt;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslClientFactory;

import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.RpcAuth;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import tests.credentials.TestCredentials4Libvirt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestAuthLibvirt {
	
	private static final String TEST_USER = TestCredentials4Libvirt.TEST_USER;
    private static final String VIRT_URI = "qemu:///system";
	private static final String TARHET_HOST = TestCredentials4Libvirt.TARHET_HOST;;
	boolean isLibVirtLocal = false;
	final static Logger logger = LoggerFactory.getLogger(TestLibvirtClientConnectOpen.class);
	 
	  private static final String TEST_PASSWORD = TestCredentials4Libvirt.TEST_PASSWORD; 
	private LibvirtProtocolClient client;

	long _timeoutValue =10;
	TimeUnit _timeoutUnit =TimeUnit.SECONDS;
	RpcAuth _auth = null;

	private static String mkUserName() {
		return TEST_USER+ "@" + TARHET_HOST;
	}
	private static class ClientHandler implements CallbackHandler {

		@Override
		  public void handle(Callback[] cbs) throws IOException, UnsupportedCallbackException {
		   for (Callback cb : cbs) {
		    if (cb instanceof NameCallback) {

		     System.out.println("Client - NameCallback");

		     NameCallback nc = (NameCallback)cb;
		     nc.setName(mkUserName());
		    } else if (cb instanceof PasswordCallback) {

		     System.out.println("Client - PasswordCallback");

		     PasswordCallback pc = (PasswordCallback)cb;
		     pc.setPassword(TEST_PASSWORD.toCharArray());
		    }
		   }
		  }

		 }
	
	
	

	@Before
	public void prepare () throws OncRpcException, UnknownHostException, IOException{
		logger.debug("start -- debug");
		logger.info("start -- info");
		client = new LibvirtProtocolClient(InetAddress.getByName(TARHET_HOST), 16509);
		logger.info("new client");
		assertNotNull(client);
	}
	
	@Test
	public void testAuthlist() throws OncRpcException, IOException, TimeoutException{
		{
			
			 Enumeration<SaslClientFactory> factories = Sasl.getSaslClientFactories();
			 while(factories.hasMoreElements()){
				 System.out.println(factories.nextElement().getClass().getName());
			 }
		   remote_auth_list_ret ret = client.AuthList_1(10,TimeUnit.SECONDS,null);
		   assertNotNull(ret);
		   assertNotNull(ret.getTypes());
		   assertEquals(1,ret.getTypes().length);
		   assertEquals(1,ret.getTypes()[0]);
		   assertEquals(1,remote_auth_type.REMOTE_AUTH_SASL);
		   
		   remote_auth_sasl_init_ret resAuthInit = client.AuthSaslInit_1(10, TimeUnit.SECONDS, null);
		   assertEquals("DIGEST-MD5",resAuthInit.mechlist.value);
		   remote_auth_sasl_start_args arg1 = new remote_auth_sasl_start_args();
		   arg1.mech=resAuthInit.mechlist;
		   arg1.data=new byte[0];
		   remote_auth_sasl_start_ret resStart = client.AuthSaslStart_1(arg1, 10, TimeUnit.SECONDS, null);
		   assertNotNull(resStart);
			
		   
		   assertEquals(isLibVirtLocal?165:164,resStart.data.length);
		   byte[] startData = resStart.getData();
		   String[] mechanisms = {
				   resAuthInit.mechlist.value
		   };
		   
		   String prop = System.getProperty("java.util.logging.config.file");
		   //assertEquals("src/test/resources/SASLLogging.properties",prop);
		   prop = System.getProperty("javax.security.sasl.level");
		   
		   //assertEquals("FINEST",prop);
		CallbackHandler callbackHandler = new ClientHandler();
		Map<String, String> props = new TreeMap<>();
		props.put(Sasl.QOP,"auth-conf");
		
		SaslClient sc = Sasl.createSaslClient(mechanisms, mkUserName(), "libvirt", 
	                "debjmk", props, callbackHandler);
		System.out.println(startData);;
		byte[] resSaslEval = sc.evaluateChallenge(startData);
		assertNotNull(resSaslEval);
		System.out.println(new String(resSaslEval));;
		remote_auth_sasl_step_args arg2 = new remote_auth_sasl_step_args();
		arg2.data=resSaslEval;
		remote_auth_sasl_step_ret resStep = client.AuthSaslStep_1(arg2, 10, TimeUnit.SECONDS, null);
		assertNotNull(resStep);
		assertTrue(resStep.complete==1);
		sc.evaluateChallenge(resStep.getData());
		assertTrue(sc.isComplete());
		assertEquals("auth-conf",sc.getNegotiatedProperty(Sasl.QOP));
		assertEquals("65536",sc.getNegotiatedProperty(Sasl.MAX_BUFFER));
		assertEquals("65510",sc.getNegotiatedProperty(Sasl.RAW_SEND_SIZE));
		//assertEquals("65510",sc.getNegotiatedProperty(Sasl.REUSE));
		assertEquals("high",sc.getNegotiatedProperty(Sasl.STRENGTH));
		assertEquals("65510",sc.getNegotiatedProperty("javax.security.sasl.rawsendsize"));
		assertNull(sc.getNegotiatedProperty("com.sun.security.sasl.digest.cipher"));
		assertNull(sc.getNegotiatedProperty("com.sun.security.sasl.digest.realm"));
		
		
		
		
		client.setPacketWrapper(sc);
		}
		{
		System.out.println("Before opens");
		{
		remote_string s = new remote_string();
		remote_nonnull_string s1 = new remote_nonnull_string();
		s1.value=VIRT_URI;
		s.value=s1;
		
		remote_connect_open_args arg4 = new remote_connect_open_args();
		arg4.setName(s);
		
		long _timeoutValue = 10;
		TimeUnit _timeoutUnit = TimeUnit.SECONDS;
		RpcAuth _auth = null;
		client.ConnectOpen_1(arg4, _timeoutValue, _timeoutUnit, _auth);
		}

		
		}
		System.out.println("Before version");
		remote_connect_get_version_ret resVersion = client.ConnectGetVersion_1(10, TimeUnit.SECONDS, null);
		assertEquals(isLibVirtLocal?2:2001002,resVersion.getHv_ver());
		
		System.out.println("Before domains");
		remote_connect_num_of_domains_ret resNum = client.ConnectNumOfDomains_1(10, TimeUnit.SECONDS, null);
		assertEquals(isLibVirtLocal?1:15,resNum.getNum());

		
		remote_connect_list_defined_domains_args arg6 = new remote_connect_list_defined_domains_args();
		arg6.maxnames=10;
		remote_connect_list_defined_domains_ret resDefDomains = client.ConnectListDefinedDomains_1(arg6,10, TimeUnit.SECONDS,null);
		//pourquoi 0 ?
		assertEquals(isLibVirtLocal?0:7,resDefDomains.names.length);
		
		
		remote_connect_list_all_domains_args arg3 = new remote_connect_list_all_domains_args();
		arg3.need_results=1;
		remote_connect_list_all_domains_ret resListDomains = client.ConnectListAllDomains_1(arg3,10 , TimeUnit.SECONDS, null);
		assertEquals(isLibVirtLocal?1:22,resListDomains.domains.length);
		for (remote_nonnull_domain dom: resListDomains.domains){
			System.out.println(dom.name.value);
		}
		if (! isLibVirtLocal){
			remote_connect_list_all_interfaces_args argItfIn = new
					remote_connect_list_all_interfaces_args();
			argItfIn.need_results=1;
			remote_connect_list_all_interfaces_ret resItf = client.ConnectListAllInterfaces_1(argItfIn, _timeoutValue, _timeoutUnit, _auth);
			assertEquals(15,resItf.ifaces.length);
			for (remote_nonnull_interface itf: resItf.ifaces){
				System.out.println(itf.mac.value);
				System.out.println(itf.name.value);
				 remote_interface_is_active_args argActive = new remote_interface_is_active_args();
				 argActive.iface=itf;
				 
				remote_interface_is_active_ret resAct = client.InterfaceIsActive_1(argActive, _timeoutValue, _timeoutUnit, _auth);
				System.out.println(resAct.active);
			}
		}
	}
	@Test
	public void TestSASLOpenClose() throws OncRpcException, UnknownHostException, IOException, TimeoutException{
		LibvirtProtocolClient client = virSASLConnect(VIRT_URI,InetAddress.getByName(TARHET_HOST), 16509,mkUserName(),TEST_PASSWORD);
		
		client.ConnectClose_1(10, TimeUnit.SECONDS, null);
		
	}
	@Test
	public void TestSASLInterfaces() throws OncRpcException, UnknownHostException, IOException, TimeoutException{
		try (LibvirtProtocolClient client =
				virSASLConnect(VIRT_URI,InetAddress.getByName(TARHET_HOST), 16509,mkUserName(),TEST_PASSWORD);
				)
		
		{
			long _timeoutValue =10;
			TimeUnit _timeoutUnit =TimeUnit.SECONDS;
			RpcAuth _auth = null;
			
			remote_connect_list_all_interfaces_args arg1 = 
					new remote_connect_list_all_interfaces_args();
			arg1.need_results=1;
			remote_connect_list_all_interfaces_ret res1 = 
					client.ConnectListAllInterfaces_1(arg1, _timeoutValue, _timeoutUnit, _auth);
			assertEquals(isLibVirtLocal ? 1 : 15,res1.ret);
			
		}
	}
	@Test
	public void TestSASLListDomains() throws OncRpcException, UnknownHostException, IOException, TimeoutException{
		try (LibvirtProtocolClient client =
				virSASLConnect(VIRT_URI,InetAddress.getByName(TARHET_HOST), 16509,mkUserName(),TEST_PASSWORD);
				)
		
		{
			remote_connect_list_all_domains_args arg3 = new remote_connect_list_all_domains_args();
			arg3.need_results=1;
			remote_connect_list_all_domains_ret resListDomains = client.ConnectListAllDomains_1(arg3,10 , TimeUnit.SECONDS, null);
			assertEquals(22,resListDomains.ret);
			assertEquals(22,resListDomains.getDomains().length);
			remote_nonnull_domain dom = resListDomains.getDomains(0);
			assertEquals("wupdate",dom.name.value);
			assertEquals(15,dom.id);
			assertEquals("399E0D33FC2C47FB8F3D12C452A88EF3", bytesToHex(dom.uuid.value,0,dom.uuid.value.length));
			ByteBuffer bb = ByteBuffer.wrap(dom.uuid.value);
			bb.order(ByteOrder.BIG_ENDIAN);
			assertEquals("399e0d33-fc2c-47fb-8f3d-12c452a88ef3",new UUID(bb.getLong(),bb.getLong()).toString());
			long _timeoutValue =10;
			
			TimeUnit _timeoutUnit =TimeUnit.SECONDS;
			RpcAuth _auth = null;
			
			remote_domain_get_state_args argState = new remote_domain_get_state_args();
			argState.dom=dom;
			remote_domain_get_state_ret resState = client.DomainGetState_1(argState, _timeoutValue, _timeoutUnit, _auth);
			assertEquals(1,resState.state);
		}
	}
	
	
	
	
	
	
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes, int offset, int count) {
	    char[] hexChars = new char[count * 2];
	    for ( int j = 0; j < count; j++ ) {
	        int v = bytes[j+offset] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	
	private LibvirtProtocolClient virSASLConnect(String domainUrl, InetAddress address, int port,String username, String password) throws OncRpcException, IOException, TimeoutException{
		CallbackHandler callbackHandler = new CallbackHandler(){
			@Override
			public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
				   for (Callback cb : callbacks) {
					    if (cb instanceof NameCallback) {
					     NameCallback nc = (NameCallback)cb;
					     nc.setName(username);
					    } else if (cb instanceof PasswordCallback) {
					     PasswordCallback pc = (PasswordCallback)cb;
					     pc.setPassword(password.toCharArray());
					    }
					   }
					  };
			
			
		};
		
		LibvirtProtocolClient client = new LibvirtProtocolClient(address, port);
		remote_auth_sasl_init_ret resAuthInit = client.AuthSaslInit_1(10, TimeUnit.SECONDS, null);
		   assertEquals("DIGEST-MD5",resAuthInit.mechlist.value);
		   remote_auth_sasl_start_args arg1 = new remote_auth_sasl_start_args();
		   arg1.mech=resAuthInit.mechlist;
		   arg1.data=new byte[0];

		   String[] mechanisms = {
				   resAuthInit.mechlist.value
		   };
		   
		   remote_auth_sasl_start_ret resStart = client.AuthSaslStart_1(arg1, 10, TimeUnit.SECONDS, null);
		   assertNotNull(resStart);
		   byte[] startData = resStart.getData();
		   Map<String, String> props = new TreeMap();
		   props.put(Sasl.QOP,"auth-conf");
		   SaslClient sc = Sasl.createSaslClient(mechanisms, mkUserName(), "libvirt", 
	                "debjmk", props, callbackHandler);
		   byte[] resSaslEval = sc.evaluateChallenge(startData);

			assertNotNull(resSaslEval);
			System.out.println(new String(resSaslEval));;
			remote_auth_sasl_step_args arg2 = new remote_auth_sasl_step_args();
			arg2.data=resSaslEval;
			remote_auth_sasl_step_ret resStep = client.AuthSaslStep_1(arg2, 10, TimeUnit.SECONDS, null);
			assertNotNull(resStep);
			assertTrue(resStep.complete==1);
			sc.evaluateChallenge(resStep.getData());
			assertTrue(sc.isComplete());
			assertEquals("auth-conf",sc.getNegotiatedProperty(Sasl.QOP));
			assertEquals("65536",sc.getNegotiatedProperty(Sasl.MAX_BUFFER));
			assertEquals("65510",sc.getNegotiatedProperty(Sasl.RAW_SEND_SIZE));
			//assertEquals("65510",sc.getNegotiatedProperty(Sasl.REUSE));
			assertEquals("high",sc.getNegotiatedProperty(Sasl.STRENGTH));
			assertEquals("65510",sc.getNegotiatedProperty("javax.security.sasl.rawsendsize"));
			assertNull(sc.getNegotiatedProperty("com.sun.security.sasl.digest.cipher"));
			assertNull(sc.getNegotiatedProperty("com.sun.security.sasl.digest.realm"));
			
			
			
			
			client.setPacketWrapper(sc);
			remote_string s = new remote_string();
			remote_nonnull_string s1 = new remote_nonnull_string();
			s1.value=VIRT_URI;
			s.value=s1;
			
			remote_connect_open_args arg4 = new remote_connect_open_args();
			arg4.setName(s);
			
			long _timeoutValue = 10;
			TimeUnit _timeoutUnit = TimeUnit.SECONDS;
			RpcAuth _auth = null;
			client.ConnectOpen_1(arg4, _timeoutValue, _timeoutUnit, _auth);
			
			return client;
		   
		   
		   
	}
}
