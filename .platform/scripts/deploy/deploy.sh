#!/usr/bin/env bash
set -e

echo "build"

echo "[1] clean project"
./gradlew clean

echo "[2] build core api"
./gradlew baezzal-application:build -x test

echo "[3] create docker image"
cd baezzal-application
docker build --platform linux/amd64 -t wwan13/baezzal-api:"$1" .

echo "[4] push to docker hub"
docker push wwan13/baezzal-api:"$1"
