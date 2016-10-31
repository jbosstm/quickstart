
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

echo "Quickstart: Running quickstart to show reply to"

# GENERATE A SERVER
generate_server -Dserver.includes=BarService.c -Dserver.name=server
if [ "$?" != "0" ]; then
    exit -1
fi

# RUN THE SERVER - MUST PROVIDE BLACKTIE_CONFIGURATION
# AS THIS EXAMPLE USES IT TO FIND THE CORRECT LIBRARY_NAME
btadmin startup
if [ "$?" != "0" ]; then
	exit -1
fi

# BUILD THE C CLIENT
generate_client -Dclient.includes=client.c

# RUN THE C clientA
echo '0
0' | ./client clientA CLIENTRESPONSEHANDLER_1 hello1
if [ "$?" != "0" ]; then
	killall -9 server
	exit -1
fi

# RUN THE C clientB
echo '0
0' | ./client clientB CLIENTRESPONSEHANDLER_2 hello2
if [ "$?" != "0" ]; then
	killall -9 server
	exit -1
fi


# SHUTDOWN THE SERVER
BLACKTIE_SERVER=server btadmin shutdown
if [ "$?" != "0" ]; then
	exit -1
fi
