class UserAccount(val username: String) {
    var penalty: Int = 0
        private set
    private val matches = mutableListOf<Match>()
    var kdaTarget:Double = 2.0
    fun addPenalty(penaltyValue:Int){
        if(penaltyValue > 0) {
            penalty += penaltyValue
            println("Penalty: $penalty")
        }
    }
    fun resetPenalty(){
        penalty = 0
        println("Penalty: $penalty, reset completed")
    }
    fun completeWork(repsDone: Int)
    {
        if(repsDone <= 0 || penalty == 0) return
        if(repsDone < penalty)
        {
            println("Too weak, son")
            penalty -= repsDone
        }
        else {
            println("Well done")
            resetPenalty()
        }
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
