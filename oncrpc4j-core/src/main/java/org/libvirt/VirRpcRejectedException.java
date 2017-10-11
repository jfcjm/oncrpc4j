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
 ******************************************************************************/

package org.libvirt;

import org.dcache.xdr.OncRpcException;

public class VirRpcRejectedException extends OncRpcException {
    
    private static final long serialVersionUID = 8947480749178768718L;
    private int _status;

    public VirRpcRejectedException(){
        super("No message, see Cause exception");
    }
    
    public VirRpcRejectedException(int rejectStatus) {
        super(Integer.toString(rejectStatus));
        _status = rejectStatus;
    }

    public VirRpcRejectedException(remote_error error) {
        this(error.getCode(),error.getMessage());
    }

    public VirRpcRejectedException(int code, remote_string message) {
        super(message.value.value + ";Error code "+Integer.toString(code));
        _status= code;
    }

}
