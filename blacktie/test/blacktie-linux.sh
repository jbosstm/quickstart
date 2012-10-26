ulimit -c unlimited

# CHECK IF WORKSPACE IS SET
if [ -n "${WORKSPACE+x}" ]; then
  echo WORKSPACE is set
else
  echo WORKSPACE not set
  exit -1
fi

# Do not use the CI setting of JBOSS_HOME
JBOSS_HOME=$WORKSPACE/jboss-as-7.1.1.Final

if [ -z "${JBOSSAS_IP_ADDR+x}" ]; then
  echo JBOSSAS_IP_ADDR not set
  JBOSSAS_IP_ADDR=localhost
fi


# KILL ANY PREVIOUS BUILD REMNANTS
ps -f
for i in `ps -eaf | grep java | grep "standalone-full.xml" | grep -v grep | cut -c10-15`; do kill -9 $i; done
killall -9 testsuite
killall -9 server
killall -9 client
killall -9 cs
ps -f

# GET THE TNS NAMES
TNS_ADMIN=$WORKSPACE/instantclient_11_2/network/admin
mkdir -p $TNS_ADMIN
if [ -e $TNS_ADMIN/tnsnames.ora ]; then
	echo "tnsnames.ora already downloaded"
else
	(cd $TNS_ADMIN; wget http://albany/userContent/blacktie/tnsnames.ora)
fi

# INITIALIZE JBOSS and CREATE BLACKTIE DISTRIBUTION
ant -f $WORKSPACE/blacktie/test/initializeBlackTie.xml -Dbasedir=.. -DJBOSS_HOME=$JBOSS_HOME -DBT_HOME=$WORKSPACE/blacktie/target/dist/ -DVERSION=5.0.0.M2-SNAPSHOT -DMACHINE_ADDR=`hostname` -DJBOSSAS_IP_ADDR=$JBOSSAS_IP_ADDR -DBLACKTIE_DIST_HOME=$BLACKTIE_DIST_HOME initializeJBoss initializeBlackTie -debug
if [ "$?" != "0" ]; then
	ps -f
	for i in `ps -eaf | grep java | grep "standalone-full.xml" | grep -v grep | cut -c10-15`; do kill -9 $i; done
	killall -9 testsuite
	killall -9 server
	killall -9 client
	killall -9 cs
  	ps -f
	exit -1
fi
chmod u+x $JBOSS_HOME/bin/standalone.sh

# START JBOSS
$JBOSS_HOME/bin/standalone.sh -c standalone-full.xml -Djboss.bind.address=$JBOSSAS_IP_ADDR -Djboss.bind.address.unsecure=$JBOSSAS_IP_ADDR&
sleep 20

# TWEAK txfooapp FOR THIS NODE
ant -f $WORKSPACE/blacktie/test/initializeBlackTie.xml tweak-txfooapp-for-environment
if [ "$?" != "0" ]; then
	ps -f
	for i in `ps -eaf | grep java | grep "standalone-full.xml" | grep -v grep | cut -c10-15`; do kill -9 $i; done
	killall -9 testsuite
	killall -9 server
	killall -9 client
	killall -9 cs
  ps -f
	exit -1
fi

# RUN ALL THE SAMPLES
chmod u+x $WORKSPACE/blacktie/target/dist/blacktie-5.0.0.M2-SNAPSHOT/setenv.sh
. $WORKSPACE/blacktie/target/dist/blacktie-5.0.0.M2-SNAPSHOT/setenv.sh
if [ "$?" != "0" ]; then
	ps -f
	for i in `ps -eaf | grep java | grep "standalone-full.xml" | grep -v grep | cut -c10-15`; do kill -9 $i; done
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
./run_all_quickstarts.sh tx
if [ "$?" != "0" ]; then
	ps -f
	for i in `ps -eaf | grep java | grep "standalone-full.xml" | grep -v grep | cut -c10-15`; do kill -9 $i; done
	killall -9 testsuite
	killall -9 server
	killall -9 client
	killall -9 cs
  ps -f
	exit -1
fi

# KILL ANY BUILD REMNANTS
ps -f
for i in `ps -eaf | grep java | grep "standalone-full.xml" | grep -v grep | cut -c10-15`; do kill -9 $i; done
killall -9 testsuite
killall -9 server
killall -9 client
killall -9 cs
ps -f
