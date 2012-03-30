# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Quickstart: Running library quickstart"

# GENERATE AN EMPTY SERVER
generate_server -Dserver.name=library
if [ "$?" != "0" ]; then
	exit -1
fi

# GENERATE A LIBRARY WITH THE BarService.c IN IT
generate_library -Dlibrary.includes=BarService.c
if [ "$?" != "0" ]; then
	exit -1
fi

# RUN THE SERVER - MUST PROVIDE BLACKTIE_CONFIGURATION
# AS THIS EXAMPLE USES IT TO FIND THE CORRECT LIBRARY_NAME
export BLACKTIE_CONFIGURATION=linux
btadmin startup
if [ "$?" != "0" ]; then
	exit -1
fi
unset BLACKTIE_CONFIGURATION

# RUN THE C CLIENT
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
