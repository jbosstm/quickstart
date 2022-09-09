package io.narayana.rts.lra;

import org.eclipse.microprofile.lra.annotation.ParticipantStatus;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import static io.narayana.rts.lra.StateHolder.FaultTarget.API;
import static io.narayana.rts.lra.StateHolder.FaultTarget.CDI;
import static io.narayana.rts.lra.StateHolder.FaultTarget.MIXED;
import static io.narayana.rts.lra.StateHolder.FaultTarget.NONE;
import static io.narayana.rts.lra.StateHolder.FaultType.HALT;
import static io.narayana.rts.lra.StateHolder.FaultWhen.AFTER;
import static io.narayana.rts.lra.StateHolder.FaultWhen.BEFORE;

@ApplicationScoped
public class StateHolder implements Serializable {
    private AtomicInteger completedCount = new AtomicInteger(0);
    private AtomicInteger compensatedCount = new AtomicInteger(0);

    private FaultTarget target = FaultTarget.NONE;
    private FaultType type = FaultType.NONE;
    private FaultWhen when = FaultWhen.DURING;

    enum FaultTarget { CDI, API, MIXED, NONE }
    enum FaultType { HALT, NONE }
    enum FaultWhen { // when to inject a fault
        NOW, // immediately
        BEFORE, // before the end phase
        DURING, // during the end phase
        AFTER // after the end phase
    };

    public int getCompletedCount() {
        return completedCount.get();
    }

    public int getCompensatedCount() {
        return compensatedCount.get();
    }

    @Override
    public String toString() {
        return String.format("%d completed and %d compensated", getCompletedCount(), getCompensatedCount());
    }

    public void update(FaultTarget target, ParticipantStatus status) {
        if (status == null) {
            injectFault(target, BEFORE);
        }

        if (status == ParticipantStatus.Completed) {
            completedCount.incrementAndGet();
            System.out.printf("%d completions%n", completedCount.get());
        } else if (status == ParticipantStatus.Compensated) {
            System.out.printf("%d compensations%n", compensatedCount.get());
            compensatedCount.incrementAndGet();
        }
    }

    void injectFault(FaultTarget target, FaultWhen when) {
        if (this.target == target && this.when == when) {
            System.out.printf("injecting fault type %s ...%n", type);

            if (type == HALT) {
                Runtime.getRuntime().halt(1);
            }
        }
    }

    void setFault(String fault) {
        if (fault == null) {
            fault = "";
        }

        fault = fault.toLowerCase();

        if (fault.contains("cdi")) {
            target = CDI;
        } else if (fault.contains("api")) {
            target = API;
        } else if (fault.contains("mixed")) {
            target = MIXED;
        } else {
            target = NONE;
        }

        if (fault.contains("halt")) {
            type = HALT;
        }

        if (fault.contains("before")) {
            when = BEFORE;
        } else if (fault.contains("during")) {
            when = FaultWhen.DURING;
        } else if (fault.contains("after")) {
            when = AFTER;
        }
    }
}
