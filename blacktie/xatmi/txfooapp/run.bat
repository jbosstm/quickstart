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

rem RUN THE TXFOOAPP SERVER
IF ["%1"] EQU ["tx"] (
SHIFT

echo "Quickstart: Running txfooapp"

copy tnsnames.ora %ORACLE_HOME%\network\admin

call generate_server -Dservice.names=TXFOOAPP -Dserver.includes="request.c ora.c DbService.c" -Dx.inc.dir="%ORACLE_HOME%\OCI\include" -Dx.lib.dir="%ORACLE_HOME%\OCI\lib\MSVC" -Dx.libs="oci" -Dx.define="ORACLE" -Dserver.name=txfooap
IF %ERRORLEVEL% NEQ 0 exit -1
call generate_client -Dclient.includes="client.c request.c ora.c cutil.c" -Dx.inc.dir="%ORACLE_HOME%\OCI\include" -Dx.lib.dir="%ORACLE_HOME%\OCI\lib\MSVC" -Dx.libs="oci" -Dx.define="ORACLE"
IF %ERRORLEVEL% NEQ 0 exit -1

set BLACKTIE_CONFIGURATION_DIR=svr
set BLACKTIE_CONFIGURATION=win32
call btadmin startup
IF %ERRORLEVEL% NEQ 0 exit -1

set BLACKTIE_CONFIGURATION_DIR=cli
set BLACKTIE_CONFIGURATION=win32
rem RUN THE C CLIENT
client
IF %ERRORLEVEL% NEQ 0 exit -1

rem SHUTDOWN THE SERVER RUNNING THE btadmin TOOL
set BLACKTIE_CONFIGURATION_DIR=cli
set BLACKTIE_CONFIGURATION=win32
call btadmin shutdown
IF %ERRORLEVEL% NEQ 0 exit -1
set BLACKTIE_CONFIGURATION=
set BLACKTIE_CONFIGURATION_DIR=
)
