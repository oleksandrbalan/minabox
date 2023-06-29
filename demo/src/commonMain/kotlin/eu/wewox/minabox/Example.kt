package eu.wewox.minabox

/**
 * Enumeration of available demo examples.
 *
 * @param label Example name.
 * @param description Brief description.
 */
enum class Example(
    val label: String,
    val description: String,
) {
    MinaBoxSimple(
        "Mina Box Simple",
        "Simple Mina Box layout example"
    ),
    MinaBoxContentPadding(
        "Mina Box Padding",
        "Mina Box example with content padding"
    ),
    MinaBoxAdvanced(
        "Mina Box Advanced",
        "Advanced Mina Box layout example"
    ),
}
