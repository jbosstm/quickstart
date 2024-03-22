Non-Transactional Resource quickstart
=====================================

Get a WildFly that has the expected version of Narayana in it. If you need to build it:
```
WORKSPACE=<QUICKSTARTS_REPO_ROOT> <QUICKSTARTS_REPO_ROOT>/scripts/hudson/quickstart.sh clone_as
WORKSPACE=<QUICKSTARTS_REPO_ROOT> <QUICKSTARTS_REPO_ROOT>/scripts/hudson/quickstart.sh build_as
#You should be able to find a WildFly to use for JBOSS_HOME under <QUICKSTARTS_REPO_ROOT>jboss-as/build/target/wildfly-<BUILT_WILDFLY_VERSION>
```

This quickstart covers the code example documented here: https://jbossts.blogspot.co.uk/2013/05/compensating-transactions-when-acid-is_29.html
