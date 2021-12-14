pipeline{
  agent any
  
  stages{
    
    stage("GIT checkout"){
      steps{
        git credentialsId: 'Git', url: 'https://github.com/Shivani-38/android-mobile-engage-sample-app.git'
      }
    }
  }
