# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Running Mapped Service Names"

generate_server -Dservice.names=ONE,TWO -Dserver.output.file=hiprio  -Dserver.includes=BarService.c -Dserver.name=hiprio
if [ "$?" != "0" ]; then
	exit -1
fi
generate_server -Dservice.names=THREE,FOUR -Dserver.output.file=loprio  -Dserver.includes=BarService.c -Dserver.name=loprio
if [ "$?" != "0" ]; then
	exit -1
fi

export BLACKTIE_CONFIGURATION=linux
btadmin startup
if [ "$?" != "0" ]; then
	exit -1
fi
unset BLACKTIE_CONFIGURATION

generate_client -Dclient.includes=client.c
./client
if [ "$?" != "0" ]; then
	killall -9 hiprio
	killall -9 loprio
	exit -1
fi

export BLACKTIE_CONFIGURATION=linux
btadmin shutdown
if [ "$?" != "0" ]; then
	exit -1
fi
unset BLACKTIE_CONFIGURATION
