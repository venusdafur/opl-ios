package cc.webbiii.app.openplural.screens.helper

import androidx.navigation.NavController

typealias NavControl = Pair<(String) -> Unit, () -> Unit>

fun NavControl.navigate(route: String) {
    this.first(route)
}

fun NavControl.back() {
    this.second()
}

fun navControl(navController: NavController): NavControl {
    return Pair(
        { route -> navController.navigate(route) },
        { navController.popBackStack() }
    )
}