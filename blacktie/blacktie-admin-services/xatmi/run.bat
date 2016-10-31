rem JBoss, Home of Professional Open Source
rem Copyright 2016, Red Hat, Inc., and others contributors as indicated
rem by the @authors tag. All rights reserved.
rem See the copyright.txt in the distribution for a
rem full listing of individual contributors.
rem This copyrighted material is made available to anyone wishing to use,
rem modify, copy, or redistribute it subject to the terms and conditions
rem of the GNU Lesser General Public License, v. 2.1.
rem This program is distributed in the hope that it will be useful, but WITHOUT A
rem WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
rem PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
rem You should have received a copy of the GNU Lesser General Public License,
rem v.2.1 along with this distribution; if not, write to the Free Software
rem Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
rem MA  02110-1301, USA.

@echo off

echo "Quickstart: Running XATMI admin quickstart"

rem RUN THE FOOAPP SERVER
cd ..\..\xatmi\fooapp
call generate_server -Dservice.names=FOOAPP -Dserver.includes=BarService.c -Dserver.name=fooapp
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_CONFIGURATION=win32
call btadmin startup
IF %ERRORLEVEL% NEQ 0 echo "Could not start server" & exit -1
set BLACKTIE_CONFIGURATION=

rem RUN THE ADMIN JMX CLIENT
cd ..\..\blacktie-admin-services\xatmi
call generate_client -Dclient.includes=client.c
(echo 0& echo 0& echo 0& echo 0& echo 1) | client
IF %ERRORLEVEL% NEQ 0 exit -1
(echo 0& echo 0& echo 0& echo 0& echo 2) | client
IF %ERRORLEVEL% NEQ 0 exit -1

rem PICK UP THE CLOSING SERVER
@ping 127.0.0.1 -n 3 -w 1000 > nul
