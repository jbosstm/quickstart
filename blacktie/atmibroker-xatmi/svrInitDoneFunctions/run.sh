# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Quickstart: Running svrInitDoneFunctions"

cd $BLACKTIE_HOME/quickstarts/xatmi/svrInitDoneFunctions

# RUN THE FOOAPP SERVER
generate_server -Dserver.includes=BarService.c,SvrInitDone.c -Dserver.name=iniDone
if [ "$?" != "0" ]; then
	exit -1
fi
btadmin startup
if [ "$?" != "0" ]; then
	exit -1
fi

btadmin shutdown
if [ "$?" != "0" ]; then
	exit -1
fi
