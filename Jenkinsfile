pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk 'JDK17'
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
                sshagent(['wsl-ssh-key']) {   // 🔑 Use your Jenkins SSH credential ID here
                    bat '''
                    scp -o StrictHostKeyChecking=no target\\bookstore-1.0-SNAPSHOT.jar srinivas@172.22.21.68:/home/srinivas/app/
                    ssh srinivas@172.22.21.68 "nohup java -jar /home/srinivas/app/bookstore-1.0-SNAPSHOT.jar > /home/srinivas/app/app.log 2>&1 &"
                    '''
                }
            }
        }
    }
}
