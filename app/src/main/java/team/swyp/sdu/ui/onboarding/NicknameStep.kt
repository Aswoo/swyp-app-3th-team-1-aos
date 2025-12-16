package team.swyp.sdu.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import team.swyp.sdu.ui.theme.WalkItTheme

/**
 * 닉네임 입력 단계 컴포넌트
 */
@Composable
fun NicknameStep(
    value: String,
    onChange: (String) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("닉네임") },
            placeholder = { Text("(입력칸)") },
            singleLine = true,
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            color = Color(0xFFE6E6E6),
            shape = RoundedCornerShape(24.dp),
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("캐릭터 이미지", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 버튼 영역
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onPrev,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Text("이전", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = value.trim().isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text("다음", color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NicknameStepPreview() {
    WalkItTheme {
        NicknameStep(
            value = "",
            onChange = {},
            onNext = {},
            onPrev = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NicknameStepFilledPreview() {
    WalkItTheme {
        NicknameStep(
            value = "홍길동",
            onChange = {},
            onNext = {},
            onPrev = {},
        )
    }
}

