#!/bin/bash

set -m

SCRIPT_DIR=$(dirname "${0}")

if [ -z "${PROJECT_ID}" ] || [ -z "${INSTANCE_ID}" ] || [ -z "${DATABASE_ID}" ]; then
  echo "Env vars PROJECT_ID (\"${PROJECT_ID}\"), INSTANCE_ID (\"${INSTANCE_ID}\") and DATABASE_ID (\"${DATABASE_ID}\") must be set."
  exit 1
fi

echo "PROJECT_ID=${PROJECT_ID}"
echo "INSTANCE_ID=${INSTANCE_ID}"
echo "DATABASE_ID=${DATABASE_ID}"

SPANNER_HOST="0.0.0.0"
SPANNER_GRPC_PORT=9010
SPANNER_HTTP_PORT=9020
STARTUP_TIMEOUT_IN_SECS=60

function abort {
  echo "SIGINT received. Aborting."
  exit 1
}

trap "abort" SIGINT

function check_spanner_port_open {
  echo "Checking if spanner is available..."
  nc -z "${SPANNER_HOST}" "${SPANNER_HTTP_PORT}"
  return ${?}
}

function wait_for_spanner_port_open {
  local BACKOFF_IN_SECS=3
  local START_TIME=${SECONDS}

  local time_delta=$(("${SECONDS}"-"${START_TIME}"))
  check_spanner_port_open
  local return_code=${?}
  while [[ "${return_code}" -ne 0 ]]; do
    if [ ${time_delta} -gt ${STARTUP_TIMEOUT_IN_SECS} ]; then
      echo "Cannot connect to spanner, no available after ${STARTUP_TIMEOUT_IN_SECS}s!"
      exit
    fi
    sleep "${BACKOFF_IN_SECS}s";

    time_delta=$(("${SECONDS}"-"${START_TIME}"))
    check_spanner_port_open
    return_code=${?}
  done
}

function run_spanner {
  mkdir -p  "${HOME}"/.config/gcloud/emulators/spanner
  cat <<EOF > "${HOME}"/.config/gcloud/emulators/spanner/env.yaml
---
SPANNER_EMULATOR_HOST: ${SPANNER_HOST}:${SPANNER_HTTP_PORT}
EOF
  gcloud emulators spanner env-init
  gcloud config configurations create emulator
  gcloud config set auth/disable_credentials true
  gcloud config set project "$PROJECT_ID"
  gcloud config set api_endpoint_overrides/spanner "http://${SPANNER_HOST}:${SPANNER_HTTP_PORT}/"
  gcloud config configurations activate emulator
  gcloud emulators spanner start --quiet --host-port "${SPANNER_HOST}:${SPANNER_GRPC_PORT}" --rest-port "${SPANNER_HTTP_PORT}" & # starting spanner in the background to run migrations
  wait_for_spanner_port_open
  gcloud spanner instances create "${INSTANCE_ID}" \
     --config=emulator-config --description="Test Instance" --nodes=1
  gcloud spanner databases create "${DATABASE_ID}" --instance="${INSTANCE_ID}" --ddl-file=${SCRIPT_DIR}/schema.ddl
  echo "Spanner emulator ready."
  wait
}

function check_spanner_can_execute_sql {
  echo "Checking if spanner is healthy..."
  gcloud spanner databases execute-sql "${DATABASE_ID}" --instance="${INSTANCE_ID}" --sql="SELECT * FROM information_schema.tables"
}

case $1 in
  healthcheck)
    check_spanner_can_execute_sql
    ;;
  run)
    run_spanner
    ;;
  *)
    echo "$0 run|healthcheck"
    exit 1
    ;;
esac
