lnfJavaPipelineWithCD ([repo: 'lnf-client-service', awsAccount: "433686923958", awsRegion: "us-east-1", deploy: true], {
    return {
        echo '=== Deploying Container Image on EC2 Docker  ==='
        sh 'kubectl version -o yaml'
        sh 'cat deployment.yaml'
        sh 'kubectl apply -f deployment.yaml'
    }
})