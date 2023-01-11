#!/usr/bin/env bash
# ALLOW JOBS TO BE BACKGROUNDED
set -m
set -x

quarkusjar="quarkus-app/quarkus-run.jar"
txlogdir="../txlogs"
qsdir="$PWD"
service_port=8082
coord_port=8080

usage() { echo -e "$1\nUsage: $0 [-s <service port>] [-c <coordinator port>] [-d <directory>]" 1>&2; exit 1; }

while getopts ":s:c:d:" opt; do
    case "${opt}" in
        c) coord_port=${OPTARG};
           [[ $coord_port =~ ^[0-9]+$ ]] || usage "${OPTARG} is not a valid port" ;;
        s) service_port=${OPTARG};
           [[ $service_port =~ ^[0-9]+$ ]] || usage "${OPTARG} is not a valid port" ;;
        d) qsdir=${OPTARG} ;;
        *) usage ;;
    esac
done
shift $((OPTIND-1))

[[ -d "$qsdir" ]] || usage "$qsdir is not a valid directory"
cd $qsdir
qsname=$(basename "$PWD")

arr=(${qsname//-/ })
svctype=${arr[0]}
last=${arr[${#arr[@]} - 1]}

[[ "$svctype" =~ ^(cdi)$ ]] || usage "quickstart directory must start with {cdi}"

[ "$svctype" = "mixed" ] && completions=2 || completions=1

CURL_IP_OPTS=""
IP_OPTS="${IPV6_OPTS}" # use setup of IPv6 if it's defined, otherwise go with IPv4
if [ -z "$IP_OPTS" ]; then
  IP_OPTS="-Djava.net.preferIPv4Stack=true -Djava.net.preferIPv4Addresses"
  CURL_IP_OPTS="-4"
fi

function killpid {
  kill $2
  test $? || echo "===== could not kill $1"
}

function start_service {
  echo "===== starting service on port ${service_port}"
  if test -f "target/${quarkusjar}"; then
    java ${IP_OPTS} -Dquarkus.http.port=${service_port} -Dlra.http.port=${coord_port} -jar target/${quarkusjar} &
  else
    echo "cannot find particpant jar: target/${quarkusjar}"
    return 1
  fi
  service_pid=$!
  sleep `timeout_adjust 10 2>/dev/null || echo 10`
}

function test_service {
  curl ${CURL_IP_OPTS} -X PUT -I http://localhost:${service_port}/${svctype} || echo ===== failed
  sleep `timeout_adjust 1 2>/dev/null || echo 1`
  if [ "$(curl ${CURL_IP_OPTS} http://localhost:${service_port}/${svctype})" = "$1" ]; then
    return 0
  else
    return 1
  fi
}

function start_and_test_service {
  start_service

  if [ "$1" = "true" ]; then
    echo " ===== waiting for recovery ......."
    curl ${CURL_IP_OPTS} http://localhost:${coord_port}/lra-coordinator/recovery
    # sometimes it can take two scans to complete recovery
    curl ${CURL_IP_OPTS} http://localhost:${coord_port}/lra-coordinator/recovery
    echo " ===== recovery should have happened"
    xcmd="curl ${CURL_IP_OPTS} http://localhost:${service_port}/${svctype}"
    svcstatus="$(curl ${CURL_IP_OPTS} http://localhost:${service_port}/${svctype})"
    echo "===== after recovery cmd $xcmd returned $svcstatus"

    if [ "$svcstatus" = "${completions} completed and 0 compensated" ]; then
      echo "===== recovery was successful"
      res=0
    else
      echo "===== recovery failed ($svcstatus versus ${completions}) sleeping for 1 minute to allow debugging"
      sleep 60
      res=1
    fi
  else
    echo "===== testing service"
    test_service "$completions completed and 0 compensated"
    res=$?
  fi
}

function test_recovery {
  # now test recovery
  echo "===== CdiBasedResource halt the service on pid $service_pid"
  curl ${CURL_IP_OPTS} -X PUT -I "http://localhost:${service_port}/${svctype}?fault=halt${svctype}during"
  sleep `timeout_adjust 1 2>/dev/null || echo 1`
  # verify that the service is not running
  kill -0 $service_pid > /dev/null 2>&1
  if [ "$?" != 1 ]; then
    echo "${svctype} service on pid $service_pid failed to halt"
    res=1
  else
    start_and_test_service true # restart and test the service
  fi
}

echo "===== Running qickstart $qsname in directory $PWD"

if [[ "$last" = "participant" ]]; then
#  start_coordinator
  echo "===== starting external coordinator on port ${coord_port}"
  JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
  java ${JAVA_OPTS} ${IP_OPTS} -jar $PWD/../lra-coordinator/target/lra-coordinator-quarkus.jar &
  coord_pid=$!
  sleep `timeout_adjust 10 2>/dev/null || echo 10`
elif [[ "$last" = "embedded" ]]; then
  service_port=${coord_port} # the coordinator is running in-VM with the service
else
  echo "===== error you need to choose if you need external coordinator and edit the run.sh file"
  exit 1
fi

start_and_test_service

test_recovery

# clean up

killpid "service" $service_pid
[[ -z ${coord_pid+x} ]] || killpid "coordinator" $coord_pid

echo "===== test $qsname completed with status $res"

exit $res
