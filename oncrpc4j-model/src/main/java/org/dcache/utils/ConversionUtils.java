package org.dcache.utils;

import java.nio.channels.CompletionHandler;

public final class  ConversionUtils {

    /**
     * helper for converting between a supertype and its subtype
     * @param subTypeObject
     * @return
     */
    @SuppressWarnings("unchecked")
    public static  <TGT,SRC> TGT helperCAST(final SRC subTypeObject) {
        return (TGT) subTypeObject;
    }

    public static <K,V>CompletionHandler<K,V>nullCompletionHandler(){
        return (CompletionHandler<K,V>) null;
    }

}


