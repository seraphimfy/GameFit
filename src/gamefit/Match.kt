data class Match(val kills: Int, val deaths: Int, val assists: Int, val matchWon: Boolean){
    val kda:Double
        get() = (kills+assists).toDouble()/ deaths.coerceAtLeast(1)
}