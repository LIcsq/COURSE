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
            sh  'mvn install -DskipTests'
            }
        
        post {
            success{
                echo 'Archive artifacts'
                archiveArtifacts artifacts: '**/*.war'
            }
             }
        }
        stage('Unit TEST')
        {
            steps{
                sh 'mvm test'
            }
        }
        stage("Code Analysis"){
            steps{
                sh 'mvn checkstyle:checkstyle'
            }
         steps("Sonar Analysis")
            environment={
             scanner=tool 'sonar4.7'
            }
                steps {
                                withSonarQubeEnv('sonar') {
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
    }
}