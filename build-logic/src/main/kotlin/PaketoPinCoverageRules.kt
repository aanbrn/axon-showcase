/**
 * The rule the Paketo-pin coverage gate applies: every `paketo-*` pin the catalog holds has an entry in the build's
 * Paketo check list, and every entry names a pin the catalog holds.
 *
 * The catalog's keys reach the build as version aliases, where Gradle normalizes `-`, `_` and `.` to `.`, so a key
 * `paketo-nginx` reads back as `paketo.nginx`. This object maps an alias back to the catalog key form and compares the
 * two sets. The logic is pure so it is testable without Gradle: [VerifyDocumentedPinsTask] reads the sets, calls these
 * rules, and turns a message into a failure.
 */
internal object PaketoPinCoverageRules {

    /** The prefix every Paketo pin's catalog key carries. */
    const val KEY_PREFIX = "paketo-"

    /**
     * The catalog's Paketo keys, in key form, from the catalog's version aliases.
     *
     * @param aliases the catalog's version aliases, as the catalog extension yields them
     * @return the `paketo-*` keys, dashes restored
     */
    fun catalogKeys(aliases: Iterable<String>): Set<String> =
        aliases.map { it.replace('.', '-') }.filter { it.startsWith(KEY_PREFIX) }.toSet()

    /**
     * The catalog keys the check list does not cover.
     *
     * @param catalogKeys the catalog's Paketo keys
     * @param checkNames the names of the build's Paketo check entries
     * @return the uncovered catalog keys, sorted, empty when every pin has an entry
     */
    fun uncovered(catalogKeys: Set<String>, checkNames: Set<String>): List<String> =
        catalogKeys.filterNot { it in checkNames }.sorted()

    /**
     * The check entries naming a key the catalog does not hold.
     *
     * @param catalogKeys the catalog's Paketo keys
     * @param checkNames the names of the build's Paketo check entries
     * @return the unknown entry names, sorted, empty when every entry names a catalog pin
     */
    fun unknown(catalogKeys: Set<String>, checkNames: Set<String>): List<String> =
        checkNames.filterNot { it in catalogKeys }.sorted()
}
