package com.yama.marshal.ui.alert

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yama.marshal.LocalAppDimens
import com.yama.marshal.data.model.CartFullDetail
import com.yama.marshal.repository.CartRepository
import com.yama.marshal.tool.Strings
import com.yama.marshal.tool.closeKeyboard
import com.yama.marshal.ui.theme.Sizes

@Composable
internal fun BoxScope.CartMessagesAlert(onOpenMap: (CartFullDetail) -> Unit) = Card(
    modifier = Modifier
        .fillMaxWidth(0.7f)
        .align(Alignment.Center),
    elevation = CardDefaults.cardElevation(
        defaultElevation = 8.dp
    ),
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.background
    )
) {
    val messages = remember {
        CartRepository.cartMessages
    }

    if (messages.isEmpty())
        return@Card

    val nMessage = messages.first()

    val cart by remember(nMessage) {
        CartRepository.findCart(nMessage.cartID)
    }.collectAsState(null)

    Box(
        modifier = Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier.padding(Sizes.screenPadding / 2),
            text = "Cart: ${cart?.cartName}",
            fontSize = LocalAppDimens.current.title,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }

    Box(
        modifier = Modifier.fillMaxWidth().padding(Sizes.screenPadding * 2),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = nMessage.message,
            textAlign = TextAlign.Center,
            fontSize = LocalAppDimens.current.labelLarge
        )
    }

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Button(
            modifier = Modifier.weight(1f),
            onClick = {
                CartRepository.removeCartMessage(0)
            },
            shape = RoundedCornerShape(0.dp),
        ) {
            Text(Strings.cart_messages_alert_skip_button_label.uppercase())
        }

        if ((cart?.currPosHole ?: -1) >= 0
            && (cart?.currPosLat ?: 0.0) > 0
            && (cart?.currPosLon ?: 0.0) > 0
        ) {
            Spacer(
                modifier = Modifier
                    .width(1.dp)
                    .height(Sizes.screenPadding)
                    .background(Color.LightGray)
            )

            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    CartRepository.removeCartMessage(0)
                    onOpenMap(cart ?: return@Button)
                },
                shape = RoundedCornerShape(0.dp)
            ) {
                Text(Strings.cart_messages_alert_map_button_label.uppercase())
            }
        }
    }
}