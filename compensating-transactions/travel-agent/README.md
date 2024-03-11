Travel Agent Quickstart
=======================

Get a WildFly that has the expected version of Narayana in it. If you need to build it:
```
WORKSPACE=<QUICKSTARTS_REPO_ROOT> <QUICKSTARTS_REPO_ROOT>/scripts/hudson/quickstart.sh clone_as
WORKSPACE=<QUICKSTARTS_REPO_ROOT> <QUICKSTARTS_REPO_ROOT>/scripts/hudson/quickstart.sh build_as
#You should be able to find a WildFly to use for JBOSS_HOME under <QUICKSTARTS_REPO_ROOT>jboss-as/build/target/wildfly-<BUILT_WILDFLY_VERSION>
```

Currently the main documentation for this quickstart is the following two blog posts. Watch this space for more docs.

http://jbossts.blogspot.co.uk/2013/06/compensating-transactions-when-acid-is_26.html
http://jbossts.blogspot.co.uk/2013/07/compensating-transactions-when-acid-is.html