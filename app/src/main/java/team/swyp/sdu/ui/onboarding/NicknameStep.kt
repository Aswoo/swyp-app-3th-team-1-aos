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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

/**
 * 닉네임 입력 단계 컴포넌트
 */
@Composable
fun NicknameStep(
    value: String,
    selectedImageUri: String?,
    onChange: (String) -> Unit,
    onImageSelected: (String?) -> Unit,
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

        // 이미지 선택 런처
        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = { uri ->
                onImageSelected(uri?.toString())
            }
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .clickable { imagePickerLauncher.launch("image/*") },
            color = Color(0xFFE6E6E6),
            shape = RoundedCornerShape(24.dp),
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (selectedImageUri != null) {
                    // 선택된 이미지가 있으면 표시 (실제로는 Coil이나 Glide로 이미지 로드)
                    Text("이미지 선택됨", color = MaterialTheme.colorScheme.primary)
                } else {
                    Text("캐릭터 이미지 선택", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                // 우측 하단에 카메라 아이콘 추가 가능
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", style = MaterialTheme.typography.headlineSmall, color = Color.Black)
                }
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
            selectedImageUri = null,
            onChange = {},
            onImageSelected = {},
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
            selectedImageUri = "content://media/picker/0/com.android.providers.media.photopicker/media/1000000018",
            onChange = {},
            onImageSelected = {},
            onNext = {},
            onPrev = {},
        )
    }
}

