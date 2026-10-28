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
import androidx.compose.ui.focus.onFocusChanged
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
 * Texte d'un champ dont la valeur fait l'aller-retour par la base. Pendant la saisie, c'est le
 * texte local qui fait foi : sinon une émission en retard (valeur d'il y a une ou deux frappes)
 * écraserait les derniers caractères tapés ou ferait réapparaître ceux qu'on vient d'effacer.
 * La valeur externe n'est reprise que lorsque le champ n'a pas le focus.
 */
private class EditableText(initial: String) {
    var text by mutableStateOf(initial)
    var focused by mutableStateOf(false)
}

@Composable
private fun rememberEditableText(display: String, isInSync: (String) -> Boolean): EditableText {
    val state = remember { EditableText(display) }
    LaunchedEffect(display, state.focused) {
        if (!state.focused && !isInSync(state.text)) state.text = display
    }
    return state
}

private fun Modifier.trackFocus(state: EditableText): Modifier =
    onFocusChanged { state.focused = it.hasFocus }

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
    val state = rememberEditableText(formatNumber(value, decimals)) { it.toDoubleOrNullFlexible() == value }
    OutlinedTextField(
        value = state.text,
        onValueChange = { raw ->
            state.text = raw
            raw.toDoubleOrNullFlexible()?.let(onValueChange)
        },
        label = { Text(label) },
        suffix = suffix?.let { { Text(it) } },
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.fillMaxWidth().trackFocus(state),
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
    val state = rememberEditableText(value.toString()) { it.trim().toIntOrNull() == value }
    OutlinedTextField(
        value = state.text,
        onValueChange = { raw ->
            state.text = raw.filter { it.isDigit() || it == '-' }
            state.text.toIntOrNull()?.let(onValueChange)
        },
        label = { Text(label) },
        suffix = suffix?.let { { Text(it) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth().trackFocus(state),
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
    val state = rememberEditableText(value?.let { formatNumber(it, decimals) } ?: "") {
        it.toDoubleOrNullFlexible() == value
    }
    OutlinedTextField(
        value = state.text,
        onValueChange = { raw ->
            state.text = raw
            onValueChange(raw.toDoubleOrNullFlexible())
        },
        label = { Text(label) },
        suffix = suffix?.let { { Text(it) } },
        placeholder = { Text("facultatif") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.fillMaxWidth().trackFocus(state),
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
    val state = rememberEditableText(value) { it == value }
    OutlinedTextField(
        value = state.text,
        onValueChange = { raw ->
            state.text = raw
            onValueChange(raw)
        },
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        modifier = modifier.fillMaxWidth().trackFocus(state),
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
    val state = rememberEditableText(values.joinToString(", ")) { text ->
        text.split(',').map { it.trim() }.filter { it.isNotEmpty() } == values
    }
    OutlinedTextField(
        value = state.text,
        onValueChange = { raw ->
            state.text = raw
            onValuesChange(raw.split(',').map { it.trim() }.filter { it.isNotEmpty() })
        },
        label = { Text(label) },
        placeholder = { Text("séparés par des virgules") },
        modifier = modifier.fillMaxWidth().trackFocus(state),
    )
}
