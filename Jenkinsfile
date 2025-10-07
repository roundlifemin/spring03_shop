pipeline {
  agent any

  environment {
    // (선택) 추가 환경변수
    APP_NAME = 'spring03_shop'
  }

  stages {
    stage('Checkout') {
      steps {
        // 간단한 git 스텝: credentialsId에 Jenkins에 등록한 ID 사용
        git branch: 'master',
            url: 'https://github.com/roundlifemin/spring03_shop.git',
            credentialsId: 'github-credentials'
      }
      
      post {
                success {
                    echo 'Success Github에 배포 성공!'
                }
                failure {
                    echo 'Fail Github에 배포 실패!'
                }
            }
    }
    
     stage('Prepare') {
      steps {
        // gradlew에 실행권한 부여
        sh 'chmod +x ./gradlew'
        // (옵션) 현재 파일 권한 확인
        sh 'ls -l ./gradlew || true'
      }
    }
    

    stage('Build (skip tests)') {
      steps {
        sh './gradlew clean build -x test --no-daemon'
        archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
      }
      post {
        success {          
          echo 'Success Build!'
        }
        failure {
            echo 'Fail Build!'
        }
      }
    }
    
     stage('Initialize Buildx Builder') {
            steps {              
                
                // 1. 빌더 생성 (오래된 Buildx 버전 호환)
                sh 'docker buildx create --name mybuilder' 
                
                // 2. 생성된 빌더 사용
                sh 'docker buildx use mybuilder' 
            }
        }
    
    stage('Build and Push Docker Image') {
            steps {                
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                         sh """
                            echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                            docker buildx build --platform ${env.DOCKER_BUILD_PLATFORMS} -t ${env.DOCKER_IMAGE_NAME} --push .
                        """
                    }
                }            
        }
        
        
    
  }
}