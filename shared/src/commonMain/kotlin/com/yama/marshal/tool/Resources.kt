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

    const val fleet_view_holder_action_view_cart_btn_label = "VIEW CAR"
    const val fleet_view_holder_action_flag_cart_btn_label = "FLAG"
    const val fleet_view_holder_action_message_btn_label = "MESSAGE"
    const val fleet_view_holder_action_shutdown_btn_label = "SHUTDOWN"
    const val fleet_view_holder_action_restore_btn_label = "RESTORE"

    const val send_message_screen_title = "Send message"
    const val send_message_screen_message_text_field_label = "Message"
    const val send_message_screen_messages_list_label = "Simple messages"

    const val hole_screen_table_row_hole_label = "Hole"
    const val hole_screen_table_row_pace_of_play_label = "Pace of play"

    const val alerts_item_type_fence_title = "GEO-FENCE"
    const val alerts_item_type_pence_title = "PACE OF PLAY"
    const val alerts_item_type_battery_title = "LOW BATTERY"

    const val map_screen_cart_in_shut_down_label = "Car is in shutdown mode"
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