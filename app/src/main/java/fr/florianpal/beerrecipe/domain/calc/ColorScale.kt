package fr.florianpal.beerrecipe.domain.calc

/**
 * Table de référence SRM → couleur. Les valeurs sont celles employées par les guides
 * de dégustation ; l'interpolation entre deux degrés évite les sauts de teinte.
 */
object ColorScale {
    private val SRM_COLORS = longArrayOf(
        0xFFF8F4C8, // 0 — eau de moût très pâle
        0xFFFFE699, 0xFFFFD878, 0xFFFFCA5A, 0xFFFFBF42, 0xFFFBB123,
        0xFFF8A733, 0xFFF39C00, 0xFFEA8F00, 0xFFE58500, 0xFFDE7C00,
        0xFFD77200, 0xFFCF6900, 0xFFCB6200, 0xFFC35900, 0xFFBB5100,
        0xFFB54C00, 0xFFB04500, 0xFFA63E00, 0xFFA13700, 0xFF9B3200,
        0xFF952D00, 0xFF8E2900, 0xFF882300, 0xFF821E00, 0xFF7B1A00,
        0xFF771900, 0xFF701400, 0xFF6A0E00, 0xFF660D00, 0xFF5E0B00,
        0xFF5A0A02, 0xFF600903, 0xFF520907, 0xFF4C0505, 0xFF470606,
        0xFF440607, 0xFF3F0708, 0xFF3B0607, 0xFF3A070B, 0xFF36080A,
    )

    /** Couleur ARGB correspondant à une valeur SRM, interpolée entre les degrés entiers. */
    fun srmToArgb(srm: Double): Long {
        val clamped = srm.coerceIn(0.0, (SRM_COLORS.size - 1).toDouble())
        val low = clamped.toInt()
        val high = (low + 1).coerceAtMost(SRM_COLORS.size - 1)
        val t = clamped - low
        return lerpArgb(SRM_COLORS[low], SRM_COLORS[high], t)
    }

    fun ebcToArgb(ebc: Double): Long = srmToArgb(ebc / 1.97)

    private fun lerpArgb(a: Long, b: Long, t: Double): Long {
        fun channel(shift: Int): Long {
            val ca = (a shr shift) and 0xFF
            val cb = (b shr shift) and 0xFF
            return (ca + (cb - ca) * t).toLong().coerceIn(0, 255)
        }
        return (0xFFL shl 24) or (channel(16) shl 16) or (channel(8) shl 8) or channel(0)
    }

    /** Vrai si la couleur est assez sombre pour exiger un texte clair par-dessus. */
    fun isDark(argb: Long): Boolean {
        val r = ((argb shr 16) and 0xFF).toDouble()
        val g = ((argb shr 8) and 0xFF).toDouble()
        val b = (argb and 0xFF).toDouble()
        return (0.299 * r + 0.587 * g + 0.114 * b) < 140.0
    }
}
