[![Maven Central](https://img.shields.io/maven-central/v/io.github.oleksandrbalan/minabox.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.oleksandrbalan/minabox)

<img align="right" width="128" src="https://github.com/oleksandrbalan/minabox/assets/20944869/616a3df3-d83a-4e2f-896a-b6be6cb98a29">

# Mina Box

Lazy box library for Compose UI.

Mina box allows to display lazy loaded items on the 2D plane. It is build on the `LazyLayout` and provides methods to register item(s) and handles scrolling on the plane.

## Multiplatform

Library supports [Android](https://developer.android.com/jetpack/compose), [iOS](https://github.com/JetBrains/compose-multiplatform-ios-android-template/#readme), [Desktop](https://github.com/JetBrains/compose-multiplatform-desktop-template/#readme) (Windows, MacOS, Linux) and [Wasm](https://github.com/Kotlin/kotlin-wasm-examples/blob/main/compose-example/README.md#compose-multiplatform-for-web) targets.

<img align="left" width="100" src="https://github.com/oleksandrbalan/minabox/assets/20944869/7af845f2-617f-4edb-bf34-9a12335f963d">

## Mina?

Maybe you are asking yourself why library has such a strange name? It is named after our family cat "Mina", and she is too damn lazy and likes to sit in the boxes, thus I think it matches perfectly this layout's purpose.

## Usage

### Get a dependency

**Step 1.** Add the MavenCentral repository to your build file.
Add it in your root `build.gradle.kts` at the end of repositories:
```kotlin
allprojects {
    repositories {
        ...
        mavenCentral()
    }
}
```

Or in `settings.gradle.kts`:
```kotlin
dependencyResolutionManagement {
    repositories {
        ...
        mavenCentral()
    }
}
```

**Step 2.** Add the dependency.
Check latest version on the [releases page](https://github.com/oleksandrbalan/minabox/releases).
```kotlin
dependencies {
    implementation("io.github.oleksandrbalan:minabox:$version")
}
```

### Use in Composable

The core element of the `MinaBox` layout is a `content` lambda, where items are registered in the similar manner as in `LazyColumn` or `LazyRow`. The main difference is that each item must provide its position and size in the `layoutInfo` lambda. Each item could be locked horizontally or vertically to create pinned rows and / or columns of data. The item size could be defined absolutely (in pixels) or relatively to parent size.

```kotlin
val columns = 10
val rows = 10
val width = 128f // pixels
val height = 64f // pixels
MinaBox {
    items(
        count = columns * rows,
        layoutInfo = {
            MinaBoxItem(
                x = width * (it % columns),
                y = height * (it / columns),
                width = width,
                height = height,
            )
        }
    ) { index ->
        Text(text = "#$index")
    }
}
```

See Demo application and [examples](demo/src/commonMain/kotlin/eu/wewox/minabox/screens) for more usage examples.

### More usages

There are two libraries which are build on top of the Mina Box layout:
* [LazyTable](https://github.com/oleksandrbalan/lazytable) - displays columns and rows of data on the two directional plane.
* [ProgramGuide](https://github.com/oleksandrbalan/programguide) - displays program guide data on the two directional plane.

Make sure to check them for tip & tricks if you want to build your own implementation.

## Examples

Simple table with items aligned in columns and rows.

https://github.com/oleksandrbalan/minabox/assets/20944869/da8a8733-441b-47cb-ada6-7240c7f0645f

⬡ Fancy hexagons ⬡. 

https://github.com/oleksandrbalan/minabox/assets/20944869/45cc619a-8517-456c-8124-5399d2c07891

Lazy table with pinned columns & rows, check [LazyTable](https://github.com/oleksandrbalan/lazytable) library.

https://github.com/oleksandrbalan/minabox/assets/20944869/6c74bd21-a1d5-4f9d-92a6-823b6bf8ec5f

Program Guide (aka EPG), check [ProgramGuide](https://github.com/oleksandrbalan/programguide) library.

https://github.com/oleksandrbalan/minabox/assets/20944869/a6db5dbe-f5a7-4fa5-aae0-9ca16d10a84b
