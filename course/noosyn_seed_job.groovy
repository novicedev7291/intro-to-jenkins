import javaposse.jobdsl.dsl.DslFactory

DslFactory factory = this

repos = [
        [name: "analytics-svc", url: "https://bitbucket.org/noosyn/analytics-svc.git", branch: "dev"]
]

repos.forEach({
    factory.job('noosyn-seed-job'){
        triggers {
            githubPush()
        }
        scm {
            github('novicedev7291/intro-to-jenkins')
        }
    }

   factory.pipelineJob(it.name + "-" + it.branch){
       triggers {
           githubPush()
       }
       logRotator{
           logRotator(null, 20)
       }
       definition {
           cps {
               script(readFileFromWorkspace("course/example_job.groovy"))
               sandbox()
           }
       }

   }
});
