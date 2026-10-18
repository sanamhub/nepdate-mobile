extension Digits {
  /// The text with its digits rewritten to this setting.
  ///
  /// Event names can contain digits, and MR-07 AC1 applies the Digits setting to every number on
  /// screen, so a label or a list row passes names through this before drawing them.
  /// - Parameter text: The text, in either script.
  /// - Returns: The text with ASCII or Devanagari digits in this setting.
  public func applying(to text: String) -> String {
    let ascii: [Character] = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9"]
    let devanagari: [Character] = ["०", "१", "२", "३", "४", "५", "६", "७", "८", "९"]
    let from = self == .nepali ? ascii : devanagari
    let to = self == .nepali ? devanagari : ascii
    var output = ""
    output.reserveCapacity(text.count)
    for character in text {
      if let index = from.firstIndex(of: character) {
        output.append(to[index])
      } else {
        output.append(character)
      }
    }
    return output
  }
}
