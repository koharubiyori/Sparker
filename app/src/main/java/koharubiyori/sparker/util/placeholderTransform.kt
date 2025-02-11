package koharubiyori.sparker.util

import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class PlaceholderTransformation(private val placeholder: String) : VisualTransformation {
  override fun filter(text: AnnotatedString): TransformedText {
    return placeholderFilter(text, placeholder)
  }

  companion object {
    @Composable
    fun textFieldColors(
      inputValueEmpty: Boolean,
      focusedTextColor: Color = Color. Unspecified,
      unfocusedTextColor: Color = Color. Unspecified,
      disabledTextColor: Color = Color. Unspecified,
      errorTextColor: Color = Color. Unspecified,
      focusedContainerColor: Color = Color. Unspecified,
      unfocusedContainerColor: Color = Color. Unspecified,
      disabledContainerColor: Color = Color. Unspecified,
      errorContainerColor: Color = Color. Unspecified,
      cursorColor: Color = Color. Unspecified,
      errorCursorColor: Color = Color. Unspecified,
      selectionColors: TextSelectionColors? = null,
      focusedBorderColor: Color = Color. Unspecified,
      unfocusedBorderColor: Color = Color. Unspecified,
      disabledBorderColor: Color = Color. Unspecified,
      errorBorderColor: Color = Color. Unspecified,
      focusedLeadingIconColor: Color = Color. Unspecified,
      unfocusedLeadingIconColor: Color = Color. Unspecified,
      disabledLeadingIconColor: Color = Color. Unspecified,
      errorLeadingIconColor: Color = Color. Unspecified,
      focusedTrailingIconColor: Color = Color. Unspecified,
      unfocusedTrailingIconColor: Color = Color. Unspecified,
      disabledTrailingIconColor: Color = Color. Unspecified,
      errorTrailingIconColor: Color = Color. Unspecified,
      focusedLabelColor: Color = Color. Unspecified,
      unfocusedLabelColor: Color = Color. Unspecified,
      disabledLabelColor: Color = Color. Unspecified,
      errorLabelColor: Color = Color. Unspecified,
      focusedPlaceholderColor: Color = Color. Unspecified,
      unfocusedPlaceholderColor: Color = Color. Unspecified,
      disabledPlaceholderColor: Color = Color. Unspecified,
      errorPlaceholderColor: Color = Color. Unspecified,
      focusedSupportingTextColor: Color = Color. Unspecified,
      unfocusedSupportingTextColor: Color = Color. Unspecified,
      disabledSupportingTextColor: Color = Color. Unspecified,
      errorSupportingTextColor: Color = Color. Unspecified,
      focusedPrefixColor: Color = Color. Unspecified,
      unfocusedPrefixColor: Color = Color. Unspecified,
      disabledPrefixColor: Color = Color. Unspecified,
      errorPrefixColor: Color = Color. Unspecified,
      focusedSuffixColor: Color = Color. Unspecified,
      unfocusedSuffixColor: Color = Color. Unspecified,
      disabledSuffixColor: Color = Color. Unspecified,
      errorSuffixColor: Color = Color. Unspecified
    ) = OutlinedTextFieldDefaults.colors(
      focusedContainerColor = focusedContainerColor,
      unfocusedContainerColor = unfocusedContainerColor,
      disabledContainerColor = disabledContainerColor,
      errorContainerColor = errorContainerColor,
      cursorColor = cursorColor,
      errorCursorColor = errorCursorColor,
      selectionColors = selectionColors,
      focusedBorderColor = focusedBorderColor,
      unfocusedBorderColor = unfocusedBorderColor,
      disabledBorderColor = disabledBorderColor,
      errorBorderColor = errorBorderColor,
      focusedLeadingIconColor = focusedLeadingIconColor,
      unfocusedLeadingIconColor = unfocusedLeadingIconColor,
      disabledLeadingIconColor = disabledLeadingIconColor,
      errorLeadingIconColor = errorLeadingIconColor,
      focusedTrailingIconColor = focusedTrailingIconColor,
      unfocusedTrailingIconColor = unfocusedTrailingIconColor,
      disabledTrailingIconColor = disabledTrailingIconColor,
      errorTrailingIconColor = errorTrailingIconColor,
      focusedLabelColor = focusedLabelColor,
      unfocusedLabelColor = unfocusedLabelColor,
      disabledLabelColor = disabledLabelColor,
      errorLabelColor = errorLabelColor,
      focusedPlaceholderColor = focusedPlaceholderColor,
      unfocusedPlaceholderColor = unfocusedPlaceholderColor,
      disabledPlaceholderColor = disabledPlaceholderColor,
      errorPlaceholderColor = errorPlaceholderColor,
      focusedSupportingTextColor = focusedSupportingTextColor,
      unfocusedSupportingTextColor = unfocusedSupportingTextColor,
      disabledSupportingTextColor = disabledSupportingTextColor,
      errorSupportingTextColor = errorSupportingTextColor,
      focusedPrefixColor = focusedPrefixColor,
      unfocusedPrefixColor = unfocusedPrefixColor,
      disabledPrefixColor = disabledPrefixColor,
      errorPrefixColor = errorPrefixColor,
      focusedSuffixColor = focusedSuffixColor,
      unfocusedSuffixColor = unfocusedSuffixColor,
      disabledSuffixColor = disabledSuffixColor,
      errorSuffixColor = errorSuffixColor,

      focusedTextColor = if (inputValueEmpty) MaterialTheme.colorScheme.onSurfaceVariant else focusedTextColor,
      unfocusedTextColor = if (inputValueEmpty) MaterialTheme.colorScheme.onSurfaceVariant else unfocusedTextColor,
      disabledTextColor = if (inputValueEmpty) MaterialTheme.colorScheme.onSurfaceVariant else disabledTextColor,
      errorTextColor = if (inputValueEmpty) MaterialTheme.colorScheme.onSurfaceVariant else errorTextColor,
    )
  }
}

private fun placeholderFilter(text: AnnotatedString, placeholder: String): TransformedText {

  val numberOffsetTranslator = object : OffsetMapping {
    override fun originalToTransformed(offset: Int): Int {
      return 0
    }

    override fun transformedToOriginal(offset: Int): Int {
      return 0
    }
  }

  return TransformedText(AnnotatedString(placeholder), numberOffsetTranslator)
}