#!/bin/bash
set -e

docker-compose down --remove-orphans
docker-compose build