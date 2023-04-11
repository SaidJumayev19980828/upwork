#!/bin/sh
set -e

COLOR='\033[1;33m'
NC='\033[0m'

BASE_PATH="$( cd "$(dirname "$0")" ; cd .. ; pwd -P )"
cd "$BASE_PATH"

#- push

echo -e "${COLOR}-> Publish to ${IMAGE_REGISTRY}${NC}"
docker push $IMAGE_TAG_FQN
docker push $IMAGE_TAG_BRANCH
if [ "$BRANCH" == "prod" ]; then docker push $IMAGE_REGISTRY:latest; fi
