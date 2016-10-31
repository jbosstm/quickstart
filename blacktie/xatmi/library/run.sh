
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
