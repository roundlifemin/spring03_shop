pipeline {
   agent any

    environment {
      // 안전한 기본값: 빌드가 목적이면 'build'로 설정
      GRADLE_TASK = 'build'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'master',
                url: 'https://github.com/roundlifemin/spring03_shop.git'
            }  
            
            post{
				success {
					echo 'Successfully Cloned Repository'					
				}
				
				failure {
					echo 'Fail Cloned Repository'
				}
			}         
        }

        stage('Build') {
            steps {                
                    sh "./gradlew ${env.GRADLE_TASK}"
                
            }
            
            post {
			 success {
				archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
				echo 'Success Build'
			 }
			 failure {
				echo 'Fail Build'
			 }
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
