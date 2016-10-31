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

echo "Quickstart: Running externally managed queue quickstart"

rem Running externally managed queue quickstart
call generate_client -Dclient.includes=queues.c -Dx.define=WIN32
client put 10
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_SERVER_ID=1
client get 5
IF %ERRORLEVEL% NEQ 0 exit -1
client get 5
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_SERVER_ID=
rem Successful

rem Running txsender queue quickstart
echo "Quickstart: Running transactional queue quickstart"
call generate_client -Dclient.includes=txsender.c -Dclient.output.file=txsender -Dx.define=WIN32
IF %ERRORLEVEL% NEQ 0 exit -1
call generate_client -Dclient.includes=queues.c -Dx.define=WIN32
IF %ERRORLEVEL% NEQ 0 exit -1
(echo 1) | txsender
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_SERVER_ID=1
client get 2
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_SERVER_ID=
rem Successful

rem Running propagated transaction queue quickstart
echo "Quickstart: Running propagated transaction queue quickstart"
call generate_client -Dclient.includes=queues.c -Dclient.output.file=client
IF %ERRORLEVEL% NEQ 0 exit -1
call generate_server -Dserver.includes=BarService.c  -Dservice.names=QUEUES -Dserver.name=queues
IF %ERRORLEVEL% NEQ 0 exit -1
call generate_client -Dclient.includes=client.c -Dclient.output.file=clientSender
call btadmin startup
IF %ERRORLEVEL% NEQ 0 exit -1
(echo 1) | clientSender
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_SERVER=myserv
set BLACKTIE_SERVER_ID=1
client get 1
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_SERVER_ID=
set BLACKTIE_SERVER=
call btadmin shutdown
IF %ERRORLEVEL% NEQ 0 exit -1
rem Successful
