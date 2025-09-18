// Jenkinsfile (located at the root of your monorepo)
pipeline {
    agent any // Uses any available agent. For better performance, use a label with Docker installed.

    environment {
        // 1. Configure your container registry details
        DOCKER_REGISTRY = "dhakalpramithas@gmail.com"
        REGISTRY_CREDENTIALS = 'jenkins-docker-hub-id' // ID of credentials stored in Jenkins

        // 2. Define all your service names in one place for easy management
        SERVICE_LIST_STRING = 'authserver,api-gateway,eureka-server,client-application'
    }

    stages {
        // STAGE 1: Ensures a pristine, up-to-date checkout of the remote repository.
        stage('Checkout & Clean Workspace') {
            steps {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: "main"]], // Builds the triggering branch, defaults to 'main'
                    doGenerateSubmoduleConfigurations: false,
                    extensions: [
                        [$class: 'CleanBeforeCheckout'], // Discards all local changes
                        [$class: 'PruneStaleBranch'],    // Removes outdated remote references
                        [$class: 'WipeWorkspace']        // Deletes the entire workspace before checkout (most thorough)
                    ],
                    userRemoteConfigs: [[
                        url: 'git@github.com:pramithas/MicroservicesDemoApp.git',
                        credentialsId: 'GitHub-credentials-jenkins' // SSH or HTTPS token credential ID
                    ]]
                ])
                // Print the latest commit to confirm what we're building
                sh 'git log -1 --oneline'
            }
        }

        // STAGE 2: Build and Test all services in parallel for maximum speed.
        stage('Build & Test All Services') {
			steps {
				// Build the entire multi-module project from root
       			 sh 'mvn clean package'
    		}
        }

        // STAGE 3: Build Docker images for all successfully built services.
        stage('Build Docker Images') {
            steps {
                script {
					def services = env.SERVICE_LIST_STRING.split(',')
                    for (service in services) {
                        dir(service) {
                            // Creates a Docker image tagged with the build number
                            docker.build("${env.DOCKER_REGISTRY}/${service}:${env.BUILD_ID}")
                        }
                    }
                }
            }
        }

        // STAGE 4: Push the images to the remote registry.
        stage('Push Docker Images') {
            steps {
                script {
					def services = env.SERVICE_LIST_STRING.split(',')
                    for (service in services) {
                        dir(service) {
                            docker.withRegistry('', "${env.REGISTRY_CREDENTIALS}") {
                                docker.image("${env.DOCKER_REGISTRY}/${service}:${env.BUILD_ID}").push()
                            }
                        }
                    }
                }
            }
        }

        // STAGE 5: Deploy to a development environment.
        stage('Deploy to Dev') {
            steps {
                script {
					def services = env.SERVICE_LIST_STRING.split(',')
                    for (service in services) {
                        dir(service) {
                            // Example for Kubernetes. Replace with your deployment commands.
                            // sh "kubectl set image deployment/${service} ${service}=${env.DOCKER_REGISTRY}/${service}:${env.BUILD_ID} -n dev"
                            echo "Deploying ${service}:${env.BUILD_ID} to Development"
                        }
                    }
                }
            }
        }

        // STAGE 6: Manual approval gate for production deployment.
        stage('Deploy to Prod') {
            input {
                message "Deploy all services to PRODUCTION?"
                ok "Yes, deploy!"
            }
            steps {
                script {
					def services = env.SERVICE_LIST_STRING.split(',')
                    for (service in services) {
                        dir(service) {
                            // sh "kubectl set image deployment/${service} ${service}=${env.DOCKER_REGISTRY}/${service}:${env.BUILD_ID} -n prod"
                            echo "Deploying ${service}:${env.BUILD_ID} to PRODUCTION"
                        }
                    }
                }
            }
        }
    }

    post {
		always {
			// Clean up workspace
			cleanWs()
		}
		failure {
				echo "❌ Build FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"
			echo "Check build details: ${env.BUILD_URL}"
		}
		success {
				echo "✅ Build SUCCEEDED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"
			echo "Build URL: ${env.BUILD_URL}"
		}
	}

}

// DEFINITION OF THE buildService FUNCTION
// This function contains the logic for building a single service
//def buildService(String serviceName) {
//    echo "Building service: ${serviceName}"
//
//    // Identify the build tool and execute the appropriate command
//    if (fileExists('pom.xml')) {
//        sh 'mvn clean package' // Runs tests as part of the package goal
//    } else if (fileExists('build.gradle')) {
//        sh './gradlew build'    // Runs tests as part of the build task
//    } else if (fileExists('package.json')) {
//        sh 'npm ci && npm run build' // 'npm ci' for clean, reliable installs
//        if (fileExists('package-lock.json')) {
//            sh 'npm ci' // Use for projects with package-lock.json
//        } else {
//            sh 'npm install' // Fallback
//        }
//    } else {
//        error "No recognized build tool (pom.xml, build.gradle, package.json) found in ${serviceName}."
//    }
//}