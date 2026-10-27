package fr.florianpal.beerrecipe.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import fr.florianpal.beerrecipe.domain.model.Labelled
import java.util.Locale

/** Convertit une saisie en nombre en acceptant aussi bien la virgule que le point décimal. */
fun String.toDoubleOrNullFlexible(): Double? =
    trim().replace(',', '.').takeIf { it.isNotEmpty() }?.toDoubleOrNull()

fun formatNumber(value: Double, decimals: Int): String =
    if (decimals == 0) value.toLong().toString()
    else String.format(Locale.FRANCE, "%.${decimals}f", value).trimEnd('0').trimEnd(',', '.')

/**
 * Champ numérique tolérant : la saisie reste libre tant qu'elle n'est pas analysable,
 * ce qui permet de taper « 12, » puis « 12,5 » sans que le champ ne se réécrive tout seul.
 */
@Composable
fun NumberField(
    label: String,
    value: Double,
    onValueChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
    suffix: String? = null,
    decimals: Int = 1,
    enabled: Boolean = true,
) {
    var text by remember { mutableStateOf(formatNumber(value, decimals)) }
    LaunchedEffect(value) {
        if (text.toDoubleOrNullFlexible() != value) text = formatNumber(value, decimals)
    }
    OutlinedTextField(
        value = text,
        onValueChange = { raw ->
            text = raw
            raw.toDoubleOrNullFlexible()?.let(onValueChange)
        },
        label = { Text(label) },
        suffix = suffix?.let { { Text(it) } },
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.fillMaxWidth(),
    )
}

/** Variante entière, pour les durées et les années. */
@Composable
fun IntField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    suffix: String? = null,
) {
    var text by remember { mutableStateOf(value.toString()) }
    LaunchedEffect(value) {
        if (text.trim().toIntOrNull() != value) text = value.toString()
    }
    OutlinedTextField(
        value = text,
        onValueChange = { raw ->
            text = raw.filter { it.isDigit() || it == '-' }
            text.toIntOrNull()?.let(onValueChange)
        },
        label = { Text(label) },
        suffix = suffix?.let { { Text(it) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth(),
    )
}

/** Champ optionnel : vide signifie « non renseigné », et non zéro. */
@Composable
fun OptionalNumberField(
    label: String,
    value: Double?,
    onValueChange: (Double?) -> Unit,
    modifier: Modifier = Modifier,
    suffix: String? = null,
    decimals: Int = 2,
) {
    var text by remember { mutableStateOf(value?.let { formatNumber(it, decimals) } ?: "") }
    LaunchedEffect(value) {
        if (text.toDoubleOrNullFlexible() != value) {
            text = value?.let { formatNumber(it, decimals) } ?: ""
        }
    }
    OutlinedTextField(
        value = text,
        onValueChange = { raw ->
            text = raw
            onValueChange(raw.toDoubleOrNullFlexible())
        },
        label = { Text(label) },
        suffix = suffix?.let { { Text(it) } },
        placeholder = { Text("facultatif") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
fun LabeledTextField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    placeholder: String? = null,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        modifier = modifier.fillMaxWidth(),
    )
}

/** Liste déroulante générique, adossée à une liste d'éléments quelconques. */
@Composable
fun <T> Dropdown(
    label: String,
    options: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    optionLabel: (T) -> String,
    modifier: Modifier = Modifier,
    placeholder: String = "— aucun —",
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selected?.let(optionLabel) ?: placeholder,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
        )
        // Une surface transparente par-dessus le champ en lecture seule capte le clic.
        Box(
            Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 400.dp),
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
    }
}

/** Liste déroulante sur une énumération du domaine, affichée par son libellé français. */
@Composable
fun <T> EnumDropdown(
    label: String,
    options: List<T>,
    selected: T,
    modifier: Modifier = Modifier,
    onSelect: (T) -> Unit,
) where T : Enum<T>, T : Labelled {
    Dropdown(
        label = label,
        options = options,
        selected = selected,
        onSelect = onSelect,
        optionLabel = { it.label },
        modifier = modifier,
    )
}

/**
 * Édition d'une liste d'étiquettes (arômes, styles) sous forme de texte séparé par des virgules.
 * Plus direct à saisir au clavier qu'une interface à puces.
 */
@Composable
fun TagsField(
    label: String,
    values: List<String>,
    modifier: Modifier = Modifier,
    onValuesChange: (List<String>) -> Unit,
) {
    var text by remember { mutableStateOf(values.joinToString(", ")) }
    LaunchedEffect(values) {
        val parsed = text.split(',').map { it.trim() }.filter { it.isNotEmpty() }
        if (parsed != values) text = values.joinToString(", ")
    }
    OutlinedTextField(
        value = text,
        onValueChange = { raw ->
            text = raw
            onValuesChange(raw.split(',').map { it.trim() }.filter { it.isNotEmpty() })
        },
        label = { Text(label) },
        placeholder = { Text("séparés par des virgules") },
        modifier = modifier.fillMaxWidth(),
    )
}
