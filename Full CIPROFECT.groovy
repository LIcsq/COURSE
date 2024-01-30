def COLOR = [
'SUCCESS': 'good',
'FAILURE': 'danger',
]
pipeline {
    agent any
    tools {
        maven "MAVEN3"
        jdk "OracleJDK11"
    }
    stages {
        stage ('Fetch code'){
         steps{
            git branch: 'main',url: 'https://github.com/devopshydclub/vprofile-project.git'
         }
        }
        stage('Build'){
            steps{
            sh 'mvn install -DskipTests'
            }
        post {
            success{
                echo "Archive artifacts"
                archiveArtifacts artifacts: '**/*.war'
            }
             }
        }
        stage('Unit TEST')
        {
            steps{
                sh 'mvn test'
            }
        }
        stage("Code Analysis"){
            steps{
                sh 'mvn checkstyle:checkstyle'
            }
        }
         stage("Sonar Analysis"){
            environment {
             scanner=tool 'sonar4.7'
            }
                steps {
               withSonarQubeEnv('Sonar') {
                   sh '''${scanner}/bin/sonar-scanner -Dsonar.projectKey=vprofile \
                   -Dsonar.projectName=vprofile \
                   -Dsonar.projectVersion=1.0 \
                   -Dsonar.sources=src/ \
                   -Dsonar.java.binaries=target/test-classes/com/visualpathit/account/controllerTest/ \
                   -Dsonar.junit.reportsPath=target/surefire-reports/ \
                   -Dsonar.jacoco.reportsPath=target/jacoco.exec \
                   -Dsonar.java.checkstyle.reportPaths=target/checkstyle-result.xml'''
              }
            }
         }
        stage("Quality Gate") {
            steps {
                timeout(time: 1, unit: 'HOURS') {
                    waitForQualityGate abortPipeline: true
                  }
         }
      }
      stage("Delivery"){
        steps{
      nexusArtifactUploader(
    nexusVersion: 'nexus3',
    protocol: 'http',
    nexusUrl: '172.31.88.253:8081',
    groupId: 'QA',
    version: "${env.BUILD_ID}-${env.BUILD_TIMESTAMP}",
    repository: 'vprofile-repo',
    credentialsId: 'Nexus',
    artifacts: [
        [artifactId: 'vprofile',
         classifier: '',
         file: "target/vprofile-v2.war",
         type: 'war']
    ]
)
             }
      }
}
         post{
            always{
                slackSend channel: '#jenkins',
                color: COLOR[currentBuild.currentResult],
              message: "*${currentBuild.currentResult}:* Job ${env.JOB_NAME} build ${env.BUILD_NUMBER} \n More info at: ${env.BUILD_URL}"          
                }
         }

}