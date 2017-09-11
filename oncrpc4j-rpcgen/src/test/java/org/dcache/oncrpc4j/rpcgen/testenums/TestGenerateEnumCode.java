package org.dcache.oncrpc4j.rpcgen.testenums;

import static org.junit.Assert.*;
import static spoon.testing.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Collections;

import javax.xml.ws.spi.Invoker;

import org.acplt.oncrpc.apps.jrpcgen.jrpcgen;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.Assertion;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import spoon.Launcher;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtType;

public class TestGenerateEnumCode {

    private static final String M2_HOME = "/usr/share/maven/";
    private static final String POM_XML = "pom.xml";
    private static final String MAVEN_TARGET = "exec:java@TestTrafficLight";
    private static Launcher spoon;


    private static final String GEN_TOP_DIR = "target/generated-test-sources/rpc";
    private static final String JAVA_FILE_DIR = "target/generated-test-sources/rpc/org/dcache/oncrpc4j/rpcgen";
    /**
     * Call maven task to generate java code before running test cases
     * 
     * @throws MavenInvocationException
     */

    @BeforeClass
    public static void invokeMavenTarget() throws MavenInvocationException {
        InvocationRequest request = new DefaultInvocationRequest();
        File pomFile = new File(POM_XML);
        assertTrue(pomFile.exists());
        request.setPomFile(pomFile);
        request.setGoals(Collections.singletonList(MAVEN_TARGET));
        DefaultInvoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(M2_HOME));
        invoker.execute(request);

        File calculatorJavaCode = new File(getJavaFilePath("TrafficLight.java"));
        assertTrue(calculatorJavaCode.exists());

        spoon = new Launcher();
        spoon.addInputResource("target/generated-test-sources/rpc");
        spoon.run();

    }

    private static final String getJavaFilePath(String name) {
        return JAVA_FILE_DIR + File.separator + name;
    }
    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
    @Test
    public void testJrpcgenParameterExists(){
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        String args[] = {
              "-enums",
        };
        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(new Assertion(){

            @Override
            public void checkAssertion() throws Exception {
                assertFalse(outContent.toString().contains("Unrecognized option: -enums"));
            }
            
        });
        jrpcgen.main(args);
        System.setOut(null);
    }
    

    //mvn  -Dexec.skip=true -Dtest="**/TestGenerateEnumCode.java" test
    @Test
    public void test() {
        CtType<Object> type = spoon.getFactory().Type().get("org.dcache.oncrpc4j.rpcgen.TrafficLightColor");
        assertNotNull(type);
        assertTrue(type.isEnum());
        CtField<?> redDefintion = type.getField("RED");
        assertNotNull(redDefintion);
        assertThat(type.getField("RED")).isEqualTo("RED(1)");
        
        fail("Not yet implemented");
    }

}
