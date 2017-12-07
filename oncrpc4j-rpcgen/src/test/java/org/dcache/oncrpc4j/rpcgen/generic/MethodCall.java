package org.dcache.oncrpc4j.rpcgen.generic;

import java.util.Set;

public class MethodCall {
    private final long startTimestamp;
    private final long finishTimestamp;
    private final String methodName;
    private final Object[] arguments;
    private final Object returnValue;
    private final Throwable throwable;
    private final Set<String> principalNames;
    private final int clientPort;

    public MethodCall(long startTimestamp, long finishTimestamp, String methodName, Object[] arguments, Object returnValue, Throwable throwable, Set<String> principalNames, int clientPort) {
        this.startTimestamp = startTimestamp;
        this.finishTimestamp = finishTimestamp;
        this.methodName = methodName;
        this.arguments = arguments;
        this.returnValue = returnValue;
        this.throwable = throwable;
        this.principalNames = principalNames;
        this.clientPort = clientPort;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public long getFinishTimestamp() {
        return finishTimestamp;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public Set<String> getPrincipalNames() {
        return principalNames;
    }

    public int getClientPort() {
        return clientPort;
    }
}
