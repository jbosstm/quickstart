#!/bin/bash

source init.sh
set +e

export PATH=$GLASSFISH/bin:$PATH
[ -d "$JBOSS_HOME"  ] || fatal "file not found: $JBOSS_HOME"
[ -f "$GLASSFISH/bin/asadmin"  ] || fatal "asadmin not found"

WF_DEPLOY_DIR=$JBOSS_HOME/standalone/deployments

usage() {
  echo -e "$1: Usage: $0 [-a <gf|gf2|wf>] [-f <archive>] [-t <gfgf|gfwf|wfgf >] [-t <haltbefore|haltafter>]" 1>&2;
  echo -e "examples:"
  echo -e "\t$0 -t wfgf # send a message from WildFly to GlassFish"
  echo -e "\t$0 -t wfgf -t halt # send a message from WildFly to GlassFish and then halt GlassFish"
  echo -e "\t$0 -a wf1 -f ${QS_DIR}/target/ejbtest.war # deploy an archive to wildfly"
  echo -e "$0 -a gf1 -f recovery/target/dummy-resource-recovery.jar  # deploy an archive to WildFly"
  exit 1;
}

http_get() {
  res=$( curl --max-time 10 -qSfsw '\n%{http_code}' $1 ) # 2>/dev/null
  exit_code=$?
  http_code=$(echo "$res" | tail -n1) # the HTTP status code is the last line of the curl output

  echo "http get for $1 curl status: $exit_code http status: $http_code res=$res"

  if [ $exit_code = 0 ]; then
    echo "http get for $1 curl ok"
  else
    fatal "curl error: $exit_code http status: $http_code for url $1"
  fi

  if [ $http_code = 200 ]; then
    echo "http get for $1 status ok"
  else
    fatal "unexpected http status code: $http_code for url $1"
  fi

  # the last line is the body of the response:
  echo "EJB test returned: $res" | head -n-1
}

itest() {
  how=x
  [ $# -gt 1 ] && how=$2

  case $1 in
  gfgf) http_get http://localhost:7080/ejbtest/rs/remote/3700/gf/$how ;;
  gfwf) http_get http://localhost:7080/ejbtest/rs/remote/3528/wf/$how ;;
  wfgf) http_get http://localhost:8080/ejbtest/rs/remote/3700/gf/$how ;;
  wfwf) http_get http://localhost:8080/ejbtest/rs/remote/3728/wf/$how ;;
  *)
  esac

  exit $?
}

while getopts "t:a:f:" o; do
case "${o}" in
  t) iargs+=("$OPTARG");;
  a) a=${OPTARG};;
  f) f=${OPTARG};;
  *) usage;;
esac
done

[ ${#iargs[@]} != 0 ] && itest ${iargs[*]}

[ $f ] || usage "missing file"

case $a in
  gf)  asadmin --port 4848 deploy --force=true $f
       asadmin --port 4948 deploy --force=true $f
       ;;
  wf)  cp $f $WF_DEPLOY_DIR
       cp $f $WF2_DEPLOY_DIR
       ;;
  wf1) cp $f $WF_DEPLOY_DIR;; # WildFly uses ports 8080 3528
  gf1) asadmin --port 4848 deploy --force=true $f;;

  uwf1) rm $WF_DEPLOY_DIR/${f}.war;; # WildFly uses ports 8080 3528
  ugf1) asadmin --port 4848 undeploy $f;;

  gf2) asadmin --port 4948 deploy --force=true $f;;
  wf2) cp $f $WF2_DEPLOY_DIR;; # GlassFish uses ports 9280 3628
  *) usage "missing as"
esac

