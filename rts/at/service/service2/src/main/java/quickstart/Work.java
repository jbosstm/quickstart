/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package quickstart;

import org.jboss.narayana.rest.integration.api.Participant;
import org.jboss.narayana.rest.integration.api.Prepared;
import org.jboss.narayana.rest.integration.api.Vote;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

// Work defines the actions performed by a service that should be transactional
public class Work implements Participant, Serializable {
    // package scope since these two fields are accessed by the service (TransactionAwareResource)
    // commitCnt and abortCnt are guaranteed to be incremented even in the JVM crashes
    static AtomicInteger commitCnt = new AtomicInteger(0);
    static AtomicInteger abortCnt = new AtomicInteger(0);

    private final int id;

    public Work(int id) {
        this.id = id;
    }

    @Override
    public Vote prepare() {
        System.out.printf("Service: preparing: wId=%d\n", id);
        return new Prepared();
    }

    @Override
    public void commit() {
        System.out.printf("Service: committing: wId=%d\n", id);

        if (String.valueOf(id).equals(TransactionAwareResource.FAIL_COMMIT)) {
            System.out.println("Service: Halting VM during commit of work unit wId=" + id);
            Runtime.getRuntime().halt(1);
        }

        commitCnt.incrementAndGet();
    }

    @Override
    public void commitOnePhase() {
        commit();
    }

    @Override
    public void rollback() {
        System.out.printf("Service: aborting: wId=%d\n", id);
        abortCnt.incrementAndGet();
    }

    public int getId() {
        return id;
    }
}
