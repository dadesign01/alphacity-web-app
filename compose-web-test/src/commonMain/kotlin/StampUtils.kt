package com.alphacity.stamptour.ui.screen

import com.alphacity.stamptour.network.dto.ProgramItem
import com.alphacity.stamptour.network.dto.StampItem
import com.alphacity.stamptour.network.dto.UserStampItem

// 실질 스탬프 개수 계산
fun getEffectiveStampCount(
    userStamp: UserStampItem,
    stamps: List<StampItem>,
    programs: List<ProgramItem>,
): Int {
    val stampMap = stamps.associateBy { it.id }
    val programMap = programs.associateBy { it.id }

    val stamp = stampMap[userStamp.stampId]
    val program = stamp?.programId?.let { programMap[it] }

    return if (program?.category == "seminar") {
        2
    } else {
        1
    }
}

// 유저의 실질 스탬프 총 개수
fun getEffectiveTotalStampCount(
    userStamps: List<UserStampItem>,
    stamps: List<StampItem>,
    programs: List<ProgramItem>,
): Int {
    val stampMap = stamps.associateBy { it.id }
    val programMap = programs.associateBy { it.id }

    return userStamps
        .distinctBy { it.stampId }
        .sumOf { userStamp ->
            val stamp = stampMap[userStamp.stampId]
            val program = stamp?.programId?.let { programMap[it] }

            if (program?.category == "seminar") {
                2
            } else {
                1
            }
        }
}