# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Quickstart: Running library quickstart"

EXAMPLE_HOME=$BLACKTIE_HOME/quickstarts/xatmi/library

# GENERATE AN EMPTY SERVER
cd $EXAMPLE_HOME
generate_server -Dserver.name=library
if [ "$?" != "0" ]; then
	exit -1
fi

# GENERATE A LIBRARY WITH THE BarService.c IN IT
cd $EXAMPLE_HOME
generate_library -Dlibrary.includes=BarService.c
if [ "$?" != "0" ]; then
	exit -1
fi

# RUN THE SERVER - MUST PROVIDE BLACKTIE_CONFIGURATION
# AS THIS EXAMPLE USES IT TO FIND THE CORRECT LIBRARY_NAME
cd $EXAMPLE_HOME
export BLACKTIE_CONFIGURATION=linux
btadmin startup
if [ "$?" != "0" ]; then
	exit -1
fi
unset BLACKTIE_CONFIGURATION

# RUN THE C CLIENT
cd $EXAMPLE_HOME
generate_client -Dclient.includes=client.c
./client
if [ "$?" != "0" ]; then
	killall -9 server
	exit -1
fi

# SHUTDOWN THE SERVER
btadmin shutdown
if [ "$?" != "0" ]; then
	exit -1
fi
