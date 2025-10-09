package org.jboss.narayana.quickstart.spring;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
interface EntriesRepository extends JpaRepository<Entry, Integer> {
}