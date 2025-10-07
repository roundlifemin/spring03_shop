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
				archiveArtifacts 'build/libs/*.jar'
				echo 'Success Build'
			 }
			 failure {
				echo 'Fail Build'
			 }
		   }
        }
}
