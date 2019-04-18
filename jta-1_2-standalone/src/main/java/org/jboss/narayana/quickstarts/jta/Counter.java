/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.narayana.quickstarts.jta;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import javax.transaction.TransactionScoped;

/**
 * <p>
 * Transactional scoped counter.<br/>
 * Data of the counter are stored along the existence
 * of the particular transaction.
 * </p>
 * <p>
 * With starting a new transaction the injected counter
 * is initiated as a new instance.
 * When transaction finishes the counter is cleared up.
 * </p>
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@TransactionScoped
public class Counter implements Serializable {
    private static final long serialVersionUID = 1L;

    private final AtomicInteger counter = new AtomicInteger();

    public int get() {
        return counter.get();
    }

    public void increment() {
        counter.incrementAndGet();
    }

    @Override
    public String toString() {
        return this.hashCode() + "[value:" + counter.get() + "]";
    }
}
