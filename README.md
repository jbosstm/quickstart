Quickstarts
-----------

Guidelines for contributing a quickstart are as per:
https://docs.jboss.org/author/display/AS71/Writing+a+quickstart


To run the quickstarts:

1. set WORKSPACE (to the root of the quickstart checkout)
2. set JBOSSAS_IP_ADDR (default is localhost)
3. set JBOSS_HOME (to the path of JBossAS server, e.g. /home/tom/projects/jbosstm/narayana/jboss-as/build/target/jboss-as-8.0.0.Alpha1-SNAPSHOT/)
4. mvn clean install

One of the BlackTie quickstarts requires the Oracle driver to be downloaded and configured, see blacktie/test/initializeBlackTie.xml for more details.

It is disabled by default but running "./blacktie/run_all_quickstarts.[sh|bat] tx" will execute it
