def buildAll = false
def affectedModules = [] as Set
def build_goal = "clean install -DskipTests"
def test_goal = "test"
pipeline {
    agent any
    options {
        disableConcurrentBuilds()
    }
    environment {
        CHANGE_TARGET = "main"
        SONAR_TOKEN = credentials('sonar-token')
    }
    tools {
        maven 'maven'
        jdk 'jdk-17'
    }
    stages {
        stage('Analyze Changes') {
            steps {
                script {
                    echo "Push Triggered on ${env.BRANCH_NAME}"
                    echo "Detect Changes committed"
                    def changes = bat(script: 'git diff --name-only HEAD~1 HEAD', returnStdout: true)
                        .trim().split('\n').toList()
                    def changedModules = [] as Set
                    for (change in changes) {
                        if (change == 'pom.xml' || change.startsWith("shared-lib/") || change == "services/pom.xml") {
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
        stage('Build') {
            steps {
                script {
                    if (buildAll) {
                        echo "Building All the Modules"
                        bat "mvn ${build_goal}"
                    } else if (env.CHANGED_MODULES) {
                        echo "Building Modules: ${env.CHANGED_MODULES}"
                        bat "mvn ${build_goal} -pl ${env.CHANGED_MODULES} -am"
                    } else {
                        echo 'No modules affected — skipping build'
                    }
                }
            }
        }
        stage('Test') {
            steps {
                script {
                    if (buildAll) {
                        echo "Running test on All the Modules"
                        bat "mvn ${test_goal}"
                    } else if (env.CHANGED_MODULES) {
                        echo "Running test on Modules: ${env.CHANGED_MODULES}"
                        bat "mvn ${test_goal} -pl ${env.CHANGED_MODULES} -am"
                    } else {
                        echo 'No modules affected — skipping test'
                    }
                }
            }
        }
        // sonar is always triggered for full build ( we cant be selective at the project level )
        stage('SonarQube Scan') {
            steps {
                script {
                    if (env.CHANGED_MODULES ||  buildAll){
                        bat "mvn sonar:sonar -Dsonar.token=${SONAR_TOKEN}"
                    }else {
                        echo 'No modules affected — skipping SonarQube Scan'
                    }
                }
            }
        }
        stage('Deploy - push to nexus') {
            steps {
                script {
                    if (buildAll) {
                        bat 'mvn deploy -DskipTests'
                    } else if (env.CHANGED_MODULES) {
                        echo "Deploying Modules: ${env.CHANGED_MODULES}"
                        bat "mvn deploy -DskipTests -pl ${env.CHANGED_MODULES}"
                    } else {
                        echo 'No modules affected — skipping deploy'
                    }
                }
            }
        }
    } 
}