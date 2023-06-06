
# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Running maven quickstart"

[ "x$QUICKSTART_NARAYANA_VERSION" != 'x' ] &&\
  NARAYANA_VERSION_PARAM="-Dversion.narayana=${QUICKSTART_NARAYANA_VERSION}"

mvn compile exec:exec $NARAYANA_VERSION_PARAM
if [ "$?" != "0" ]; then
	exit -1
fi
