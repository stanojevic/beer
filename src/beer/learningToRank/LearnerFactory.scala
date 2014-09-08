package beer.learningToRank

import beer.Configuration

object LearnerFactory {

  def createLearner(configuration:Configuration) : Learner = {
    val learnerClass  = configuration.modelConfig("brewer").asInstanceOf[Map[String, String]]("class")
    val learner = 
        Class.forName(learnerClass).getConstructors()(0).newInstance(configuration).asInstanceOf[Learner]
    learner
  }

}