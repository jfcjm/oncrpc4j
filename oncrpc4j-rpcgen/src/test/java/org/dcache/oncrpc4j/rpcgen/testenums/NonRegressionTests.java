package org.dcache.oncrpc4j.rpcgen.testenums;

import static org.junit.Assert.*;
import static spoon.testing.Assert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;

import org.acplt.oncrpc.apps.jrpcgen.jrpcgen;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.BeforeClass;
import org.junit.Test;

import spoon.Launcher;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtType;
/**
 * Non regression tests.
 * @author jmk
 *
 */
public class NonRegressionTests {
    private static Launcher spoon;
    

    private static final String JAVA_FILE_DIR = "target/generated-test-sources/rpc/org/dcache/oncrpc4j/rpcgen";
    @BeforeClass
    public static void createSpoonBase() throws MavenInvocationException, FileNotFoundException, IOException, XmlPullParserException {
        String args[] = {
                "-verbose",
                "-d", "target/generated-test-sources/rpc/",
                "-p", "org.dcache.oncrpc4j.rpcgen",
                "-bean",
                "-asyncfuture",
                "-asynccallback",
                "-oneway",
                "-c",
                "TrafficLightClient",
                "-s",
                "TrafficLightServer",
                "src/test/xdr/TrafficLight.x"
        };
        jrpcgen.main(args);
        

        File baseJavaCode = new File(getBaseJavaFilePath("TrafficLight.java"));
        assertTrue(baseJavaCode.exists());
        
        spoon = new Launcher();
        spoon.addInputResource("target/generated-test-sources/rpc");
        spoon.run();
        

        CtType<Object> baseTrafficLight = spoon.getFactory().Type().get("org.dcache.oncrpc4j.rpcgen.TrafficLightColor");
        assertNotNull(baseTrafficLight);
        
    }
    private static final String getBaseJavaFilePath(String name) {
        return Paths.get(JAVA_FILE_DIR,name).toString();
    }
  //mvn  -Dexec.skip=true -Dtest="**/NonRegressionTests.java" test
    @Test
    public void testNonRegression() {
        CtType<Object> type = spoon.getFactory().Type().get("org.dcache.oncrpc4j.rpcgen.TrafficLightColor");
        assertNotNull(type);
        assertTrue(type.isInterface());
        CtField<?> redDefintion = type.getField("RED");
        assertNotNull(redDefintion);
        System.err.println(redDefintion.toString());;
        assertThat(type.getField("RED")).isEqualTo("public static final int RED = 1;");
    }
}
