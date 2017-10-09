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

package org.dcache.libvirt;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.dcache.xdr.OncRpcSvc;
import org.dcache.xdr.OncRpcSvcBuilder;
import org.junit.Test;
import org.libvirt.VirOncRpcSvcBuilder;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.*;

/**
 *
 */
public class OncRpcSvcBuilderTest {

    @Test
    public void shouldReturnSameThreadExecutorForSameThreadStrategy() {

        VirOncRpcSvcBuilder builder = new VirOncRpcSvcBuilder();

        ExecutorService executorService = builder.getWorkerThreadExecutorService();
        final Object[] holder = new Object[1];
        final Thread thisThread = Thread.currentThread();
        executorService.execute( new Runnable() {

            @Override
            public void run() {
                holder[0] = Thread.currentThread();
            }
        });

        assertTrue("Executoed in a different thread", thisThread == holder[0]);
    }

    @Test
    public void shouldReturnDifferentExecutorForWorkerThreadStrategy() {

        OncRpcSvcBuilder builder = new VirOncRpcSvcBuilder()
                .withWorkerThreadIoStrategy();

        ExecutorService executorService = builder.getWorkerThreadExecutorService();
        final Object[] holder = new Object[1];
        final Thread thisThread = Thread.currentThread();
        executorService.execute(new Runnable() {

            @Override
            public void run() {
                holder[0] = Thread.currentThread();
            }
        });

        assertTrue("Executoed in the same thread", thisThread != holder[0]);
    }

    @Test
    public void shouldReturnGivenExecutorForWorkerThreadStrategy() {

        ExecutorService mockedExecutorService = mock(ExecutorService.class);
        OncRpcSvcBuilder builder = new VirOncRpcSvcBuilder()
                .withWorkerThreadIoStrategy()
                .withWorkerThreadExecutionService(mockedExecutorService);

        ExecutorService executorService = builder.getWorkerThreadExecutorService();
        assertTrue("Provided executor service not used", mockedExecutorService  == executorService);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnInvalidProtocol() {

        OncRpcSvc svc = new VirOncRpcSvcBuilder()
                .withIpProtocolType(1)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfClientUsesTwoProtocols() {

        OncRpcSvc svc = new VirOncRpcSvcBuilder()
                .withClientMode()
                .withTCP()
                .withUDP()
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionDefinedWorkerThreadPoolWithExtern() {

        OncRpcSvc svc = new VirOncRpcSvcBuilder()
                .withTCP()
                .withUDP()
                .withWorkerThreadExecutionService(Executors.newCachedThreadPool())
                .withWorkerThreadPoolSize(2)
                .build();
    }

}
