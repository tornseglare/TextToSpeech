package com.mask.texttospeech

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mask.texttospeech.ui.theme.TextToSpeechTheme
import java.util.Locale

// Working code från den här videon:
// https://www.youtube.com/watch?v=92XqUVmLUPg&list=PLlSuJy9SfzvEj3RMnk9fLihCwUUllXIht

// LazyColumn video som fungerar, samt en massa annat smart:
// https://www.youtube.com/watch?v=XfYlRn_Jy1g

// TODO: Tanken är att man ska få upp alla "subspråk när man valt tex. engelska. I en ny lista, så extrahera din fina kod så den kan populeras med olika listor
//  och anropa olika callbacks.

class MainActivity : ComponentActivity() {
    private lateinit var textToSpeech: TextToSpeech
    private var textToSpeak: String = "there once was a little redhood in the forest."
    private var selectedMainLanguage = Locale.ENGLISH
    private var selectedLocale = Locale.CANADA
    private var pitch : String = "2.0"
    private var speechRate : String = "0.6"

    val buttonCornerRadius = 20.dp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Det här krävs för att findViewById() ska hitta recycler_view.xml alls. (Som jag raderade efter en hel kväll av helvete med xml layout-skit)
        //setContentView(R.layout.activity_main)

        val locales = Locale.getAvailableLocales()

        // Använde phind.com som gav mig den här koden! :O
        // Först sorterar jag rubbet på friendly name, och sedan grupperar jag per språk, så att listan endast visar de olika språken och skippar de olika varianterna.
        val groupedByLanguage = locales.sortedBy { it.displayName }.groupBy { it.language }

        // map är grym, här ber jag den plocka ut language ur härket (it) och lägga allihopa i en lista. List<String>
        val languageList: List<Locale> = groupedByLanguage.map {
            // Man _kan_ ta first() eftersom det alltid är "standardspråket", tex Swedish utan Åmålska. displayName är alltså i samtliga fall språkets namn utan dess kursiver.
            it.value.first() //.displayName
        }

        //val dataset = listOf<String>("January", "February", "March")

        // RecyclerView är ett jävla skit som är helt icke-kompatibel med @Content..
        /*val recyclerViewAdapter = RecyclerViewAdapter(dataset)
        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = recyclerViewAdapter*/

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
                        Column(modifier = Modifier
                            .weight(0.5f, false)
                            .background(color = Color.Black)
                            .fillMaxSize()
                        ) {
                            LanguagesList(languageList)
                        }
                        Column(modifier = Modifier
                            .weight(0.5f, false)
                            .fillMaxSize()
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
    }

