lnfJavaPipelineWithCD ([repo: 'lnf-client-service', awsAccount: "account_number", awsRegion: "us-east-1", deploy: true], {
    return {
        echo '=== Deploying Container Image on Kube  ==='
        sh 'kubectl apply -f deployment.yaml'
    }
})