pipeline {
    agent any

    tools {
        maven 'Maven3'   // Jenkins Maven installation name
        jdk 'JDK17'      // Jenkins JDK installation name
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/srinivasaryan/bookStore.git'
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean compile'
            }
        }

        stage('Test') {
            steps {
                bat 'mvn test'
            }
        }

        stage('Package') {
            steps {
                bat 'mvn package'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Deploy to VM') {
            steps {
                withCredentials([sshUserPrivateKey(credentialsId: 'wsl-ssh-key', keyFileVariable: 'SSH_KEY')]) {
                    bat """
                    scp -o StrictHostKeyChecking=no -i %SSH_KEY% target\\bookstore-1.0-SNAPSHOT.jar srinivas@172.22.21.68:/home/srinivas/app/
                    ssh -i %SSH_KEY% srinivas@172.22.21.68 "nohup java -jar /home/srinivas/app/bookstore-1.0-SNAPSHOT.jar > /home/srinivas/app/app.log 2>&1 &"
                    """
                }
            }
        }
    }
}
