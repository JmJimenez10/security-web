pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'security-spring-boot-app'
        DOCKER_TAG = 'latest'
    }

    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/JmJimenez10/security-web.git'
            }
        }

        stage('Build Backend') {
            steps {
                dir('security-web/security-spring-boot') {
                    sh 'mvn clean package'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}", 'security-web/security-spring-boot')
                }
            }
        }

        stage('Run Docker Container') {
            steps {
                script {
                    // Detener contenedores viejos si ya están corriendo
                    sh 'docker ps -q --filter "name=security-spring-boot" | xargs -r docker stop'

                    // Ejecutar el nuevo contenedor
                    sh 'docker run -d -p 8080:8080 --name security-spring-boot security-spring-boot-app:latest'
                }
            }
        }
    }

    post {
        always {
            cleanWs()  // Limpia el workspace después de la ejecución
        }
    }
}
