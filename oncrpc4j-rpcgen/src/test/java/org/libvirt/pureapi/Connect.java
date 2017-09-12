package org.libvirt.pureapi;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;

import org.dcache.oncrpc4j.rpcgen.libvirt.LibvirtProtocolClient;
import org.dcache.oncrpc4j.rpcgen.libvirt.remote_auth_sasl_init_ret;
import org.dcache.oncrpc4j.rpcgen.libvirt.remote_auth_sasl_start_args;
import org.dcache.oncrpc4j.rpcgen.libvirt.remote_auth_sasl_start_ret;
import org.dcache.oncrpc4j.rpcgen.libvirt.remote_auth_sasl_step_args;
import org.dcache.oncrpc4j.rpcgen.libvirt.remote_auth_sasl_step_ret;
import org.dcache.oncrpc4j.rpcgen.libvirt.remote_connect_baseline_cpu_args;
import org.dcache.oncrpc4j.rpcgen.libvirt.remote_connect_baseline_cpu_ret;
import org.dcache.oncrpc4j.rpcgen.libvirt.remote_connect_compare_cpu_args;
import org.dcache.oncrpc4j.rpcgen.libvirt.remote_connect_compare_cpu_ret;
import org.dcache.oncrpc4j.rpcgen.libvirt.remote_connect_get_capabilities_ret;
import org.dcache.oncrpc4j.rpcgen.libvirt.remote_connect_get_hostname_ret;
import org.dcache.oncrpc4j.rpcgen.libvirt.remote_connect_open_args;
import org.dcache.oncrpc4j.rpcgen.libvirt.remote_node_device_create_xml_args;
import org.dcache.oncrpc4j.rpcgen.libvirt.remote_node_device_create_xml_ret;
import org.dcache.oncrpc4j.rpcgen.libvirt.remote_node_device_lookup_by_name_args;
import org.dcache.oncrpc4j.rpcgen.libvirt.remote_node_device_lookup_by_name_ret;
import org.dcache.oncrpc4j.rpcgen.libvirt.remote_node_get_free_memory_ret;
import org.dcache.oncrpc4j.rpcgen.libvirt.remote_nonnull_string;
import org.dcache.oncrpc4j.rpcgen.libvirt.remote_string;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.RpcAuth;
import org.libvirt.CPUCompareResult;
import org.libvirt.VirRpcCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Connect implements Closeable{
    private final static Logger _log = LoggerFactory.getLogger(VirRpcCall.class);
	private static final long DEFAULT_TIMEOUT_VALUE = 3;
	private static final TimeUnit DEFAULT_TIMEOUT_UNIT = TimeUnit.SECONDS;
	private static final RpcAuth DEFAULT_AUTH = null;
	
	private static final int DEFAULT_TCP_PORT = 16509;
	private LibvirtProtocolClient m_client;
	private long _timeoutValue;
	private TimeUnit _timeoutUnit;
	private RpcAuth _auth;
	private String m_hostname;
	
	private Connect(){
		super();
		_timeoutValue 	= DEFAULT_TIMEOUT_VALUE;
		_timeoutUnit 	= DEFAULT_TIMEOUT_UNIT;
		_auth			= DEFAULT_AUTH;
	}
	
	public Connect(String connectUtiString) throws OncRpcException, IOException, TimeoutException, URISyntaxException{
		this(connectUtiString,false);
	}
	public Connect(String connectUtiString, boolean readOnly) throws OncRpcException, IOException, TimeoutException, URISyntaxException {
		
		this();
		final URI connectUri = new URI(connectUtiString);
		final VirtURI virConnectURI = new VirtURI(connectUri);
		m_hostname = connectUri.getHost();
		
		final InetAddress host 	= 	InetAddress.getByName(m_hostname);
		int 		port	=	connectUri.getPort();
		port = -1 == port ? DEFAULT_TCP_PORT : port;
		
		
		m_client = new LibvirtProtocolClient(host,port);
		
		openClient(virConnectURI,readOnly);
		
	}
	
	/**
	 * @param connectUri
	 * @param readOnly
	 * @throws OncRpcException
	 * @throws IOException
	 * @throws TimeoutException
	 * @throws URISyntaxException 
	 */
	private void openClient(VirtURI connectUri, boolean readOnly) throws OncRpcException, IOException, TimeoutException, URISyntaxException {
		LibvirtProtocolClient client = getClient();
		remote_auth_sasl_init_ret resAuthInit = client.AuthSaslInit_1(_timeoutValue, _timeoutUnit, _auth);
		_log.info("SASL INIT");
		remote_auth_sasl_start_args arg1 = new remote_auth_sasl_start_args();
		arg1.setMech(resAuthInit.getMechlist());
		arg1.setData(new byte[0]);
		
		remote_auth_sasl_start_ret resStart = client.AuthSaslStart_1(arg1, _timeoutValue, _timeoutUnit, _auth);
		_log.info("SASL START");
		byte[] saslData = resStart.getData();
		String[] mechanisms = {
				   resAuthInit.getMechlist().value
		};
		_log.info("Connection with connectUri {}",connectUri.toString());
		String username = connectUri.getUserName();
	    String password = connectUri.getPassword();
	    _log.info("SASL Connection to {} with username {} and password {}",m_hostname,username,password);
	    String saslUserName=constructSaslUserName(username,m_hostname);
	    _log.info("saslusername {}",saslUserName);
		CallbackHandler callbackHandler = new CallbackHandler(){
			@Override
			public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
				   for (Callback cb : callbacks) {
					    if (cb instanceof NameCallback) {
					     NameCallback nc = (NameCallback)cb;
						nc.setName(saslUserName);
						_log.info("namecallback {}",nc.getName());
					    } else if (cb instanceof PasswordCallback) {
					     PasswordCallback pc = (PasswordCallback)cb;
						pc.setPassword(password.toCharArray());
						_log.info("pwdcallback {}",pc.getPassword());
					    }
					   }
					  };
			
			
		};

		
		Map<String, String> props = new TreeMap<>();
		props.put(Sasl.QOP,"auth-conf");
		SaslClient sc = Sasl.createSaslClient(mechanisms, saslUserName, "libvirt", 
                m_hostname, props, callbackHandler);
		
		byte[] resSaslEval = sc.evaluateChallenge(saslData);
		remote_auth_sasl_step_args arg2 = new remote_auth_sasl_step_args();
		arg2.setData(resSaslEval);
		remote_auth_sasl_step_ret resStep = client.AuthSaslStep_1(arg2, 10, TimeUnit.SECONDS, null);

		_log.info("SASL STEP");
		sc.evaluateChallenge(resStep.getData());
		if (sc.isComplete()){
			_log.info("Challenge is complete, wraping packets with confidentiality enabler");
			client.setPacketWrapper(sc);
			remote_string s = new remote_string();
			remote_nonnull_string s1 = new remote_nonnull_string();
			s1.value=connectUri.getLocalURI().toString();
			s.value=s1;
			remote_connect_open_args arg4 = new remote_connect_open_args();
			arg4.setName(s);
			
			long _timeoutValue = 10;
			TimeUnit _timeoutUnit = TimeUnit.SECONDS;
			RpcAuth _auth = null;
			client.ConnectOpen_1(arg4, _timeoutValue, _timeoutUnit, _auth);
		}
		
	}

	

	private String constructSaslUserName(String username, String hostname) {
		return new StringBuilder(username).append('@').append(hostname).toString();
	}

	protected  LibvirtProtocolClient getClient() {
		return m_client;
	}

	public String baselineCPU(String[] xmlCPUs) throws OncRpcException, IOException, TimeoutException {
		remote_connect_baseline_cpu_args arg1 = new remote_connect_baseline_cpu_args();
		arg1.setXmlCPUs(toNonNullStrings(xmlCPUs));
		remote_connect_baseline_cpu_ret res = m_client.ConnectBaselineCPU_1(arg1, _timeoutValue, _timeoutUnit, _auth);
		return res.getCpu().value;
	}
	
	public CPUCompareResult compareCPU(String xmlDesc) throws OncRpcException, IOException, TimeoutException {
		remote_connect_compare_cpu_args arg1 = new
				remote_connect_compare_cpu_args();
		arg1.setXml(new remote_nonnull_string(xmlDesc));
		remote_connect_compare_cpu_ret res = m_client.ConnectCompareCPU_1(arg1, _timeoutValue, _timeoutUnit, _auth);
		return CPUCompareResult.get(res.getResult());
	}
	public Device deviceCreateXML(String xmlDesc) throws OncRpcException, IOException, TimeoutException  {
		remote_node_device_create_xml_args arg1 = new remote_node_device_create_xml_args();
		arg1.setXml_desc(toNonNullString(xmlDesc));
		remote_node_device_create_xml_ret res = m_client.NodeDeviceCreateXML_1(arg1, _timeoutValue, _timeoutUnit, DEFAULT_AUTH);
		return new Device(this,res.getDev());
	}
	
	public Device deviceLookupByName(String name) throws  OncRpcException, IOException, TimeoutException{
		remote_node_device_lookup_by_name_args arg1 = new remote_node_device_lookup_by_name_args();
		remote_node_device_lookup_by_name_ret res = 
				m_client.NodeDeviceLookupByName_1(arg1, _timeoutValue, _timeoutUnit, DEFAULT_AUTH);
		return new Device(this,res.getDev());
	}
	public String getCapabilities() throws OncRpcException, IOException, TimeoutException  {
		
		remote_connect_get_capabilities_ret res = m_client.ConnectGetCapabilities_1(_timeoutValue, _timeoutUnit, DEFAULT_AUTH);
		return res.getCapabilities().value;
	}
	
	public long getFreeMemory() throws OncRpcException, IOException, TimeoutException  {
		remote_node_get_free_memory_ret res = m_client.NodeGetFreeMemory_1(_timeoutValue, _timeoutUnit, DEFAULT_AUTH);
		return res.getFreeMem();
	}
	
	public String getHostName() throws OncRpcException, IOException, TimeoutException  {
		remote_connect_get_hostname_ret res = m_client.ConnectGetHostname_1(_timeoutValue, _timeoutUnit, DEFAULT_AUTH);
		return res.getHostname().value;
	}
	
	
	
	private remote_nonnull_string[] toNonNullStrings(String[] aStringArrays) {
		return Arrays.stream(aStringArrays).map(x -> toNonNullString(x)).toArray(remote_nonnull_string[]::new);
	}
	private remote_nonnull_string toNonNullString(String aString) {
		return new remote_nonnull_string(aString);
	}

	
	
	
	@Override
	public void close() throws IOException {
		try {
			m_client.ConnectClose_1(_timeoutValue, _timeoutUnit, _auth);
			m_client.close();
		} catch (TimeoutException e) {
			throw new IOException(e);
		}
	}
}
