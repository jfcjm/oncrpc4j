package org.dcache.oncrpc4j.rpcgen.testenums;

import static org.junit.Assert.*;
import static spoon.testing.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

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
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.TypeFactory;

public class TestGenerateEnumCode {

    private static final String M2_HOME = "/usr/share/maven/";
    private static final String POM_XML = "pom.xml";
    private static final String MAVEN_TARGET = "exec:java@TestTrafficLightWithEnum";
    private static final String MAVEN_TARGET_NON_REGRESSION = "exec:java@TestTrafficLight";
    private static Launcher spoonBase;
    private static Launcher spoonEnum;


    private static final String GEN_TOP_DIR = "target/generated-test-sources/rpc";
    private static final String JAVA_FILE_DIR = "target/generated-test-sources/rpc/org/dcache/oncrpc4j/rpcgen";
    private static final String JAVA_FILE_DIR_ENUM = "target/generated-test-sources/rpc-with-enum/org/dcache/oncrpc4j/rpcgen/enums";
    /**
     * Call maven task to generate java code before running test cases
     * 
     * @throws MavenInvocationException
     */


    private static void createSpoonBase() throws MavenInvocationException {

        InvocationRequest request = new DefaultInvocationRequest();
        File pomFile = new File(POM_XML);
        assertTrue(pomFile.exists());
        request.setPomFile(pomFile);
         java.util.List<String> goals = Arrays.asList(new String[]{
                MAVEN_TARGET,
        });
        request.setGoals(goals);
        
        
        DefaultInvoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(M2_HOME));
        invoker.execute(request);

        File baseJavaCode = new File(getBaseJavaFilePath("TrafficLight.java"));
        assertTrue(baseJavaCode.exists());

        spoonBase = new Launcher();
        spoonBase.addInputResource("target/generated-test-sources/rpc");
        spoonBase.run();
        
    }

    private static void createSpoonEnum() {
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        //System.setOut(new PrintStream(outContent));
        String args[] = {
                "-verbose",
                "-d", "target/generated-test-sources/rpc-with-enum/",
                "-p", "org.dcache.oncrpc4j.rpcgen.enums",
                "-bean",
                "-asyncfuture",
                "-asynccallback",
                "-oneway",
                "-enums",
                "-c",
                "TrafficLightClient",
                "-s",
                "TrafficLightServer",
                "src/test/xdr/TrafficLight.x"
        };
        jrpcgen.main(args);
        System.setOut(null);
        System.err.println(outContent);
        assertFalse(outContent.toString().contains("Unrecognized option: -enums"));
        File enumJavaCode = new File(getJavaFilePath("TrafficLight.java"));

        System.err.println("Checking " + enumJavaCode.getPath());
        assertTrue(enumJavaCode.exists());
        
        spoonEnum = new Launcher();
        spoonEnum.addInputResource("target/generated-test-sources/rpc-with-enum");
        spoonEnum.run();
        
    }
    @BeforeClass
    public static void invokeMavenTarget() throws MavenInvocationException {
        createSpoonBase();
        createSpoonEnum();

    }

    private static final String getBaseJavaFilePath(String name) {
        return JAVA_FILE_DIR + File.separator + name;
    }

    private static final String getJavaFilePath(String name) {
        return JAVA_FILE_DIR_ENUM + File.separator + name;
    }
    
    /*
     *
     * 
                            <arguments>
                                <argument>-debug</argument>
                                <argument>-d</argument>
                                <argument>${project.build.directory}/generated-test-sources/rpc-with-enum</argument>
                                <argument>-p</argument>
                                <argument>org.dcache.oncrpc4j.rpcgen.enums</argument>
                                <argument>-bean</argument>
                                <argument>-asyncfuture</argument>
                                <argument>-asynccallback</argument>
                                <argument>-oneway</argument>
                                <argument>-enums</argument>
                                <argument>-c</argument>
                                <argument>TrafficLightClient</argument>
                                <argument>-s</argument>
                                <argument>TrafficLightServer</argument>
                                <argument>${project.basedir}/src/test/xdr/TrafficLight.x</argument>
                            </arguments>
     */
    
    
    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
    @Test
    public void testJrpcgenParameterExists(){
        
        
    }
  //mvn  -Dexec.skip=true -Dtest="**/TestGenerateEnumCode.java" test
    @Test
    public void testNonRegression() {
        CtType<Object> type = spoonBase.getFactory().Type().get("org.dcache.oncrpc4j.rpcgen.TrafficLightColor");
        assertNotNull(type);
        assertTrue(type.isInterface());
        CtField<?> redDefintion = type.getField("RED");
        assertNotNull(redDefintion);
        System.err.println(redDefintion.toString());;
        assertThat(type.getField("RED")).isEqualTo("public static final int RED = 1;");
    }

    //mvn  -Dexec.skip=true -Dtest="**/TestGenerateEnumCode.java" test
    @Test
    public void test() {
        CtType<Object> type = spoonEnum.getFactory().Type().get("org.dcache.oncrpc4j.rpcgen.enums.TrafficLightColor");
        assertNotNull(type);
        
        //Check if TrafficLightColor is an enum
        assertTrue(type.isEnum());
        CtField<?> redDefintion = type.getField("RED");
        assertNotNull(redDefintion);
        assertThat(redDefintion).isEqualTo("RED(1)");
        
        
        CtType<Object> client = spoonEnum.getFactory().Type().get("org.dcache.oncrpc4j.rpcgen.enums.TrafficLightClient");
        assertNotNull(type);
        System.err.println(client.toString());
        assertTrue(type.isEnum());
        //Check if TrafficLightClient as still a getter for int value
        TypeFactory typeFactory = new TypeFactory();
        CtMethod<Integer> getColorMethod = client.getMethod(typeFactory.INTEGER_PRIMITIVE,"getColor_1");
        assertNotNull(getColorMethod);
        System.err.println(getColorMethod.toString());
        //Check if TrafficLightClient as still a setter for int value
        CtMethod<Integer> setColorMethod = client.getMethod(typeFactory.INTEGER_PRIMITIVE,"setColor_1",typeFactory.INTEGER_PRIMITIVE);
        assertNotNull(setColorMethod);
        System.err.println(setColorMethod.toString());
    }

}
