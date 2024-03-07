pipeline {
    agent any

    tools {
        maven '3.9.6'
    }
    stages {
        stage('Checkout'){
            steps{
                checkout scmGit(branches: [[name: 'develop']], extensions: [], userRemoteConfigs: [[url: 'https://github.com/pauloherbt/mandacarubroker-fixed']])
            }
        }
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests=true'
            }
        }
        stage('Tests'){
            steps{
                sh 'mvn test'
            }
        }
        stage('Static Code Analysis'){
            steps{
                sh 'mvn checkstyle:checkstyle'
            }
        }
        stage('Build Docker image'){
            script{
                def dockerImg
                def dockerfilePath = "Dockerfile"
                try{
                    if(fileExists(dockerfilePath)){
                        dockerImg = sh(return Stdout: true, script:"docker build -t app:latest .")
                    } else{
                        echo "File not found ${dockerfilePath}"
                        currentBuild.result = 'FAILURE'
                    }
                }catch(Exception e){
                    echo "Failed to build Docker img: ${e.message}"
                    currentBuild.result = 'FAILURE'
                }
                if(dockerImg!=null){
                    echo 'Docker Image build with sucess'
                }
            }
        }
        stage('Deploy on Tests environment'){
            steps {
                script {
                    try{
                        sh "docker run -d -p 8080:8080 app:latest"
                    }catch(Exception e){
                        echo "Failed to init docker container: ${e.message}"
                        currentBuild.result = 'FAILURE'
                    }
                }
            }
        }
        stage('Deploy on Prod environment'){
            steps {
                script {
                    try{
                        sh "docker run -d -p 8085:8080 app:latest"
                    }catch(Exception e){
                        echo "Failed to init docker container: ${e.message}"
                        currentBuild.result = 'FAILURE'
                    }
                }
            }
        }
    }
}
