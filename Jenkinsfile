import com.anaplan.buildtools.jenkins_pipelines.DefaultConfig
import com.anaplan.systemspecification.jenkinslibrary.SysSpecContainerTemplates

@Library(['Anaplan_Pipeline', 'SysSpecJenkinsLibrary'])

def BUILD_LABEL = "kazuki.${UUID.randomUUID().toString()}"

pipeline {
    agent {
        kubernetes {
            label BUILD_LABEL
            yaml pod([SysSpecContainerTemplates.sysspecContainer('eclipse-temurin:11-jdk-ubi10-minimal', '3Gi')])
        }
    }

    parameters {
        text(
            description: 'Version to publish',
            name: 'version'
        )
    }

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(daysToKeepStr: '60'))
        timeout(time: 30, unit: 'MINUTES')
    }

    stages {
        stage('Build & Test') {
            steps {
                script {
                    container('gradle') {
                        try {
                            sh "./gradlew check"
                        } finally {
                            junit '**/build/test-results/**/*.xml'
                        }
                    }
                }
            }
        }

        stage('Publish') {
            when {
                expression {
                    params.version?.trim()
                }
            }
            steps {
                container('gradle') {
                    withCredentials([usernamePassword(
                            credentialsId: 'AzukiSonatype',
                            usernameVariable: 'ORG_GRADLE_PROJECT_sonatypeUsername',
                            passwordVariable: 'ORG_GRADLE_PROJECT_sonatypePassword',
                    )]) {
                        // use `closeAndReleaseSonatypeStagingRepository` instead of `closeSonatypeStagingRepository`
                        // to automatically release the artifacts without requiring a manual step
                        sh "./gradlew -Pversion=${params.version} publishToSonatype closeSonatypeStagingRepository --info"
                    }
                }
            }
        }
    }

}
