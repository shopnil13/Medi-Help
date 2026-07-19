package com.medihelp.app.feature_vitals.domain.model

import java.time.Instant

data class BiomarkerDetail(
    val id: String,
    val name: String,
    val value: String,
    val unit: String?,
    val referenceRange: String?,
    val status: String,
    val recordedAt: Instant,
    val explanation: String,
    val statusExplanation: String,
    val moreDetails: String,
    val askDoctor: Boolean,
)
