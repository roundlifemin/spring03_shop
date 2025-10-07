pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                git branch: 'master',
                url: 'https://github.com/roundlifemin/spring03_shop.git'
            }           
        }

        stage('Build') {
            steps {
                dir('backend') {
                    sh "./gradlew ${env.GRADLE_TASK}"
                }
            }
            
            post {
			 success {
				archiveArtifacts 'target/*.jar'
			 }
		   }
        }
}
