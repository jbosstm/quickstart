
# JBoss, Home of Professional Open Source
# Copyright 2016, Red Hat, Inc., and others contributors as indicated
# by the @authors tag. All rights reserved.
# See the copyright.txt in the distribution for a
# full listing of individual contributors.
# This copyrighted material is made available to anyone wishing to use,
# modify, copy, or redistribute it subject to the terms and conditions
# of the GNU Lesser General Public License, v. 2.1.
# This program is distributed in the hope that it will be useful, but WITHOUT A
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
# You should have received a copy of the GNU Lesser General Public License,
# v.2.1 along with this distribution; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
# MA  02110-1301, USA.

# ALLOW JOBS TO BE BACKGROUNDED
set -m

if [ "$1" ]; then
if [ "$1" = "tx" ]; then
shift

echo "Quickstart: Running txfooapp"

# RUN THE FOOAPP SERVER
if [ "$1" = "db2" ]; then
    echo "using RMs for DB2"
    generate_server -Dservice.names=TXFOOAPP -Dserver.includes="request.c db2.c DbService.c" -Dx.inc.dir2="$DB2DIR/include" -Dx.lib.dir2="$DB2_LIB" -Dx.libs2="db2" -Dx.define="DB2" -Dserver.name=txfooap
	[[ "$?" != "0" ]] && exit 1

	generate_client -Dclient.includes="client.c request.c db2.c cutil.c" -Dx.inc.dir2="$DB2DIR/include" -Dx.lib.dir2="$DB2_LIB" -Dx.libs2="db2" -Dx.define="DB2"
	[[ "$?" != "0" ]] && exit 1
elif [ "$1" = "ora" ]; then
	echo "using RMs for Oracle only"
	rm -rf instantclient_11_2
	OS_BITS=`uname -m`
	if [ "$OS_BITS" = "x86_64" ]; then
		unzip ~/instantclient-basic-linux.x64-11.2.0.4.0.zip
		unzip ~/instantclient-sdk-linux.x64-11.2.0.4.0.zip
  else
		unzip ~/instantclient-basic-linux-11.2.0.4.0.zip
		unzip ~/instantclient-sdk-linux-11.2.0.4.0.zip
	fi
	ORACLE_HOME=`pwd`/instantclient_11_2
	mkdir $ORACLE_HOME/lib
	mkdir -p $ORACLE_HOME/network/admin
	ORACLE_INC_DIR=$ORACLE_HOME/include
	ORACLE_LIB_DIR=$ORACLE_HOME/lib
	mv $ORACLE_HOME/lib*.so* $ORACLE_LIB_DIR
	ln -s $ORACLE_LIB_DIR/libocci.so.11.1 $ORACLE_LIB_DIR/libocci.so
	ln -s $ORACLE_LIB_DIR/libclntsh.so.11.1 $ORACLE_LIB_DIR/libclntsh.so
	mv $ORACLE_HOME/sdk/include $ORACLE_INC_DIR
	cp tnsnames.ora $ORACLE_HOME/network/admin
	export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$ORACLE_LIB_DIR
	export ORACLE_HOME

	sed -i /201/,+2s#P/scott/tiger#P/dtf11/dtf11# cli/btconfig.xml
	sed -i /201/,+2s#P/scott/tiger#P/dtf11/dtf11# svr/btconfig.xml
	sed -i /203/,+2s#P/scott/tiger#P/dtf12/dtf12# cli/btconfig.xml
	sed -i /203/,+2s#P/scott/tiger#P/dtf12/dtf12# svr/btconfig.xml

	generate_server -Dservice.names=TXFOOAPP -Dserver.includes="request.c ora.c DbService.c" -Dx.inc.dir="$ORACLE_INC_DIR" -Dx.lib.dir="$ORACLE_LIB_DIR" -Dx.libs="occi clntsh nnz11" -Dx.define="ORACLE" -Dserver.name=txfooap
	[[ "$?" != "0" ]] && exit 1
	generate_client -Dclient.includes="client.c request.c ora.c cutil.c" -Dx.inc.dir="$ORACLE_INC_DIR" -Dx.lib.dir="$ORACLE_LIB_DIR" -Dx.libs="occi clntsh nnz11" -Dx.define="ORACLE"
	[[ "$?" != "0" ]] && exit 1
else
	echo "using RMs for Oracle and DB2"
    generate_server -Dservice.names=TXFOOAPP -Dserver.includes="request.c db2.c ora.c DbService.c" -Dx.inc.dir="$ORACLE_INC_DIR" -Dx.inc.dir2="$DB2DIR/include" -Dx.lib.dir="$ORACLE_LIB_DIR" -Dx.libs="occi clntsh" -Dx.lib.dir2="$DB2_LIB" -Dx.libs2="db2" -Dx.define="DB2" -Dx.define2="ORACLE" -Dserver.name=txfooap
	[[ "$?" != "0" ]] && exit 1

	generate_client -Dclient.includes="client.c request.c db2.c ora.c cutil.c" -Dx.inc.dir="$ORACLE_INC_DIR" -Dx.inc.dir2="$DB2DIR/include" -Dx.lib.dir="$ORACLE_LIB_DIR" -Dx.libs="occi clntsh" -Dx.lib.dir2="$DB2_LIB" -Dx.libs2="db2" -Dx.define="DB2" -Dx.define2="ORACLE"
	[[ "$?" != "0" ]] && exit 1
fi

export BLACKTIE_CONFIGURATION=linux
# use a different config for server and client for different listen ports
export BLACKTIE_CONFIGURATION_DIR=svr
# use a different logfile for the server
export LOG4CXXCONFIG=log4cxx.server.properties
btadmin startup
if [ "$?" != "0" ]; then
        exit -1
fi

#the client for this test needs to act as a server
#unset BLACKTIE_CONFIGURATION

# RUN THE C CLIENT
# use the default logfile for the client
unset LOG4CXXCONFIG
export BLACKTIE_CONFIGURATION_DIR=cli
export export BLACKTIE_CONFIGURATION=linux
./client
if [ "$?" != "0" ]; then
	exit -1
fi

# SHUTDOWN THE SERVER RUNNING THE btadmin TOOL
export BLACKTIE_CONFIGURATION=linux
btadmin shutdown
if [ "$?" != "0" ]; then
	exit -1
fi
unset BLACKTIE_CONFIGURATION BLACKTIE_CONFIGURATION_DIR

fi
fi
