pipeline {
    agent any
    
    tools {
        maven 'Maven'
    }
    
    environment {
        MAILTRAP_HOST = credentials('MAILTRAP_HOST')
        MAILTRAP_PORT = credentials('MAILTRAP_PORT')
        MAILTRAP_USERNAME = credentials('MAILTRAP_USERNAME')
        MAILTRAP_PASSWORD = credentials('MAILTRAP_PASSWORD')
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/sriharieetyala/FlightApp_01.git'
            }
        }

        stage('Build & Test') {
            steps {
                dir('Backend') {
                    bat 'mvn package'
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                dir('Backend') {
                    bat 'docker-compose build'
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed.'
        }
    }
}
