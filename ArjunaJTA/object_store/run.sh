
# ALLOW JOBS TO BE BACKGROUNDED
set -m
set -x

echo "Running object_store quickstart"

[ "x$QUICKSTART_NARAYANA_VERSION" != 'x' ] &&\
  NARAYANA_VERSION_PARAM="-Dversion.narayana=${QUICKSTART_NARAYANA_VERSION}"

mvn -e exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.VolatileStoreExample $NARAYANA_VERSION_PARAM
if [ "$?" != "0" ]; then
	exit -1
fi

mvn -e exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.HornetqStoreExample $NARAYANA_VERSION_PARAM
if [ "$?" != "0" ]; then
	exit -1
fi

mvn -e exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.FileStoreExample $NARAYANA_VERSION_PARAM
if [ "$?" != "0" ]; then
	exit -1
fi

mvn -e exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.JDBCStoreExample $NARAYANA_VERSION_PARAM
if [ "$?" != "0" ]; then
	exit -1
fi
