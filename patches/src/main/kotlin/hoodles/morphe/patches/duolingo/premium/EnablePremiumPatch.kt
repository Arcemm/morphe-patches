package hoodles.morphe.patches.duolingo.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
import app.morphe.util.getReference
import hoodles.morphe.patches.duolingo.shared.Constants
import hoodles.morphe.patches.duolingo.shared.Utils.fieldFromToString
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import hoodles.morphe.util.constructor
import hoodles.morphe.util.fieldByName
import app.morphe.util.indexOfFirstInstructionOrThrow
import hoodles.morphe.util.removeFlag

enum class PremiumVariant {
    SUPER,
    MAX
}

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Enables app features locked behind the subscription paywall."
) {
    compatibleWith(Constants.COMPATIBILITY)

    val premiumVariant by stringOption(
        key = "premiumVariant",
        default = PremiumVariant.SUPER.name,
        values = mapOf(
            "Duolingo Super" to PremiumVariant.SUPER.name,
            "Duolingo MAX" to PremiumVariant.MAX.name
        ),
        title = "Type",
        description = "Choose which type of premium Duolingo subscription to enable.",
        required = true,
    )

    execute {
        val optionIsMax = enumValueOf<PremiumVariant>(premiumVariant!!) == PremiumVariant.MAX
        val subscriberLevel = if (optionIsMax) "GOLD" else "PREMIUM"

        val hasPlusField = UserFingerprint.classDef.fieldFromToString("hasPlus")
        val subscriberLevelField = UserFingerprint.classDef.fieldFromToString("subscriberLevel")

        // isPaidField (O0)
        val isPaidField = UserIsPaidFieldUsageFingerprint.method.let {
            val isPaidIndex = it.indexOfFirstInstructionOrThrow(Opcode.IGET_BOOLEAN)
            it.getInstruction<ReferenceInstruction>(isPaidIndex).getReference<FieldReference>()!!
        }

        // Retire le flag FINAL sur les champs utilisés
        val fields = mutableSetOf(hasPlusField, subscriberLevelField, isPaidField)
        fields.forEach { UserFingerprint.classDef.fieldByName(it.name).removeFlag(AccessFlags.FINAL) }

        // Injection dans le constructeur de Lhy/c1
        LoggedInStateFingerprint.classDef.constructor().apply {
            val userType = UserFingerprint.classDef.type
            val patchIndex = this.instructions.count() - 1

            val instrSb = StringBuilder()
            instrSb.appendLine(
                """
                const/4 v0, 0x1
                iput-boolean v0, p1, $userType->${isPaidField.name}:Z
                iput-boolean v0, p1, $userType->${hasPlusField.name}:Z
                """.trimIndent()
            )

            // Injection du niveau d'abonnement (PREMIUM ou GOLD)
            instrSb.appendLine(
                """
                sget-object v0, ${subscriberLevelField.type}->$subscriberLevel:${subscriberLevelField.type}
                iput-object v0, p1, $userType->${subscriberLevelField.name}:${subscriberLevelField.type}
                """.trimIndent()
            )

            this.addInstructions(patchIndex, instrSb.toString())
        }
    }
}
