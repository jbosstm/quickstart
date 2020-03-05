#!/bin/bash
set -ex

echo "===== JTS interop quickstart: step 1 (compile gf and wf):"; ./step1.sh; sleep 3
echo "===== JTS interop quickstart: step 2 (start GlassFish):"; ./step2.sh; sleep 10
echo "===== JTS interop quickstart: step 3 (start WildFly):"; ./step3.sh; sleep 10
trap ./step7.sh EXIT
echo "===== JTS interop quickstart: step 4 (deploy EJBs):"; ./step4.sh; sleep 10
echo "===== JTS interop quickstart: step 5 (EJB call gf -> wf):"; ./step5.sh; sleep 4
echo "===== JTS interop quickstart: step 6 (EJB call wf -> gf):"; ./step6.sh; sleep 4

