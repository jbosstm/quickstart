package org.jboss.narayana.quickstart.spring;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RepositoryRestResource(exported = false)
public interface PortfolioEntryRepository extends JpaRepository<PortfolioEntry, Integer> {

    PortfolioEntry findByUserAndShare(User user, Share share);

}