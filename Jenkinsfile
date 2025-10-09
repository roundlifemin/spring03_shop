pipeline {
  agent any

  environment {
    // (선택) 추가 환경변수
     APP_NAME = 'spring03_shop'
     DOCKER_BUILD_PLATFORMS = 'linux/amd64'
     DOCKER_IMAGE_NAME = 'roundlifemin/spring03_shop:latest'
     DEPLOY_SERVER = '13.125.126.120'
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
                    // 메시지를 'Checkout 성공'으로 수정하여 단계 목적을 명확히 함
                    echo 'Success: 소스 코드 Checkout 성공!' 
                }
                failure {
                    echo 'Fail: 소스 코드 Checkout 실패! (Git 설정 또는 권한 확인)'
                }
            }
    }
    
     stage('Prepare') {
      steps {
        // [유지] gradlew에 실행권한 부여
        sh 'chmod +x ./gradlew'
        // (옵션) 현재 파일 권한 확인
        sh 'ls -l ./gradlew || true'
      }
    }
    

    stage('Build (skip tests)') {
      steps {
        // [유지] 테스트를 건너뛰고 빌드
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
    
     
    
    stage('Build and Push Docker Image') {
        steps {
          withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
           sh """
             echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
             # --platform 옵션과 태그를 사용하여 Docker 이미지 빌드 및 푸시
             docker buildx build --platform ${env.DOCKER_BUILD_PLATFORMS} -t ${env.DOCKER_IMAGE_NAME} --push .
            """
              }
          }
        }
        
      stage('Deploy to Production') {
      steps {
        // SSH 프라이빗 키(deploy-backend-server-credentials)를 사용하여 원격 서버에 접속
        sshagent(['deploy-backend-server-credentials']) {
          sh """
            ssh -o StrictHostKeyChecking=no ubuntu@\${DEPLOY_SERVER} '
              # 원격 서버에서 Docker 이미지 풀, 이전 컨테이너 정리 및 새 컨테이너 실행
              docker pull \${DOCKER_IMAGE_NAME} &&
              docker stop \${APP_NAME} || true &&
              docker rm \${APP_NAME} || true &&
              docker run -d --name \${APP_NAME} -p 8090:8090 \${DOCKER_IMAGE_NAME}
            '
          """
        }
      }
    }
            
  }
}