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

echo "Quickstart: Running library quickstart"

rem GENERATE AN EMPTY SERVER
call generate_server -Dserver.name=library
IF %ERRORLEVEL% NEQ 0 exit -1

rem GENERATE A LIBRARY WITH THE BarService SERVICE IN IT
call generate_library -Dlibrary.includes=BarService.c
IF %ERRORLEVEL% NEQ 0 exit -1

rem START THE SERVER
set BLACKTIE_CONFIGURATION=win32
call btadmin startup
set BLACKTIE_CONFIGURATION=
IF %ERRORLEVEL% NEQ 0 exit -1

rem RUN THE C CLIENT
call generate_client -Dclient.includes=client.c
client
IF %ERRORLEVEL% NEQ 0 exit -1

rem SHUTDOWN THE SERVER
call btadmin shutdown
IF %ERRORLEVEL% NEQ 0 exit -1
