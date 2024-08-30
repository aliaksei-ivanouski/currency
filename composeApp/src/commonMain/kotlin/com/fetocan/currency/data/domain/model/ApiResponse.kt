package com.fetocan.currency.data.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse(
    val meta: MetaData,
    val data: Map<String, Currency>
)

@Serializable
data class MetaData(
    @SerialName("last_updated_at")
    val lastUpdatedAt: String
)

//@Serializable
//open class Currency: RealmObject {
//    @PrimaryKey
//    var _id: ObjectId = ObjectId()
//    var code: String = ""
//    var value: Double = 0.0
//
//    companion object
//}

@Serializable
open class Currency {
    var code: String = ""
    var value: Double = 0.0

    constructor(code: String, value: Double) {
        this.code = code
        this.value = value
    }
}