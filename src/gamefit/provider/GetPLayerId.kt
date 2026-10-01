package gamefit.provider

fun getPlayerId(): Int {
    println("SteamId32 or SteamId64? (enter 1/2)")
    val input = readln()

    println("Enter your steamID (SteamID32 for dota):")
    val steamID = readln().toLongOrNull() ?: return 0
    if (steamID < 0) {println("wrong Input"); return 0}
    return when (input) {
        "1" -> steamID.toInt()
        "2" -> {

            val steamId64Base = 76561197960265728L
            (steamID - steamId64Base).toInt()
        }
        else -> 0
    }
}
fun getPlayerLim():Int {
    println("Enter limit of recent matches (up to 20)")
    return readln().toIntOrNull() ?: 1
}