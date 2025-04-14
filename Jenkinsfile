pipeline {
    agent any

    tools{
        jdk 'jdk21'
        maven 'maven3'
    }

    stages {
        stage('Code Checkout') {
            steps {
                git branch: 'main', changelog: false, poll: false, url: 'https://github.com/sr-biker/usermanager'
            }
        }


        stage('Sonarqube Analysis') {
            steps {
                sh ''' mvn sonar:sonar \
                    -Dsonar.host.url=http://localhost:9000/ \
                    -Dsonar.login= '''
            }
        }

        stage('Clean, Test & Package'){
            steps{
                sh "mvn clean package spring-boot:build-image"
            }
        }

       stage("Docker Build & Push"){
            steps{
                script{
                    withDockerRegistry(credentialsId: 'docker-token', toolName: 'docker') {
                        docker push docker.io/library/usermanager:0.0.1-SNAPSHOT
                    }

                }
            }
        }
    }
}