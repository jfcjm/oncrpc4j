/*
 * Copyright (c) 2009 - 2016 Deutsches Elektronen-Synchroton,
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program (see the file COPYING.LIB for more
 * details); if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.dcache.xdr;

import static org.dcache.utils.ConversionUtils.helperCAST;

import org.dcache.xdr.gss.GssSessionManager;
import org.dcache.xdr.model.root.AbstractOncRpcSvcBuilder;
/**
 * 
 * Implémentation du builder de service adapté pour les ONC-RPC.
 * Les méthodes redéfines (ou ajoutéesà doivent être utilisées en premier
 * dans l'invocation .
 * @author jmk
 *
 */
public  final class OncRpcSvcBuilder  extends AbstractOncRpcSvcBuilder<OncRpcSvc> implements IOncRpcSvcBuilder{

    private GssSessionManager _gssSessionManager;
    private boolean _autoPublish = true;
    
    @Override
    protected OncRpcSvc getNewOncRpcSvc() {
        return  OncRpcSvc.getImpl(this);
    }

   
    public IOncRpcSvcBuilder withGssSessionManager(GssSessionManager gssSessionManager) {
        _gssSessionManager = gssSessionManager;
        return this;
    }

    
    public GssSessionManager getGssSessionManager() {
        return _gssSessionManager;
    }
    
    
    @Override
    public IOncRpcSvcBuilder withUDP() {
        return helperCAST(super.withUDP());
    }
    

    @Override
    public IOncRpcSvcBuilder withTCP() {
        return helperCAST(super.withTCP());
    }

    @Override
    public  IOncRpcSvcBuilder withIpProtocolType(int protocolType) {
        return helperCAST(super.withIpProtocolType(protocolType));
    }
    
    public IOncRpcSvcBuilder  withAutoPublish() {
        _autoPublish = true;
        return this;
    }

   
    public IOncRpcSvcBuilder withoutAutoPublish() {
        _autoPublish = false;
        return this;
    }


    public boolean isAutoPublish() {
        return _autoPublish;
    }
}
