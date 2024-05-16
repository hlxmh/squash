#!/bin/bash

sudo docker build -t serverlesskt-ws . &&
gcloud auth configure-docker "us-east1-docker.pkg.dev" &&
docker tag serverlesskt-ws us-east1-docker.pkg.dev/cultivated-pen-417119/repo/ktorserver &&
docker push us-east1-docker.pkg.dev/cultivated-pen-417119/repo/ktorserver &&
gcloud run deploy ktorserver \
   --image us-east1-docker.pkg.dev/cultivated-pen-417119/repo/ktorserver \
   --project cultivated-pen-417119 \
   --region us-east1 \
   --port 8080 \
   --allow-unauthenticated
