package com.tszlung.photoapp.networking

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.net.URL

@Serializable
data class PhotoResponse(
    val id: String,
    val author: String,
    val width: Int,
    val height: Int,
    @Serializable(with = URLSerializer::class)
    val url: URL,
    @SerialName("download_url")
    @Serializable(with = URLSerializer::class)
    val downloadURL: URL
)

object URLSerializer : KSerializer<URL> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("stringAsURLSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: URL) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): URL {
        return URL(decoder.decodeString())
    }
}
