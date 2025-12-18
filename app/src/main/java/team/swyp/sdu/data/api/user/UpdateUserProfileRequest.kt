package team.swyp.sdu.data.api.user

import team.swyp.sdu.domain.model.Sex

data class UpdateUserProfileRequest(
    val nickname: String,
    val birthDate: String,
    val sex: String,
)