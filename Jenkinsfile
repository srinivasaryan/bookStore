pipeline {
    agent any

    tools {
        maven 'Maven3'   // Name of Maven installation in Jenkins
        jdk 'JDK17'      // Name of JDK installation in Jenkins
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/srinivasaryan/bookStore.git'
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean install -DskipTests'
            }
        }

        stage('Test') {
            steps {
                bat 'mvn test'
            }
        }

        stage('Package') {
            steps {
                bat 'mvn package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Deploy to VM') {
            steps {
                sh '''
                scp -o StrictHostKeyChecking=no target/*.jar user@<linux-vm-ip>:/home/user/app/
                ssh user@<linux-vm-ip> "nohup java -jar /home/user/app/*.jar > app.log 2>&1 &"
                '''
            }
        }
    }
}
