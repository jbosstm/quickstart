# ALLOW JOBS TO BE BACKGROUNDED
set -m

echo "Running recovery2 quickstart"

mvn -P fail clean compile exec:java

# We expect this to fail
[ "$?" != "0" ] || exit -1

echo "Recovering failed service - this could take up to 2 minutes"
mvn -P recover exec:java
if [ "$?" != "0" ]; then
    echo "Service recovery2 example FAILED"
    exit -1
else
    echo "Service recovery2 example SUCCEEDED"
fi

