package org.dcache.oncrpc4j.rpcgen.testenums;

import static org.junit.Assert.*;
import static spoon.testing.Assert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;

import org.acplt.oncrpc.apps.jrpcgen.jrpcgen;
import org.apache.log4j.Logger;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.BeforeClass;
import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.TypeFactory;


// mvn  -Dexec.skip=true -Dtest="**/TestGenerateEnumCode.java" test
public class TestGenerateEnumCode {
    public static final Logger LOGGER = Logger.getLogger(TestGenerateEnumCode.class);
    private static Launcher spoon;


    private static final String JAVA_FILE_DIR_ENUM = "target/generated-test-sources/rpc-with-enum/org/dcache/oncrpc4j/rpcgen/enums";
    
    static String[] baseArgArray ;
    
    /**
     * Call maven task to generate java code before running test cases
     * 
     * @throws MavenInvocationException
     * @throws XmlPullParserException 
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    
    public static void createSpoonEnum() {
        
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
        
        File enumJavaCode = new File(getJavaFilePath("TrafficLight.java"));

        System.err.println("Checking " + enumJavaCode.getPath());
        assertTrue(enumJavaCode.exists());
        
        spoon = new Launcher();
        spoon.addInputResource("target/generated-test-sources/rpc-with-enum");
        spoon.run();
        
        CtType<Object> enumTrafficLigth = spoon.getFactory().Type().get("org.dcache.oncrpc4j.rpcgen.enums.TrafficLightColor");
        assertNotNull(enumTrafficLigth);
        
    }
    @BeforeClass
    public static void invokeMavenTarget() {
    createSpoonEnum();

    }

    private static final String getJavaFilePath(String name) {
        return Paths.get(JAVA_FILE_DIR_ENUM,name).toString();
    }
    
    
    @Test
    public void test() {
        CtType<Object> type = spoon.getFactory().Type().get("org.dcache.oncrpc4j.rpcgen.enums.TrafficLightColor");
        assertNotNull(type);
        
        //Check if TrafficLightColor is an enum
        assertTrue(type.isEnum());
        CtField<?> redDefintion = type.getField("RED");
        assertNotNull(redDefintion);
        assertThat(redDefintion).isEqualTo("RED(1)");
        
        
        CtType<Object> client = spoon.getFactory().Type().get("org.dcache.oncrpc4j.rpcgen.enums.TrafficLightClient");
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
    @Test
    public void TestGenCodeForDependency(){
        CtType<Object> reducedType = spoon.getFactory().Type().get("org.dcache.oncrpc4j.rpcgen.enums.ReducedTrafficLightColor");
        assertNotNull(reducedType);
        
        CtField<?> redDefintion = reducedType.getField("ReducedRED");
        System.err.println(redDefintion);
        assertThat(redDefintion).isEqualTo("ReducedRED(org.dcache.oncrpc4j.rpcgen.enums.TrafficLightColor.RED.getValue())");
        
        CtType<Object> rainbowType = spoon.getFactory().Type().get("org.dcache.oncrpc4j.rpcgen.enums.RainbowTrafficLightColor");
        assertNotNull(rainbowType);
        System.err.println(rainbowType);
        CtField<?> rainbowRed = rainbowType.getField("RAINBOW_RED");
        assertNotNull(rainbowRed);
        assertThat(rainbowRed).isEqualTo("RAINBOW_RED(org.dcache.oncrpc4j.rpcgen.enums.ReducedTrafficLightColor.ReducedRED.getValue())");
    }
}
