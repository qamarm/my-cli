pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x mvnw && ./mvnw -B clean compile'
            }
        }

        stage('Test') {
            steps {
                sh './mvnw -B test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                sh './mvnw -B package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/my-cli.jar', fingerprint: true
                }
            }
        }
    }
}
