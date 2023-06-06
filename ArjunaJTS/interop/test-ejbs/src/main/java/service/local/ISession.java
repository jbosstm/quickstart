package service.local;

import jakarta.ejb.Local;

// Extending remote interface for convenience of method declaration
@Local
public interface ISession extends service.remote.ISession {
}