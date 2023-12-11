package com.mask.texttospeech

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mask.texttospeech.ui.theme.TextToSpeechTheme
import java.util.Locale

// Working code från den här videon:
// https://www.youtube.com/watch?v=92XqUVmLUPg&list=PLlSuJy9SfzvEj3RMnk9fLihCwUUllXIht

class MainActivity : ComponentActivity() {
    private lateinit var textToSpeech: TextToSpeech
    private var textToSpeak: String = "there once was a little redhood in the forest."
    private var locale = Locale.CANADA
    private var pitch : String = "2.0"
    private var speechRate : String = "0.6"

    val buttonCornerRadius = 20.dp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TextToSpeechTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DropdownMenuWithLanguages()
                        EditPitch()
                        EditSpeechRate()
                        TextInput()
                        BtnTextToSpeech()
                    }
                }
            }
        }
    }

    // All the fun happens here. :)
    fun TextToSpeech() {
        textToSpeech = TextToSpeech(this) {
            status ->
            if(status == TextToSpeech.SUCCESS) {
                //var locale = Locale.getDefault()

                val result = textToSpeech.setLanguage(locale)
                if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                    Toast.makeText(this, "language is not supported", Toast.LENGTH_LONG).show()
                }
                else
                {
                    textToSpeech.setPitch(pitch.toFloat())
                    textToSpeech.setSpeechRate(speechRate.toFloat())
                    //textToSpeech.setVoice()

                    textToSpeech.speak(
                        textToSpeak,
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        null)
                }
            }
        }
    }

    // Lite bråk om hur och så här:
    // https://stackoverflow.com/questions/40352684/what-is-the-equivalent-of-java-static-methods-in-kotlin
    // Men! en skriver att det rek. är att lägga funktionen utanför någon klass, så blir det ju utan this och därmed statisk.
    // (Kotlin skapar då en under-the-hood klass class TextToSpeechPackage, där namespacet är med i namnet.
    companion object {
        fun StaticFunctionExample() {
            var a = 2 // yeye, men du kan inte använda this förstås.
        }
    }

    @Composable
    fun TextInput(modifier: Modifier = Modifier) {
        // Utan en remember så uppdateras inte vyn när jag skriver in nya värden.
        var theValue by remember { mutableStateOf(textToSpeak) }

        TextField(
            label = { Text("Text to speak")},
            value = theValue,
            onValueChange = {
                // Skönt med automatik..
                theValue = it

                // ..jag måste så klart fortfarande uppdatera min variabel.
                textToSpeak = it
                            },
            modifier = modifier
        )
    }

    @Composable
    fun BtnTextToSpeech(modifier: Modifier = Modifier) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Lägger jag clip() före clickable() så kan jag sedan bara klicka på den rundade ytan och inte hela knappens rektangel.
            Column(
                modifier = modifier
                    .background(color = Color(160, 110, 40))
                    .clip(RoundedCornerShape(buttonCornerRadius, buttonCornerRadius, 0.dp, buttonCornerRadius))
                    .clickable {
                        // wörks
                        // MainActivity.StaticFunctionExample()

                        // Men eftersom this behövs så lägger jag in alltsammans inkl. den här @Composablen i klassen. Ser inga större skador med det. :-)
                        TextToSpeech()
                    }
                    .background(color = Color(140, 210, 140))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Hello"
                )
            }
        }
    }

    // https://stackoverflow.com/questions/70813804/dropdownmenu-in-jetpack-compose
    @Composable
    fun DropdownMenuWithLanguages(modifier: Modifier = Modifier) {
        var showMenu by remember { mutableStateOf(false) }
        val locales = Locale.getAvailableLocales()

        Box(modifier = modifier) {
            Column(
                modifier = modifier
                    .clip(RoundedCornerShape(buttonCornerRadius, buttonCornerRadius, buttonCornerRadius, buttonCornerRadius))
                    .clickable {
                        showMenu = !showMenu
                    }
                    .background(color = Color(140, 210, 140))
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.change_locale) + ": $locale"
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = {  }) {
                // Well. Att skapa en lista av DropdownMenuItems är visst omöjligt.
                locales.forEach {
                    DropdownMenuItem(
                        onClick = {
                            locale = it
                            showMenu = false
                        },
                        text = {
                            // inte lätt att förstå att det var det här kompilatorn ville ha.
                            Text(text = it.displayName)
                        }
                    )
                }
            }
        }

        /*DropdownMenu(
            content = {
                val locales = Locale.getAvailableLocales()

                locales.forEach {
                    var dn = it.displayName
                    DropdownMenuItem(
                        text = dn,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                        }
                    ) {
                        Text(it, modifier = Modifier.wrapContentWidth().align(Alignment.Start))
                    }
                }
            },
            expanded = false,
            onDismissRequest = {}
            )*/
    }

    @Composable
    // vore ju häftigare med upp och ner pilar, men vem orkar.
    fun EditPitch(modifier: Modifier = Modifier) {
        var daPitch by remember { mutableStateOf(pitch) }

        EditNumberField(
            label = R.string.change_pitch,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            value = daPitch,
            onValueChanged = {
                daPitch = it
                pitch = it
            })
    }

    @Composable
    fun EditSpeechRate(modifier: Modifier = Modifier) {
        var daSpeechRate by remember { mutableStateOf(speechRate) }

        EditNumberField(
            label = R.string.change_speech_rate,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            value = daSpeechRate,
            onValueChanged = {
                daSpeechRate = it
                speechRate = it
            })
    }

    @Composable
    fun EditNumberField(
        @StringRes label: Int,
        keyboardOptions: KeyboardOptions,
        value: String,
        onValueChanged: (String) -> Unit,
        modifier: Modifier = Modifier
    ) {
        // Intressanta är: leadingIcon och label, dvs. ikonen till vänster i rutan samt
        // "bakgrundstexten" som brukar försvinna när rutan får fokus, men i det här fallet
        // visas strax ovanför själva inmatningsytan fast inom rutan.
        TextField(
            value = value,
            singleLine = true,
            modifier = modifier,
            onValueChange = onValueChanged,
            label = { Text(stringResource(label)) },
            keyboardOptions = keyboardOptions
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun PreviewTextInput() {
        TextToSpeechTheme {
            TextInput()
        }
    }
    @Preview(showBackground = true)
    @Composable
    fun PreviewBtnSpeechToText() {
        TextToSpeechTheme {
            BtnTextToSpeech()
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun PreviewDropdownMenuWithLanguages() {
        TextToSpeechTheme {
            DropdownMenuWithLanguages()
        }
    }
}

