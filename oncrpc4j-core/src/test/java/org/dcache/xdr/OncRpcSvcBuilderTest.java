/*
 * Copyright (c) 2009 - 2015 Deutsches Elektronen-Synchroton,
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.dcache.xdr.model.itf.GenItfOncRpcSvcBuilder;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.*;

/**
 *
 */
public class OncRpcSvcBuilderTest {

    @Test
    public void shouldReturnSameThreadExecutorForSameThreadStrategy() {

        GenOncRpcSvcBuilder builder = new GenOncRpcSvcBuilder();

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

        GenItfOncRpcSvcBuilder<GenOncRpcSvc> builder = new GenOncRpcSvcBuilder()
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
        GenItfOncRpcSvcBuilder<GenOncRpcSvc> builder = new GenOncRpcSvcBuilder()
                .withWorkerThreadIoStrategy()
                .withWorkerThreadExecutionService(mockedExecutorService);

        ExecutorService executorService = builder.getWorkerThreadExecutorService();
        assertTrue("Provided executor service not used", mockedExecutorService  == executorService);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnInvalidProtocol() {

        GenOncRpcSvc svc = new GenOncRpcSvcBuilder()
                .withIpProtocolType(1)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfClientUsesTwoProtocols() {

        GenOncRpcSvc svc = new GenOncRpcSvcBuilder()
                .withUDP()
                .withTCP()
                .withClientMode()
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionDefinedWorkerThreadPoolWithExtern() {

        GenOncRpcSvc svc = new GenOncRpcSvcBuilder()
                .withUDP()
                .withTCP()
                .withWorkerThreadExecutionService(Executors.newCachedThreadPool())
                .withWorkerThreadPoolSize(2)
                .build();
    }

}
