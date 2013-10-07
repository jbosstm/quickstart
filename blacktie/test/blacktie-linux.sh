ulimit -c unlimited

# CHECK IF WORKSPACE IS SET
if [ -n "${WORKSPACE+x}" ]; then
  echo WORKSPACE is set
else
  echo WORKSPACE not set
  exit -1
fi

if [ -z "${JBOSSAS_IP_ADDR+x}" ]; then
  echo JBOSSAS_IP_ADDR not set
  JBOSSAS_IP_ADDR=localhost
fi


if [ -n "${NARAYANA_CURRENT_VERSION+x}" ]; then
  echo NARAYANA_CURRENT_VERSION is set
else
  echo NARAYANA_CURRENT_VERSION not set
  exit -1
fi

if [ -n "${BLACKTIE_DIST_HOME+x}" ]; then
  echo BLACKTIE_DIST_HOME is set
else
  echo BLACKTIE_DIST_HOME not set
  exit -1
fi

if [ -n "${JBOSS_HOME+x}" ]; then
  echo JBOSS_HOME is set
else
  echo JBOSS_HOME not set
  exit -1
fi

# KILL ANY PREVIOUS BUILD REMNANTS
ps -f
for i in `ps -eaf | grep java | grep "standalone.*xml" | grep -v grep | cut -c10-15`; do kill -9 $i; done
killall -9 testsuite
killall -9 server
killall -9 client
killall -9 cs
ps -f

# GET THE TNS NAMES
TNS_ADMIN=$WORKSPACE/instantclient_11_2/network/admin
mkdir -p $TNS_ADMIN
if [ -e $TNS_ADMIN/tnsnames.ora ]; then
	echo "tnsnames.ora already in place"
else
	cp tnsnames.ora $TNS_ADMIN/
fi

# INITIALIZE JBOSS and CREATE BLACKTIE DISTRIBUTION
ant -f $WORKSPACE/blacktie/test/initializeBlackTie.xml -Dbasedir=.. -DJBOSS_HOME=$JBOSS_HOME -DBT_HOME=$WORKSPACE/blacktie/target/dist/ -DVERSION=$NARAYANA_CURRENT_VERSION -DMACHINE_ADDR=`hostname` -DJBOSSAS_IP_ADDR=$JBOSSAS_IP_ADDR -DBLACKTIE_DIST_HOME=$BLACKTIE_DIST_HOME initializeJBoss initializeBlackTie -debug
if [ "$?" != "0" ]; then
	ps -f
	for i in `ps -eaf | grep java | grep "standalone.*xml" | grep -v grep | cut -c10-15`; do kill -9 $i; done
	killall -9 testsuite
	killall -9 server
	killall -9 client
	killall -9 cs
  	ps -f
	exit -1
fi
export JBOSS_HOME=
chmod u+x $WORKSPACE/jboss-as/bin/standalone.sh

# START JBOSS
$WORKSPACE/jboss-as/bin/standalone.sh -c standalone-blacktie.xml -Djboss.bind.address=$JBOSSAS_IP_ADDR -Djboss.bind.address.unsecure=$JBOSSAS_IP_ADDR&
sleep 20

# TWEAK txfooapp FOR THIS NODE
ant -f $WORKSPACE/blacktie/test/initializeBlackTie.xml tweak-txfooapp-for-environment
if [ "$?" != "0" ]; then
	ps -f
	for i in `ps -eaf | grep java | grep "standalone.*xml" | grep -v grep | cut -c10-15`; do kill -9 $i; done
	killall -9 testsuite
	killall -9 server
	killall -9 client
	killall -9 cs
  ps -f
	exit -1
fi

# RUN ALL THE SAMPLES
chmod u+x $WORKSPACE/blacktie/target/dist/blacktie-$NARAYANA_CURRENT_VERSION/setenv.sh
. $WORKSPACE/blacktie/target/dist/blacktie-$NARAYANA_CURRENT_VERSION/setenv.sh
if [ "$?" != "0" ]; then
	ps -f
	for i in `ps -eaf | grep java | grep "standalone.*xml" | grep -v grep | cut -c10-15`; do kill -9 $i; done
	killall -9 testsuite
	killall -9 server
	killall -9 client
	killall -9 cs
    ps -f
	exit -1
fi

export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$ORACLE_LIB_DIR
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$DB2_LIB_DIR

cd $WORKSPACE/blacktie/
./run_all_quickstarts.sh
if [ "$?" != "0" ]; then
	ps -f
	for i in `ps -eaf | grep java | grep "standalone.*xml" | grep -v grep | cut -c10-15`; do kill -9 $i; done
	killall -9 testsuite
	killall -9 server
	killall -9 client
	killall -9 cs
  ps -f
	exit -1
fi

# KILL ANY BUILD REMNANTS
ps -f
for i in `ps -eaf | grep java | grep "standalone.*xml" | grep -v grep | cut -c10-15`; do kill -9 $i; done
killall -9 testsuite
killall -9 server
killall -9 client
killall -9 cs
ps -f
