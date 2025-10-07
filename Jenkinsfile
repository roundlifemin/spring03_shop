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
            credentialsId: 'github-username-password'
      }
    }

    stage('Build') {
      steps {
        sh './gradlew clean build --no-daemon'
      }
      post {
        success {
          archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
        }
      }
    }
  }
}