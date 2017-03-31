OVERVIEW
--------

This example shows how to propagate a transaction context between the WildFly and GlassFish application
servers. There is a single script to run the full example but you may find it more instructive to run
the example in steps perhaps in separate command windows:

USAGE
-----

./run.sh

or to run each step at a time:

1. step1.sh:
  - builds GlassFish either from source (using svn) or via prebuilt binaries. The build from source also needs patching using the included patch file (see https://java.net/jira/browse/GLASSFISH-21532 for details);
  - builds narayana using the latest version (controlled by the environment variable NARAYANA_CURRENT_VERSION)
  - patches and builds the WildFly version downloaded during the narayana build. The patch disables the interceptor that blocks the importing of foreign transactions. When we have a JIRA to for the issue we will update this README.
2. step2.sh: starts WildFly in JTS mode
3. step3.sh: starts a GlassFish domain
4. step4.sh: deploys the test EJB to both servers (for making transactional calls between them)
5. step5.sh: Make an ejb call from GlassFish to WildFly (using the script in ejb_operations.sh)
6. step6.sh: Make an ejb call from WildFly to GlassFish (using the script in ejb_operations.sh)
7. step7.sh: Undeploy EJBs and shutdown GlassFish and WildFly

EXPECTED OUTPUT
---------------

These steps generate too much output to show in full.

The key output lines to verify that the GlassFish to WildFly transactional EJB call worked is to look for the line:

> ===== JTS interop quickstart: step 5 (EJB call gf -> wf):

followed soon after by a line that includes

> Next: 8000

The number printed will increment on each EJB call.

The key output lines to verify that the WildFly to GlassFish transactional EJB call worked is to look for the line:
> ===== JTS interop quickstart: step 6 (EJB call wf -> gf):

followed soon after by

> Next: 7000

The number printed will increment on each EJB call.

