package org.jboss.narayana.quickstart.spring;

import org.springframework.data.repository.CrudRepository;

/**
 * @author <a href="mailto:zfeng@redhat.com>Zheng Feng</a>
 */
public interface UserRepository extends CrudRepository <User, Integer> {
}