/*******************************************************************************
 * Copyright (C) 2017 INU Champollion, Albi, France
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 ******************************************************************************/package org.libvirt;

import static org.dcache.xdr.ConversionUtils.helperCAST;
import        org.dcache.xdr.model.root.GenAbstractOncRpcSvcBuilder;
public  class GenVirOncRpcSvcBuilder extends GenAbstractOncRpcSvcBuilder<GenVirOncRpcSvc> {

    public GenVirOncRpcSvcBuilder(){
        super();
        super.withTCP();
    }
    
    @Override
    protected GenVirOncRpcSvc getNewOncRpcSvc() {
        return new GenVirOncRpcSvc(this);
    }
    /**
     * Le casting de cette méthode permet de retourner le type GenVirOncRpcSvcBuilder
     * à la place de celui utilisé pour caster : facilite l'utilisation par les clients
     * 
     * Les méthodes rajoutées ou redéfinies doivent être appelée en premier dans la chaîne
     * d'appels fuildes.
     */
    @Override 
    public GenVirOncRpcSvcBuilder withTCP(){
        return helperCAST(super.withTCP());
    }
    
    @Override
    public GenVirOncRpcSvc build(){
        return getNewOncRpcSvc();
    }
}
