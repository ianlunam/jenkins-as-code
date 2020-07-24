#!/usr/bin/env bash

docker kill my-jenkins 2>/dev/null
docker rm my-jenkins  2>/dev/null

if [[ ! -z "$1" ]]; then
    docker rm $(docker ps -q -f 'status=exited') 2>/dev/null
    docker rmi $(docker images -q -f "dangling=true") 2>/dev/null
    docker volume rm $(docker volume ls -qf dangling=true) 2>/dev/null
fi

set -e

time docker build -t my-jenkins .
docker run -d \
        --name my-jenkins \
        -p 8080:8080 \
        -v /var/run/docker.sock:/var/run/docker.sock \
        -v $(which docker):$(which docker) \
        --env-file ./env.file \
        my-jenkins:latest
docker logs -f my-jenkins
