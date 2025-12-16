package team.swyp.sdu.ui.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import team.swyp.sdu.ui.components.AppHeader
import team.swyp.sdu.ui.mypage.component.FilledTextField
import team.swyp.sdu.ui.theme.Grey10
import team.swyp.sdu.ui.theme.Grey2
import team.swyp.sdu.ui.theme.Grey3
import team.swyp.sdu.ui.theme.Grey7
import team.swyp.sdu.ui.theme.WalkItTheme
import team.swyp.sdu.ui.theme.walkItTypography

/**
 * 내 정보 관리 화면
 *
 * 사용자의 정보를 관리하는 화면
 * - 프로필 이미지 업로드
 * - 이름 변경
 * - 생년월일 설정
 * - 닉네임 변경
 * - 유저 ID 표시
 * - 연동된 계정 표시
 *
 * @param modifier Modifier
 * @param onNavigateBack 뒤로가기 클릭 핸들러
 * @param onSave 저장 버튼 클릭 핸들러
 * @param onBack 뒤로가기 버튼 클릭 핸들러
 */
@Composable
fun UserInfoManagementScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onSave: (name: String, birthYear: String, birthMonth: String, birthDay: String, nickname: String) -> Unit = { _, _, _, _, _ -> },
    onBack: () -> Unit = {},
    onImageUpload: () -> Unit = {},
) {
    var name by remember { mutableStateOf("홍길동") }
    var birthYear by remember { mutableStateOf("") }
    var birthMonth by remember { mutableStateOf("") }
    var birthDay by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }

    val greenPrimary = Color(0xFF52CE4B)
    val redPrimary = Color(0xFFE65C4A)
    val tertiaryText = Color(0xFFC2C3CA)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        AppHeader(
            title = "내 정보 관리",
            onNavigateBack = onNavigateBack,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 프로필 업로드 섹션
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 프로필 이미지
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (profileImageUrl != null) {
                    Image(
                        painter = rememberAsyncImagePainter(profileImageUrl),
                        contentDescription = "프로필 이미지",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Grey2, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Grey7,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }
            }

            // 이미지 업로드 버튼 및 안내 텍스트
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // 이미지 업로드 버튼
                Row(
                    modifier = Modifier
                        .clickable(onClick = onImageUpload)
                        .border(
                            width = 1.dp,
                            color = greenPrimary,
                            shape = RoundedCornerShape(8.dp),
                        )
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = greenPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = "이미지 업로드",
                        style = MaterialTheme.walkItTypography.bodyS.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = greenPrimary,
                    )
                }

                // 안내 텍스트
                Text(
                    text = "10MB 이내의 파일만 업로드 가능합니다.",
                    style = MaterialTheme.walkItTypography.captionM.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Grey7,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 이름 입력 필드
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "이름",
                    style = MaterialTheme.walkItTypography.bodyS.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = Grey10,
                )
                Text(
                    text = "*",
                    style = MaterialTheme.walkItTypography.bodyS.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = redPrimary,
                )
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(Grey2, RoundedCornerShape(8.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Grey10,
                    unfocusedTextColor = Grey10,
                    disabledTextColor = tertiaryText,
                    disabledBorderColor = Color.Transparent,
                ),
                shape = RoundedCornerShape(8.dp),
                textStyle = MaterialTheme.walkItTypography.bodyM.copy(
                    fontWeight = FontWeight.Bold,
                ),
                singleLine = true,
                enabled = true,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 생년월일 선택 필드
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "생년월일",
                    style = MaterialTheme.walkItTypography.bodyS.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = Grey10,
                )
                Text(
                    text = "*",
                    style = MaterialTheme.walkItTypography.bodyS.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = redPrimary,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // 년도 선택
                DateDropdown(
                    value = birthYear,
                    onValueChange = { birthYear = it },
                    placeholder = "년도",
                    modifier = Modifier.weight(1f),
                )

                // 월 선택
                DateDropdown(
                    value = birthMonth,
                    onValueChange = { birthMonth = it },
                    placeholder = "월",
                    modifier = Modifier.weight(1f),
                )

                // 일 선택
                DateDropdown(
                    value = birthDay,
                    onValueChange = { birthDay = it },
                    placeholder = "일",
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 닉네임 입력 필드
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "닉네임",
                    style = MaterialTheme.walkItTypography.bodyS.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = Grey10,
                )
                Text(
                    text = "*",
                    style = MaterialTheme.walkItTypography.bodyS.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = redPrimary,
                )
            }
            FilledTextField(
                value = nickname,
                onValueChange = { nickname = it },
                placeholder = "닉네임을 입력해주세요.",
            )
        }



        Spacer(modifier = Modifier.height(24.dp))

        // 유저 ID 표시 필드 (비활성화)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "유저 ID",
                style = MaterialTheme.walkItTypography.bodyS.copy(
                    fontWeight = FontWeight.Medium,
                ),
                color = Grey7,
            )

            OutlinedTextField(
                value = "000000",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(Grey2, RoundedCornerShape(8.dp)),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = tertiaryText,
                    disabledBorderColor = Color.Transparent,
                ),
                shape = RoundedCornerShape(8.dp),
                textStyle = MaterialTheme.walkItTypography.bodyM.copy(
                    fontWeight = FontWeight.Bold,
                ),
                singleLine = true,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 연동된 계정 표시 필드 (비활성화)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "연동된 계정",
                style = MaterialTheme.walkItTypography.bodyS.copy(
                    fontWeight = FontWeight.Medium,
                ),
                color = Grey7,
            )

            OutlinedTextField(
                value = "카카오",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(Grey2, RoundedCornerShape(8.dp)),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = tertiaryText,
                    disabledBorderColor = Color.Transparent,
                ),
                shape = RoundedCornerShape(8.dp),
                textStyle = MaterialTheme.walkItTypography.bodyM.copy(
                    fontWeight = FontWeight.Bold,
                ),
                singleLine = true,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 하단 버튼들
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // 뒤로가기 버튼
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(47.dp)
                    .clickable(onClick = onBack)
                    .border(
                        width = 1.dp,
                        color = Grey3,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "뒤로가기",
                    style = MaterialTheme.walkItTypography.bodyM.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = tertiaryText,
                )
            }

            // 저장하기 버튼 (비활성화 상태)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(47.dp)
                    .background(Grey3, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "저장하기",
                    style = MaterialTheme.walkItTypography.bodyM.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Grey7,
                )
            }
        }
    }
}

