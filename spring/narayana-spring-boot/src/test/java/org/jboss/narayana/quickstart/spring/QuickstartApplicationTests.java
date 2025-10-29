package org.jboss.narayana.quickstart.spring;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import dev.snowdrop.boot.narayana.autoconfigure.NarayanaAutoConfiguration;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = QuickstartApplication.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = { NarayanaAutoConfiguration.class,
        QuickstartService.class })
public class QuickstartApplicationTests {

    @Autowired
    QuickstartService quickstartService;
    @Autowired
    EntriesService entryService;

    @Test
    public void testRollback() throws Exception {
        assertTrue(entryService.getEntries().isEmpty());
        quickstartService.demonstrateRollback("Rollback Test");
        assertTrue(entryService.getEntries().isEmpty());
    }

    @Test
    public void testCommit() throws Exception {
        assertTrue(entryService.getEntries().isEmpty());
        quickstartService.demonstrateCommit("Commit Test");
        assertTrue(entryService.getEntries().size() == 1);
    }

}