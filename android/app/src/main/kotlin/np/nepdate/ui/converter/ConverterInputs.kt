package np.nepdate.ui.converter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.sanamhub.nepdate.NepaliDate

/**
 * What the reader has set on the converter: the direction, the wheel date and the two dates of the
 * "between" card, each kept across tab switches and overlays. [wheelKey] changes on a reset so the
 * wheel is rebuilt and its columns scroll back too.
 */
internal class ConverterInputs(
    directionState: MutableState<ConvertDirection>,
    pickedState: MutableLongState,
    fromState: MutableLongState,
    toState: MutableLongState,
    wheelKeyState: MutableIntState,
) {
    var direction by directionState
    var pickedDay by pickedState
    var fromDay by fromState
    var toDay by toState
    var wheelKey by wheelKeyState
        private set

    /** The "between" card's result for the two chosen dates. */
    fun comparison(): Comparison = comparisonOf(NepaliDate.ofEpochDay(fromDay), NepaliDate.ofEpochDay(toDay))

    /** True once anything differs from a fresh converter on [today]. */
    fun isChanged(today: NepaliDate): Boolean =
        direction != ConvertDirection.BS_TO_AD ||
            pickedDay != today.toEpochDay() ||
            fromDay != yearStart(today).toEpochDay() ||
            toDay != today.toEpochDay()

    /** Back to a fresh converter on [today]: BS to AD, today on the wheel, the year so far. */
    fun reset(today: NepaliDate) {
        direction = ConvertDirection.BS_TO_AD
        pickedDay = today.toEpochDay()
        fromDay = yearStart(today).toEpochDay()
        toDay = today.toEpochDay()
        wheelKey++
    }

    companion object {
        /** Baisakh 1 of [today]'s year, where the "between" card starts. */
        fun yearStart(today: NepaliDate): NepaliDate = NepaliDate.of(today.year, 1, 1)
    }
}

@Composable
internal fun rememberConverterInputs(
    picked: NepaliDate,
    from: NepaliDate,
    to: NepaliDate,
): ConverterInputs {
    val direction = rememberSaveable { mutableStateOf(ConvertDirection.BS_TO_AD) }
    val pickedDay = rememberSaveable { mutableLongStateOf(picked.toEpochDay()) }
    val fromDay = rememberSaveable { mutableLongStateOf(from.toEpochDay()) }
    val toDay = rememberSaveable { mutableLongStateOf(to.toEpochDay()) }
    val wheelKey = rememberSaveable { mutableIntStateOf(0) }
    return ConverterInputs(direction, pickedDay, fromDay, toDay, wheelKey)
}
