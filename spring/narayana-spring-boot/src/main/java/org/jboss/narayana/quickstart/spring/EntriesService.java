package org.jboss.narayana.quickstart.spring;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

/**
 * Service to store entries in the database.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Service
public class EntriesService {

    private final Logger logger = LoggerFactory.getLogger(EntriesService.class);
    private final EntriesRepository entriesRepository;

    public EntriesService(EntriesRepository entriesRepository) {
        this.entriesRepository = entriesRepository;
    }

    @Transactional
    public Entry createEntry(String value) {
        this.logger.info("Creating entry '{}'", value);
        return this.entriesRepository.save(new Entry(value));
    }

    public List<Entry> getEntries() {
        List<Entry> entries = this.entriesRepository.findAll();
        this.logger.info("Returning entries '{}'", entries);
        return entries;
    }

    public void clearEntries() {
        this.entriesRepository.deleteAll();
    }

}