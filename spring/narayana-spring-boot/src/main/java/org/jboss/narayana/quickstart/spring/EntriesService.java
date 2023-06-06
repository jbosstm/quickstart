package org.jboss.narayana.quickstart.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;

/**
 * Service to store entries in the database.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Service
@Transactional
public class EntriesService {

    private final EntriesRepository entriesRepository;

    @Autowired
    public EntriesService(EntriesRepository entriesRepository) {
        this.entriesRepository = entriesRepository;
    }

    public Entry create(String value) {
        System.out.println("Creating entry '" + value + "'");

        return entriesRepository.save(new Entry(value));
    }

    public List<Entry> getAll() {
        return entriesRepository.findAll();
    }

}