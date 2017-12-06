package org.dcache.generics.alt.dispatchable;

import org.dcache.xdr.model.root.AbstractOncRpcSvcBuilder;

public class OncSvcBuilderAlt extends AbstractOncRpcSvcBuilder<OncSvcAlt,OncSvcBuilderAlt>{

    boolean _dummyOption;

    @Override
    protected OncSvcBuilderAlt getThis() {
        return this;
    }

    @Override
    protected OncSvcAlt getOncRpcSvc(OncSvcBuilderAlt builder_T) {
        return new OncSvcAlt(this);
    }

    public OncSvcBuilderAlt withDummyOption() {
        _dummyOption = true;
        return this;
    }

    public boolean hasDummyOption() {
        return _dummyOption;
    }

}
