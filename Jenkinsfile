#!/usr/bin/env groovy

pipeline {
    agent any

     tools {
                jdk 'openjdk11'
            }

    environment {
        APPLICATION_NAME = 'syfosmoppgave'
        ZONE = 'fss'
        DOCKER_SLUG = 'syfo'
    }

    stages {
        stage('initialize') {
            steps {
                init action: 'default'
                script {
                    sh(script: './gradlew clean')
                    def applicationVersionGradle = sh(script: './gradlew -q printVersion', returnStdout: true).trim()
                    env.APPLICATION_VERSION = "${applicationVersionGradle}-${env.COMMIT_HASH_SHORT}"
                    if (applicationVersionGradle.endsWith('-SNAPSHOT')) {
                        env.APPLICATION_VERSION = "${applicationVersionGradle}.${env.BUILD_ID}-${env.COMMIT_HASH_SHORT}"
                    } else {
                        env.DEPLOY_TO = 'production'
                    }
                    init action: 'updateStatus', applicationName: env.APPLICATION_NAME, applicationVersion: env.APPLICATION_VERSION
                }
            }
        }
        stage('build') {
            steps {
                sh './gradlew build -x test'
            }
        }
        stage('run tests (unit & intergration)') {
            steps {
                sh './gradlew test'
            }
        }
        stage('create uber jar') {
            steps {
                sh './gradlew shadowJar'
                slackStatus status: 'passed'
            }
        }
        stage('Create kafka topics') {
            steps {
                sh 'echo TODO'
                // TODO
            }
        }
        stage('Create docker image') {
            steps {
                dockerUtils action: 'createPushImage'
            }
        }
        stage('deploy to preprod') {
            steps {
                deployApp action: 'kubectlDeploy', cluster: 'preprod-fss', placeholderFile: "preprod.env"
            }
        }
        stage('deploy to prod') {
            when { environment name: 'DEPLOY_TO', value: 'production' }
            steps {
                deployApp action: 'kubectlDeploy', cluster: 'prod-fss', placeholderFile: "prod.env"
            }
        }
    }
    post {
        always {
            postProcess action: 'always'
            junit '**/build/test-results/test/*.xml'
            archiveArtifacts artifacts: 'naiserator-deployment-yamls/*'
            archiveArtifacts artifacts: '**/build/libs/*', allowEmptyArchive: true
        }
        success {
            postProcess action: 'success'
        }
        failure {
            postProcess action: 'failure'
        }
    }
}
