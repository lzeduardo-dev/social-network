#!/bin/bash
# Executado pelo LocalStack quando fica pronto: cria o bucket de midias
set -e
awslocal s3 mb "s3://${AWS_S3_BUCKET}" || true
echo "Bucket ${AWS_S3_BUCKET} pronto"
