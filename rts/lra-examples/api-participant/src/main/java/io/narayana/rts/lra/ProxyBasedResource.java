package io.narayana.rts.lra;

import org.eclipse.microprofile.lra.annotation.CompensatorStatus;
import org.eclipse.microprofile.lra.client.LRAClient;
import org.eclipse.microprofile.lra.participant.JoinLRAException;
import org.eclipse.microprofile.lra.participant.LRAManagement;
import org.eclipse.microprofile.lra.participant.LRAParticipant;
import org.eclipse.microprofile.lra.participant.TerminationException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Path("/")
@ApplicationScoped
public class ProxyBasedResource implements LRAParticipant, Serializable {

    @Inject
    private StateHolder stats;

    @Inject
    private LRAClient lraClient;

    @Inject
    private LRAManagement lraManagement;

    @Inject
    private ParticipantDeserializer participantDeserializer;

    @PostConstruct
    private void postConstruct() {
        lraManagement.registerDeserializer(participantDeserializer);
    }

    @PreDestroy
    private void preDestroy() {
        lraManagement.unregisterDeserializer(participantDeserializer);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
    }

    private void readObject(ObjectInputStream in) throws IOException,ClassNotFoundException {
    }

    @Path("/api")
    @PUT
    public String doInTransaction(@QueryParam("fault") String fault) throws JoinLRAException {
        getStateHolder().setFault(fault);
        URL lraId = lraClient.startLRA("No CDI based client", 0L, TimeUnit.MILLISECONDS);

        getStateHolder().injectFault(StateHolder.FaultTarget.API, StateHolder.FaultWhen.BEFORE);
        lraManagement.joinLRA(this, lraId, 0L, TimeUnit.SECONDS);

        // do something interesting
        lraClient.closeLRA(lraId);
        getStateHolder().injectFault(StateHolder.FaultTarget.API, StateHolder.FaultWhen.AFTER);

        return getStateHolder().toString();
    }

    @Path("/api")
    @GET
    @Produces("text/plain")
    public String getStats() {
        return getStateHolder().toString();
    }

    @Override
    public Future<Void> completeWork(URL lraId) throws NotFoundException, TerminationException {
        getStateHolder().update(StateHolder.FaultTarget.API, CompensatorStatus.Completed);
        getStateHolder().injectFault(StateHolder.FaultTarget.API, StateHolder.FaultWhen.DURING);

        return null;
    }

    @Override
    public Future<Void> compensateWork(URL lraId) throws NotFoundException, TerminationException {
        getStateHolder().update(StateHolder.FaultTarget.API, CompensatorStatus.Completed);
        getStateHolder().injectFault(StateHolder.FaultTarget.API, StateHolder.FaultWhen.DURING);

        return null;
    }

    private StateHolder getStateHolder() {
        // we do not use the injected StateHolder since in recovery situations CDI may not have
        // initialised this bean and doing it manually via
        // CDI.current().select(ProxyBasedResource.class).get() is no good either
        return stateHolder;
    }

    private static StateHolder stateHolder = new StateHolder();
}
