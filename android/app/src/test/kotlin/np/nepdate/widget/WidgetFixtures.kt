package np.nepdate.widget

internal val englishWidgetCopy: WidgetCopy by lazy {
    WidgetCopy(
        "Nepal",
        "Public holiday",
        "Previous month",
        "Next month",
        "Today",
        BS_MONTHS_EN,
        AD_MONTHS_EN,
        AD_MONTHS_SHORT_EN,
        "Today",
        "Tomorrow",
        { "in $it days" },
        { "Holiday data ends on $it" },
    )
}

internal val nepaliWidgetCopy: WidgetCopy by lazy {
    WidgetCopy(
        "नेपाल",
        "सार्वजनिक बिदा",
        "अघिल्लो महिना",
        "अर्को महिना",
        "आज",
        BS_MONTHS_NE,
        AD_MONTHS_NE,
        AD_MONTHS_NE,
        "आज",
        "भोलि",
        { "$it दिनमा" },
        { "बिदा र कार्यक्रमको जानकारी $it मा सकिन्छ" },
    )
}

private val BS_MONTHS_EN =
    listOf(
        "Baisakh",
        "Jestha",
        "Asar",
        "Shrawan",
        "Bhadra",
        "Asoj",
        "Kartik",
        "Mangsir",
        "Poush",
        "Magh",
        "Falgun",
        "Chaitra",
    )

private val BS_MONTHS_NE =
    listOf("बैशाख", "जेठ", "असार", "साउन", "भदौ", "असोज", "कार्तिक", "मंसिर", "पुष", "माघ", "फागुन", "चैत")

private val AD_MONTHS_EN =
    listOf(
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December",
    )

private val AD_MONTHS_SHORT_EN =
    listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

private val AD_MONTHS_NE =
    listOf(
        "जनवरी",
        "फेब्रुअरी",
        "मार्च",
        "अप्रिल",
        "मे",
        "जुन",
        "जुलाई",
        "अगस्ट",
        "सेप्टेम्बर",
        "अक्टोबर",
        "नोभेम्बर",
        "डिसेम्बर",
    )
