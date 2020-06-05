
# JBoss, Home of Professional Open Source
# Copyright 2016, Red Hat, Inc., and others contributors as indicated
# by the @authors tag. All rights reserved.
# See the copyright.txt in the distribution for a
# full listing of individual contributors.
# This copyrighted material is made available to anyone wishing to use,
# modify, copy, or redistribute it subject to the terms and conditions
# of the GNU Lesser General Public License, v. 2.1.
# This program is distributed in the hope that it will be useful, but WITHOUT A
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
# You should have received a copy of the GNU Lesser General Public License,
# v.2.1 along with this distribution; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
# MA  02110-1301, USA.

# ALLOW JOBS TO BE BACKGROUNDED
set -m

x=`ruby -v`
if [ $? != 0 ]; then
	echo "Skipping recovery demo because it requires ruby to be installed"
	exit 0
else
	echo "Running recovery demo"
fi

mvn compile exec:java -Dexec.mainClass=quickstart.TransactionAwareResource -Dexec.args="-a 127.0.0.1:8081" &

sleep `timeout_adjust 5 2>/dev/null || echo 5`

./test.sh demo

echo "Recovering failed service - this could take up to 2 minutes"
rm -f out.txt
mvn compile exec:java -Dexec.mainClass=quickstart.TransactionAwareResource -Dexec.args="-a 127.0.0.1:8081" > out.txt &
pid=$!

# wait for message indicating that the transaction was recovered (should happen within 2 minutes)
count=0
res=0
while true; do
    grep "txStatus=TransactionCommitted" out.txt 
    if [ $? == 0 ]; then
        echo "SUCCESS: Transaction was recovered"
        res=0
		break
    fi

    sleep `timeout_adjust 6 2>/dev/null || echo 6`
    count=`expr $count + 1`

    if [ $count == 20 ]; then
        echo "FAILURE: Transaction was not recovered"
        break
    fi
done

kill $pid
exit $res
