package app.morphe.patches.macrofactor.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.macrofactor.misc.signature.spoofSignaturePatch
import app.morphe.patches.macrofactor.shared.Constants
import app.morphe.patches.shared.misc.extension.activityOnCreateExtensionHook
import app.morphe.patches.shared.misc.extension.sharedExtensionPatch

internal val extensionPatch = sharedExtensionPatch(
    "macrofactor",
    activityOnCreateExtensionHook("/FlutterFragmentActivity;")
)

val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Enables app features locked behind the subscription paywall."
) {
    compatibleWith(*Constants.COMPATIBILITY)

    dependsOn(extensionPatch, spoofSignaturePatch)

    execute {
        BuildCustomerInfoFingerprint.method.addInstructions(0, """
            invoke-static { p1 }, Lapp/morphe/extension/macrofactor/premium/EnablePremiumPatch;->updateCustomerInfo(Lorg/json/JSONObject;)V
        """.trimIndent())
    }
}