package org.libvirt.pureapi;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.dcache.oncrpc4j.rpcgen.libvirt.LibvirtProtocolClient;
import org.dcache.oncrpc4j.rpcgen.libvirt.remote_node_device_destroy_args;
import org.dcache.oncrpc4j.rpcgen.libvirt.remote_node_device_dettach_args;
import org.dcache.oncrpc4j.rpcgen.libvirt.remote_node_device_get_parent_args;
import org.dcache.oncrpc4j.rpcgen.libvirt.remote_node_device_get_parent_ret;
import org.dcache.oncrpc4j.rpcgen.libvirt.remote_node_device_num_of_caps_args;
import org.dcache.oncrpc4j.rpcgen.libvirt.remote_node_device_num_of_caps_ret;
import org.dcache.oncrpc4j.rpcgen.libvirt.remote_nonnull_node_device;
import org.dcache.oncrpc4j.rpcgen.libvirt.remote_nonnull_string;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.RpcAuth;

public class Device {

	private Connect m_connect;
	private remote_nonnull_node_device m_device;
	private LibvirtProtocolClient m_client;
	private long _timeoutValue ;;
	private TimeUnit _timeoutUnit;
	private RpcAuth _auth;

	private Device() {
		super();
	}

	public Device(Connect connect, remote_nonnull_node_device dev) {
		this();
		 m_connect = connect;
		 m_client = connect.getClient();
		 m_device = dev;
		 
	}
	
	@FunctionalInterface
	public interface VoidOperator<A> {

		void call(A arg, long _timeoutValue, TimeUnit _timeoutUnit, RpcAuth _auth) throws OncRpcException, IOException, TimeoutException;
		
	}
	
	@FunctionalInterface
	public interface Operator<R,A> {

		R call(A arg, long _timeoutValue, TimeUnit _timeoutUnit, RpcAuth _auth) throws OncRpcException, IOException, TimeoutException;
		
	}
	
	@FunctionalInterface
	public interface NameSetter<A> {
		void setName(A arg,remote_nonnull_string aString);
	}

	
	@FunctionalInterface
	public interface InstanceCreator<A> {
		A create();
	}
	
	class DeviceProcessor<A> {
		NameSetter<A> _nameSetter;
		private A _arg;
		private VoidOperator<A> _operator;
		DeviceProcessor(VoidOperator<A> operator,InstanceCreator<A> creator, NameSetter<A> nameSetter){
			_arg = creator.create();
			_nameSetter = nameSetter;
			_operator = operator;
		}
		 public <R> R operate(Operator<R,A> operator) throws OncRpcException, IOException, TimeoutException{
			 _nameSetter.setName(_arg,getDevName());
			 R result=operator.call(_arg, _timeoutValue, _timeoutUnit, _auth);
			 return result;
		 }
		 public  void voidOperate() throws OncRpcException, IOException, TimeoutException{
			 _nameSetter.setName(_arg,getDevName());
			 _operator.call(_arg, _timeoutValue, _timeoutUnit, _auth);
		 }
		 
	}
	
	
	public void destroy() throws OncRpcException, IOException, TimeoutException {
		new DeviceProcessor<>(
				getClient()::NodeDeviceDestroy_1,
				remote_node_device_destroy_args::new, 
				(x,y)->x.setName(y)).voidOperate();
	}
	
	public void detach() throws OncRpcException, IOException, TimeoutException {
		remote_node_device_dettach_args arg1 = new remote_node_device_dettach_args();
		arg1.setName(getDevName());
		getClient().NodeDeviceDettach_1(arg1, _timeoutValue, _timeoutUnit, _auth);
	}
	public String getName(){
		return getDevName().value;
	}
	public int getNumberOfCapabilities() throws OncRpcException, IOException, TimeoutException {
		remote_node_device_num_of_caps_args arg1 = new remote_node_device_num_of_caps_args();
		arg1.setName(getDevName());
		return getClient().NodeDeviceNumOfCaps_1(arg1, _timeoutValue, _timeoutUnit, _auth).getNum();
	}
	public String getParent() throws OncRpcException, IOException, TimeoutException{
		 remote_node_device_get_parent_args arg1 = new remote_node_device_get_parent_args();
		 arg1.setName(getDevName());
		remote_node_device_get_parent_ret parent = getClient().NodeDeviceGetParent_1(arg1, _timeoutValue, _timeoutUnit, _auth);
		return parent.getParent().value.value;
	}
	
	
	
	private remote_nonnull_string getDevName() {
		return m_device.getName();
	}

	private LibvirtProtocolClient getClient() {
		return m_connect.getClient();
		
	}
}
