#!/bin/bash
# Demonstrates two transaction manager nodes sharing a JGroups ReplCache store.
# Node1 creates an in-doubt transaction, node2 joins the cluster and verifies
# it can see node1's transaction (proving cross-node recovery is possible).

set -m

echo "JGroups Cluster Object Store Quickstart"
echo "========================================"

NARAYANA_VERSION_PARAM=()
if [ -n "$QUICKSTART_NARAYANA_VERSION" ]; then
  NARAYANA_VERSION_PARAM=("-Dversion.narayana=${QUICKSTART_NARAYANA_VERSION}")
fi

MAIN_CLASS=org.jboss.narayana.jta.quickstarts.JGroupsSlotStoreClusterExample

# Clean up from previous runs
rm -rf SlotStore-node1 SlotStore-node2 node1.ready node2.ready

echo "Compiling..."
if ! mvn -q compile "${NARAYANA_VERSION_PARAM[@]}"; then
    echo "FAILED: compilation error"
    exit 1
fi

cleanup() {
    kill $PID1 2>/dev/null
    wait $PID1 2>/dev/null
    rm -f node1.ready node2.ready
    rm -rf SlotStore-node1 SlotStore-node2
}
trap cleanup EXIT
trap 'exit 1' INT TERM

echo "Starting node1 on port 7800..."
mvn -q exec:java -Dexec.mainClass=$MAIN_CLASS \
    -Dexec.args="node1" -Djgroups.bind_port=7800 "${NARAYANA_VERSION_PARAM[@]}" &
PID1=$!

echo "Waiting for node1 to create its in-doubt transaction..."
TIMEOUT=30
while [ ! -f node1.ready ] && [ $TIMEOUT -gt 0 ]; do
    sleep 1
    TIMEOUT=$((TIMEOUT - 1))
done

if [ ! -f node1.ready ]; then
    echo "FAILED: node1 did not become ready within 30 seconds"
    exit 1
fi

echo "Starting node2 on port 7801..."
mvn -q exec:java -Dexec.mainClass=$MAIN_CLASS \
    -Dexec.args="node2" -Djgroups.bind_port=7801 "${NARAYANA_VERSION_PARAM[@]}"
RC=$?

if [ $RC -eq 0 ]; then
    echo "PASSED: node2 can see node1's in-doubt transactions"
else
    echo "FAILED: node2 could not see node1's transactions (exit code $RC)"
fi

exit $RC
