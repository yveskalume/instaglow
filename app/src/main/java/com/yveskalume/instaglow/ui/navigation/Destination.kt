package com.yveskalume.instaglow.ui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.compose.composable

enum class Destination(val route: String) {
    Home("home"),
    Editor("editor")
}

fun NavGraphBuilder.composable(
    destination: Destination,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(route = destination.route, content = content)
}

fun NavController.navigate(
    destination: Destination,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null
) {
    navigate(destination.route, navOptions, navigatorExtras)
}