package com.dicoding.tourismapp.home

import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.ViewModel
import com.dicoding.tourismapp.core.data.TourismRepository
import com.dicoding.tourismapp.core.domain.usecase.TourismUseCase

//clean Architecture
//class HomeViewModel(tourismRepository: TourismRepository) : ViewModel() {

//    val tourism = tourismRepository.getAllTourism()}

    class HomeViewModel(tourismUseCase: TourismUseCase) : ViewModel() {
   //terapan react 10
    val tourism = LiveDataReactiveStreams.fromPublisher(tourismUseCase.getAllTourism())
}

