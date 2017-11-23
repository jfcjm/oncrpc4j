package org.dcache.oncrpc4j.rpcgen;

import com.google.common.io.BaseEncoding;

import org.dcache.xdr.model.itf.RpcCallItf;
import org.dcache.xdr.model.itf.RpcSvcItf;
import org.dcache.xdr.model.root.RpcCall;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BlobStoreServerImpl<SVC_T extends RpcSvcItf<SVC_T>> extends BlobStoreServer<SVC_T> {
    private final ConcurrentHashMap<String, byte[]> store = new ConcurrentHashMap<>();
    private final AtomicInteger requestCounter = new AtomicInteger(0);
    private volatile long sleepFor = 1L;

    @Override
    public void put_1(RpcCallItf<SVC_T> call$, Key key, Value value) {
        String hexKey = bytesToHex(key.data);
        store.put(hexKey, value.data);
        requestCounter.incrementAndGet();
        if (sleepFor > 0) {
            try {
                Thread.sleep(sleepFor);
            } catch (InterruptedException ignored) {}
        }
    }

    @Override
    public Value get_1(RpcCallItf<SVC_T> call$, Key key) {
        String hexKey = bytesToHex(key.data);
        byte[] value = store.get(hexKey);
        Value res = new Value();
        if (value != null) {
            res.notNull = true;
            res.data = value;
        }
        requestCounter.incrementAndGet();
        return res;
    }

    public long getSleepFor() {
        return sleepFor;
    }

    public void setSleepFor(long sleepFor) {
        this.sleepFor = sleepFor;
    }

    public int getNumRequestsProcessed() {
        return requestCounter.get();
    }

    public static String bytesToHex(byte[] bytes) {
        return BaseEncoding.base16().upperCase().encode(bytes);
    }
}
