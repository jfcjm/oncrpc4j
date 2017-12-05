package org.dcache.generics.alt;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.dcache.generics.alt.BuilderTest.Test2OncRpcSvcItf;
import org.dcache.xdr.OncRpcProgram;
import org.dcache.xdr.model.itf.OncRpcSvcBuilderItf;
import org.dcache.xdr.model.itf.RpcDispatchableItf;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.dcache.xdr.model.itf.XdrTransportItf;
import org.dcache.xdr.model.root.AbstractOncRpcSvc;
import org.dcache.xdr.model.root.AbstractOncRpcSvcBuilder;
import org.junit.Test;
/**
 * This test class aims to show why it is better for OncRpcSvcBuilderItf
 * operations to return results og type BUILDER_T rather tahn results of type 
 * OncRpcSvcBuilderItf<DBC_T,BUILDERèT).
 * It permits to extend the builder interface without using downcasting
 * @author jmk
 *
 */
public class BuilderTest {
    interface TestOncRpcSvcItf extends RpcSvcItf<TestOncRpcSvcItf>{
        
    }
    interface TestOncRpcSvcBuilderItf extends OncRpcSvcBuilderItf<TestOncRpcSvcItf,TestOncRpcSvcBuilderItf>{
        TestOncRpcSvcBuilderItf withDummyProperty();
    }
     class TestOncRpcSvcBuilder extends 
                AbstractOncRpcSvcBuilder<TestOncRpcSvcItf,TestOncRpcSvcBuilderItf> implements TestOncRpcSvcBuilderItf{

        @Override
        protected TestOncRpcSvcBuilderItf getThis() {
            return this;
        }

        @Override
        public TestOncRpcSvcBuilderItf withDummyProperty() {
            return this;
        }

        @Override
        protected TestOncRpcSvcItf getOncRpcSvc(TestOncRpcSvcBuilderItf builder_T) {
            // TODO Auto-generated method stub
            return null;
        }
        
    }
     class  TestOncRpcSvc extends AbstractOncRpcSvc<TestOncRpcSvcItf> implements TestOncRpcSvcItf {

        protected TestOncRpcSvc(TestOncRpcSvcBuilderItf builder) {
            super(builder);
            // TODO Auto-generated constructor stub
        }

        @Override
        public TestOncRpcSvcItf getThis() {
            return this;
        }
        
    }
    @Test
    public void test() {
        // type de build
        TestOncRpcSvcItf svc = new TestOncRpcSvcBuilder().withAutoPublish().build();
        //types de builder 
        TestOncRpcSvcBuilderItf b1 = new TestOncRpcSvcBuilder().withDummyProperty();
        //Définition initiale : le type de retour est générique instancié"
        OncRpcSvcBuilderItf<TestOncRpcSvcItf, TestOncRpcSvcBuilderItf> b3 = new TestOncRpcSvcBuilder().withJMX();
        //Definition avec getThis() + type de retour BUILDER_T : 
        TestOncRpcSvcBuilderItf b2 = new TestOncRpcSvcBuilder().withClientMode();
        // Dans le premier cas on ne peux pas écrire ca :
        //svc = new TestOncRpcSvcBuilder().withJMX().withDummyProperty().build();
        //puique withJMX() en rasson du type de retour de withJMX() qui n'est pas TestOncRpcSvcBuilderItf
        svc = new TestOncRpcSvcBuilder().withClientMode().withJMX().build();
        svc = new TestOncRpcSvcBuilder().withDummyProperty().withClientMode().build();
        svc = new TestOncRpcSvcBuilder().withClientMode().withDummyProperty().build();
        
    }
    interface Test2OncRpcSvcBuilderItf extends TestOncRpcSvcBuilderItf {
        @Override
        public Test2OncRpcSvcItf build();
        @Override
        public Test2OncRpcSvcBuilderItf withDummyProperty();
    };
    class  Test2tOncRpcSvcBuilder extends TestOncRpcSvcBuilder implements Test2OncRpcSvcBuilderItf{

        @Override
        public Test2OncRpcSvcBuilderItf withDummyProperty() {
            // TODO Auto-generated method stub
            return null;
        }
        @Override
        public Test2OncRpcSvcItf build() {
            return new Test2OncRpcSvc(this);
        }
        
    }

    public interface Test2OncRpcSvcItf extends TestOncRpcSvcItf{

    }
    class Test2OncRpcSvc extends TestOncRpcSvc implements Test2OncRpcSvcItf {

        protected Test2OncRpcSvc(Test2OncRpcSvcBuilderItf builder) {
            super(builder);
        }
        
    }
    @Test
    public void test2() {
        //We cannot do that :
        // Cannot do that TestOncRpcSvcItf svc = new Test2tOncRpcSvcBuilder().withAutoPublish().withDummyProperty();
        // pas trop important pour le moment ...
        /*
         Test2OncRpcSvcBuilderItf b = new Test2tOncRpcSvcBuilder().withDummyProperty();
        b= b.withClientMode().withDummyProperty();
        b=b.withClientMode();
         svc = new Test2tOncRpcSvcBuilder().withJMX().build();
         */
    }

}
