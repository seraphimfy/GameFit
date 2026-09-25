class UserAccount(val username: String) {
    var penalty: Int = 0
        private set
    private var currentPlan: Map<Exercise, Int> = emptyMap()
    private val matches = mutableListOf<Match>()
    fun assignPlan(plan: Map<Exercise, Int>) {
        currentPlan = plan
    }
    var kdaTarget:Double = 2.0
    fun addPenalty(penaltyValue:Int){
        if(penaltyValue > 0) {
            penalty += penaltyValue
            println("Penalty: $penalty")
        }
    }
    fun completeWork() {
        if (currentPlan.isEmpty()) {
            println("There is no assigned exercise plan.")
            return
        }

        for ((exercise, repetitions) in currentPlan) {
            val completedPoints = repetitions * exercise.points

            exercise.donePoints += completedPoints
            penalty -= completedPoints

            println("${exercise.name}: $repetitions repetitions completed")
        }

        if (penalty < 0) {
            penalty = 0
        }

        currentPlan = emptyMap()

        println("Work completed.")
        println("Remaining penalty: $penalty")
    }
    fun addMatch(match: Match){
        matches.add(match)
    }
    fun showMatches()
    {
        for(match in matches)
        {
            println("Match: $match")
        }
    }
    fun changeTarget(target: Double) {
        if (target <= 0) {
            println("Target must be greater than 0")
            return
        }
        kdaTarget = target
        println("KDA target changed to $kdaTarget")
    }
}
