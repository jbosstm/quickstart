# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Running service2 quickstart"

mvn clean compile exec:java
if [ "$?" != "0" ]; then
	exit -1
fi
