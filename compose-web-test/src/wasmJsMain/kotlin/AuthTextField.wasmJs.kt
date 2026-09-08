@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.alphacity.stamptour.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.WebElementView
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement

@Composable
actual fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    inputMode: String,
    enabled: Boolean,
    modifier: Modifier,
) {
    WebElementView(
        factory = {
            (document.createElement("input") as HTMLInputElement).apply {

                type = "text"

                setAttribute(
                    "placeholder",
                    placeholder,
                )

                setAttribute(
                    "inputmode",
                    inputMode,
                )

                setAttribute(
                    "autocomplete",
                    if (inputMode == "tel") {
                        "tel"
                    } else {
                        "one-time-code"
                    },
                )

                setAttribute(
                    "enterkeyhint",
                    "done",
                )

                if (!enabled) {
                    setAttribute(
                        "disabled",
                        "",
                    )
                }

                if (value.isNotEmpty()) {
                    setAttribute(
                        "value",
                        value,
                    )
                }

                style.width = "100%"
                style.height = "48px"
                style.boxSizing = "border-box"

                style.border = "1px solid #E9E9E9"
                style.borderRadius = "8px"

                style.backgroundColor = "#FFFFFF"
                style.color = "#121212"

                style.fontSize = "14px"

                style.paddingLeft = "14px"
                style.paddingRight = "14px"

                style.outline = "none"
                style.fontFamily = "inherit"

                /*
                 * 중요:
                 * 입력할 때 Compose가 HTML input을 다시 만지지 않는다.
                 */
                oninput = {
                    onValueChange(this.value)
                }

                /*
                 * input을 클릭했을 때 HTML input 자체가
                 * 브라우저 focus를 유지하도록 한다.
                 */
                onclick = {
                    focus()
                }
            }
        },
        modifier = modifier,
    )
}