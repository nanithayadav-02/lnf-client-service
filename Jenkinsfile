pipeline {
     environment {
        registryCredential = 'dockerhub'
        REPO_NAME="lnf-client-service"
        IMAGE_REPO_NAME="levernfulcrum/${REPO_NAME}"
        AWS_ACCOUNT_ID="433686923958"
        AWS_DEFAULT_REGION="us-east-1"
        REPOSITORY_URL = "https://${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com"
      }
    agent any
    triggers {
        pollSCM "* * * * *"
    }
    tools {
       maven 'default_mvn'
       jdk 'openjdk11'
    }
    stages {
        stage('Build Application') {
            steps {
                echo '=== Building LnF Application ==='
                sh 'mvn -B -DskipTests clean package'
            }
        }

        stage('Test Application') {
            steps {
                echo '=== Testing LnF Application ==='
                sh 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        stage('Package Application') {
            steps {
                echo '=== Building LnF Docker Image ==='
                script {
                    docker.withTool('docker-latest') {
                            def image1 = docker.build("${IMAGE_REPO_NAME}:${BUILD_NUMBER}", ".")
                            def image2 = docker.build("${IMAGE_REPO_NAME}:latest", ".")
                    }

                }
            }
        }
        stage('Push Container Image') {
            steps {
                echo '=== Pushing Container Image to DockerHub ==='
                script {
                    docker.withTool('docker-latest') {
                                withCredentials([usernamePassword(credentialsId: 'dockerhub',
                                                 usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                                    sh "docker login -u ${USERNAME} -p ${PASSWORD} https://index.docker.io/v1/"
                                    sh "docker push ${IMAGE_REPO_NAME}:${BUILD_NUMBER}"
                                    sh "docker push ${IMAGE_REPO_NAME}:latest"
                                }
                     }

                    echo '=== Pushing Container Image to ECR ==='
                    sh "aws ecr get-login-password --region ${AWS_DEFAULT_REGION} | docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com"
                    sh "docker tag ${REPO_NAME} ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com/${IMAGE_REPO_NAME}:${BUILD_NUMBER}"
                    sh "docker tag ${REPO_NAME} ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com/${IMAGE_REPO_NAME}:latest"
                    sh "docker push ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com/${IMAGE_REPO_NAME}:${BUILD_NUMBER}"

                }
            }
        }
        stage('Cleanup') {
            steps {
                echo '=== Remove local images ==='
                script {
                    echo '=== Delete the local docker images ==='
                    docker.withTool('docker-latest') {
                        sh("docker rmi -f ${IMAGE_REPO_NAME}:latest")
                        sh("docker rmi -f ${IMAGE_REPO_NAME}:${BUILD_NUMBER}")
                        sh("docker rmi -f ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com/${IMAGE_REPO_NAME}:${BUILD_NUMBER}")
                        sh("docker rmi -f ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com/${IMAGE_REPO_NAME}:latest")
                        sh("docker images")
                    }

                }
            }
        }
    }
}
