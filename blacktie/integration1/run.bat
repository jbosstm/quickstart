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

echo "Quickstart: Running integration 1 XATMI"

rem RUN THE INTEGRATION 1 EXAMPLE
cd xatmi_service\
call generate_server -Dservice.names=CREDITEXAMPLE,DEBITEXAMPLE -Dserver.includes="CreditService.c,DebitService.c" -Dserver.name=integra
IF %ERRORLEVEL% NEQ 0 exit -1
call btadmin startup
IF %ERRORLEVEL% NEQ 0 exit -1
cd ..\client\
call generate_client -Dclient.includes=client.c 
.\client 
IF %ERRORLEVEL% NEQ 0 exit -1
cd ..\xatmi_service\
call btadmin shutdown
IF %ERRORLEVEL% NEQ 0 exit -1

cd ..\ejb
call mvn install
IF %ERRORLEVEL% NEQ 0 exit -1
cd ear
call mvn install wildfly:deploy
IF %ERRORLEVEL% NEQ 0 exit -1
cd ..\..\xatmi_adapter\
call mvn install
IF %ERRORLEVEL% NEQ 0 exit -1
cd ear\
call mvn install wildfly:deploy
IF %ERRORLEVEL% NEQ 0 exit -1

cd ..\..\client\
call generate_client -Dclient.includes=client.c 
IF %ERRORLEVEL% NEQ 0 exit -1
@ping 127.0.0.1 -n 10 -w 1000 > nul
.\client
IF %ERRORLEVEL% NEQ 0 exit -1
cd ..\xatmi_adapter\ear\
call mvn wildfly:undeploy
IF %ERRORLEVEL% NEQ 0 exit -1
cd ..\..\ejb\ear
call mvn wildfly:undeploy
IF %ERRORLEVEL% NEQ 0 exit -1
