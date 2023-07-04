/*
 * Copyright 2023 ACINQ SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.acinq.phoenix.android.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import fr.acinq.lightning.utils.Connection
import fr.acinq.phoenix.android.R
import fr.acinq.phoenix.android.components.Dialog
import fr.acinq.phoenix.android.components.HSeparator
import fr.acinq.phoenix.android.utils.*
import fr.acinq.phoenix.android.utils.datastore.UserPrefs
import fr.acinq.phoenix.managers.Connections


@Composable
fun ConnectionDialog(
    connections: Connections?,
    onClose: () -> Unit,
    onTorClick: () -> Unit,
    onElectrumClick: () -> Unit,
) {
    val context = LocalContext.current

    Dialog(title = stringResource(id = R.string.conndialog_title), onDismiss = onClose) {
        Column {
            if (connections?.internet != Connection.ESTABLISHED) {
                Text(
                    text = stringResource(id = R.string.conndialog_network),
                    modifier = Modifier.padding(top = 16.dp, start = 24.dp, end = 24.dp)
                )
            } else {
                if (connections.electrum != Connection.ESTABLISHED || connections.peer != Connection.ESTABLISHED) {
                    Text(text = stringResource(id = R.string.conndialog_summary_not_ok), Modifier.padding(horizontal = 24.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
                HSeparator()
                ConnectionDialogLine(label = stringResource(id = R.string.conndialog_internet), connection = connections.internet)
                HSeparator()

                val isTorEnabled = UserPrefs.getIsTorEnabled(context).collectAsState(initial = null).value
                if (isTorEnabled != null && isTorEnabled) {
                    ConnectionDialogLine(label = stringResource(id = R.string.conndialog_tor), connection = connections.tor, onClick = onTorClick)
                    HSeparator()
                }

                ConnectionDialogLine(label = stringResource(id = R.string.conndialog_electrum), connection = connections.electrum, onClick = onElectrumClick)
                HSeparator()
                ConnectionDialogLine(label = stringResource(id = R.string.conndialog_lightning), connection = connections.peer)
                HSeparator()
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ConnectionDialogLine(
    label: String,
    connection: Connection?,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .then(
                if (onClick != null) Modifier.clickable(role = Role.Button, onClickLabel = stringResource(id = R.string.conndialog_accessibility_desc, label), onClick = onClick) else Modifier
            )
            .padding(vertical = 12.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = when (connection) {
                Connection.ESTABLISHING -> orange
                Connection.ESTABLISHED -> positiveColor
                else -> negativeColor
            },
            modifier = Modifier.size(8.dp)
        ) {}
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, modifier = Modifier.weight(1.0f))
        Text(
            text = when (connection) {
                Connection.ESTABLISHING -> stringResource(R.string.conndialog_connecting)
                Connection.ESTABLISHED -> stringResource(R.string.conndialog_connected)
                else -> if (connection is Connection.CLOSED && connection.isBadCertificate()) {
                    stringResource(R.string.conndialog_closed_bad_cert)
                } else {
                    stringResource(R.string.conndialog_closed)
                }
            }, style = monoTypo
        )
    }
}