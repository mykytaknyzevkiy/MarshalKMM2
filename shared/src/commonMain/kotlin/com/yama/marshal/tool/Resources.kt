package com.yama.marshal.tool

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.Font

internal object Strings {
    const val app_name = "Marshal"
    
    const val login_screen_text_field_username_label = "Login"
    const val login_screen_text_field_password_label = "Password"
    const val login_screen_button_login_label = "Log in"

    const val main_screen_navigation_item_fleet_label = "Fleet"
    const val main_screen_navigation_item_hole_label = "Hole"
    const val main_screen_navigation_item_alert_label = "Alert"

    const val fleet_list_screen_title = "Fleet"
    const val fleet_list_screen_table_row_car_label = "Car"
    const val fleet_list_screen_table_row_start_time_label = "Start time"
    const val fleet_list_screen_table_row_place_of_place_label = "Pace of play"
    const val fleet_list_screen_table_row_hole_label = "Hole"
    
    const val fleet_view_holder_car_no_active = "Car not in use"
    
    const val on_pace = "On Pace"
    const val mins = "mins"
    const val min = "min"
    const val behind = "behind"
    const val ahead = "ahead"
    const val pace = "pace"
    const val of_pace = "of pace"
    const val current_pace = "current pace"
    const val clubhouse = "CH"
    const val cart_not_in_use_ended_round = "Car not in use - Recently ended round"
    const val cart_not_in_use = "Car not in use"
} 

@Composable
internal expect fun fontResources(
    font: String
): Font

internal expect fun painterResource(
    path: String
): Painter

internal expect fun stringResource(
    key: String
): String