package org.dcache.xdr;

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
}
