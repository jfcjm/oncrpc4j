package org.dcache.oncrpc4j.rpcgen;

import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.dcache.xdr.model.root.AbstractRpcCall;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CalculatorServerImpl<SVC_T extends RpcSvcItf<SVC_T>> extends CalculatorServer<SVC_T> {
    public static final int SLEEP_MILLIS = 100;
    private List<MethodCall> methodCalls = new ArrayList<>();

    @Override
    public CalculationResult add_1(RpcCallItf<SVC_T> call$, long arg1, long arg2) {
        long start = System.currentTimeMillis();
        CalculationResult result = new CalculationResult();
        result.setStartMillis(start);
        result.setResult(arg1 + arg2);
        try {
            Thread.sleep(SLEEP_MILLIS);
        } catch (InterruptedException e) {
            //ignore
        }
        long finish = System.currentTimeMillis();
        result.setFinishMillis(finish);
        recordAddCall(call$, start, finish, arg1, arg2, result.getResult(), null);
        return result;
    }

    @Override
    public long addSimple_1(RpcCallItf<SVC_T> call$, long arg1, long arg2) {
        long start = System.currentTimeMillis();
        try {
            Thread.sleep(SLEEP_MILLIS);
        } catch (InterruptedException e) {
            //ignore
        }
        long result = arg1 + arg2;
        long finish = System.currentTimeMillis();
        recordAddCall(call$, start, finish, arg1, arg2, result, null);
        return result;
    }

    private void recordAddCall(RpcCallItf<SVC_T> call, long start, long finish, long arg1, long arg2, long result, Throwable throwable) {
        System.err.println(arg1 + " + " + arg2 + " = " + result);
        Object[] args = new Object[2];
        args[0] = arg1;
        args[1] = arg2;
        Subject subject = call.getCredential().getSubject();
        Set<String> principalNames = new HashSet<>();
        for (Principal p : subject.getPrincipals()) {
            principalNames.add(p.getName());
        }
        methodCalls.add(new MethodCall(start, finish, Thread.currentThread().getStackTrace()[1].getMethodName(), args, result, throwable, principalNames, call.getTransport().getRemoteSocketAddress().getPort()));
    }

    public List<MethodCall> getMethodCalls() {
        List<MethodCall> calls = this.methodCalls;
        this.methodCalls = new ArrayList<>();
        return calls;
    }

    public void awaitMethodCalls(long timeouts) {
        try {
            long deadline = System.currentTimeMillis() + timeouts;
            while (methodCalls.isEmpty()) {
                if (System.currentTimeMillis() > deadline) {
                    throw new IllegalStateException("no method calls within the " + timeouts + " milli timeout");
                }
                Thread.sleep(SLEEP_MILLIS);
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }
}
