def buildAll = false
def affectedModules = [] as Set
def goal = "install"

pipeline {
    agent any

    options {
        disableConcurrentBuilds()
    }
    environment {
        CHANGE_TARGET = "main"
    }
    tools {
        maven 'maven'
        jdk 'jdk-17'
    }
    stages {
        stage('Analyze Changes') {
            steps {
                script {
                    if (env.CHANGE_ID) {
                        echo "Pull Request Trigger"
                        bat "git fetch  ${CHANGE_TARGET}"
                        def changes = bat(script: 'git diff --name-only ${CHANGE_TARGET}...HEAD', returnStdout: true)
                                .trim().split('\n').toList()
                        def changedModules = [] as Set
                        for (change in changes) {
                            if (change == 'pom.xml' || change.startsWith("shared-lib/")) {
                                buildAll = true
                                break
                            }
                            def parts = change.split('/')
                            if (parts.size() >= 2) {
                                changedModules << "${parts[0]}/${parts[1]}"
                            }
                        }  
                        env.CHANGED_MODULES = changedModules.join(',') 
                    } else {
                        echo "Push Trigger"
                        def changes = bat(script: 'git diff --name-only HEAD~1 HEAD', returnStdout: true)
                                .trim().split('\n').toList()
                        def changedModules = [] as Set
                        for (change in changes) {
                            if (change == 'pom.xml' || change.startsWith("shared-lib/")) {
                                buildAll = true
                                break
                            }
                            def parts = change.split('/')
                            if (parts.size() >= 2) {
                                changedModules << "${parts[0]}/${parts[1]}"
                            }
                        }  
                        env.CHANGED_MODULES = changedModules.join(',') 
                    }
                }
            }
        }
        stage('Build ') {
            steps {
                script {
                    if (buildAll) {
                        bat 'mvn clean install'
                    } else if (env.CHANGED_MODULES) {
                        bat "mvn clean install -pl ${env.CHANGED_MODULES} -am "
                    } else {
                        echo 'No modules affected — skipping build'
                    }
                }
            }
        }
        // stage('Test') {
        //     steps {
        //         script {
        //             if (buildAll) {
        //                 bat 'mvn test'
        //             } else if (env.CHANGED_MODULES) {
        //                 bat "mvn test -pl ${env.CHANGED_MODULES} -am -amd"
        //             } else {
        //                 echo 'No modules affected — skipping test'
        //             }
        //         }
        //     }
        // };
    }

}