package io.narayana.rts.lra;

import org.eclipse.microprofile.lra.participant.LRAParticipant;
//import org.eclipse.microprofile.lra.participant.LRAParticipantDeserializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;

public class ParticipantDeserializer {//implements LRAParticipantDeserializer {
//    @Override
    public LRAParticipant deserialize(URL lraId, byte[] data) {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return (LRAParticipant) ois.readObject();
        } catch (final IOException | ClassNotFoundException e) {
            return null;
        }
    }
}
