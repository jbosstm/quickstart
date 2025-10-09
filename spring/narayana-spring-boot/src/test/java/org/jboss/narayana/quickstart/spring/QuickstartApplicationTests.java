package org.jboss.narayana.quickstart.spring;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import dev.snowdrop.boot.narayana.autoconfigure.NarayanaAutoConfiguration;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = QuickstartApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = { NarayanaAutoConfiguration.class,
        QuickstartService.class })
public class QuickstartApplicationTests {

    @Autowired
    QuickstartService quickstartService;

    @Test
    @Order(1)
    public void testRollback(CapturedOutput output) throws Exception {
        quickstartService.demonstrateRollback("Rollback Test");
        String str = output.toString();
        assertThat(str, containsString("Entries at the start: []"));
        assertThat(str, containsString("Creating entry 'Rollback Test'"));
        assertThat(str, not(containsString("Received message 'Created entry 'Rollback Test''")));
        assertThat(str, containsString("Entries at the end: []"));
    }

    @Test
    @Order(2)
    public void testCommit(CapturedOutput output) throws Exception {
        quickstartService.demonstrateCommit("Commit Test");
        String str = output.toString();
        assertThat(str, containsString("Entries at the start: []"));
        assertThat(str, containsString("Creating entry 'Commit Test'"));
        assertThat(str, containsString("Received message 'Created entry 'Commit Test''"));
        assertThat(str, containsString("Entries at the end: [Entry{id=2, value='Commit Test'}]"));
    }

}