package com.alphacity.stamptour.network.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object FlexibleDoubleSerializer : KSerializer<Double?> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(
            "FlexibleDouble",
            PrimitiveKind.STRING,
        )

    override fun deserialize(decoder: Decoder): Double? {
        return try {
            decoder.decodeString().toDoubleOrNull()
        } catch (_: Exception) {
            try {
                decoder.decodeDouble()
            } catch (_: Exception) {
                null
            }
        }
    }

    override fun serialize(
        encoder: Encoder,
        value: Double?,
    ) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeDouble(value)
        }
    }
}