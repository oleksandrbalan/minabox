[![Maven Central](https://img.shields.io/maven-central/v/io.github.oleksandrbalan/minabox.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.oleksandrbalan/minabox)

TODO: Place correct image
<img align="right" src="https://user-images.githubusercontent.com/20944869/211682502-ea30da26-178f-4e91-82d3-75207dbb6356.png">

# Mina Box

Lazy box library for Jetpack Compose.

Mina box allows to display lazy loaded items on the 2D plane. It is build on the `LazyLayout` and provides methods to register item(s) and handles scrolling on the plane.

## Mina?

Maybe you are asking yourself why library has such a strange name? It is named after my cat "Mina", and she is too damn lazy and likes to sit in the boxes, thus I think it matches perfectly this layout's purpose.

## Usage

### Get a dependency

**Step 1.** Add the MavenCentral repository to your build file.
Add it in your root `build.gradle` at the end of repositories:
```
allprojects {
    repositories {
        ...
        mavenCentral()
    }
}
```

Or in `settings.gradle`:
```
pluginManagement {
    repositories {
        ...
        mavenCentral()
    }
}
```

**Step 2.** Add the dependency.
Check latest version on the [releases page](https://github.com/oleksandrbalan/minabox/releases).
```
dependencies {
    implementation 'io.github.oleksandrbalan:minabox:$version'
}
```

### Use in Composable

The core element of the `MinaBox` layout is a `content` lambda, where items are registered in the similar manner as in `LazyColumn` or `LazyRow`. The main difference is that each item must provide its position and size in the `layoutInfo` lambda. Each item could be locked horizontally or vertically to create pinned rows and / or columns of data. The item size could be defined absolutely (in pixels) or relatively to parent size.

```
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

See Demo application and [examples](demo/src/main/kotlin/eu/wewox/minabox/screens) for more usage examples.

### More usages

There are two libraries which are build on top of the Mina Box layout:
* `LazyTable` - displays columns and rows of data on the two directional plane.
* `ProgramGuide` - displays program guide data on the two directional plane.

Make sure to check them for tip & tricks if you want to build your own implementation.
