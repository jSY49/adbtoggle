# 디버깅 토글 (com.jsy.adbtoggle)

개발자 모드(`development_settings_enabled`), 유선 디버깅(`adb_enabled`), 무선 디버깅(`adb_wifi_enabled`)을 퀵설정 타일 / 홈 화면 위젯 / 앱 화면에서 **각각 따로** 켜고 끄는 앱. 앱이 건드리는 설정 키는 이 세 개뿐입니다.

- 타일 3개, 위젯 3개, 앱 화면 버튼 3개가 각각 한 항목만 반대로 바꿉니다.
- 유선/무선 디버깅을 켤 때 개발자 모드가 꺼져 있으면 먼저 켭니다. 끌 때는 누른 항목만 끕니다.
- 값을 쓴 뒤 다시 읽어 실제로 바뀌었는지 확인하고, 안 바뀌었으면 토스트로 알림.
- 선언 권한은 `WRITE_SECURE_SETTINGS` 하나뿐 (INTERNET 없음). 쉘 실행·루팅·접근성 서비스 없음.

## 빌드

```bash
./gradlew assembleDebug
```

결과물: `app/build/outputs/apk/debug/app-debug.apk`

## 설치 및 권한 부여 (PC에서 직접 실행)

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell pm grant com.jsy.adbtoggle android.permission.WRITE_SECURE_SETTINGS
```

권한 부여는 한 번만 하면 됩니다. 앱 첫 화면에서 권한 상태와 명령어(복사 버튼)를 볼 수 있습니다.

## 타일 / 위젯 추가

- 퀵설정 타일: 알림창을 내려 편집(연필) → "개발자 모드" / "유선 디버깅" / "무선 디버깅" 타일을 끌어다 놓기
- 홈 위젯: 홈 화면 빈 곳 길게 누르기 → 위젯 → 같은 이름의 토글 3종

## 되돌리는 방법

- 앱만 삭제하면 권한도 같이 사라집니다.
- 앱을 남기고 권한만 빼려면:
  ```bash
  adb shell pm revoke com.jsy.adbtoggle android.permission.WRITE_SECURE_SETTINGS
  ```
- 개발자 옵션을 끄면 설정의 "개발자 옵션" 메뉴가 사라집니다. 이 앱으로 다시 켜거나, 설정 → 휴대전화 정보 → 빌드 번호를 7번 눌러 켤 수 있습니다.

## 알아둘 점

- 디버깅을 끄면 PC와의 ADB 연결이 끊기는 것은 정상입니다.
- 무선 디버깅은 Wi-Fi 연결이 없으면 켜도 곧바로 꺼질 수 있습니다 (이 경우 앱이 "바뀌지 않음"으로 알려줍니다).
- 설정 앱에서 직접 바꾼 상태는 타일을 열거나 앱을 열 때, 또는 최대 30분 주기로 위젯에 반영됩니다.
- 권한이 없을 때 타일은 `INACTIVE` + "권한 필요" 문구로 표시됩니다 (`UNAVAILABLE`로 하면 탭 이벤트가 오지 않아 안내 토스트를 띄울 수 없음).

## 파일 구성

| 파일 | 역할 |
|---|---|
| `DebugToggle.kt` | 상태 조회/변경, `Settings.Global.putInt` 유일한 호출 위치 |
| `UiSync.kt` | 토글 + 실패 토스트 + 타일/위젯 동기화 |
| `ToggleTileServices.kt` | 퀵설정 타일 3종 |
| `ToggleWidgetProviders.kt` | 위젯 3종 화면 갱신 |
| `WidgetToggleReceiver.kt` | 위젯 탭 처리 (exported=false) |
| `MainActivity.kt` | 권한/상태 표시, 항목별 버튼, 안내 |
