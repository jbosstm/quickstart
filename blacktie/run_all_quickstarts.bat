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

echo "Running all quickstarts"

set OLDPWD=%cd%

for /R . %%i in (.) do (
	if exist %%i\run.sh (
 		cd %%i
 		call run.bat %*
		IF %ERRORLEVEL% NEQ 0 exit -1
	)
)

cd /d %OLDPWD%

rem LET THE USER KNOW THE OUTPUT
cd %BLACKTIE_HOME%
echo "All quickstarts ran OK"
