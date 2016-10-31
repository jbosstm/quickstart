
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

echo "Quickstart: Running integration 1 XATMI"

# RUN THE INTEGRATION 1 EXAMPLE
cd xatmi_service/
generate_server -Dservice.names=CREDITEXAMPLE,DEBITEXAMPLE -Dserver.includes="CreditService.c,DebitService.c" -Dserver.name=integra
if [ "$?" != "0" ]; then
        exit -1
fi
btadmin startup
if [ "$?" != "0" ]; then
        exit -1
fi
cd ../client/
generate_client -Dclient.includes=client.c 
./client 
if [ "$?" != "0" ]; then
	exit -1
fi
cd ../xatmi_service/
btadmin shutdown
if [ "$?" != "0" ]; then
	exit -1
fi

cd ..

#rem "Build Converted XATMI service"
(cd ejb && mvn install)
if [ "$?" != "0" ]; then
	exit -1
fi
(cd ejb/ear/ && mvn install wildfly:deploy)
if [ "$?" != "0" ]; then
	exit -1
fi
(cd xatmi_adapter/ && mvn install)
if [ "$?" != "0" ]; then
	exit -1
fi
(cd xatmi_adapter/ear/ && mvn install wildfly:deploy)
if [ "$?" != "0" ]; then
	exit -1
fi

#rem "Run Converted XATMI service"
cd client
generate_client -Dclient.includes=client.c
if [ "$?" != "0" ]; then
	exit -1
fi
sleep 5
./client 
if [ "$?" != "0" ]; then
	exit -1
fi
(cd ../xatmi_adapter/ear/ && mvn wildfly:undeploy)
if [ "$?" != "0" ]; then
	exit -1
fi
(cd ../ejb/ear/ && mvn wildfly:undeploy)
if [ "$?" != "0" ]; then
	exit -1
fi
