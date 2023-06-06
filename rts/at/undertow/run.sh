# ALLOW JOBS TO RUN IN THE BACKGROUND
set -m

echo "Running rest-at with undertow quickstart"

mvn clean package && java -jar target/rts-undertow-qs.jar

if [ "$?" != "0" ]; then
    exit -1
else
    exit 0
fi
