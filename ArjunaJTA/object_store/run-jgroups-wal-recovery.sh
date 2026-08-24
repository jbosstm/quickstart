#!/bin/bash
# Demonstrates WAL-based disaster recovery for the JGroups SlotStore.
# Phase 1 creates an in-doubt transaction and exits (simulating a crash).
# Phase 2 restarts from the WAL and verifies the transaction is recoverable.

echo "JGroups WAL Disaster Recovery Quickstart"
echo "========================================="

[ "x$QUICKSTART_NARAYANA_VERSION" != 'x' ] &&\
  NARAYANA_VERSION_PARAM="-Dversion.narayana=${QUICKSTART_NARAYANA_VERSION}"

MAIN_CLASS=org.jboss.narayana.jta.quickstarts.JGroupsSlotStoreWALRecoveryExample

# Clean up from previous runs
rm -rf wal-recovery-store wal-recovery-uid.txt

echo "Compiling..."
mvn -q compile $NARAYANA_VERSION_PARAM
if [ $? -ne 0 ]; then
    echo "FAILED: compilation error"
    exit 1
fi

echo ""
echo "--- Phase 1: Create in-doubt transaction ---"
mvn -q exec:java -Dexec.mainClass=$MAIN_CLASS \
    -Dexec.args="create" $NARAYANA_VERSION_PARAM
if [ $? -ne 0 ]; then
    echo "FAILED: create phase failed"
    rm -rf wal-recovery-store wal-recovery-uid.txt
    exit 1
fi

echo ""
echo "--- Phase 2: Recover from WAL ---"
mvn -q exec:java -Dexec.mainClass=$MAIN_CLASS \
    -Dexec.args="recover" $NARAYANA_VERSION_PARAM
RC=$?

# Clean up
rm -rf wal-recovery-store wal-recovery-uid.txt

if [ $RC -eq 0 ]; then
    echo ""
    echo "PASSED: transaction recovered from WAL after simulated crash"
else
    echo ""
    echo "FAILED: WAL recovery did not find the expected transaction"
fi

exit $RC
