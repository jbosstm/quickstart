package org.jboss.narayana.quickstart.spring;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.test.rule.OutputCapture;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class QuickstartApplicationTests {

    @Rule
    public OutputCapture outputCapture = new OutputCapture();

    @Test
    public void testCommit() throws Exception {
        QuickstartApplication.main(new String[] { "commit", "Test Value" });
        String output = outputCapture.toString();
        assertThat(output, containsString("Entries at the start: []"));
        assertThat(output, containsString("Creating entry 'Test Value'"));
        assertThat(output, containsString("Message received: Created entry 'Test Value'"));
        assertThat(output, containsString("Entries at the end: [Entry{id=1, value='Test Value'}]"));
    }

    @Test
    public void testRollback() throws Exception {
        QuickstartApplication.main(new String[] { "rollback", "Test Value" });
        String output = outputCapture.toString();
        assertThat(output, containsString("Entries at the start: []"));
        assertThat(output, containsString("Creating entry 'Test Value'"));
        assertThat(output, not(containsString("Message received: Created entry 'Test Value'")));
        assertThat(output, containsString("Entries at the end: []"));
    }

}