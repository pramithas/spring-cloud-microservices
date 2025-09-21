pipeline {
	agent any

    environment {
		// 1. Replace with your actual Docker Hub username (NOT email)
        DOCKER_REGISTRY = "pramithas"  
        REGISTRY_CREDENTIALS = 'jenkins-docker-hub-id'

        // 2. Define all your service names
        SERVICE_LIST_STRING = 'authserver,api-gateway,eureka-server,client-application'
    }

    stages {
		stage('Checkout & Clean Workspace') {
			steps {
				checkout([
                    $class: 'GitSCM',
                    branches: [[name: "main"]],
                    doGenerateSubmoduleConfigurations: false,
                    extensions: [
                        [$class: 'CleanBeforeCheckout'],
                        [$class: 'PruneStaleBranch'],
                        [$class: 'WipeWorkspace']
                    ],
                    userRemoteConfigs: [[
                        url: 'git@github.com:pramithas/spring-cloud-microservices.git',
                        credentialsId: 'GitHub-credentials-jenkins'
                    ]]
                ])
                sh 'git log -1 --oneline'
            }
        }

        stage('Build & Test All Services') {
			steps {
				sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build Docker Images') {
			steps {
				script {
					def services = env.SERVICE_LIST_STRING.split(',')
                    for (service in services) {
						dir(service) {
							def imageName = "${env.DOCKER_REGISTRY}/${service}:${env.BUILD_ID}"
                            echo "Building Docker image: ${imageName}"
                            docker.build(imageName)
                        }
                    }
                }
            }
        }

        stage('Push Docker Images') {
			steps {
				script {
					def services = env.SERVICE_LIST_STRING.split(',')
                    for (service in services) {
						dir(service) {
							def imageName = "${env.DOCKER_REGISTRY}/${service}:${env.BUILD_ID}"
                            echo "Pushing Docker image: ${imageName}"
                            docker.withRegistry('https://index.docker.io/v1/', "${env.REGISTRY_CREDENTIALS}") {
								docker.image(imageName).push()
                            }
                        }
                    }
                }
            }
        }

        stage('Deploy to Dev (Minikube)') {
			steps {
				script {
					// Ensure Minikube is running
            		sh "minikube status || minikube start"

					// Switch context to Minikube
					sh "kubectl config use-context minikube"

					def services = env.SERVICE_LIST_STRING.split(',')
					for (service in services) {
						echo "Deploying ${service}:${env.BUILD_ID} to Development (Minikube)"
						sh """
							kubectl set image deployment/${service} \
							${service}=${env.DOCKER_REGISTRY}/${service}:${env.BUILD_ID} \
							-n dev || kubectl apply -f k8s/dev/${service}.yaml
						"""
					}
       			}
    		}
	    }

		stage('Deploy to Prod (Minikube)') {
			input {
				message "Deploy all services to PRODUCTION on Minikube?"
				ok "Yes, deploy!"
			}
			steps {
				script {
					sh "kubectl config use-context minikube"

					def services = env.SERVICE_LIST_STRING.split(',')
					for (service in services) {
						echo "Deploying ${service}:${env.BUILD_ID} to PRODUCTION (Minikube)"
						sh """
							kubectl set image deployment/${service} \
							${service}=${env.DOCKER_REGISTRY}/${service}:${env.BUILD_ID} \
							-n prod || kubectl apply -f k8s/prod/${service}.yaml
						"""
					}
				}
			}
		}

    }

    post {
		always {
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
