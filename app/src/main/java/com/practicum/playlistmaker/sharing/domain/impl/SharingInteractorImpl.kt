package com.practicum.playlistmaker.sharing.domain.impl

import com.practicum.playlistmaker.sharing.domain.models.ExternalNavigator
import com.practicum.playlistmaker.sharing.domain.models.SharingInteractor
import com.practicum.playlistmaker.sharing.domain.models.EmailData

//class SharingInteractorImpl(
//    private val externalNavigator: ExternalNavigator,
//) : SharingInteractor {
//    override fun shareApp() {
//        externalNavigator.shareLink(getShareAppLink())
//    }
//
//    override fun openTerms() {
//        externalNavigator.openLink(getTermsLink())
//    }
//
//    override fun openSupport() {
//        externalNavigator.openEmail(getSupportEmailData())
//    }
//
//    private fun getShareAppLink(): String {
//        // Нужно реализовать
//    }
//
//    private fun getSupportEmailData(): EmailData {
//        // Нужно реализовать
//    }
//
//    private fun getTermsLink(): String {
//        // Нужно реализовать
//    }
//}