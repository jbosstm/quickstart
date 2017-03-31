#!/bin/bash
set -e

source init.sh

[ -d "$JBOSS_HOME"  ] || fatal "file not found: $JBOSS_HOME"

WF_DEPLOY_DIR=$JBOSS_HOME/standalone/deployments

usage() {
  echo -e "$1: Usage: $0 [-a <gf|gf2|wf>] [-f <archive>] [-t <gfgf|gfwf|wfgf >] [-t <haltbefore|haltafter>]" 1>&2;
  echo -e "examples:"
  echo -e "\t$0 -t wfgf # send a message from wf to gf"
  echo -e "\t$0 -t wfgf -t halt # send a message from wf to gf and then halt gf"
  echo -e "\t$0 -a wf1 -f ${QS_DIR}/target/ejbtest.war # deploy an archive to wildfly"
  echo -e "$0 -a gf1 -f recovery/target/dummy-resource-recovery.jar  # deploy an archive to WildFly"
  exit 1;
}

itest() {
  how=x
  [ $# -gt 1 ] && how=$2

  case $1 in
  gfgf) curl http://localhost:7080/ejbtest/rs/remote/3700/gf/$how ;;
  gfwf) curl http://localhost:7080/ejbtest/rs/remote/3528/wf/$how ;;
  wfgf) curl http://localhost:8080/ejbtest/rs/remote/3700/gf/$how ;;
  wfwf) curl http://localhost:8080/ejbtest/rs/remote/3728/wf/$how ;;
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
  wf1) cp $f $WF_DEPLOY_DIR;; # 8080 3528
  gf1) asadmin --port 4848 deploy --force=true $f;;

  gf2) asadmin --port 4948 deploy --force=true $f;;
  wf2) cp $f $WF2_DEPLOY_DIR;; # 9280 3628
  *) usage "missing as"
esac