    // All the fun happens here. :)
    fun TextToSpeech() {
        textToSpeech = TextToSpeech(this) {
            status ->
            if(status == TextToSpeech.SUCCESS) {
                //var selectedLocale = Locale.getDefault()

                val result = textToSpeech.setLanguage(selectedMainLanguage)
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
                    .clip(
                        RoundedCornerShape(
                            buttonCornerRadius,
                            buttonCornerRadius,
                            0.dp,
                            buttonCornerRadius
                        )
                    )
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

        // Använde phind.com som gav mig den här koden! :O
        // Först sorterar jag rubbet på friendly name, och sedan grupperar jag per språk, så att listan endast visar de olika språken och skippar de olika varianterna.
        val groupedByLanguage = locales.sortedBy { it.displayName }.groupBy { it.language }

        // same same, jag vill ha index typ.
        // var selectedLanguage = groupedByLanguage.get(selectedLocale.displayName)

        Box(modifier = modifier) {
            Column(
                modifier = modifier
                    .clip(
                        RoundedCornerShape(
                            buttonCornerRadius,
                            buttonCornerRadius,
                            buttonCornerRadius,
                            buttonCornerRadius
                        )
                    )
                    .clickable {
                        showMenu = !showMenu
                    }
                    .background(color = Color(140, 210, 140))
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.change_locale) + ": ${selectedLocale.displayName}"
                )
            }
            /*DropdownMenu(
                expanded = showMenu,
                onDismissRequest = {  }) {
                // Well. Att skapa en lista av DropdownMenuItems är visst omöjligt.
                groupedByLanguage.forEach {
                    DropdownMenuItem(
                        onClick = {
                            selectedLocale = it.value.first() // fuul it
                            showMenu = false
                        },
                        text = {
                            // inte lätt att förstå att det var det här kompilatorn ville ha.
                            Text(text = "${it.key} ${it.value.first().displayName}, ${it.value.count()} more langs")
                        }
                    )
                }
            }*/
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

    // Används i LazyColumn.
    @Composable
    private fun LanguageItem(
        locale: Locale,
        icon: ImageVector,
        onclickEvent: (Locale) -> Unit,
        modifier: Modifier = Modifier
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Start,
            modifier = modifier
                .fillMaxWidth()
                .padding(2.dp)
                .clip(RoundedCornerShape(buttonCornerRadius, 0.dp, 0.dp, buttonCornerRadius))
                .clickable {
                    // this@MainActivity because, fuck off: https://stackoverflow.com/questions/58308082/error-none-of-the-following-functions-can-be-called-with-the-arguments-supplied
                    Toast
                        .makeText(
                            this@MainActivity,
                            "Selected language: ${locale.displayName}",
                            Toast.LENGTH_SHORT
                        )
                        .show()

                    onclickEvent(locale)
                }
                .background(Color.LightGray)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = locale.displayName,
                modifier = Modifier,
                tint = Color(0, 110, 59))
            Text(
                text = locale.displayName,
                fontSize = 16.sp,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .weight(0.2f)
                    .padding(horizontal = 16.dp)
            )
        }
    }

    // Efter flera timmars ofruktsamt testande med RecyclerView så fann jag LazyColumn.
    @Composable
    fun LanguagesList(languages: List<Locale>, modifier: Modifier = Modifier) {

        // State-hoisting: komplicerat ord för att se till att LanguageItem() är "state-less", dvs. inte har några remember's i sig.
        // Genom att skicka med vilken icon som ska visas så ansvarar denna funktion för detta,
        // och, viktigt här, LanguageItem() har en callback "hit" som uppdaterar rememberLocale!
        // Då uppdateras den här, lokalt, och tvingar väl LazyColumn att rendera om sig.
        //
        // https://developer.android.com/codelabs/jetpack-compose-state#8
        //
        var rememberLocale by remember {
            mutableStateOf(selectedMainLanguage)
        }

        // Genom att bara ta bort alla inställningar så fyllde den by default allt tillgängligt utrymme. :)
        LazyColumn(
            Modifier
                .padding(8.dp)
                .background(color = Color.DarkGray)
                //.verticalScroll(state = ScrollState(0), enabled = true)
                //.height(256.dp)
        ) {
            // Verkar inte som man kan kollapsa på dessa, så begränsad nytta.
            // stickyHeader {  }

            // Över en timmas googlande och video-tittande och prat med chatbottar... Helt jävla omöjligt att reda ut hur de vill ha det..
            items(languages) { locale ->

                var icon = Icons.Filled.FavoriteBorder

                if(locale.displayName == rememberLocale.displayName) {
                    icon = Icons.Filled.Favorite
                }

                // Det är denna callbacken som uppdaterar rememberLocale, och därmed ber LazyColumn rendera om sina element.
                LanguageItem(locale, icon, onclickEvent =  {
                    // Döper om "it" till "selectedLocale" här. ;-)
                    selectedLocale ->
                        selectedMainLanguage = selectedLocale
                        rememberLocale = selectedLocale
                })
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun PreviewLanguagesList() {
        val dataset = listOf<Locale>(Locale.CANADA, Locale.CHINA, Locale.FRANCE, Locale.UK, Locale.GERMAN, Locale.GERMANY)

        TextToSpeechTheme {
            LanguagesList(dataset)
        }
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


// skrotklasser som inte används.

data class Category(
    public val name: String,
    public val items: List<String>
)

/*class RecyclerViewAdapter(private val dataSet: Array<String>) :
    RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView

        init {
            // Define click listener for the ViewHolder's View
            textView = view.findViewById(R.id.textView)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.text_row_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.textView.text = dataSet[position]
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}*/


