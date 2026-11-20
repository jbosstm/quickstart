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

[[ "$svctype" =~ ^(cdi|token)$ ]] || usage "quickstart directory must start with {cdi} or be the token-propagation example"

[ "$svctype" = "mixed" ] && completions=2 || completions=1

CURL_IP_OPTS=""
IP_OPTS="${IPV6_OPTS}" # use setup of IPv6 if it's defined, otherwise go with IPv4
if [ -z "$IP_OPTS" ]; then
  IP_OPTS="-Djava.net.preferIPv4Stack=true -Djava.net.preferIPv4Addresses"
  CURL_IP_OPTS="-4"
fi

function killpid {
  kill -9 $2
  test $? || echo "===== could not kill $1"
}

# Poll an HTTP endpoint until it answers (any HTTP status counts as "up"; we only
# care that the port is accepting connections). $1=url $2=description $3=max attempts
function wait_for_http {
  local url="$1" desc="$2" max="${3:-30}" i=0
  echo "===== waiting for ${desc} at ${url}"
  until curl ${CURL_IP_OPTS} -s -o /dev/null "${url}"; do
    i=$((i + 1))
    if [ "$i" -ge "$max" ]; then
      echo "===== ${desc} did not become ready after ${max} attempts"
      return 1
    fi
    sleep `timeout_adjust 2 2>/dev/null || echo 2`
  done
  echo "===== ${desc} is ready"
  return 0
}

# Drive the lra-jwt/quarkus/token-propagation quickstart's README workflow:
# start an external coordinator (8080) and the participant (8081), then exercise the
# happy path, a compensation and the authentication check. Sets the global $res.
function run_token_propagation {
  local participant_port=8081
  local coordinator_port=${coord_port}
  local coord_jar="$PWD/../../../lra-coordinator/target/lra-coordinator-quarkus.jar"
  local part_jar="target/${quarkusjar}"
  res=0

  if [ ! -f "$coord_jar" ]; then
    echo "===== cannot find coordinator jar: $coord_jar"
    res=1
    return 1
  fi
  if [ ! -f "$part_jar" ]; then
    echo "===== cannot find participant jar: $part_jar"
    res=1
    return 1
  fi

  echo "===== starting external coordinator on port ${coordinator_port}"
  java ${IP_OPTS} -jar "$coord_jar" &
  coord_pid=$!

  echo "===== starting participant on port ${participant_port}"
  java ${IP_OPTS} -jar "$part_jar" &
  service_pid=$!

  if ! wait_for_http "http://localhost:${coordinator_port}/lra-coordinator" "LRA coordinator"; then
    res=1
    return 1
  fi
  if ! wait_for_http "http://localhost:${participant_port}/auth/jwks" "participant"; then
    res=1
    return 1
  fi

  # 3. obtain a token (the endpoint returns it already prefixed with "Bearer ")
  echo "===== obtain a JWT via /auth/token"
  local token
  token=$(curl ${CURL_IP_OPTS} -s "http://localhost:${participant_port}/auth/token?subject=example-user")
  if [ -z "$token" ]; then
    echo "===== failed to obtain a token from /auth/token"
    res=1
    return 1
  fi
  echo "===== token: ${token}"

  # 4. happy path: create an order, expecting a CONFIRMED response
  echo "===== creating an order (happy path), expecting status CONFIRMED"
  local happy
  happy=$(curl ${CURL_IP_OPTS} -s -X POST "http://localhost:${participant_port}/orders/create" \
    -H "Content-Type: application/json" \
    -H "Authorization: ${token}" \
    -d '{"orderId":"ORDER-001","itemId":"ITEM-123","quantity":2,"amount":99.99}')
  echo "===== happy path response: ${happy}"
  if echo "$happy" | grep -q '"status":"CONFIRMED"'; then
    echo "===== happy path OK"
  else
    echo "===== happy path FAILED: expected a CONFIRMED order"
    res=1
  fi

  # 5. compensation: a negative amount makes payment decline, cancelling the LRA (HTTP 502)
  echo "===== creating an order with a negative amount, expecting HTTP 502 (compensation)"
  local comp_code
  comp_code=$(curl ${CURL_IP_OPTS} -s -o /dev/null -w '%{http_code}' -X POST "http://localhost:${participant_port}/orders/create" \
    -H "Content-Type: application/json" \
    -H "Authorization: ${token}" \
    -d '{"orderId":"ORDER-002","itemId":"ITEM-123","quantity":1,"amount":-5.00}')
  echo "===== compensation response code: ${comp_code}"
  if [ "$comp_code" = "502" ]; then
    echo "===== compensation OK"
  else
    echo "===== compensation FAILED: expected 502, got ${comp_code}"
    res=1
  fi

  # 6. authentication: a request without a token must be rejected (HTTP 401)
  echo "===== creating an order without a token, expecting HTTP 401"
  local auth_code
  auth_code=$(curl ${CURL_IP_OPTS} -s -o /dev/null -w '%{http_code}' -X POST "http://localhost:${participant_port}/orders/create" \
    -H "Content-Type: application/json" \
    -d '{"orderId":"ORDER-003","itemId":"ITEM-123","quantity":1,"amount":10.0}')
  echo "===== no-token response code: ${auth_code}"
  if [ "$auth_code" = "401" ]; then
    echo "===== authentication check OK"
  else
    echo "===== authentication check FAILED: expected 401, got ${auth_code}"
    res=1
  fi

  return $res
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

echo "===== Running quickstart $qsname in directory $PWD"

if [[ "$svctype" = "token" ]]; then
  run_token_propagation
  res=$?

  [[ -z ${service_pid+x} ]] || killpid "participant" $service_pid
  [[ -z ${coord_pid+x} ]] || killpid "coordinator" $coord_pid

  echo "===== test $qsname completed with status $res"
  exit $res
fi

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
