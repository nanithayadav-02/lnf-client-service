lnfJavaPipelineWithCD ([repo: 'lnf-client-service', awsAccount: "433686923958", awsRegion: "us-east-1", deploy: false], {
    return {
        echo '=== Deploying Container Image on EC2 Docker  ==='
        sh 'docker stop ${REPO_NAME} || true && docker rm -f ${REPO_NAME} || true'
        sh "docker run -d --name ${REPO_NAME} --network=docker_lnf-app-network -p 8082:8081 -e DATABASE_HOST=postgresdb -e DATABASE_PORT=5432 -e DATABASE_NAME=client_db -e DATABASE_USERNAME=client_user -e DATABASE_PASSWORD=client@12345 -e SPRING_PROFILES_ACTIVE=dev -e EMPLOYEE_SERVICE_URL=http://lnf-employee-service:8081/api/v1 ${IMAGE_REPO_NAME}:${BUILD_NUMBER}-${tag}"
    }
})