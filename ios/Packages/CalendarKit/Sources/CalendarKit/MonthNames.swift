/// The month names the app shows.
///
/// The English names are the product's, not the library's: the product writes Asoj, Asar and
/// Baisakh, while nepdate-swift writes Ashoj, Ashad and Baishakh. MR-01 AC2 and MR-10 AC1 use the
/// product names, so the names live here rather than come from the library. The Nepali names are
/// the same as the library's.
public enum MonthNames {
  /// The Bikram Sambat month name.
  /// - Parameters:
  ///   - month: The month, 1 to 12.
  ///   - language: The language of the name.
  /// - Returns: The full name, or an empty string for a month outside 1 to 12.
  public static func bs(_ month: Int, _ language: AppLanguage) -> String {
    guard (1...12).contains(month) else { return "" }
    return language == .nepali ? bsNepali[month - 1] : bsEnglish[month - 1]
  }

  /// A Gregorian month name.
  /// - Parameters:
  ///   - month: The month, 1 to 12.
  ///   - language: The language of the name.
  ///   - short: `true` for an abbreviation such as `Sep`, `false` for `September`.
  /// - Returns: The name, or an empty string for a month outside 1 to 12.
  public static func ad(_ month: Int, _ language: AppLanguage, short: Bool = false) -> String {
    guard (1...12).contains(month) else { return "" }
    if language == .nepali { return adNepali[month - 1] }
    return short ? adEnglishShort[month - 1] : adEnglishLong[month - 1]
  }

  private static let bsEnglish = [
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
  ]

  private static let bsNepali = [
    "बैशाख",
    "जेठ",
    "असार",
    "साउन",
    "भदौ",
    "असोज",
    "कार्तिक",
    "मंसिर",
    "पुष",
    "माघ",
    "फागुन",
    "चैत",
  ]

  private static let adEnglishShort = [
    "Jan",
    "Feb",
    "Mar",
    "Apr",
    "May",
    "Jun",
    "Jul",
    "Aug",
    "Sep",
    "Oct",
    "Nov",
    "Dec",
  ]

  private static let adEnglishLong = [
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
  ]

  private static let adNepali = [
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
  ]
}
