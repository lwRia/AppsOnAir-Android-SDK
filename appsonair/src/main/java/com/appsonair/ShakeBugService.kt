package com.appsonair

import android.content.Context
import android.graphics.Color

class ShakeBugService {
    companion object {
        private const val PAGE_BACKGROUND_COLOR: String = "#E8F1FF"
        private const val APP_BAR_BACKGROUND_COLOR: String = "#E8F1FF"
        private const val APP_BAR_TITLE_TEXT: String = "New Ticket"
        private const val APP_BAR_TITLE_COLOR: String = "#000000"
        private const val TICKET_TYPE_LABEL_TEXT: String = "Ticket Type"
        private const val DESCRIPTION_LABEL_TEXT: String = "Description"
        private const val DESCRIPTION_HINT_TEXT: String = "Add description here…"
        private const val DESCRIPTION_MAX_LENGTH: Int = 255
        private const val BUTTON_TEXT: String = "Submit"
        private const val BUTTON_TEXT_COLOR: String = "#FFFFFF"
        private const val BUTTON_BACKGROUND_COLOR: String = "#007AFF"
        private const val LABEL_COLOR: String = "#000000"
        private const val HINT_COLOR: String = "#B1B1B3"
        private const val INPUT_TEXT_COLOR: String = "#000000"

        var pageBackgroundColor: String = PAGE_BACKGROUND_COLOR
        var appbarBackgroundColor: String = APP_BAR_BACKGROUND_COLOR
        var appbarTitleText: String = APP_BAR_TITLE_TEXT
        var appbarTitleColor: String = APP_BAR_TITLE_COLOR
        var ticketTypeLabelText: String = TICKET_TYPE_LABEL_TEXT
        var descriptionLabelText: String = DESCRIPTION_LABEL_TEXT
        var descriptionHintText: String = DESCRIPTION_HINT_TEXT
        var descriptionMaxLength: Int = DESCRIPTION_MAX_LENGTH
        var buttonText: String = BUTTON_TEXT
        var buttonTextColor: String = BUTTON_TEXT_COLOR
        var buttonBackgroundColor: String = BUTTON_BACKGROUND_COLOR
        var labelColor: String = LABEL_COLOR
        var hintColor: String = HINT_COLOR
        var inputTextColor: String = INPUT_TEXT_COLOR
        var raiseNewTicket: Boolean = false

        fun isValidColorHex(colorHex: String): Boolean {
            return try {
                Color.parseColor(colorHex)
                true
            } catch (e: IllegalArgumentException) {
                false
            }
        }

        @JvmStatic
        @JvmOverloads
        fun shakeBug(
            context: Context,
            raiseNewTicket: Boolean = false,
            pageBackgroundColor: String = PAGE_BACKGROUND_COLOR,
            appbarBackgroundColor: String = APP_BAR_BACKGROUND_COLOR,
            appbarTitleText: String = APP_BAR_TITLE_TEXT,
            appbarTitleColor: String = APP_BAR_TITLE_COLOR,
            ticketTypeLabelText: String = TICKET_TYPE_LABEL_TEXT,
            descriptionLabelText: String = DESCRIPTION_LABEL_TEXT,
            descriptionHintText: String = DESCRIPTION_HINT_TEXT,
            descriptionMaxLength: Int = DESCRIPTION_MAX_LENGTH,
            buttonText: String = BUTTON_TEXT,
            buttonTextColor: String = BUTTON_TEXT_COLOR,
            buttonBackgroundColor: String = BUTTON_BACKGROUND_COLOR,
            labelColor: String = LABEL_COLOR,
            hintColor: String = HINT_COLOR,
            inputTextColor: String = INPUT_TEXT_COLOR
        ) {
            ShakeBugService.pageBackgroundColor =
                if (isValidColorHex(pageBackgroundColor)) pageBackgroundColor else PAGE_BACKGROUND_COLOR
            ShakeBugService.appbarBackgroundColor =
                if (isValidColorHex(appbarBackgroundColor)) appbarBackgroundColor else APP_BAR_BACKGROUND_COLOR
            ShakeBugService.appbarTitleText = appbarTitleText
            ShakeBugService.appbarTitleColor =
                if (isValidColorHex(appbarTitleColor)) appbarTitleColor else APP_BAR_TITLE_COLOR
            ShakeBugService.ticketTypeLabelText = ticketTypeLabelText
            ShakeBugService.descriptionLabelText = descriptionLabelText
            ShakeBugService.descriptionHintText = descriptionHintText
            ShakeBugService.descriptionMaxLength = descriptionMaxLength
            ShakeBugService.buttonText = buttonText
            ShakeBugService.buttonTextColor =
                if (isValidColorHex(buttonTextColor)) buttonTextColor else BUTTON_TEXT_COLOR
            ShakeBugService.buttonBackgroundColor =
                if (isValidColorHex(buttonBackgroundColor)) buttonBackgroundColor else BUTTON_BACKGROUND_COLOR
            ShakeBugService.labelColor =
                if (isValidColorHex(labelColor)) labelColor else LABEL_COLOR
            ShakeBugService.hintColor =
                if (isValidColorHex(hintColor)) hintColor else HINT_COLOR
            ShakeBugService.inputTextColor =
                if (isValidColorHex(inputTextColor)) inputTextColor else INPUT_TEXT_COLOR
            ShakeBugService.raiseNewTicket = raiseNewTicket
            if (raiseNewTicket) {
                AppsOnAirServices.raiseNewTicket(context)
            } else {
                AppsOnAirServices.shakeBug(context)
            }
        }
    }
}