/**
 * 생년월일 드롭다운 컴포넌트
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val tertiaryText = Color(0xFFC2C3CA)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(Color.White, RoundedCornerShape(4.dp))
                .border(1.dp, Grey3, RoundedCornerShape(4.dp)),
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                placeholder = {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.walkItTypography.bodyS.copy(
                            fontWeight = FontWeight.Medium,
                        ),
                        color = tertiaryText,
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Grey10,
                    unfocusedTextColor = Grey10,
                ),
                shape = RoundedCornerShape(4.dp),
                textStyle = MaterialTheme.walkItTypography.bodyS.copy(
                    fontWeight = FontWeight.Medium,
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .menuAnchor(),
            )
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            // 년도/월/일 목록 생성
            when (placeholder) {
                "년도" -> {
                    // 최근 100년
                    (1924..2024).reversed().forEach { year ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "$year",
                                    style = MaterialTheme.walkItTypography.bodyS,
                                )
                            },
                            onClick = {
                                onValueChange("$year")
                                expanded = false
                            },
                        )
                    }
                }

                "월" -> {
                    (1..12).forEach { month ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "$month",
                                    style = MaterialTheme.walkItTypography.bodyS,
                                )
                            },
                            onClick = {
                                onValueChange("$month")
                                expanded = false
                            },
                        )
                    }
                }

                "일" -> {
                    (1..31).forEach { day ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "$day",
                                    style = MaterialTheme.walkItTypography.bodyS,
                                )
                            },
                            onClick = {
                                onValueChange("$day")
                                expanded = false
                            },
                        )
                    }
                }
            }
        }
    }
}


@Composable
@Preview
private fun UserInfoManagementScreenPreview() {
    WalkItTheme {
        UserInfoManagementScreen()
    }
}