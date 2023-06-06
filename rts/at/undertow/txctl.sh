#!/bin/bash

SERVICE1=http://localhost:8092/eg/service
SERVICE2=http://localhost:8094/eg/service
TXN_FAC=http://localhost:8090/tx/transaction-manager

function start_tx {
  [ $1 ] && data="timeout=$1"
  curl -X POST -i -d "$data" $TXN_FAC 2>/dev/null
}

function commit_tx {
  curl -X PUT -i -d "txstatus=TransactionCommitted" $1
}
function abort_tx {
  curl -X PUT -i -d "txstatus=TransactionRolledBack" $1
}

function tx_info {
  curl -X HEAD -i "$1" 2>/dev/null
  curl -X GET -i "$1" 2>/dev/null
}

function list_txns {
  txns=`curl -H "Accept: application/txlist" $TXN_FAC 2>/dev/null`
  #txns=`curl -X GET -i $TXN_FAC 2>/dev/null`

  IFS=';' read -ra tx <<< "$txns"
  for i in "${tx[@]}"; do echo "txn: $i"; done
}

function work_info {
  case $1 in
  text) curl -X GET -i -H "Accept: text/plain" $SERVICE ;;
  xml) curl -X GET -i -H "Accept: application/xml" $SERVICE ;;
  *) curl -X GET -i -H "Accept: application/xml" $SERVICE/context 2>/dev/null | grep collection | xmllint --format - ;;
  esac
}

function work_create {
  curl -X POST  -i -d "$2" $1?enlistURL=$3 2>/dev/null
}

function work_query {
  curl -X GET $1/query
}

case $1 in
-s) start_tx $2 ;;
-l) list_txns ;;
-i) tx_info "$2" ;;
-c) commit_tx $2 ;;
-a) abort_tx $2 ;;
-w) work_create $2 $3 $4 ;;
-q) work_query $2;;
#-p) work_info $2 ;;
 *) echo "syntax:
 $0 -s [timeout]            - start a transaction (with an optional timeout)
 $0 -l                      - list active transactions 
 $0 -i <txn URL>            - show enlistment and terminator urls for a give txn url
 $0 -c <term URL>           - commit a transaction
 $0 -a <term URL>           - abort a transaction
 $0 -w <service URL> <value> <enlist URL> - ask a web service to transactionally update a value
 $0 -q <service URL>        - ask a web service for its current value
 $0 -p [text|xml]           - ask a web service to dump info about pending work"
esac
