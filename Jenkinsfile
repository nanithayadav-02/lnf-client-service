lnfJavaPipelineWithCD ([repo: 'lnf-client-service', awsAccount: "433686923958", awsRegion: "us-east-1", deploy: true], {
    return {
        echo '=== Deploying Container Image on EC2 Docker  ==='
        sh 'cat deployment.yaml | sed "s/{{BUILD_NUMBER}}/$BUILD_NUMBER/g" | kubectl apply -f -'
    }
})