package com.dicoding.tourismapp.favorite

import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.ViewModel
import com.dicoding.tourismapp.core.data.TourismRepository
import com.dicoding.tourismapp.core.domain.usecase.TourismUseCase

/* clean architecture
class FavoriteViewModel(tourismRepository: TourismRepository) : ViewModel() {

    val favoriteTourism = tourismRepository.getFavoriteTourism()

}*/
//terapan react 11
class FavoriteViewModel(tourismUseCase: TourismUseCase) : ViewModel() {

    val favoriteTourism = LiveDataReactiveStreams.fromPublisher(tourismUseCase.getFavoriteTourism())

}