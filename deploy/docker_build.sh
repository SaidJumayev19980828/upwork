#!/bin/sh
set -e

COLOR='\033[1;33m'
NC='\033[0m'

BASE_PATH="$( cd "$(dirname "$0")" ; cd .. ; pwd -P )"
cd "$BASE_PATH"

BRANCH=`echo $CI_COMMIT_REF_NAME | cut -d "-" -f 1`
IMAGE_REGISTRY="$CI_REGISTRY_IMAGE"
IMAGE_TAG_BRANCH=$IMAGE_REGISTRY:$BRANCH
REVISION="$VERSION-$BRANCH+$CI_PIPELINE_ID.$(date --utc +%Y%m%dT%H%M%S)"
IMAGE_TAG_FQN=$IMAGE_REGISTRY:$VERSION-$BRANCH

#- export build envs

mkdir ./build
echo "IMAGE_REGISTRY=$IMAGE_REGISTRY" > ./build/env
echo "IMAGE_TAG_FQN=$IMAGE_TAG_FQN" >> ./build/env
echo "IMAGE_TAG_BRANCH=$IMAGE_TAG_BRANCH" >> ./build/env
echo "$REVISION" > ./build/REVISION

#- build

echo -e "${COLOR}-> Build image: $IMAGE_TAG_FQN${NC}"
docker build -t $IMAGE_TAG_FQN .  -f ./deploy/Dockerfile.prod
docker tag $IMAGE_TAG_FQN $IMAGE_TAG_BRANCH
[ "$BRANCH" == "prod" ] && docker tag $IMAGE_TAG_FQN $IMAGE_REGISTRY:latest

true
