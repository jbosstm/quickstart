package quickstart;

import org.jboss.narayana.rest.integration.api.Participant;
import org.jboss.narayana.rest.integration.api.ParticipantDeserializer;

import java.io.ObjectInputStream;

public class WorkDeserializer implements ParticipantDeserializer {
    @Override
    public Participant deserialize(ObjectInputStream objectInputStream) {
        try {
            Object object = objectInputStream.readObject();
            if (object instanceof Participant) {
                return (Participant) object;
            }
        } catch (Exception e) {
            System.err.printf("Cannot deserialize into Work: %s\n", e.getMessage());
        }

        return null;
    }

    @Override
    public Participant recreate(byte[] recoveryState) {
        return null;
    }

}
