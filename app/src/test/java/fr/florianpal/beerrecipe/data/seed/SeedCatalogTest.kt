package fr.florianpal.beerrecipe.data.seed

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Garde-fous sur les catalogues de référence, saisis à la main. */
class SeedCatalogTest {

    private fun assertUnique(label: String, names: List<String>) {
        val duplicates = names.groupBy { it.lowercase() }.filterValues { it.size > 1 }.keys
        assertEquals("$label en double : $duplicates", emptySet<String>(), duplicates)
    }

    @Test
    fun `les noms sont uniques dans chaque catalogue`() {
        assertUnique("Styles", SeedStyles.ALL.map { it.name })
        assertUnique("Malts", SeedMalts.ALL.map { it.name })
        assertUnique("Houblons", SeedHops.ALL.map { it.name })
        assertUnique("Levures", SeedYeasts.ALL.map { it.name })
    }

    @Test
    fun `les fourchettes de style sont cohérentes`() {
        SeedStyles.ALL.forEach {
            assertTrue("${it.name} : DI", it.ogMin <= it.ogMax)
            assertTrue("${it.name} : DF", it.fgMin <= it.fgMax)
            assertTrue("${it.name} : DF au-dessus de la DI", it.fgMin < it.ogMin && it.fgMax < it.ogMax)
            assertTrue("${it.name} : ABV", it.abvMin <= it.abvMax)
            assertTrue("${it.name} : IBU", it.ibuMin <= it.ibuMax)
            assertTrue("${it.name} : EBC", it.ebcMin <= it.ebcMax)
        }
    }

    @Test
    fun `la NEIPA se trouve par son nom usuel`() {
        assertTrue(SeedStyles.ALL.any { it.name.contains("NEIPA") })
    }

    @Test
    fun `les fiches d'ingrédients sont plausibles`() {
        SeedMalts.ALL.forEach {
            assertTrue("${it.name} : rendement", it.extractYieldPct in 50.0..100.0)
            assertTrue("${it.name} : part max", it.maxBatchPct in 1.0..100.0)
        }
        SeedHops.ALL.forEach {
            assertTrue("${it.name} : alpha", it.alphaAcidPct in 1.0..25.0)
        }
        SeedYeasts.ALL.forEach {
            assertTrue("${it.name} : température", it.tempMinC < it.tempMaxC)
            assertTrue("${it.name} : atténuation", it.attenuationMinPct <= it.attenuationMaxPct)
        }
    }
}
