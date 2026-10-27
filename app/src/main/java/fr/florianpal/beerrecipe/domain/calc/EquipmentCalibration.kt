package fr.florianpal.beerrecipe.domain.calc

import fr.florianpal.beerrecipe.data.entity.Equipment

/**
 * Étalonnage d'une installation sur un brassin réel : à partir de l'eau mise et du moût
 * obtenu, on retrouve les pertes qui expliquent l'écart, au lieu de les deviner.
 *
 * La rétraction et les pertes en cuve sont gardées telles quelles (elles se mesurent mal
 * à la maison) ; on ajuste l'évaporation et, si besoin, ce que retient le grain.
 */
object EquipmentCalibration {

    data class Measurement(
        /** Toute l'eau versée : empâtage et rinçage. */
        val totalWaterL: Double,
        val grainKg: Double,
        val boilTimeMin: Int,
        /** Volume mesuré en cuve juste avant l'ébullition, s'il a été relevé. */
        val preBoilVolumeL: Double?,
        /** Moût obtenu dans le fermenteur. */
        val fermenterVolumeL: Double,
    )

    fun calibrate(eq: Equipment, m: Measurement): Equipment {
        if (m.totalWaterL <= 0 || m.fermenterVolumeL <= 0) return eq
        val hours = m.boilTimeMin / 60.0
        val keep = 1.0 - eq.coolingShrinkagePct / 100.0
        // Volume chaud en fin d'ébullition qui donne le moût mesuré, une fois refroidi et soutiré.
        val postBoilHot = (m.fermenterVolumeL + eq.trubLossL) / keep

        var absorption = eq.grainAbsorptionLPerKg
        var deadspace = eq.mashTunDeadspaceL

        val preBoil = if (m.preBoilVolumeL != null) {
            // Le volume avant ébullition sépare ce que retient le grain de ce qui s'évapore.
            val retained = (m.totalWaterL - m.preBoilVolumeL).coerceAtLeast(0.0)
            deadspace = deadspace.coerceAtMost(retained)
            absorption = if (m.grainKg > 0) (retained - deadspace) / m.grainKg else absorption
            m.preBoilVolumeL
        } else {
            val estimated = m.totalWaterL - m.grainKg * absorption - deadspace
            if (estimated >= postBoilHot) {
                estimated
            } else {
                // Même sans évaporation, le grain retiendrait trop : c'est l'absorption qui est surestimée.
                deadspace = deadspace.coerceAtMost((m.totalWaterL - postBoilHot).coerceAtLeast(0.0))
                if (m.grainKg > 0) {
                    absorption = ((m.totalWaterL - deadspace - postBoilHot) / m.grainKg).coerceAtLeast(0.0)
                }
                m.totalWaterL - m.grainKg * absorption - deadspace
            }
        }

        val evaporation = if (hours > 0) ((preBoil - postBoilHot) / hours).coerceAtLeast(0.0)
        else eq.boilOffRateLPerHour

        return eq.copy(
            boilOffRateLPerHour = evaporation,
            grainAbsorptionLPerKg = absorption,
            mashTunDeadspaceL = deadspace,
        )
    }
}
