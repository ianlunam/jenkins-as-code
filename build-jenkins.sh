#!/usr/bin/env bash

# -e enables 'fail on error' processing in bash
set -e

cd docker

# Get the tag to use for the image
BUILD_TAG="jenkins-$(git describe --tags)"

# Build and tag image
sudo docker build -t ${ECR_REPO}/jenkins:${BUILD_TAG} .
sudo docker tag ${ECR_REPO}/jenkins:${BUILD_TAG} ${ECR_REPO}/jenkins:latest

# Login for docker
sudo $(aws ecr get-login --no-include-email --region us-east-1)

# Push to ECR
sudo docker push ${ECR_REPO}/jenkins:${BUILD_TAG}
sudo docker push ${ECR_REPO}/jenkins:latest

# Remove old image
sudo docker rmi ${ECR_REPO}/jenkins:${BUILD_TAG}

# Report build version
echo "Built Tag: ${BUILD_TAG}"
