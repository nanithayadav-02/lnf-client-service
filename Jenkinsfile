lnfJavaPipelineWithCD ([repo: 'lnf-client-service', awsAccount: "433686923958", awsRegion: "us-east-1"], {
    return {
        echo '=== Deploying Container Image on EC2 Docker  ==='
        sh 'docker stop lnf-client-service || true && docker rm lnf-client-service || true'
        sh 'docker run -d --name lnf-client-service --network=docker_lnf-app-network -p 8082:8081 -e DATABASE_HOST=postgresdb -e DATABASE_PORT=5432 -e DATABASE_NAME=tsdb -e DATABASE_USERNAME=tsuser -e DATABASE_PASSWORD=ts@12345 -e SPRING_PROFILES_ACTIVE=dev levernfulcrum/lnf-client-service:latest'
    }
})