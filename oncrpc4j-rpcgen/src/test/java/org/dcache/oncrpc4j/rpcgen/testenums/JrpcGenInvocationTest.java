package org.dcache.oncrpc4j.rpcgen.testenums;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.acplt.oncrpc.apps.jrpcgen.jrpcgen;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.Assertion;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.rules.TemporaryFolder;

public class JrpcGenInvocationTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
    @Rule
    public TemporaryFolder tempFolder= new TemporaryFolder();
    @Test
    public void testEnumOption() {
        
        String args[] = {
                "-verbose",
                "-d", tempFolder.getRoot().getPath(),
                "-p", "org.dcache.oncrpc4j.rpcgen",
                "-bean",
                "-enums",
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
        assertTrue(new File( tempFolder.getRoot().getPath()+ "/org/dcache/oncrpc4j/rpcgen/TrafficLightClient.java").exists());
    }
    @Test
    public void testHelpOption() {
        String args[] = {
                "-help"
        };
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        System.err.println(outContent);
        assertFalse(outContent.toString().contains("Unrecognized option: -enums"));
        exit.expectSystemExitWithStatus(1);
        exit.checkAssertionAfterwards(new Assertion(){

            @Override
            public void checkAssertion() throws Exception {
            assertTrue(outContent.toString().contains("-enums          generate java enums for xdr enums"));
                
            }
            
        });
        jrpcgen.main(args);
        System.setOut(System.out);
    }
    
}
