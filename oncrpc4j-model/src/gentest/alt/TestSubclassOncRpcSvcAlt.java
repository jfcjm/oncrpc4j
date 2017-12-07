package org.dcache.generics.alt;
import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;
public class TestSubclassOncRpcSvcAlt  {
    @Test(expected=IllegalArgumentException.class)
    public void withDummyOption() throws IOException {
        OncSvcAlt svc = new OncSvcBuilderAlt().withDummyOption().build();
        svc.start();
        assertTrue(svc._prestartCalled);
        assertTrue(svc._prestopCalled);
        assertTrue(svc._hasDummyOption);
    }
    @Test(expected=IllegalArgumentException.class)
    public void withoutDummyOption() throws IOException {
        OncSvcAlt svc = new OncSvcBuilderAlt().build();
        svc.start();
        assertTrue(svc._prestartCalled);
        assertTrue(svc._prestopCalled);
        assertFalse(svc._hasDummyOption);
    }

}
