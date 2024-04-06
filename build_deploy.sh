#!/bin/bash

readonly DOCKER_REGISTRY="docker.io/levernfulcrum"
readonly DOCKER_IMAGE="lnf-client-service"
readonly TAG="$(date +%Y%m%d%H%M%S)-v2"
readonly TAG_LATEST="latest.jdk17"
readonly IMG=$DOCKER_REGISTRY/$DOCKER_IMAGE:$TAG
readonly IMG_LATEST=$DOCKER_REGISTRY/$DOCKER_IMAGE:$TAG_LATEST

echo -e "Build: Docker Image"
docker build -t $IMG .
echo -e "Build: Docker Image Complete"

echo -e "Tag: Docker Image to Latest"
docker tag $IMG $IMG_LATEST
echo -e "Tag: Docker Image to Latest Complete"

echo -e "Push: Docker Image to registry"
# Store the docker user password in password.txt in home directory
cat ~/password.txt | docker login --username=$DOCKER_USER --password-stdin
docker push $IMG
docker push $IMG_LATEST
echo -e "Push: Docker Image to registry Complete"