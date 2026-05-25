package hoodles.morphe.patches.duolingo.premium

import app.morphe.patcher.Fingerprint

// Cible la nouvelle classe qui remplace LoggedInState (hy/c1)
object LoggedInStateFingerprint : Fingerprint(
    strings = listOf("Lhy/c1")
)

// Matches User.toString()
object UserFingerprint : Fingerprint(
    strings = listOf("User(adsConfig=", ", id=", ", betaStatus=")
)

// Some method that has to do with subscription trials
object UserIsPaidFieldUsageFingerprint : Fingerprint(
    parameters = listOf("L", "L"),
                                                     returnType = "Z",
                                                     strings = listOf("user", "onboardingState")
)

// Some method that has to do with checking if MAX is enabled
// (on le garde même s’il n’est plus utilisé pour hasGold, au cas où on en ait besoin plus tard)
object UserHasGoldFieldUsageFingerprint : Fingerprint(
    parameters = listOf("L", "L", "L"),
                                                      returnType = "L",
                                                      strings = listOf(
                                                          "maxFeaturesEnabled",
                                                          "isEmaEnabledInCourse",
                                                          "user"
                                                      )
)
