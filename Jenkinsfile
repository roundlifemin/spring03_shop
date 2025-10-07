pipeline {
    agent {
        docker {
            image 'node:alpine'
        }
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'master',
                url: 'https://github.com/roundlifemin/spring03_shop.git'
            }
            post {
                success {
                    echo 'Successfully cloned repository'
                }
                failure {
                    echo 'Fail cloned repository'
                }
            }
        }

        stage('Test') {
            steps {
                echo '테스트 단계를 진행 중. 각 모듈을 검증합니다.'
            }
        }

        stage('Build') {
            steps {
                sh 'npm install'
                sh 'npm run build'
            }
        }

        stage('Docker Clear') {
            steps {
                script {
                    def dockerClear = sh(script: "docker ps -a -q --filter ancestor=jenkins:latest", returnStdout: true).trim()
                    if (dockerClear) {
                        sh 'docker stop jenkins'
                        sh 'docker rm jenkins'
                        sh 'docker rmi jenkins/latest'
                    } else {
                        echo 'No such container: jenkins'
                    }
                }
            }
            post {
                success {
                    echo 'Docker Clear success!'
                }
                failure {
                    echo 'Docker Clear fail!'
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t jenkins:latest .'
                sh 'docker run -d --name jenkins -p 8080:8080 -p 50000:50000 jenkins:latest'
            }
            post {
                success {
                    echo 'Build docker image success!'
                }
                failure {
                    echo 'Build docker image fail!'
                }
            }
        }

        stage('Deploy') {
            steps {
                sh 'curl -X POST http://webhook-endpoint/deploy'
            }
            post {
                success {
                    echo 'Deploy success!'
                }
                failure {
                    echo 'Deploy fail!'
                }
            }
        }
    }
}
