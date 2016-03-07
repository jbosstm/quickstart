/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.narayana.quickstart.spring;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.test.OutputCapture;

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
