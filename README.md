# PhotoAppAndroid
## Practise Android Development(Kotlin), aims to apply my knowledge, which I learned from iOS, to Android.

## Retrospective
Why not apply my knowledge gained from iOS to other platforms? Starting from this idea, I decided to try Android development with Kotlin.

## Similarity of Jetpack Compose and SwiftUI
Before that, I've already heard that Jetpack Compose is so similar to SwiftUI. After building this app, I can confirm it: they are really similar. For example, `Column`, `Row` and `Box` in Jetpack Compose are the counterparts of `VStack`, `HStack` and `ZStack` in SwiftUI. They both have `modifier`(a parameter in Jetpack, a chained method in SwiftUI), and they both are declarative. Benefit from the experiences of SwiftUI, it's quite easy for me to build this simple UI.

## Similarity of Kotlin and Swift
Maybe I just touched the surface, I feel that Kotlin and Swift are very similar too. For example: the syntax of generic, the data class likes a struct in Swift, interface in Kotlin equals protocol in Swift, and especially the `mutableStateOf` I used in ViewModel, it really likes the `@Published` property in `ObservableObject`. These similarities save me a lot of time to learn, beginning development as quickly as I could.

However, I'm really confused about the error handling. In Kotlin, it's an unchecked exception. Kotlin won't force me to handle any exceptions. It's unimaginable that we must handle every exception(by do/catch or keep throwing until client function) in Swift. The silent ignoring exception behaviour in Kotlin makes me very uncomfortable. In order to force the client function to handle the error, I resort to using the `Result` type as the return value of my APIs if needed.

## Foundations
This app has applied clean architecture, different design patterns and unit testing. These are all the foundations of programming. Once you grasp them, you can easily apply those concepts on any other language/platform.

## Frameworks
1. [Ktor HTTP Client](https://ktor.io/docs/client-create-new-application.html)
2. Jetpack Compose for building UI
3. JUnit 5 for unit testing
4. Coroutines

## Goals to achieve
1. Learn Kotlin
2. Apply clean architecture
3. Learn unit testing in Android
4. Use MVVM UI pattern
5. Make use of design patterns: adapter, composite and decorator
6. Dependency injection
