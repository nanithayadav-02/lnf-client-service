pipeline {
     environment {
        registry = "levernfulcrum/lnf-client-service"
        registryCredential = 'dockerhub'
        dockerImage = ''
      }
    agent any
       triggers {
        pollSCM "* * * * *"
       }
    tools {
            maven 'Maven 3.6.3'
            jdk 'jdk8'
        }
    stages {
        stage ('Initialize') {
                steps {
                    sh '''
                        echo "PATH = ${PATH}"
                        echo "M2_HOME = ${M2_HOME}"
                    '''
                }
            }
        stage('Build Application') { 
            steps {
                echo '=== Building LnF Application ==='
                sh 'mvn -B -DskipTests clean package' 
            }
        }

        stage('sonar-scanner') {
            steps {
               script {
                def SONARQUBE_HOSTNAME = 'sonarqube'
                def sonarqubeScannerHome = tool name: 'sonar', type: 'hudson.plugins.sonar.SonarRunnerInstallation'
                withCredentials([string(credentialsId: 'sonar', variable: 'sonarLogin')]) {
                sh "${sonarqubeScannerHome}/bin/sonar-scanner -e -Dsonar.host.url=http://${SONARQUBE_HOSTNAME}:9000 -Dsonar.login=${sonarLogin} -Dsonar.projectName=WebApp -Dsonar.projectVersion=${env.BUILD_NUMBER} -Dsonar.projectKey=GS -Dsonar.sources=src/main/ -Dsonar.tests=src/test/ -Dsonar.java.binaries=target/**/* -Dsonar.language=java"
               }
             }
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
        stage('Build Docker Image') {
            steps {
                echo '=== Building LnF Docker Image ==='
                script {
                    def DOCKERHUB_REPO = 'levernfulcrum/lnfapp'
                    docker.withTool('docker-latest') {
                            sh "printenv"
                            sh "pwd"
                            sh "ls -la"
                            def image1 = docker.build("lnfapp:${BUILD_NUMBER}", ".")
                            def image2 = docker.build("lnfapp:latest", ".")
                    }

                }
            }
        }
        stage('Push Docker Image') {
            steps {
                echo '=== Pushing LnF Docker Image ==='
                script {
                    def DOCKERHUB_REPO = 'levernfulcrum/lnfapp'
                    docker.withTool('docker-latest') {
                                withCredentials([usernamePassword(credentialsId: 'dockerhub',
                                                 usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                                    sh "docker login -u ${USERNAME} -p ${PASSWORD} https://index.docker.io/v1/"
                                    sh "docker tag lnfapp ${DOCKERHUB_REPO}:${BUILD_NUMBER}"
                                    sh "docker tag lnfapp ${DOCKERHUB_REPO}:latest"
                                    sh "docker push ${DOCKERHUB_REPO}:${BUILD_NUMBER}"
                                    sh "docker push ${DOCKERHUB_REPO}:latest"
                                }
                    }
                }
            }
        }
        stage('Remove local images') {
            steps {
                echo '=== Remove local images ==='
                script {
                    echo '=== Delete the local docker images ==='
                    docker.withTool('docker-latest') {
                        sh("docker rmi -f lnfapp:latest")
                        sh("docker rmi -f lnfapp:${BUILD_NUMBER}")
                    }

                }
            }
        }
    }
}
