# ALLOW JOBS TO BE BACKGROUNDED
set -m

swarmjar="lra-participant-example-swarm.jar"
txlogdir="../txlogs"
txlogprop="swarm.transactions.object-store-path"
qsdir="$PWD"
service_port=8082
coord_port=8082

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

[[ "$svctype" =~ ^(cdi|api|mixed)$ ]] || usage "quickstart directory must start with {cdi|api|mixed}"

function killpid {
  kill $2
  test $? || echo "===== could not kill $1"
}

function test_service {
  curl -X PUT -I http://localhost:${service_port}/${svctype} || echo ===== failed
  sleep 1
  if [ "$(curl http://localhost:${service_port}/${svctype})" == "$1" ]; then
    return 0
  else
    return 1
  fi
}

echo "===== Running qickstart $qsname in directory $PWD"

if [[ "$last" != "coordinator" ]]; then
  echo "===== starting external coordinator on port ${coord_port}"
  java -D${txlogprop}=${txlogdir} -Dswarm.http.port=${coord_port} -jar ../lra-coordinator/target/lra-coordinator-swarm.jar &
  coord_pid=$!
  sleep 10
else
  coord_port=${service_port} # the coordinator is running in-VM with the service
fi

echo "===== starting service on port ${service_port}"
java -Dswarm.http.port=${service_port} -Dlra.http.port=${coord_port} -jar target/${swarmjar} &
echo "===== Running qickstart $qsname in directory $PWD"
service_pid=$!
sleep 10

echo "===== testing service"
if [ $svctype = "mixed" ]; then
  test_service "2 completed and 0 compensated"
else
  test_service "1 completed and 0 compensated"
fi
res=$?

killpid "service" $service_pid 
[[ -z ${coord_pid+x} ]] || killpid "coordinator" $coord_pid

echo "===== test $qsname completed with status $res"

exit $res
